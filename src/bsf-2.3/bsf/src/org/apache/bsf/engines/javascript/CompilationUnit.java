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

import org.apache.bsf.*;
import org.apache.bsf.debug.jsdi.*;
import org.apache.bsf.debug.util.DebugLog;
import org.mozilla.javascript.*;
import org.mozilla.javascript.debug.*;

import java.util.*;

/**
 * A compilation unit is a Rhino concept.
 * When a piece of script is provided for eval or
 * execute to a Rhino engine, it is compiled down
 * to either JavaScript or Java bytecode.
 * In debug mode, only the compilation down to JavaScript
 * bytecode is supported.
 * During the compilation process, the original piece
 * of script is sliced into compilation units.
 * For instance, the script text may contain a function
 * declaration and an expression to eval. The compilation
 * will result in two compilation units: the function and
 * the expression. Each compilation unit will correspond
 * to a range of the lines of the original script compiled.
 * All line numbers are global to the document the compiled
 * script is part of.
 * It is on compilation units that breakpoints can be set
 * or removed, more exactly on the DebuggableScript attached
 * to them. See Rhino for more details.
 *
 * @author: Olivier Gruber.
 */
public class CompilationUnit {

    FnOrScript m_fnOrScript;
    int m_firstLine;
    int m_lineCount;
    String m_fnName;
    DebuggableScript m_dbgScript;
    boolean[] m_breakpoints;

    /**
     * CompilationUnit constructor comment.
     */
    public CompilationUnit(FnOrScript fnOrScript, DebuggableScript dbgScript) {

        m_fnOrScript = fnOrScript;
        m_dbgScript = dbgScript;

        int[] lines = dbgScript.getLineNumbers();
        if (lines.length != 0) {
            int lastLine;
            m_firstLine = lines[0];
            lastLine = m_firstLine;
            for (int i = 1; i != lines.length; ++i) {
                int lineno = lines[i];
                if (m_firstLine > lineno) {
                    m_firstLine = lineno;
                } else if (lastLine < lineno) {
                    lastLine = lineno;
                }
            }
            m_lineCount = lastLine - m_firstLine + 1;
            m_breakpoints = new boolean[m_lineCount];
        }

        String name = dbgScript.getFunctionName();
        if (name != null && name.length() != 0 && !name.equals("anonymous")) {
            m_fnName = name;
        }
    }
    //----------------------------------------------------------
    boolean contains(int lineno) {
        return (m_firstLine <= lineno && lineno < m_firstLine + m_lineCount);
    }
    /**
     * Returns true if the compilation unit contains
     * the breakpoint.
     * Notice only breakpoint defined at a line number
     * are supported here.
     */
    boolean contains(BreakPoint bp) {
        try {
            return contains(bp.getLineNo());
        } catch (BSFException ex) {
            return false;
        }
    }
    /**
     * Set a breakpoint at the given line if Rhino has provided us with the
     * valid lines information.
     */
    void propagate(int lineno) {
        if (m_breakpoints != null) {
            int i = lineno - m_firstLine;
            if (0 <= i && i < m_lineCount) {
                m_breakpoints[i] = true;
            }
        }
    }
    /**
     * Clear a breakpoint at the given line if Rhino has provided us with the
     * valid lines information.
     */
    void unpropagate(int lineno) {
        if (m_breakpoints != null) {
            int i = lineno - m_firstLine;
            if (0 <= i && i < m_lineCount) {
                m_breakpoints[i] = false;
            }
        }
    }

    boolean hasBreakpoint(int lineno) {
        if (m_breakpoints != null) {
            int i = lineno - m_firstLine;
            return 0 <= i && i < m_lineCount && m_breakpoints[i];
        }
        return false;
    }
}
