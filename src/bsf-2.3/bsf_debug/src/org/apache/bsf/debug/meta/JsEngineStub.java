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

package org.apache.bsf.debug.meta;

import java.rmi.RemoteException;
import java.util.*;
import java.io.*;
import org.apache.bsf.debug.jsdi.*;
import org.apache.bsf.debug.util.*;

public class JsEngineStub extends Stub implements JsEngine {

    //-------------------------------------------------
    boolean fSuspended;
    JsCallbacks fCallbacks;
    //-------------------------------------------------
    public  JsEngineStub(SocketConnection con,int tid, int uid) {
        super(con,tid,uid);
    }	

    ////////////////////////////////////////////////////////////////////
    // JsEngine Interface 	
    ////////////////////////////////////////////////////////////////////

    public boolean poll() throws RemoteException { 
        ResultCell cell;
        try {
            cell = m_con.prepareOutgoingInvoke(this,DebugConstants.JS_ENGINE_TID,DebugConstants.CB_POLL);
            return cell.waitForBooleanValue();		
        } catch (IOException ex) {
            throw new RemoteException("Marshalling error", ex);
        } catch (Exception ex) {
            throw new RemoteException("Error at server", ex);
        }
    }		
    /**
     * Set the associated debugger.
     * @param debugger the debugger to be used on callbacks from
     * the engine.
     */
    public void setDebugger(JsCallbacks debugger) throws RemoteException {

        fCallbacks = debugger;

        ResultCell cell;
        try {
            cell = m_con.prepareOutgoingInvoke(this,DebugConstants.JS_ENGINE_TID,DebugConstants.JE_SET_DEBUGGER);
            cell.writeObject(debugger);
            cell.waitForCompletion();		
        } catch (IOException ex) {
            throw new RemoteException("Marshalling error", ex);
        } catch (Exception ex) {
            throw new RemoteException("Error at server", ex);
        }
    }

    /**
     * Return the current debugger.
     * @return the debugger, or null if none is attached.
     */
    public JsCallbacks getDebugger() throws RemoteException {
        return fCallbacks;
    }


    /**
     * Allow the debugger to evaluate an expression
     * within the current context.
     */
    public Object eval(String docname, String fnOrScript, int lineno)
        throws RemoteException {
        throw new Error("NYI");	
    }

    /**
     * Returns the count of JsContext on the current stack.
     * This is a valid call only if the engine is stopped
     * in a callback to the debugger (breakpoint or stepping
     * completed).
     */
    public int getContextCount() 
        throws RemoteException {
		
        ResultCell cell;
        try {
            cell = m_con.prepareOutgoingInvoke(this,DebugConstants.JS_ENGINE_TID,DebugConstants.JE_GET_CONTEXT_COUNT);
            return cell.waitForIntValue();		
        } catch (IOException ex) {
            throw new RemoteException("Marshalling error", ex);
        } catch (Exception ex) {
            throw new RemoteException("Error at server", ex);
        }
    }

    public String getThread() throws RemoteException {

        ResultCell cell;
        try {
            cell = 
                m_con.prepareOutgoingInvoke(this, 
                                            DebugConstants.JS_ENGINE_TID,
                                            DebugConstants.JE_GET_THREAD);
            return (String)cell.waitForObject();
        } 
        catch (IOException ex) {
            throw new RemoteException("Marshalling error", ex);
        } 
        catch (Exception ex) {
            throw new RemoteException("Error at server", ex);
        }
    }

    public String getThreadGroup() throws RemoteException {

        ResultCell cell;
        try {
            cell =
                m_con.prepareOutgoingInvoke(this,
                                            DebugConstants.JS_ENGINE_TID,
                                            DebugConstants.JE_GET_THREADGROUP);
            return (String)cell.waitForObject();
        }
        catch (IOException ex) {
            throw new RemoteException("Marshalling error", ex);
        }
        catch (Exception ex) {
            throw new RemoteException("Error at server", ex);
        }
    }

    /**
     * Returns the JsContext at a certain depth.
     * Depth zero is the top of the stack, that is,
     * the inner execution context.
     * 
     * This is a valid call only if the engine is stopped
     * in a callback to the debugger (breakpoint or stepping
     * completed).
     */
    public JsContext getContext(int depth) 
        throws RemoteException {

        ResultCell cell;
        try {
            cell = m_con.prepareOutgoingInvoke(this,DebugConstants.JS_ENGINE_TID,DebugConstants.JE_GET_CONTEXT_AT);
            cell.writeInt(depth);
            return (JsContext)cell.waitForObject();	
        } catch (IOException ex) {
            throw new RemoteException("Marshalling error", ex);
        } catch (Exception ex) {
            throw new RemoteException("Error at server", ex);
        }
    }			
    /**
     * Any execution in JavaScript happen with respect to a 
     * global object, sort of the top-level name space for 
     * properties. This is global object return by this call.
     */
    public JsObject getGlobalObject() 
        throws RemoteException {
			
        ResultCell cell;
        try {
            cell = m_con.prepareOutgoingInvoke(this,DebugConstants.JS_ENGINE_TID,DebugConstants.JE_GET_GLOBAL_OBJECT);
            return (JsObject)cell.waitForObject();
				
        } catch (IOException ex) {
            throw new RemoteException("Marshalling error", ex);
        } catch (Exception ex) {
            throw new RemoteException("Error at server", ex);
        }
    }			

    /**
     * As per ECMA specification, each JavaScript execution 
     * defines a unique object for the undefined value.
     */	
    public JsObject getUndefinedValue() 
        throws RemoteException {
			
        ResultCell cell;
        try {
            cell = m_con.prepareOutgoingInvoke(this,DebugConstants.JS_ENGINE_TID,DebugConstants.JE_GET_UNDEFINED_VALUE);
            return (JsObject)cell.waitForObject();	
				
        } catch (IOException ex) {
            throw new RemoteException("Marshalling error", ex);
        } catch (Exception ex) {
            throw new RemoteException("Error at server", ex);
        }
    }
	
    /**
     * Stepping commands:
     * 		run: resume execution until it finishes or a breakpoint is hit.
     * 		stepIn: steps to the next statement, considering callee's statement if any.
     * 		stepOut: steps until the current JsContext exits.
     * 		stepOver: steps to the next statement within the same JsContext.
     */
    public void run() throws RemoteException {
        resume(DebugConstants.JE_RUN);		
    }
    public void stepIn() throws RemoteException {
        resume(DebugConstants.JE_STEP_IN);				
    }
    public void stepOut() throws RemoteException {
        resume(DebugConstants.JE_STEP_OUT);
    }
    public void stepOver() throws RemoteException {
        resume(DebugConstants.JE_STEP_OVER);
    }

    private void resume(int cmd) throws RemoteException {
        ResultCell cell;
        try {
            cell = m_con.prepareOutgoingInvoke(this,DebugConstants.JS_ENGINE_TID,cmd);
            cell.waitForCompletion();
        } catch (Exception ex) {
            throw new RemoteException("Marshalling error",ex);
        } 
        this.suspended(false);
    }
	
    void suspended(boolean suspended) {
        fSuspended = suspended;
    }
	
    public boolean isSuspended() {
        return fSuspended;
    }
}

