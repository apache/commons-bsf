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

/**
 * Insert the type's description here.
 * Creation date: (8/23/2001 4:09:36 PM)
 * @author: Administrator
 */

import java.util.Hashtable;

import org.apache.bsf.*;
import org.apache.bsf.debug.jsdi.*;
import org.mozilla.javascript.*;
import org.mozilla.javascript.debug.*;

import java.rmi.RemoteException;

class RhinoContextProxy {

    RhinoEngineDebugger m_reDbg;

    ObjArray m_frameStack = new ObjArray();

    private static final int NO_STEP = 0, STEP_IN = 1, STEP_OVER = 2,
        STEP_OUT = 3, STOP_ENGINE = 4, RUNNING = 5;

    private int m_stepCmd, m_stepDepth;

    RhinoContextProxy(RhinoEngineDebugger reDbg) {
        m_reDbg = reDbg;
    }

    static RhinoContextProxy getCurrent() {
        Context cx = Context.getCurrentContext();
        if (cx == null) { return null; }
        return (RhinoContextProxy)cx.getDebuggerContextData();
    }

    void cancelStepping() {
        m_stepCmd = NO_STEP;
        m_stepDepth = -1;
    }

    int getContextCount() {
        return m_frameStack.size();
    }

    JsContextStub getContextStub(int no) {
        if (!(0 <= no && no < m_frameStack.size())) { return null; }
        return (JsContextStub)m_frameStack.get(no);
    }

    JsContextStub getTopContextStub() {
        return getContextStub(m_frameStack.size() - 1);
    }

    int getLineNumber() {
        JsContextStub stub = getTopContextStub();
        return stub.m_lineno;
    }

    RhinoEngineDebugger getRhinoEngineDebugger() {
        return m_reDbg;
    }

    String getSourceName() {
        JsContextStub stub = getTopContextStub();
        return stub.m_unit.m_dbgScript.getSourceName();
    }

    // We hit a known breakpoint.
    // We need to update the stack.
    // Also, cancel any pending stepping operation.
    JsContextStub hitBreakpoint() throws RemoteException {
        cancelStepping();
        return getTopContextStub();
    }

    JsContextStub exceptionThrown() throws RemoteException {
        cancelStepping();
        return getTopContextStub();
    }

    void resumed() {
        for (int f = 0, N = getContextCount(); f != N; ++f) {
            getContextStub(f).atBreakpoint(false);
        }
    }

    void run() {
        m_stepCmd = RUNNING;
        m_stepDepth = -1;
    }

    void stepIn() {
        m_stepCmd = STEP_IN;
        m_stepDepth = getContextCount();
    }

    void stepOut() {
        m_stepCmd = STEP_OUT;
        m_stepDepth = getContextCount();
    }

    void stepOver() {
        m_stepCmd = STEP_OVER;
        m_stepDepth = getContextCount();
    }

    JsContextStub entry_exit_mode() throws RemoteException {
        cancelStepping();
        return getTopContextStub();
    }

    JsContextStub stepping() {
        // Did we hit a known breakpoint?

        int frameCount = getContextCount();

        try {
            switch (m_stepCmd) {
            case NO_STEP :
                cancelStepping();
                break;
            case STOP_ENGINE :
                cancelStepping();
                return getTopContextStub();
            case STEP_IN :
                // OG if ((frameCount == m_stepDepth + 1) ||
                // (frameCount == m_stepDepth)) {
                // step if we are in the same frame (nothing to step in... :-)
                // if we are in a called frame...
                // but also if we stepped out of the current frame...
                    cancelStepping();
                    return getTopContextStub();
            case STEP_OVER :
                // OG if (frameCount == m_stepDepth) {
                // step if we are in the same frame or above...
                // this basically avoids any children frame but
                // covers the return of the current frame.
                if (frameCount <= m_stepDepth) {
                    cancelStepping();
                    return getTopContextStub();
                }
                break;
            case STEP_OUT :
                // OG if (frameCount == m_stepDepth - 1) {
                if (frameCount < m_stepDepth) {
                    cancelStepping();
                    return getTopContextStub();
                }
                break;
            default :
                throw new Error("Unknown command.");
            }
        } catch (Throwable t) {
            t.printStackTrace();
            cancelStepping();
        }
        return null;
    }

    void stopEngine() {
        m_stepCmd = STOP_ENGINE;
        m_stepDepth = -1;
    }
}
