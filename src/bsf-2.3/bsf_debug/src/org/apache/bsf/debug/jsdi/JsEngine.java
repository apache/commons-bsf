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

package org.apache.bsf.debug.jsdi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Represents a remote engine as seen from the debugger.
 * It matches the notion of global object in JavaScript
 * specification. In other words, a JsEngine is an 
 * execution context rather than an explicit engine.
 * This also means that a JsEngine is not multi-threaded,
 * there is only one execution being carried.
 * Hence, there is only one stack of JsContext for a 
 * JsEngine. The stack is only accesible if the engine is
 * in a callback mode to the debugger, that is, the 
 * execution is stopped at a breakpoint or after a stepping
 * order has completed.
 * 
 */
public interface JsEngine extends Remote {

	/**
	 * Allows the client debugger to poll the connection.
	 */ 
	public boolean poll() throws RemoteException;

	/**
	 * Set the associated debugger.
	 * @param debugger the debugger to be used on callbacks from
	 * the engine.
	 */
	public void setDebugger(JsCallbacks debugger) throws RemoteException;

	/**
	 * Return the current debugger.
	 * @return the debugger, or null if none is attached.
	 */
	public JsCallbacks getDebugger() throws RemoteException;


	/**
	 * Allow the debugger to evaluate an expression
	 * within the current context.
	 */
	public Object eval(String docname, String fnOrScript, int lineno)
		throws RemoteException;

	/**
	 * Returns the count of JsContext on the current stack.
	 * This is a valid call only if the engine is stopped
	 * in a callback to the debugger (breakpoint or stepping
	 * completed).
	 */
 	public int getContextCount() 
                throws RemoteException;

        /**
         * Returns name of the thread currently running in the engine
         */
        public String getThread()
                throws RemoteException;

        /**
         * Returns name of the ThreadGroup of the thread currently running in the engine
         */
        public String getThreadGroup()
                throws RemoteException;

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
		throws RemoteException;
	/**
	 * Any execution in JavaScript happen with respect to a 
	 * global object, sort of the top-level name space for 
	 * properties. This is global object return by this call.
	 */
	public JsObject getGlobalObject() 
		throws RemoteException;

	/**
	 * As per ECMA specification, each JavaScript execution 
	 * defines a unique object for the undefined value.
	 */	
	public JsObject getUndefinedValue() 
		throws RemoteException;
	
	/**
	 * Stepping commands:
	 * 		run: resume execution until it finishes or a breakpoint is hit.
	 * 		stepIn: steps to the next statement, considering callee's statement if any.
	 * 		stepOut: steps until the current JsContext exits.
	 * 		stepOver: steps to the next statement within the same JsContext.
	 */
	public void run() throws RemoteException;
	public void stepIn() throws RemoteException;
	public void stepOut() throws RemoteException;
	public void stepOver() throws RemoteException;

	public boolean isSuspended() throws RemoteException;

}
