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

import java.io.InputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.Reader;
import java.rmi.RemoteException;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Hashtable;
import java.security.*;

import org.mozilla.javascript.*;
import org.mozilla.javascript.debug.*;

import org.apache.bsf.debug.util.DebugLog;
import org.apache.bsf.*;
import org.apache.bsf.util.*;
import org.apache.bsf.debug.*;
import org.apache.bsf.debug.jsdi.*;

public class RhinoEngineDebugger implements Debugger {

    /** The global script object, where all embedded functions are defined,
     * as well as the standard ECMA "core" objects.
     */	
    private Scriptable global;
    private JsObject globalstub;

    private RhinoContextProxy m_rcp;
    private Scriptable undefined;
    private JsObject undefinedStub;

    /** 
     *  Hashtable allowing to find the stub for an object in the JavaScript
     *  environment if one exists.
     *  Typically: Scriptable, Function, Script, etc.
     *  This is not used for Context and DebugFrame.
     *  They typically contains JsObject associated to 
     *  org.mozilla.javascript.ScriptableObject
     */
    private Hashtable stubs;

    private JsCallbacks m_callbacks;
    private JsEngineStub engineStub;

    private FnOrScript m_compilingFnOrScript;
    private JavaScriptEngine m_eng;

    private Thread m_thread;

    private Hashtable m_documents;

    BSFDebugManagerImpl dbgmgr;

    public RhinoEngineDebugger(JavaScriptEngine eng) 
        throws RemoteException {
        super();
        m_thread = Thread.currentThread();
        m_eng = eng;
        dbgmgr = eng.getDebugManager();

        m_documents = new Hashtable();

        stubs = new Hashtable();
        m_callbacks = null;

        engineStub = new JsEngineStub(this);

    }

    /**
     * Called when our debugger has been disconnected.
     */
    public void disconnectedDebuggerNotify() {
        m_callbacks = null;
    }

    void addStub(Context cx, RhinoContextProxy jscx) {
        stubs.put(cx, jscx);
    }

    void addStub(DebugFrame frame, JsContextStub stub) {
        stubs.put(frame, stub);
    }

    void addStub(Scriptable sobj, JsObject jsobj) {
        stubs.put(sobj, jsobj);
    }

    void dropStub(Object key) {
        stubs.remove(key);
    }

    public synchronized DocumentCell getDocumentCell(String name) {
        return (DocumentCell) m_documents.get(name);
    }

    // Called upon creation of a BSFManager.
    public synchronized DocumentCell loadDocumentNotify(String name) {
        DocumentCell cell;

        cell = (DocumentCell) m_documents.get(name);
        if (cell == null) {
            cell = new DocumentCell(this, name);
            m_documents.put(name, cell);
            if (dbgmgr!=null) 
                dbgmgr.loadDocumentNotify(m_eng, name);
        }
        return cell;
    }

    public synchronized void placeBreakpointAtLine(int brkptid,
                                                   String docname,
                                                   int lineno) {

        DocumentCell cell;
        cell = (DocumentCell) m_documents.get(docname);
        cell.addBreakpointAtLine(brkptid, lineno);
    }

    public synchronized void placeBreakpointAtOffset(int brkptid,
                                                     String docname,
                                                     int offset) {

        DocumentCell cell;
        cell = (DocumentCell) m_documents.get(docname);
        cell.addBreakpointAtOffset(brkptid, offset);
    }

    public void removeBreakpoint(String docname, int brkptid)
        throws BSFException {

        DocumentCell cell;
        cell = (DocumentCell) m_documents.get(docname);
        cell.removeBreakpoint(brkptid);
    }

    public void setEntryExit(String docname, boolean on)
        throws BSFException {

        DocumentCell cell;
        cell = (DocumentCell) m_documents.get(docname);
        cell.setEntryExit(on);
    }

    public Object eval(String docname, String fnOrScript, int lineno)
        throws RemoteException {
        Object retval;
        try {
            retval = m_eng.eval(docname, lineno, -1, fnOrScript);
            return marshallProperty(retval);
        } catch (BSFException ex) {
            throw new RemoteException("Failed eval", ex);
        }
    }

