/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.mozilla.javascript;

/**
 * Hack to enable creating E4X XML from Axiom OMElements
 * outside of a script invocation. To do this requires
 * Context.topCallScope not be null, but outside of a script
 * invocation it is null, hence this method to enable setting it.
 * Could be a bug in the Axiom E4X impl as the XmlBeans impl
 * does not require this.
 */
public class ContextHelper {
	
	public static void setTopCallScope(Context cx, Scriptable scope) {
		if (cx.topCallScope == null) {
			cx.topCallScope = scope;
		}
	}

}
