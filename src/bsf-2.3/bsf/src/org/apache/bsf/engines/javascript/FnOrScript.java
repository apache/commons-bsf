/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Apache BSF", "Apache", and "Apache Software Foundation"
 *    must not be used to endorse or promote products derived from
 *    this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * Sanjiva Weerawarana and others at International Business Machines
 * Corporation. For more information on the Apache Software Foundation,
 * please see <http://www.apache.org/>.
 */

package org.apache.bsf.engines.javascript;

import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

import org.apache.bsf.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import org.mozilla.javascript.debug.*;
import org.mozilla.javascript.*;

/**
 * This class represents a function or script, that is,
 * a piece of a document that is provided to the JavaScript
 * engine for evaluation, execution, or simply compilation.
 *
 * A FnOrScript represents a range of lines or characters
 * in its document. For now, Rhino only supports ranges
 * of lines, really, but the code for offsets is there anyway.
 *
 * Warning: Offsets have never been quite tested yet...
 *
 * A FnOrScript has compilation units. When Rhino compiles
 * a function or a script, even in interpreted mode where the
 * compilation is done to JavaScript bytecode, it calls back
 * its debugger with different compilation units; see
 * Debugger::handleCompilationDone method on the RhinoEngineDebugger
 * class.
 *
 * A FnOrScript also keeps track of the known breakpoints
 * in its range of lines or characters. It makes sure
 * that they are propagated to the underlying Rhino
 * engine (i.e. set) as well as unpropagated (i.e. unset).
 *
 * @author: Olivier Gruber
 */
public class FnOrScript {

    protected DocumentCell m_cell;
    protected boolean m_lineDefined;
    protected int m_startLine, m_lineCount, m_column;
    protected int m_offset, m_charCount;

    protected Vector m_breakpoints;

    protected StringBuffer m_text;

    protected Script m_script;

    private Vector m_units; // of CompilationUnit.
    private Hashtable m_functionToUnit;

    protected Hashtable m_functionMap;

    public FnOrScript(DocumentCell cell) {
        m_cell = cell;
        m_lineDefined = true;
        m_startLine = -1;
        m_column = -1;
        m_lineCount = 0;
        m_breakpoints = new Vector();
        m_text = new StringBuffer();

        m_units = new Vector();
        m_functionToUnit = new Hashtable();
        m_functionMap = new Hashtable();
    }

    private BreakPoint _addBreakpoint(BreakPoint bp) {
        bp = createBreakpoint(bp);
        m_breakpoints.addElement(bp);
        return bp;
    }

    public BreakPoint addBreakpoint(BreakPoint bp) {

        m_breakpoints.addElement(bp);

        // now, look for a unit containing it and
        // if one is found, set the breakpoint unit
        // and propagate...
        Enumeration e;
        CompilationUnit unit;
        e = m_units.elements();
        while (e.hasMoreElements()) {
            unit = (CompilationUnit) e.nextElement();
            if (unit.contains(bp)) {
                bp.setUnit(unit);
                bp.propagate();
                break;
            }
        }
        return bp;
    }

    private BreakPoint _removeBreakpoint(int brkptId) {
        Enumeration e;
        BreakPoint bp;

        e = m_breakpoints.elements();
        while (e.hasMoreElements()) {
            bp = (BreakPoint) e.nextElement();
            if (brkptId == bp.getId()) {
                m_breakpoints.removeElement(bp);
                return bp;
            }
        }
        return null;
    }

    public BreakPoint removeBreakpoint(int bpid) {

        Enumeration e;
        BreakPoint bp;

        e = m_breakpoints.elements();
        while (e.hasMoreElements()) {
            bp = (BreakPoint) e.nextElement();
            if (bpid == bp.getId()) {
                m_breakpoints.removeElement(bp);
                bp.unpropagate();
                return bp;
            }
        }
        return null;
    }

    boolean contains(BreakPoint bp) throws BSFException {
        if (m_lineDefined) {
            int line = bp.getLineNo();
            return (m_startLine <= line && line < m_startLine + m_lineCount);
        } else {
            int offset = bp.getOffset();
            return (m_offset <= offset && offset < m_offset + m_charCount);
        }
    }

    //-----------------------------------------
    // This protected method works as a factory
    // for language-specific breakpoints.
    // The default behavior is to use the provided
    // generic breakpoint.
    // See javascript for an example of language-specific
    // breakpoints.

    protected BreakPoint createBreakpoint(BreakPoint bp) {
        return bp;
    }

    public BreakPoint findBreakpointAtLine(int lineno) throws BSFException {
        Enumeration e;
        BreakPoint bp;

        if (!m_lineDefined)
            throw new BSFException(BSFException.REASON_INVALID_ARGUMENT,
                                   "Function or Script is defined with ranges, lines are not supported.");

        e = m_breakpoints.elements();
        while (e.hasMoreElements()) {
            bp = (BreakPoint) e.nextElement();
            if (lineno == bp.getLineNo())
                return bp;
        }
        return null;
    }

    public BreakPoint findBreakpointAtOffset(int offset) throws BSFException {
        Enumeration e;
        BreakPoint bp;

        if (m_lineDefined)
            throw new BSFException(BSFException.REASON_INVALID_ARGUMENT,
                                   "Function or Script is defined with lines, offsets are not supported.");

        e = m_breakpoints.elements();
        while (e.hasMoreElements()) {
            bp = (BreakPoint) e.nextElement();
            if (offset == bp.getOffset())
                return bp;
        }
        return null;
    }

