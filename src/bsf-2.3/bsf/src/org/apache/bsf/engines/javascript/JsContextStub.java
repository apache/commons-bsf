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

import org.mozilla.javascript.*;
import org.mozilla.javascript.debug.*;

import org.apache.bsf.debug.*;
import org.apache.bsf.debug.jsdi.*;

import java.rmi.RemoteException;
/**
* Insert the type's description here.
* Creation date: (8/23/2001 4:16:50 PM)
* @author: Administrator
*/

public class JsContextStub
extends org.apache.bsf.debug.util.Skeleton
   implements JsContext {

    RhinoContextProxy m_rcp;
    RhinoEngineDebugger m_rhinoDbg;
    int m_frameno;
    int m_lineno;
    boolean m_atBreakpoint;
    boolean m_invalid;

    CompilationUnit m_unit;
    Scriptable m_variableObject;
    Scriptable m_thisObj;

    /**
     * JsContextStub constructor comment.
     */
    JsContextStub(RhinoContextProxy rcp, CompilationUnit unit)
    throws RemoteException {
        super(org.apache.bsf.debug.util.DebugConstants.JS_CONTEXT_TID);

        m_rhinoDbg = rcp.getRhinoEngineDebugger();
        m_rcp = rcp;
        m_unit = unit;
        m_invalid = false;
        m_atBreakpoint = true;
    }

    DebugFrame getRhinoDebugFrame() {
        return new RhinoDebugFrame(this);
    }

    //--------------------------------------------------
    void atBreakpoint(boolean atbrkpt) {
        m_atBreakpoint = atbrkpt;
    }
    public JsObject bind(String id) throws RemoteException {
        try {
            Context.enter();
            Scriptable obj = m_variableObject;
            Object prop;
            while (obj != null) {
                Scriptable m = obj;
                do {
                    if (m.has(id, obj))
                        return m_rhinoDbg.marshallScriptable(obj);
                    m = m.getPrototype();
                } while (m != null);
                obj = obj.getParentScope();
            }
            throw new JsdiException("Name not in scope.");
        } finally {
            Context.exit();
        }
    }
    //--------------------------------------------------
    public JsCode getCode() {
        if (m_invalid)
            throw new JsdiException("This context no longer exists.");
        if (!m_atBreakpoint)
            throw new JsdiException("Resumed context, can't get the code.");

        try {
            Context.enter();
            return null;
        } finally {
            Context.exit();
        }
    }
    public int getDepth() {
        return m_frameno;
    }
    //--------------------------------------------------
    public JsEngine getEngine() {
        RhinoEngineDebugger redbg;
        redbg = m_rcp.getRhinoEngineDebugger();
        return (JsEngine) redbg.getDebugInterface();

    }
    //--------------------------------------------------
    public int getLineNumber() {
        if (m_invalid)
            throw new JsdiException("This context no longer exists.");
        if (!m_atBreakpoint)
            throw new JsdiException("Resumed context, can't get line number.");

        return m_lineno;
    }
    //------------------------------------------------------
    public JsObject getScope() throws RemoteException {

        if (m_invalid)
            throw new JsdiException("This context no longer exists.");
        if (!m_atBreakpoint)
            throw new JsdiException("Resumed context, can't get line number.");

        try {
            Context.enter();
            JsObject scope = m_rhinoDbg.marshallScriptable(m_variableObject);
            return scope;
        } finally {
            Context.exit();
        }
    }
    //------------------------------------------------------
    public String getSourceName() {
        if (m_invalid)
            throw new JsdiException("This context no longer exists.");
        if (!m_atBreakpoint)
            throw new JsdiException("Resumed context, can't get line number.");

        return m_unit.m_dbgScript.getSourceName();
    }
    //------------------------------------------------------
    public JsObject getThis() throws RemoteException {

        if (m_invalid)
            throw new JsdiException("This context no longer exists.");
        if (!m_atBreakpoint)
            throw new JsdiException("Resumed context, can't get line number.");

        try {
            Context.enter();
            JsObject thisobj = null;
            Scriptable obj = null;
            NativeCall call = null;
            Scriptable varobj = m_variableObject;
            if (varobj instanceof NativeCall) {
                call = (NativeCall) varobj;
                obj = call.getThisObj();
                thisobj = m_rhinoDbg.marshallScriptable(varobj);
            }
            return thisobj;
        } finally {
            Context.exit();
        }
    }
    //--------------------------------------------------
    void invalidate() {
        m_invalid = true;
    }
    //------------------------------------------------------
    public boolean isEvalContext() {
        if (m_invalid)
            throw new JsdiException("This context no longer exists.");
        if (!m_atBreakpoint)
            throw new JsdiException("Resumed context, can't get line number.");

        try {
            Context.enter();
            return false;
        } finally {
            Context.exit();
        }
    }
    //------------------------------------------------------
    public boolean isFunctionContext() {
        if (m_invalid)
            throw new JsdiException("This context no longer exists.");
        if (!m_atBreakpoint)
            throw new JsdiException("Resumed context, can't get line number.");

        try {
            Context.enter();
            return false;
        } finally {
            Context.exit();
        }
    }
    //------------------------------------------------------
    public boolean isScriptContext() {
        if (m_invalid)
            throw new JsdiException("This context no longer exists.");
        if (!m_atBreakpoint)
            throw new JsdiException("Resumed context, can't get line number.");
        try {
            Context.enter();
            return true;
        } finally {
            Context.exit();
        }
    }
    public Object lookupName(String name) {

        try {
            Context.enter();
            Scriptable obj = m_variableObject;
            Object prop;
            while (obj != null) {
                Scriptable m = obj;
                do {
                    Object result = m.get(name, obj);
                    if (result != Scriptable.NOT_FOUND)
                        return result;
                    m = m.getPrototype();
                } while (m != null);
                obj = obj.getParentScope();
            }
            throw new JsdiException("Name is not in scope.");
        } finally {
            Context.exit();
        }
    }
    /**
     * Looks up a name in the scope chain and returns its value.
     */
    public Object lookupName(Scriptable scopeChain, String id) {

        try {
            Context.enter();
            Scriptable obj = scopeChain;
            Object prop;
            while (obj != null) {
                Scriptable m = obj;
                do {
                    Object result = m.get(id, obj);
                    if (result != Scriptable.NOT_FOUND)
                        return result;
                    m = m.getPrototype();
                } while (m != null);
                obj = obj.getParentScope();
            }
            return null;
        } finally {
            Context.exit();
        }
    }
}

class RhinoDebugFrame implements DebugFrame {

    JsContextStub m_stub;

    RhinoDebugFrame(JsContextStub stub) {
        m_stub = stub;
    }

    public void onEnter(Context cx, Scriptable activation,
                        Scriptable thisObj, Object[] args)
    {
        m_stub.m_variableObject = activation;
        m_stub.m_thisObj = thisObj;
        m_stub.m_frameno = m_stub.m_rcp.m_frameStack.size();
        m_stub.m_rcp.m_frameStack.push(m_stub);
    }

    public void onExit(Context cx, boolean byThrow, Object resultOrException)
    {
        m_stub.m_rcp.m_frameStack.pop();
        m_stub.invalidate();
    }

    public void onExceptionThrown(Context cx, Throwable ex) {
        m_stub.m_rcp.m_reDbg.handleExceptionThrown(cx, m_stub.m_rcp, ex);
    }

    public void onLineChange(Context cx, int lineNumber) {
        m_stub.m_lineno = lineNumber;
        if (m_stub.m_unit.hasBreakpoint(lineNumber)) {
            m_stub.m_rcp.m_reDbg.handleBreakpointHit(cx, m_stub.m_rcp);
        }
    }
}
