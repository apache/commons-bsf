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

import java.rmi.RemoteException;
import org.apache.bsf.debug.util.DebugLog;

import org.apache.bsf.util.*;
import org.apache.bsf.debug.jsdi.*;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;

/**
 * Insert the type's description here.
 * Creation date: (9/6/2001 1:21:46 PM)
 * @author: Administrator
 */
public class JsEngineStub
    extends org.apache.bsf.debug.util.Skeleton
    implements JsEngine {

    RhinoEngineDebugger m_rhinoDbg;
    boolean m_inCallback;
    boolean m_resumeExecution;
    Object  m_lock;

    /**
     * JsEngineStub constructor comment.
     */
    public JsEngineStub(RhinoEngineDebugger rhinoDbg)
        throws RemoteException {
        super(org.apache.bsf.debug.util.DebugConstants.JS_ENGINE_TID);
        m_rhinoDbg = rhinoDbg;
        m_lock = new Object();
    }

    public boolean isSuspended() throws RemoteException {
        return m_inCallback;
    }

    public boolean poll() { return true; }

    public Object eval(String docname, String exp, int lineNo)
        throws RemoteException {

        Object retval = null;

        Context.enter();
        try {
            retval = m_rhinoDbg.eval(docname, exp, lineNo);
        } catch (Throwable ex) {
            throw new RemoteException("Failed eval", ex);
        } finally {
            Context.exit();
        }
        return retval;
    }

    public JsContext getContext(int depth) throws RemoteException {
        try {
            Context.enter();
            return m_rhinoDbg.getContext(depth);
        } finally {
            Context.exit();
        }

    }

    public int getContextCount() throws RemoteException {
        int count;
        try {
            Context.enter();
            count = m_rhinoDbg.getContextCount();
            DebugLog.stdoutPrintln("    count = "+count,
                                   DebugLog.BSF_LOG_L3);
            return count;
        } finally {
            Context.exit();
        }

    }

    public String getThread() throws RemoteException {
        return m_rhinoDbg.getThread();
    }

    public String getThreadGroup() throws RemoteException {
        return m_rhinoDbg.getThreadGroup();
    }

    /**
     * Return the current debugger.
     * @return the debugger, or null if none is attached.
     */
    public JsCallbacks getDebugger() throws RemoteException {
        try {
            Context.enter();
            return m_rhinoDbg.getDebugger();
        } finally {
            Context.exit();
        }
    }

    public JsObject getGlobalObject() throws RemoteException {
        try {
            Context.enter();
            return m_rhinoDbg.getGlobalObject();
        } finally {
            Context.exit();
        }
    }

    public JsObject getUndefinedValue() throws RemoteException {
        try {
            Context.enter();
            return m_rhinoDbg.getUndefinedValue();
        } finally {
            Context.exit();
        }
    }

    public void run() throws RemoteException {
        try {
            Context.enter();
            m_rhinoDbg.run(this);
        } catch (Exception ex) {
            throw new RemoteException("Internal JSDI error",ex);
        } finally {
            Context.exit();
        }
    }

    /**
     * Set the associated debugger.
     * @param debugger the debugger to be used on callbacks from
     * the engine.
     */
    public void setDebugger(JsCallbacks debugger) throws RemoteException {
        try {
            Context.enter();
            m_rhinoDbg.setDebugger(debugger);
        } catch (Exception ex) {
            throw new RemoteException("Internal JSDI error",ex);
        } finally {
            Context.exit();
        }
    }

    public void stepIn() throws RemoteException {
        try {
            Context.enter();
            DebugLog.stdoutPrintln("Step In command on "+this,
                                   DebugLog.BSF_LOG_L3);
            m_rhinoDbg.stepIn(this);
        } catch (Exception ex) {
            throw new RemoteException("Internal JSDI error",ex);
        } finally {
            Context.exit();
        }
    }

    public void stepOut() throws RemoteException {
        try {
            Context.enter();
            m_rhinoDbg.stepOut(this);
        } catch (Exception ex) {
            throw new RemoteException("Internal JSDI error",ex);
        } finally {
            Context.exit();
        }
    }

    public void stepOver() throws RemoteException {
        try {
            Context.enter();
            m_rhinoDbg.stepOver(this);
        } catch (Exception ex) {
            throw new RemoteException("Internal JSDI error",ex);
        } finally {
            Context.exit();
        }
    }
}
