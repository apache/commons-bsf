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

import java.io.StringReader;
import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;
import java.util.Enumeration;

import org.mozilla.javascript.*;
import org.mozilla.javascript.debug.*;

import org.apache.bsf.*;
import org.apache.bsf.util.BSFEngineImpl;
import org.apache.bsf.util.BSFFunctions;

import java.util.Hashtable;
import org.apache.bsf.debug.jsdi.*;

import java.io.Reader;

/**
 * A document cell materializes a known document.
 * A document is a container for scripts or functions
 * in JavaScript. The document is known as soon as 
 * a function or script is compiled in the engine.
 * Compilation occurs as a side-effect of evaluating 
 * or executing a function or a script.
 * 
 * Upon the first loading of a function or script of 
 * a document, the document becomes known and the debug
 * manager is notified of the load. The debug manager
 * will in turn notify the engine of all the known 
 * breakpoints for that document. 
 * 
 * When a breakpoint is propagated from the debug manager
 * to an engine, the document will be added a breakpoint.
 * The document will memorize the breakpoints if their 
 * corresponding function or script is not known at that 
 * time. If it is known, it is the FnOrScript that will
 * memorize the breakpoint. See FnOrScript to see how
 * a breakpoint is actually forwarded to the underlying
 * Rhino engine.
 * 
 */
public class DocumentCell {

    RhinoEngineDebugger m_rhinoDebugger;

    String m_docName;
    Vector m_fnOrScripts;
    Vector m_breakpoints;
    private boolean m_entryexit;
    private FnOrScript m_lastFnOrScript;

    Hashtable m_functionMap;

    public DocumentCell(RhinoEngineDebugger rhinoDebugger, String name) {
        m_rhinoDebugger = rhinoDebugger;
        m_docName = name;
        m_breakpoints = new Vector();
        m_functionMap = new Hashtable();
        m_fnOrScripts = new Vector();
        m_entryexit = false;
        m_lastFnOrScript = null;
    }

    public String getName() {
        return m_docName;
    }

    /**
     * Add a breakpoint.
     * Two cases exist. 
     * If a function or a script (FnOrScript) is known for 
     * the given line number, the breakpoint will be remembered 
     * by that FnOrScript.
     * Otherwise, the breakpoint is memorized at the document 
     * level until a function or script is known, that is,
     * compiled in our engine.
     * 
     */
    public void addBreakpointAtLine(int brkptId, int lineno) {
        Enumeration e;
        FnOrScript fnOrScript;

        BreakPoint bp = new BreakPoint(this, brkptId);
        bp.setLineNo(lineno);

        // Propagate the breakpoint at document level
        // to the level of already known function or 
        // scripts. It will propagate it down to the 
        // known compilation units.
        e = m_fnOrScripts.elements();
        while (e.hasMoreElements()) {
            fnOrScript = (FnOrScript) e.nextElement();
            try {
                if (fnOrScript.contains(bp)) {
                    fnOrScript.addBreakpoint(bp);
                    return;
                }
            } catch (BSFException ex) {
            }
        }
        m_breakpoints.addElement(bp);
    }

    /**
     * Same as above, except the breakpoint is specified
     * at an character offset rather than a line number.
     */
    public void addBreakpointAtOffset(int brkptId, int offset) {
        Enumeration e;
        FnOrScript fnOrScript;

        BreakPoint bp = new BreakPoint(this, brkptId);
        bp.setOffset(offset);

        e = m_fnOrScripts.elements();
        while (e.hasMoreElements()) {
            fnOrScript = (FnOrScript) e.nextElement();
            try {
                if (fnOrScript.contains(bp)) {
                    fnOrScript.addBreakpoint(bp);
                    return;
                }
            } catch (BSFException ex) {
            }
        }
        m_breakpoints.addElement(bp);
    }

    //-----------------------------------------
    // Check to see if we have pending breakpoints
    // at the document level that belong to this 
    // Function or Script.	
    private void attachBreakpoints(FnOrScript fnOrScript) {

        Enumeration e;
        BreakPoint bp;
        Vector toremove = new Vector();
        e = m_breakpoints.elements();
        while (e.hasMoreElements()) {
            bp = (BreakPoint) e.nextElement();
            try {
                if (fnOrScript.contains(bp)) {
                    // we got a pending breakpoint...
                    // add it to the compilation unit and remember it
                    // to remove it later.
                    fnOrScript.addBreakpoint(bp);
                    toremove.addElement(bp);
                }
            } catch (BSFException ex) {
            }
        }
        // now that we are doning iterating over breakpoints,
        // we can remove all the ones that need to be removed.
        e = toremove.elements();
        while (e.hasMoreElements()) {
            bp = (BreakPoint) e.nextElement();
            m_breakpoints.removeElement(bp);
        }
    }

    public BreakPoint findBreakpointAtLine(int lineno) throws BSFException {
        Enumeration e;
        BreakPoint bp;
        FnOrScript fnOrScript;

        e = m_fnOrScripts.elements();
        while (e.hasMoreElements()) {
            fnOrScript = (FnOrScript) e.nextElement();
            bp = fnOrScript.findBreakpointAtLine(lineno);
            if (bp != null)
                return bp;
        }
        return null;
    }

