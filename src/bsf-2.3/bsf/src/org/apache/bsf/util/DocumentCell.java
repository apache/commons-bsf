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
 * 4. The names "BSF", "Apache", and "Apache Software Foundation"
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

package org.apache.bsf.util;

import java.io.StringReader;
import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;
import java.util.Enumeration;

import org.apache.bsf.*;
import org.apache.bsf.util.BSFEngineImpl;
import org.apache.bsf.util.BSFFunctions;
import org.apache.bsf.debug.util.DebugLog;

import java.util.Hashtable;
import org.apache.bsf.debug.jsdi.*;

import java.io.Reader;

/**
 * A DocumentCell allows the debug manager to keep track
 * of known documents for which it has breakpoints.
 * When a debugger sets a breakpoint, it specifies a document.
 * This is when a DocumentCell is created for that document
 * (identified through its URI). 
 * The debug manager will keep the document cells at least
 * as long as there is breakpoints known and as long as there
 * are engines known to currently execute the document.
 * 
 * @author   Olivier Gruber
 */

public class DocumentCell {

    BSFDebugManagerImpl m_mger;

    String m_docName;
    Vector m_fnOrScripts;
    Vector m_breakpoints;
    Vector m_engines;

    Hashtable m_functionMap;
    private boolean m_entryexit;

    //-----------------------------------------
    public DocumentCell(BSFDebugManagerImpl mger, String name) {
        m_mger = mger;
        m_docName = name;
        m_breakpoints = new Vector();
        m_functionMap = new Hashtable();
        m_fnOrScripts = new Vector();
        m_engines = new Vector();
        m_entryexit = false;
    }
    //-----------------------------------------
    public String getName() {
        return m_docName;
    }
    //-----------------------------------------
    // Register an engine that has loaded this 
    // document. Notice there is no unload notify.
    // This is a problem with the current BSF approach
    // where the use of a document is not bracketed.
    // Only when an engine terminates that we will
    // known it does not execute a document 
    // anymore.
    public void loadNotify(BSFEngine eng) {
        m_engines.addElement(eng);
        propagateBreakpoints(eng);
        propagateEntryExit(eng);
    }
    //-----------------------------------------
    // Upon termination of an engine, remove it 
    // from the list of engines known as having
    // loaded this document.
    //-----------------------------------------
    public void terminateEngineNotify(BSFEngine eng) {
        m_engines.removeElement(eng);
    }
    //-----------------------------------------
    // Propagate the known breakpoints for this 
    // document in the provided engine.
    //-----------------------------------------
    private void propagateBreakpoints(BSFEngine eng) {
        Enumeration e;
        BreakPoint bp;
        int id;
        int lineno, offset;

        e = m_breakpoints.elements();
        while (e.hasMoreElements()) {
            bp = (BreakPoint) e.nextElement();
            id = bp.getId();
            try {
                if (bp.isLineDefined()) {
                    lineno = bp.getLineNo();
                    eng.placeBreakpointAtLine(id, m_docName, lineno);
                } else {
                    offset = bp.getOffset();
                    eng.placeBreakpointAtOffset(id, m_docName, offset);
                }
            } catch (BSFException ex) {
            }
        }
    }
    //-----------------------------------------
    // Place the provided breakpoint in all the 
    // engines known to have loaded this document.
    //-----------------------------------------
    private void propagateBreakpoint(BreakPoint bp) {
        Enumeration e;
        BSFEngine eng;
        int id;
        int lineno, offset;

        e = m_engines.elements();
        while (e.hasMoreElements()) {
            eng = (BSFEngine) e.nextElement();
            id = bp.getId();
            try {
                if (bp.isLineDefined()) {
                    lineno = bp.getLineNo();
                    eng.placeBreakpointAtLine(id, m_docName, lineno);
                } else {
                    offset = bp.getOffset();
                    eng.placeBreakpointAtOffset(id, m_docName, offset);
                }
            } catch (BSFException ex) {
            }
        }
    }
    //-----------------------------------------
    // Propagate the removal to all the 
    // engines known to have loaded this document.
    //-----------------------------------------
    private void propagateBreakPointRemove(BreakPoint bp) {
        Enumeration e;
        BSFEngine eng;
        int id;
        int lineno, offset;

        e = m_engines.elements();
        while (e.hasMoreElements()) {
            eng = (BSFEngine) e.nextElement();
            id = bp.getId();
            try {
                eng.removeBreakpoint(m_docName,id);
            } catch (BSFException ex) {
            }
        }
    }
    //-----------------------------------------
    public void addBreakpointAtLine(int brkptId, int lineno) {
        BreakPoint bp = new BreakPoint(this, brkptId);
        bp.setLineNo(lineno);
        m_breakpoints.addElement(bp);
        propagateBreakpoint(bp);
    }
    //-----------------------------------------
    public void addBreakpointAtOffset(int brkptId, int offset) {
        BreakPoint bp = new BreakPoint(this, brkptId);
        bp.setOffset(offset);
        m_breakpoints.addElement(bp);
        propagateBreakpoint(bp);
    }
    //-----------------------------------------
    public void removeBreakpoint(int brkptId) {
        Enumeration e;
        BreakPoint bp;

        e = m_breakpoints.elements();
        while (e.hasMoreElements()) {
            bp = (BreakPoint) e.nextElement();
            if (brkptId == bp.getId()) {
                m_breakpoints.removeElement(bp);
                propagateBreakPointRemove(bp);
                break;
            }
        }
    }

    public void setEntryExit(boolean on_value) {
        Enumeration e;

        m_entryexit = on_value;

        e = m_engines.elements();
        while (e.hasMoreElements()) {
            propagateEntryExit((BSFEngine) e.nextElement());
        }
    }

    private void propagateEntryExit(BSFEngine eng) {
        try {
            eng.setEntryExit(m_docName, m_entryexit);
        }
        catch (BSFException ex) {
        }
    }
    
    public boolean getEntryExit() {
        return m_entryexit;
    }

    //-----------------------------------------
    public void removeAllBreakpoints() {

        Enumeration e;
        BreakPoint bp;

        DebugLog.stdoutPrintln("Drop breakpoints for "+m_docName,
                               DebugLog.BSF_LOG_L3);
        e = m_breakpoints.elements();
        while (e.hasMoreElements()) {
            bp = (BreakPoint) e.nextElement();
            DebugLog.stdoutPrintln("	Breakpoints "+bp,
                                   DebugLog.BSF_LOG_L3);
            propagateBreakPointRemove(bp);
        }
        m_breakpoints = new Vector();
    }
	
    //-----------------------------------------
    public BreakPoint findBreakpointAtLine(int lineno) {
        Enumeration e;
        BreakPoint bp;

        e = m_breakpoints.elements();
        while (e.hasMoreElements()) {
            bp = (BreakPoint) e.nextElement();
            if (lineno == bp.getLineNo())
                return bp;
        }
        return null;
    }
    //-----------------------------------------
    public BreakPoint findBreakpointAtOffset(int offset) {
        Enumeration e;
        BreakPoint bp;

        e = m_breakpoints.elements();
        while (e.hasMoreElements()) {
            bp = (BreakPoint) e.nextElement();
            if (offset == bp.getOffset())
                return bp;
        }
        return null;
    }
}