    public JsContext getContext(int depth) {
        if (m_rcp != null) return m_rcp.getContext(depth);
        return null;
    }

    public int getContextCount() {
        if (m_rcp != null) return m_rcp.getContextCount();
        return -1;
    }

    /**
     * Return the current debugger.
     * @return the debugger, or null if none is attached.
     */
    public JsCallbacks getDebugger() {
        return m_callbacks;
    }

    public Object getDebugInterface() {
        return engineStub;
    }

    public JsObject getGlobalObject() {
        return globalstub;
    }

    public RhinoContextProxy getRhinoContextProxy() {
        return m_rcp;
    }

    RhinoContextProxy getStub(Context cx) {
        return (RhinoContextProxy) stubs.get(cx);
    }

    JsContextStub getStub(DebugFrame frame) {
        return (JsContextStub) stubs.get(frame);
    }

    JsObject getStub(Scriptable sobj) {
        return (JsObject) stubs.get(sobj);
    }

    public JsObject getUndefinedValue() {
        return undefinedStub;
    }

    public String getThread() {
        String resultstr = "";

        if (m_thread != null) {
            try {
                final String resultstrf = (String)
                AccessController.doPrivileged(new PrivilegedExceptionAction() {
                        public Object run() throws Exception {
                            return m_thread.getName();
                        }
                    });
            resultstr = resultstrf;
            }
            catch (PrivilegedActionException prive) {
                resultstr = "Security Exception triggered. " +
                    "Thread info unavailable";
            }
        }
        return resultstr;
    }

    public String getThreadGroup() {
        String resultstr = "";

        if (m_thread != null) {
            try {
                final String resultstrf = (String)
                AccessController.doPrivileged(new PrivilegedExceptionAction() {
                        public Object run() throws Exception {
                            return m_thread.getThreadGroup().getName();
                        }
                    });
            resultstr = resultstrf;
            }
            catch (PrivilegedActionException prive) {
                resultstr = "Security Exception triggered. " +
                    "ThreadGroup info unavailable";
            }
        }
        return resultstr;
    }

    //---------------------------------------------------------
    // The Rhino engine stopped.
    // It could be that it hit a breakpoint that we set or
    // that it is in stepping mode. The stepping mode is used
    // to implement STEP_IN, STEP_OUT, and STEP_OVER.
    //---------------------------------------------------------

    public void handleBreakpointHit(Context cx) {
        JsCallbacks debugger;
        BreakPoint bp;
        Enumeration e;
        DocumentCell cell;
        boolean breakpointFound=false;
        String name;
        int lineno;
        boolean suspend=false;
		
        m_thread = Thread.currentThread();
        DebugLog.stdoutPrintln("**** Handling a breakpoint hit...", 
                               DebugLog.BSF_LOG_L3);
        m_rcp = getStub(cx);
        if (m_rcp == null) {
            m_rcp = new RhinoContextProxy(this, cx);
            addStub(cx, m_rcp);
        }
        // if we have no callbacks... then just 
        // ignore the breakpoint hit, do a run
        // so that execution resumes...
        if (m_callbacks==null) {
            DebugLog.stdoutPrintln("	No callbacks, resuming...", DebugLog.BSF_LOG_L3);
            m_rcp.run();

        } else {
            // First, check that we didn't hit a known breakpoint.
            // First, search if we have breakpoints for the current documents

            name = m_rcp.getSourceName();
            lineno = m_rcp.getLineNumber();

            DebugLog.stdoutPrintln("	in "+name+" at "+lineno, DebugLog.BSF_LOG_L3);

            cell = getDocumentCell(name);
            if (cell != null) 
                _handleBreakpointHit(cell,lineno);
        } 
        m_rcp = null;
    }

