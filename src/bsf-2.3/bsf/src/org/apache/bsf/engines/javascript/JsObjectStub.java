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

package org.apache.bsf.engines.javascript;

import org.apache.bsf.debug.*;
import org.apache.bsf.debug.jsdi.*;

import org.mozilla.javascript.*;
import org.mozilla.javascript.debug.*;

import java.rmi.RemoteException;

/**
 * Insert the type's description here.
 * Creation date: (8/24/2001 9:54:48 AM)
 * @author: Administrator
 */
public class JsObjectStub
extends org.apache.bsf.debug.util.Skeleton
	implements JsObject {

	Scriptable m_object;

	RhinoEngineDebugger m_rhinoDbg;

	//////////////////////////////////////////////////////////////////
	public JsObjectStub(RhinoEngineDebugger rhinoDbg, Scriptable object)
		throws RemoteException {
		super(org.apache.bsf.debug.util.DebugConstants.JS_OBJECT_TID);
		m_rhinoDbg = rhinoDbg;
		m_object = object;
	}
	//////////////////////////////////////////////////////////////////
	public void define(String propertyName, JsObject value, int attributes) {
		try {
			Context.enter();
		} finally {
			Context.exit();
		}
	}
	//////////////////////////////////////////////////////////////////
	public void define(String propertyName, Object value, int attributes) {
		try {
			Context.enter();
		} finally {
			Context.exit();
		}
	}
	//////////////////////////////////////////////////////////////////
	public void delete(int index) {
		try {
			Context.enter();
		} finally {
			Context.exit();
		}
	}
	//////////////////////////////////////////////////////////////////
	public void delete(String name) {
		try {
			Context.enter();
		} finally {
			Context.exit();
		}
	}
	//////////////////////////////////////////////////////////////////
	public Object get(int index) throws RemoteException {
		try {
			Context.enter();
			Object prop = m_object.get(index, m_object);
			return m_rhinoDbg.marshallProperty(prop);
		} finally {
			Context.exit();
		}
	}
	//////////////////////////////////////////////////////////////////
	public Object get(String name) throws RemoteException {
		try {
			Context.enter();
			Object prop = m_object.get(name, m_object);
			return m_rhinoDbg.marshallProperty(prop);
		} finally {
			Context.exit();
		}
	}
	//////////////////////////////////////////////////////////////////
	public String getClassName() {
		try {
			Context.enter();
			return null;
		} finally {
			Context.exit();
		}
	}
	//////////////////////////////////////////////////////////////////
	public Object getDefaultValue(Class hint) {
		try {
			Context.enter();
			return null;
		} finally {
			Context.exit();
		}
	}
	//////////////////////////////////////////////////////////////////
	public Object[] getIds(boolean all) {
		try {
			Context.enter();
			Object ids[] = null;
			if (all) {
				if (m_object instanceof ScriptableObject) {
					ScriptableObject so = (ScriptableObject) m_object;
					ids = so.getAllIds();
				} else
					ids = m_object.getIds();
			} else
				ids = m_object.getIds();

			return ids;
		} finally {
			Context.exit();
		}
	}
	//////////////////////////////////////////////////////////////////
	public JsObject getPrototype() throws RemoteException {
		try {
			Context.enter();
			Scriptable prot = m_object.getPrototype();
			return m_rhinoDbg.marshallScriptable(prot);
/*
			JsObject stub;

			stub = m_rhinoDbg.getStub(prot);
			if (stub == null) {
				stub = new JsObjectStub(m_rhinoDbg, prot);
			}
			return (JsPrototype) stub;
*/
		} finally {
			Context.exit();
		}
	}
	//////////////////////////////////////////////////////////////////
	public JsObject getScope() throws RemoteException {
		try {
			Context.enter();
			Scriptable obj = m_object.getParentScope();
			return m_rhinoDbg.marshallScriptable(obj);
		} finally {
			Context.exit();
		}
	}
	//////////////////////////////////////////////////////////////////
	public boolean has(int index) {
		try {
			Context.enter();
			return false;
		} finally {
			Context.exit();
		}
	}
	//////////////////////////////////////////////////////////////////
	public boolean has(String name) {
		try {
			Context.enter();
			return false;
		} finally {
			Context.exit();
		}
	}
	//////////////////////////////////////////////////////////////////
	public boolean hasInstance(JsObject instance) {
		try {
			Context.enter();
			return false;
		} finally {
			Context.exit();
		}
	}
	//////////////////////////////////////////////////////////////////
	public boolean isFunction() {
		try {
			Context.enter();
			return (m_object instanceof NativeFunction)
				&& !(m_object instanceof NativeScript);
		} finally {
			Context.exit();
		}
	}
	//////////////////////////////////////////////////////////////////
	public boolean isScript() {
		try {
			Context.enter();
			return (m_object instanceof NativeScript);
		} finally {
			Context.exit();
		}
	}
	//////////////////////////////////////////////////////////////////
	public boolean isWrapper() {
		try {
			Context.enter();
			return (m_object instanceof Wrapper);
		} finally {
			Context.exit();
		}
	}
	//////////////////////////////////////////////////////////////////
	public void put(int index, Object value) {

		JsObjectStub stub;
		try {
			
			Context.enter();
			
			if (value instanceof JsObject) {
				stub = (JsObjectStub)value;
				value = stub.m_object;
			}
			m_object.put(index,m_object,value);
		} finally {
			Context.exit();
		}
	}
	//////////////////////////////////////////////////////////////////
	public void put(String name, Object value) {
		JsObjectStub stub;
		try {
			
			Context.enter();
			
			if (value instanceof JsObject) {
				stub = (JsObjectStub)value; 
				value = stub.m_object;
			}
			m_object.put(name,m_object,value);
		} finally {
			Context.exit();
		}
	}
	//////////////////////////////////////////////////////////////////
	public void setPrototype(JsObject prototype) {
		JsObjectStub stub;
		Scriptable prot;
		try {
			Context.enter();
			stub = (JsObjectStub)prototype;
			prot = stub.m_object;
			m_object.setPrototype(prot);
		} finally {
			Context.exit();
		}
	}
	//////////////////////////////////////////////////////////////////
	public void setScope(JsObject jsobj) {
		JsObjectStub stub;
		Scriptable scope;
		try {
			Context.enter();
			stub = (JsObjectStub)jsobj;
			scope = stub.m_object;
			m_object.setParentScope(scope);
		} finally {
			Context.exit();
		}
	}
	//////////////////////////////////////////////////////////////////
	public Object unwrap() {
		try {
			Context.enter();
			if (m_object instanceof Wrapper)
				return ((Wrapper) m_object).unwrap();
			else
				return null;
		} finally {
			Context.exit();
		}
	}
	//////////////////////////////////////////////////////////////////
	public boolean wrapsJavaObject() {
		try {
			Context.enter();
			return false;
		} finally {
			Context.exit();
		}
	}
	//////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////
	//////////////////////////////////////////////////////////////////
	/*
	public void deleteProperty(int index) {
		try {
			Context.enter();
		} finally {
			Context.exit();
		}
	}
	public void deleteProperty(String name) {
		try {
			Context.enter();
		} finally {
			Context.exit();
		}
	}
	public JsPrototype getBase(int index) throws RemoteException {
		try {
			Context.enter();
			Scriptable m = m_object;
			do {
				if (m.has(index, m)) {
					return (JsPrototype) m_rhinoDbg.marshallScriptable(m);
				}
				m = m.getPrototype();
			} while (m != null);
			/ *
			// CAN'T REMEMBER WHY I WAS LOOKUP THE PARENT SCOPES...
			// FOR FINDING THE BASE IT DOES NOT SEEM APPROPRIATE.
			// 
			Context.enter();
			Scriptable obj = m_object;
			Object prop;
			while (obj != null) {
				Scriptable m = obj;
				do {
					// if (m.get(id, obj) != Scriptable.NOT_FOUND)
					if (m.has(index, obj)) {
						return (JsPrototype) m_rhinoDbg.marshallScriptable(m);
					}
					m = m.getPrototype();
				} while (m != null);
				obj = obj.getParentScope();
			}
			* /
			return null;
		} finally {
			Context.exit();
		}
	}
	public JsPrototype getBase(String id) throws RemoteException {
		try {
			Context.enter();
			Scriptable m = m_object;
			do {
				if (m.has(id, m)) {
					return (JsPrototype) m_rhinoDbg.marshallScriptable(m);
				}
				m = m.getPrototype();
			} while (m != null);
			
			// CAN'T REMEMBER WHY I WAS LOOKUP THE PARENT SCOPES...
			// FOR FINDING THE BASE IT DOES NOT SEEM APPROPRIATE.
			// 
			/ *			
			Context.enter();
			Scriptable obj = m_object;
			Object prop;
			while (obj != null) {
				Scriptable m = obj;
				do {
					// if (m.get(id, obj) != Scriptable.NOT_FOUND)
					if (m.has(id, obj)) {
						return (JsPrototype) m_rhinoDbg.marshallScriptable(m);
					}
					m = m.getPrototype();
				} while (m != null);
				obj = obj.getParentScope();
			}
			* /
			return null;
		} finally {
			Context.exit();
		}
	}
	public Object getProperty(int index) throws RemoteException {
		try {
			Context.enter();
			JsPrototype prot = getBase(index);
			if (prot != null) {
				return prot.get(index);
			}
			return null;
		} finally {
			Context.exit();
		}
	}
	public Object getProperty(String name) throws RemoteException {
		try {
			Context.enter();
			JsPrototype prot = getBase(name);
			if (prot != null) {
				return prot.get(name);
			}
			return null;
		} finally {
			Context.exit();
		}
	}
	public Object[] getPropertyIds() {
		try {
			Context.enter();
			return null;
		} finally {
			Context.exit();
		}
	}
	public void putProperty(int index, Object value) {
		try {
			Context.enter();
		} finally {
			Context.exit();
		}
	}
	public void putProperty(String name, Object value) {
		try {
			Context.enter();
		} finally {
			Context.exit();
		}
	}
	*/	
}
