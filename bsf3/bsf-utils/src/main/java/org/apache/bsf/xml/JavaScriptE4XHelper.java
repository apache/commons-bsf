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
package org.apache.bsf.xml;

import javax.script.ScriptEngine;

/**
 * XMLHelper for JavaScript E4X
 */
public class JavaScriptE4XHelper extends DefaultXMLHelper {

	public static XMLHelper getXMLHelper(ScriptEngine engine) {

		try {
			Class.forName("org.wso2.javascript.xmlimpl.XMLLibImpl", true, JavaScriptE4XHelper.class.getClassLoader());
			return new JavaScriptE4XAxiomHelper(engine);
		} catch (ClassNotFoundException e) {
			// TODO: support Rhino 1.6R7 DOM based E4X impl 
			return new JavaScriptE4XXmlBeansHelper(engine);
		}
	}

}
