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

public class JsContextStub extends Stub implements JsContext {
	public  JsContextStub(SocketConnection m_con, int tid, int uid) {
		super(m_con,tid,uid);
	}
	
	//------------------------------------------------------------
	/*
	public void parseResult(ResultCell cell) throws Exception {

		int tid,uid;
		switch (cell.methodId) {
			case DebugConstants.CX_BIND:
			case DebugConstants.CX_GET_CODE:
			case DebugConstants.CX_GET_ENGINE:
			case DebugConstants.CX_GET_SCOPE:
			case DebugConstants.CX_GET_THIS:
				cell.stub = (Stub)m_con.readObject();
				break;
			case DebugConstants.CX_GET_DEPTH:
			case DebugConstants.CX_GET_LINE_NUMBER:
				cell.val32 = m_con.readInt();
				break;
			case DebugConstants.CX_GET_SOURCE_NAME:
				cell.oval = m_con.readObject();
				break;
			
			default:
				throw new Error("Error in the request/answer protocol");
		}
	}
*/
	
	public JsObject bind(String id) throws RemoteException {
		
		ResultCell cell;
		
		try {
			cell = m_con.prepareOutgoingInvoke(this,DebugConstants.JS_CONTEXT_TID,DebugConstants.CX_BIND);
			cell.writeObject(id);
			return (JsObject)cell.waitForObject();		
			
		} catch (IOException ex) {
			throw new RemoteException("Marshalling error", ex);
		} catch (Exception ex) {
			throw new RemoteException("Error at server", ex);
		}
	}
	
	public JsCode getCode() throws RemoteException {
		
		ResultCell cell;		
		try {
			cell = m_con.prepareOutgoingInvoke(this,DebugConstants.JS_CONTEXT_TID,DebugConstants.CX_GET_CODE);
			return (JsCode)cell.waitForObject();		
			
		} catch (IOException ex) {
			throw new RemoteException("Marshalling error", ex);
		} catch (Exception ex) {
			throw new RemoteException("Error at server", ex);
		}
	}
	
	public int getDepth() throws RemoteException {
		
		ResultCell cell;
		try {
			cell = m_con.prepareOutgoingInvoke(this,DebugConstants.JS_CONTEXT_TID,DebugConstants.CX_GET_DEPTH);
			return cell.waitForIntValue();		
			
		} catch (IOException ex) {
			throw new RemoteException("Marshalling error", ex);
		} catch (Exception ex) {
			throw new RemoteException("Error at server", ex);
		}
	}
	
	public JsEngine getEngine() throws RemoteException {
		ResultCell cell;
		try {
			cell = m_con.prepareOutgoingInvoke(this,DebugConstants.JS_CONTEXT_TID,DebugConstants.CX_GET_ENGINE);
			return (JsEngine)cell.waitForObject();
			
		} catch (IOException ex) {
			throw new RemoteException("Marshalling error", ex);
		} catch (Exception ex) {
			throw new RemoteException("Error at server", ex);
		}
	}
	
	public int getLineNumber() throws RemoteException {
		
		ResultCell cell;
		try {
			cell = m_con.prepareOutgoingInvoke(this,DebugConstants.JS_CONTEXT_TID,DebugConstants.CX_GET_LINE_NUMBER);
			return cell.waitForIntValue();		
			
		} catch (IOException ex) {
			throw new RemoteException("Marshalling error", ex);
		} catch (Exception ex) {
			throw new RemoteException("Error at server", ex);
		}
	}
	public JsObject getScope() throws RemoteException {
		
		ResultCell cell;
		try {
			cell = m_con.prepareOutgoingInvoke(this,DebugConstants.JS_CONTEXT_TID,DebugConstants.CX_GET_SCOPE);
			return (JsObject)cell.waitForObject();		
			
		} catch (IOException ex) {
			throw new RemoteException("Marshalling error", ex);
		} catch (Exception ex) {
			throw new RemoteException("Error at server", ex);
		}
	}
	public String getSourceName() throws RemoteException {
		
		ResultCell cell;
		try {
			cell = m_con.prepareOutgoingInvoke(this,DebugConstants.JS_CONTEXT_TID,DebugConstants.CX_GET_SOURCE_NAME);
			return (String)cell.waitForValueObject();
			
		} catch (IOException ex) {
			throw new RemoteException("Marshalling error", ex);
		} catch (Exception ex) {
			throw new RemoteException("Error at server", ex);
		}
	}
	public JsObject getThis() throws RemoteException {
		
		ResultCell cell;
		try {
			cell = m_con.prepareOutgoingInvoke(this,DebugConstants.JS_CONTEXT_TID,DebugConstants.CX_GET_THIS);
			return (JsObject)cell.waitForObject();
			
		} catch (IOException ex) {
			throw new RemoteException("Marshalling error", ex);
		} catch (Exception ex) {
			throw new RemoteException("Error at server", ex);
		}
	}
}