    public void _handleBreakpointHit(DocumentCell cell, int lineno) {

        JsCallbacks debugger;
        BreakPoint bp;
        Enumeration e;
        JsContext stub=null;
        boolean breakpointFound=false;
        boolean suspend=false;
		
        try {
            bp = cell.findBreakpointAtLine(lineno);
        } catch (BSFException bsfex) {
            bp = null;
        }
        if (bp != null) {
            breakpointFound = true;
            try {
                stub = m_rcp.hitBreakpoint();
                DebugLog.stdoutPrintln("	breakpoint callback...", DebugLog.BSF_LOG_L3);
             	m_callbacks.createFuture(m_rcp);
                m_callbacks.handleBreakpointHit(stub);
                suspend = true;
            } catch (RemoteException rex) {
                DebugLog.stderrPrintln("	EXCEPTION OCCURED DURING BREAKPOINT CALLBACK", DebugLog.BSF_LOG_L0);				
                DebugLog.stderrPrintln(rex.getMessage(), DebugLog.BSF_LOG_L0);
                rex.printStackTrace();
                suspend = false;
            }
        } else {
            DebugLog.stdoutPrintln("	didn't find a breakpoint...", DebugLog.BSF_LOG_L3);
            breakpointFound = false;
        }

        if (!breakpointFound) {
            // we haven't found a breakpoint at the current
            // line in the current document, we must be stepping
            // or in entry/exit mode
            try {
                stub = m_rcp.stepping();
                FnOrScript current = cell.findFnOrScriptContaining(lineno);
                if (stub != null) {
                    cell.setLastFnOrScript(current);
                    DebugLog.stdoutPrintln("	stepping-done callback...", 
                                           DebugLog.BSF_LOG_L3);
                	m_callbacks.createFuture(m_rcp);
                    m_callbacks.handleSteppingDone(stub);
                    suspend = true;
                } 
                else if (cell.getEntryExit() &&
                         (current != cell.getLastFnOrScript()) &&
                         (m_rcp.getContextCount() == 0)) {
                    cell.setLastFnOrScript(current);
                    stub = m_rcp.entry_exit_mode();
                    DebugLog.stdoutPrintln("    entry/exit mode...", 
                                           DebugLog.BSF_LOG_L3);
                	m_callbacks.createFuture(m_rcp);
                    m_callbacks.handleSteppingDone(stub);
                    suspend = true;
                }
                else {
                    DebugLog.stdoutPrintln("	No reason to suspend execution.", DebugLog.BSF_LOG_L3);				
                    suspend = false;
                }
            } catch (RemoteException rex) {
                DebugLog.stderrPrintln("	EXCEPTION OCCURED DURING STEPPING-DONE CALLBACK", DebugLog.BSF_LOG_L0);				
                DebugLog.stderrPrintln(rex.getMessage(), DebugLog.BSF_LOG_L0);
                rex.printStackTrace();
                suspend = false;
            }
        }
        if (suspend) {
            // now, suspend this thread... until 
            // we restart.
            try {
                m_callbacks.suspendFuture(m_rcp);
            } catch (Exception ex) {
                DebugLog.stdoutPrintln("Future creation failed... releasing the engine", DebugLog.BSF_LOG_L3);
                m_rcp.run();
            }
        }			
    }

    public void run(JsEngineStub eng) throws Exception {
        DebugLog.stdoutPrintln("RhinoEngineDebugger::run()...",
                               DebugLog.BSF_LOG_L3);
        m_rcp.run(); 
        m_callbacks.completeFuture(m_rcp);
    }

    public void stepIn(JsEngineStub eng) throws Exception {
        DebugLog.stdoutPrintln("RhinoEngineDebugger::stepIn()...",
                               DebugLog.BSF_LOG_L3);
        m_rcp.stepIn();
        m_callbacks.completeFuture(m_rcp);
    }

