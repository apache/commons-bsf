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

package org.apache.bsf.dbline;

import java.rmi.RemoteException;
import org.apache.bsf.debug.*;
import org.apache.bsf.debug.jsdi.*;

/**
 * This object is the remote object that will be passed to 
 * the remote debug manager as both the BSFDebugger and 
 * the JavaScript-specific callback object. 
 * 
 * Note: there is no obligation that both be implemented by the 
 * same object.
 * 
 * 
 * @author: Olivier Gruber
 */ 
public class Callbacks  
extends org.apache.bsf.debug.util.Skeleton
    implements BSFDebugger, JsCallbacks {

	JsDb db;

	Callbacks(JsDb db) throws RemoteException {
		super(org.apache.bsf.debug.util.DebugConstants.BSF_DEBUGGER_TID);
		this.db = db;
	} 
	////////////////////////////////////////////////////////////////
	// BSF Debugger Interface	
	////////////////////////////////////////////////////////////////
	
	public boolean poll() {
		return true;
	}	
	public void createdEngine(String lang, Object engine) throws RemoteException {
		if (lang.equals("javascript")) {
			db.createdEngine((JsEngine)engine);
		}
	}
	public void deletedEngine(Object engine) throws RemoteException {
		if (engine instanceof JsEngine) {
			db.deletedEngine((JsEngine)engine);
		}
	}
	public void disconnect() throws RemoteException {
		System.out.println("Line debugger disconnected...");
	}
	////////////////////////////////////////////////////////////////
	// JsCallbacks Interface	
	////////////////////////////////////////////////////////////////
	public void handleBreakpointHit(JsContext cx) throws RemoteException {
		db.handleBreakpointHit(cx);
	}
	public void handleEngineStopped(JsContext cx) throws RemoteException {
		db.handleEngineStopped(cx);
	}
	public void handleExceptionThrown(JsContext cx, Object exception) throws RemoteException {
		db.handleExceptionThrown(cx,exception);
	}
	public void handleSteppingDone(JsContext cx) throws RemoteException {
		db.handleSteppingDone(cx);
	}
}