    public int getCharCount() throws BSFException {
        if (!m_lineDefined)
            return m_charCount;
        throw new BSFException(BSFException.REASON_INVALID_ARGUMENT,
                               "Function or Script is defined with ranges, lines are not supported.");
    }
    public DocumentCell getDocument() {
        return m_cell;
    }

    public int getFirstLine() throws BSFException {
        if (m_lineDefined)
            return m_startLine;
        throw new BSFException(BSFException.REASON_INVALID_ARGUMENT,
                               "Function or Script is defined with ranges, lines are not supported.");
    }

    public int getColumn() throws BSFException {
        if (m_lineDefined)
            return m_column;
        throw new BSFException(BSFException.REASON_INVALID_ARGUMENT,
                               "Function or Script is defined with ranges, lines are not supported.");
    }

    public StringBuffer getFnOrScriptAsStringBuffer() {
        return m_text;
    }

    public int getLineCount() throws BSFException {
        if (m_lineDefined)
            return m_lineCount;
        throw new BSFException(BSFException.REASON_INVALID_ARGUMENT,
                               "Function or Script is defined with ranges, lines are not supported.");
    }

    public int getOffset() throws BSFException {
        if (!m_lineDefined)
            return m_offset;
        throw new BSFException(BSFException.REASON_INVALID_ARGUMENT,
                               "Function or Script is defined with ranges, lines are not supported.");
    }

    private void readChars(Reader reader) {
        char chars[] = new char[256];
        char c;
        int n, charCount;
        String line;
        charCount = 0;
        n = 0;
        try {
            while (true) {
                n = reader.read(chars, 0, 256);
                if (n == -1)
                    break;
                m_text.append(chars, 0, n);
                charCount += n;
            }
            m_charCount = charCount;
        } catch (IOException ex) {
        }
    }

    private void readLines(Reader reader) {
        char chars[] = new char[256];
        char c;
        int n, val, lineCount;
        String line;
        lineCount = 0;
        n = 0;
        while (true) {
            if (n >= chars.length) {
                char tmp[] = chars;
                chars = new char[2 * chars.length];
                System.arraycopy(chars, 0, tmp, 0, tmp.length);
            }
            try {
                val = reader.read();
            } catch (IOException ex) {
                val = -1;
            }
            if (val == -1)
                break;
            c = (char) val;
            chars[n++] = c;
            if (c == '\n') {
                line = new String(chars, 0, n);
                m_text.append(line);
                n = 0;
                lineCount++;
            }
        }
        // Don't forget the last line if it is not
        // terminated by a \n
        if (n != 0) {
            line = new String(chars, 0, n);
            m_text.append(line);
            lineCount++;
        }
        m_lineCount = lineCount;
    }

    public void specifyLinesPos(Reader reader, int startLine, int column)
        throws BSFException, IOException {
        m_startLine = startLine;
        m_column = column;
        m_lineCount = -1;
        m_charCount = -1;
        m_offset = -1;
        m_lineDefined = true;
        readLines(reader);
    }

    public void specifyRange(Reader reader, int offset)
        throws BSFException, IOException {
        m_offset = offset;
        m_charCount = -1;
        m_startLine = -1;
        m_lineCount = -1;
        m_lineDefined = false;
        readChars(reader);
    }

    public void addCompilationUnit(Context cx,
                                   DebuggableScript dbgScript,
                                   String source) {

        CompilationUnit unit;

        unit = new CompilationUnit(this, dbgScript);
        m_units.addElement(unit);
        m_functionToUnit.put(dbgScript, unit);
        if (unit.m_fnName != null) {
            m_functionMap.put(unit.m_fnName, unit);
        }

        // Associate breakpoints to this unit if
        // the unit contains them...
        Enumeration e;
        BreakPoint bp;
        e = m_breakpoints.elements();
        while (e.hasMoreElements()) {
            bp = (BreakPoint) e.nextElement();
            if (unit.contains(bp))
                bp.setUnit(unit);
        }
        propagateAll();
    }

    CompilationUnit getCompilationUnit(DebuggableScript dbgScript) {
        return (CompilationUnit)m_functionToUnit.get(dbgScript);
    }

    public void compile(Context cx, Scriptable global)
        throws BSFException, IOException {

        Enumeration e;
        Reader reader = new StringReader(m_text.toString());
        m_script =
            cx.compileReader(global, reader, m_cell.getName(),
                             m_startLine, null);
        if (m_script == null)
            throw new BSFException("Compilation of the script "
                                   + m_cell.getName()
                                   + " failed at line "
                                   + m_startLine);
    }

    boolean contains(CompilationUnit unit) {
        return (m_startLine <= unit.m_firstLine
                && unit.m_firstLine + unit.m_lineCount <=
                m_startLine + m_lineCount);
    }

    public Script getScript() {
        return m_script;
    }

    private void propagateAll() {
        Enumeration e;
        BreakPoint bp;
        e = m_breakpoints.elements();
        while (e.hasMoreElements()) {
            bp = (BreakPoint) e.nextElement();
            bp.propagate();
        }
    }
}
