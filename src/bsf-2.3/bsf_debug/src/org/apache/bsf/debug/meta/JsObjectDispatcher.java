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

package org.apache.bsf.debug.meta;
import java.io.*;
import java.net.*;
import org.apache.bsf.debug.*;
import org.apache.bsf.debug.jsdi.*;
import org.apache.bsf.debug.util.*;

public class JsObjectDispatcher extends Dispatcher {
	public JsObjectDispatcher(SocketConnection con) {
		super(con);
	}

	public void dispatch(ResultCell rcell) throws Exception {
		String name, lang;
		int depth, lineno, attributes, index;
		boolean bool, all;
		JsContext cx;
		Object obj, val;
		Class clazz;
		JsObject self;

		self = (JsObject) rcell.selfSkel;

		switch (rcell.methodId) {
			case DebugConstants.JO_GET_DEFAULT_VALUE :
				name = (String) rcell.readObject();
				clazz = Class.forName(name);
				obj = self.getDefaultValue(clazz);

				rcell.objectResult(obj);
				break;

			case DebugConstants.JO_DEFINE :
				name = (String) rcell.readObject();
				val = rcell.readObject();
				attributes = rcell.readInt();
				self.define(name, val, attributes);
				rcell.voidResult();
				break;

			case DebugConstants.JO_DELETE_BY_INDEX :
				index = rcell.readInt();
				self.delete(index);
				rcell.voidResult();
				break;

			case DebugConstants.JO_DELETE_BY_NAME :
				name = (String) rcell.readObject();
				self.delete(name);
				rcell.voidResult();
				break;

			case DebugConstants.JO_PUT_BY_NAME :
				name = (String) rcell.readObject();
				val = rcell.readObject();
				self.put(name, val);
				rcell.voidResult();
				break;

			case DebugConstants.JO_PUT_BY_INDEX :
				index = rcell.readInt();
				val = rcell.readObject();
				self.put(index, val);
				rcell.voidResult();
				break;

			case DebugConstants.JO_SET_PROTOTYPE :
				obj = rcell.readObject();
				self.setScope((JsObject) obj);
				rcell.voidResult();
				break;

			case DebugConstants.JO_SET_SCOPE :
				obj = rcell.readObject();
				self.setPrototype((JsObject) obj);
				rcell.voidResult();
				break;

			case DebugConstants.JO_GET_BY_NAME :
				name = (String) rcell.readObject();
				obj = self.get(name);
				rcell.objectResult(obj);
				break;

			case DebugConstants.JO_GET_BY_INDEX :
				index = rcell.readInt();
				obj = self.get(index);
				rcell.objectResult(obj);
				break;

			case DebugConstants.JO_GET_CLASSNAME :
				name = self.getClassName();
				rcell.objectResult(name);
				break;
			case DebugConstants.JO_GET_IDS :
				all = rcell.readBoolean();
				obj = self.getIds(all);
				rcell.objectResult(obj);
				break;

			case DebugConstants.JO_GET_PROTOTYPE :
				obj = self.getPrototype();
				rcell.objectResult(obj);
				break;

			case DebugConstants.JO_GET_SCOPE :
				obj = self.getScope();
				rcell.objectResult(obj);
				break;

			case DebugConstants.JO_HAS_BY_INDEX :
				index = rcell.readInt();
				bool = self.has(index);
				rcell.booleanResult(bool);
				break;
			case DebugConstants.JO_HAS_BY_NAME :
				name = (String) rcell.readObject();
				bool = self.has(name);
				rcell.booleanResult(bool);
				break;

			case DebugConstants.JO_HAS_INSTANCE :
				obj = rcell.readObject();
				bool = self.hasInstance((JsObject) obj);
				rcell.booleanResult(bool);
				break;
		}
	}
}
