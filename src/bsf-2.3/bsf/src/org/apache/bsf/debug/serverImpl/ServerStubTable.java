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

package org.apache.bsf.debug.serverImpl;

import java.util.*;
import org.apache.bsf.debug.jsdi.*;
import org.apache.bsf.debug.util.*;
import org.apache.bsf.debug.meta.*;

/**
 * This class provides the resident object table for the
 * mirrors. A Stub represents a remote JavaScript object
 * or any remote concept that needs to be identified such 
 * engines or contexts which are not JavaScript objects.
 * 
 * A Stub has a unique identifier allocated on the 
 * server. This is called its oid, for object identifier.
 * The oid is not only carrying the identity but also the 
 * type of the remote object. Knowing the type is essential
 * when a Stub has to be created. For instance, it is important
 * to know if a remote object is just an object or a reified
 * code as a script or a function.
 * 
 * The oid is a long, the high word is the type (tid), the low word
 * is the unique identifier (uid). The unique identifier is globally 
 * unique across all types.
 * 
 * This class also acts as the factory for Stub objects.
 * When a Stub is created, it is remembered in the resident
 * object table for later rebinding from its uid.
 * 
 */
public class ServerStubTable extends StubTable {
	private ObjectServer m_server;
		
	public ServerStubTable(ObjectServer server) {
		super(server);
		m_server = server;
	}
	protected Stub factory(int tid, int uid) {
		Stub stub=null;
		switch(tid) {
			case DebugConstants.BSF_DEBUGGER_TID:
				stub = new DebuggerStub(m_server,tid,uid);
				break;
			case DebugConstants.JS_CALLBACKS_TID:
				stub = new JsCallbacksStub(m_server,tid,uid);
				break;  
			default:
				throw new Error("Unknown TID="+tid+" ["+DebugConstants.getConstantName(tid)+"]");
		}
		return stub;
	}		
}