    public void stepOut(JsEngineStub eng) throws Exception {
        DebugLog.stdoutPrintln("RhinoEngineDebugger::stepOut()...",
                               DebugLog.BSF_LOG_L3);
        m_rcp.stepOut();
        m_callbacks.completeFuture(m_rcp);
    }
    public void stepOver(JsEngineStub eng) throws Exception {

        DebugLog.stdoutPrintln("RhinoEngineDebugger::stepOver()...",
                               DebugLog.BSF_LOG_L3);
        m_rcp.stepOver();
        m_callbacks.completeFuture(m_rcp);
    }
	
    public void handleCompilationDone(Context cx,
                                      DebuggableScript fnOrScript,
                                      StringBuffer source) {

        m_thread = Thread.currentThread();
        m_compilingFnOrScript.addCompilationUnit(cx, fnOrScript, source);
    }

    public void handleExceptionThrown(Context cx, Object exceptionThrown) {
        JsContext stub;
        JsCallbacks debugger;
        BreakPoint bp;
        Enumeration e;
        DocumentCell cell;
        String name,msg;
        Exception ex;
        int lineno;
        NativeError error;
		
        m_thread = Thread.currentThread();
        m_rcp = getStub(cx);
        if (m_rcp == null) {
            m_rcp = new RhinoContextProxy(this, cx);
            addStub(cx, m_rcp);
        }
        try {
            // if we have no callbacks... then just 
            // ignore the breakpoint hit, do a run
            // so that execution resumes...
            if (m_callbacks==null) {
                m_rcp.run();
                return;
            }

            // First, check that we didn't hit a known breakpoint.
            // First, search if we have breakpoints for the current documents
            name = m_rcp.getSourceName();
            lineno = m_rcp.getLineNumber();
            try {
                error = (NativeError)exceptionThrown;
                msg = error.getName() + ": " + error.getMessage();
            } catch (ClassCastException ccex) {
                msg = "Unknown JavaScript Exception";
            }
            ex = new Exception(msg);

            cell = getDocumentCell(name);
            if (cell == null) return;

            try {
                stub = m_rcp.exceptionThrown();	
                m_callbacks.createFuture(m_rcp);
                m_callbacks.handleExceptionThrown(stub,ex);
				
                // now, suspend this thread... until 
                // we restart.
                m_callbacks.suspendFuture(m_rcp);
				
            } catch (Exception ex2) {
                m_rcp.run();
				
            }
        } finally {
            m_rcp = null;
        }
    }

    Object marshallProperty(Object prop) throws RemoteException {
        if (prop == null)
            return null;
        if (prop == Scriptable.NOT_FOUND)
            return null;
        if (prop == Context.getUndefinedValue())
            return undefinedStub;
        if (prop instanceof Scriptable) {
            JsObject stub;
            Scriptable sprop = (Scriptable) prop;
            stub = getStub(sprop);
            if (stub == null) {
                stub = new JsObjectStub(this, sprop);
                this.addStub(sprop, stub);
            }
            return stub;
        }
        return prop;
    }

    JsObject marshallScriptable(Scriptable prop) throws RemoteException {
        if (prop == null)
            return null;
        if (prop == Scriptable.NOT_FOUND)
            return null;
        if (prop == Context.getUndefinedValue())
            return undefinedStub;

        JsObject stub;
        Scriptable sprop = (Scriptable) prop;
        stub = getStub(sprop);
        if (stub == null) {
            stub = new JsObjectStub(this, sprop);
            this.addStub(sprop, stub);
        }
        return stub;
    }

    /**
     * Set whether the engine should break when it encounters
     * the next line.
     * <p>
     * The engine will call the attached debugger's handleBreakpointHit
     * method on the next line it executes if isLineStep is true.
     * May be used from another thread to interrupt execution.
     * 
     * @param isLineStep if true, break next line
     */
    public void setBreakNextLine(JsContext context, boolean isLineStep) {
    }

    void setCompilingFnOrScript(FnOrScript fnOrScript) {
        m_compilingFnOrScript = fnOrScript;
    }

    /**
     * Set the associated debugger.
     * @param debugger the debugger to be used on callbacks from
     * the engine.
     */
    public void setDebugger(JsCallbacks debugger) {
        m_callbacks = debugger;
    }
}