    public BreakPoint findBreakpointAtOffset(int offset) throws BSFException {
        Enumeration e;
        BreakPoint bp;
        FnOrScript fnOrScript;

        e = m_fnOrScripts.elements();
        while (e.hasMoreElements()) {
            fnOrScript = (FnOrScript) e.nextElement();
            bp = fnOrScript.findBreakpointAtOffset(offset);
            if (bp != null)
                return bp;
        }
        return null;
    }

    public FnOrScript findFnOrScript(int startLine, int column) {
        Enumeration e;
        FnOrScript fnOrScript;
        e = m_fnOrScripts.elements();
        while (e.hasMoreElements()) {
            fnOrScript = (FnOrScript) e.nextElement();
            if (fnOrScript.m_startLine == startLine)
                if (fnOrScript.m_column == column)
                    return fnOrScript;
        }
        return null;
    }

    public FnOrScript findFnOrScriptContaining(int line) {
        Enumeration e;
        FnOrScript fnOrScript;
        e = m_fnOrScripts.elements();
        while (e.hasMoreElements()) {
            fnOrScript = (FnOrScript) e.nextElement();
            if (fnOrScript.m_startLine <= line &&
                (fnOrScript.m_startLine + fnOrScript.m_lineCount) >= line)
                return fnOrScript;
        }
        return null;
    }

    public Enumeration fnOrScripts() {
        return m_fnOrScripts.elements();
    }

    public FnOrScript registerFnOrScriptLines(Reader reader, 
                                              int startLine, 
                                              int column) 
        throws BSFException {

        FnOrScript fnOrScript;
        Enumeration e;
        // first, search if we already have the script or function.
        e = m_fnOrScripts.elements();
        while (e.hasMoreElements()) {
            fnOrScript = (FnOrScript) e.nextElement();
            if (fnOrScript.getFirstLine() == startLine)
                if (fnOrScript.getColumn() == column)
                    return fnOrScript;
        }
        try {

            fnOrScript = new FnOrScript(this);
            m_fnOrScripts.addElement(fnOrScript);

            fnOrScript.specifyLinesPos(reader, startLine, column);

            this.attachBreakpoints(fnOrScript);
        } catch (IOException ex) {
            throw new BSFException(
                                   BSFException.REASON_EXECUTION_ERROR,
                                   "while registering script or function.",
                                   ex);
        }
        return fnOrScript;
    }

    public FnOrScript registerFnOrScriptLines(String source, 
                                              int startLine, 
                                              int column)
        throws BSFException {
        Reader reader = new StringReader(source);
        return registerFnOrScriptLines(reader, startLine, column);
    }

    public FnOrScript registerFnOrScriptRange(Reader reader, int offset)
        throws BSFException {

        FnOrScript fnOrScript;
        try {

            fnOrScript = new FnOrScript(this);
            m_fnOrScripts.addElement(fnOrScript);

            fnOrScript.specifyRange(reader, offset);

            this.attachBreakpoints(fnOrScript);
        } catch (IOException ex) {
            throw new BSFException(
                                   BSFException.REASON_EXECUTION_ERROR,
                                   "while registering script or function.",
                                   ex);
        }
        return fnOrScript;
    }

    public FnOrScript registerFnOrScriptRange(String source, int offset)
        throws BSFException {

        Reader reader = new StringReader(source);
        return registerFnOrScriptRange(reader, offset);
    }

    /**
     * Removing a breakpoint.
     * Two cases, a breakpoint is only remembered at 
     * the document level, it has not been propagated
     * to a function or script (FnOrScript). Then, just
     * drop it.
     * Second case, the breakpoint has been propagated,
     * then scan the FnOrScript objects and ask them 
     * to drop the breakpoint. 
     * Note: only one will have it, see addBreakpoint...
     */
    public BreakPoint removeBreakpoint(int brkptId) {
        Enumeration e;
        BreakPoint bp=null;
        FnOrScript fnOrScript;

        // search for the breakpoint to remove
        // at the document level first.
        e = m_breakpoints.elements();
        while (e.hasMoreElements()) {
            bp = (BreakPoint) e.nextElement();
            if (bp.getId()==brkptId) {
                // we found it, just drop it
                // and return.
                m_breakpoints.removeElement(bp);
                return bp;
            }
        }
        // the breakpoint has not been found at 
        // the document level. It must have been
        // propagated at a FnOrScript level.
        e = m_fnOrScripts.elements();
        while (e.hasMoreElements()) {
            fnOrScript = (FnOrScript) e.nextElement();
            bp = fnOrScript.removeBreakpoint(brkptId);
            if (null!=bp) break;
        }
        return bp;
    }

    public void setEntryExit(boolean on_value) {
        m_entryexit = on_value;
    }

    public boolean getEntryExit() {
        return m_entryexit;
    }

    public void setLastFnOrScript(FnOrScript fnos) {
        m_lastFnOrScript = fnos;
    }

    public FnOrScript getLastFnOrScript() {
        return m_lastFnOrScript;
    }
}
