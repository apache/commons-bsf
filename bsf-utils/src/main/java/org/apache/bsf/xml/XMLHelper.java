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
import javax.script.ScriptException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.w3c.dom.Node;

/**
 * Utility for converting between Java representations of XML such DOM nodes,
 * StAX streams, or Axiom OMElements, into script language representations such
 * as JavaScript E4X, Ruby ReXML, or Python ElementTree.
 */
public abstract class XMLHelper {

	/**
	 * Register axiom-e4x if its available
	 * @deprecated temp approach for beta2 release
	 */
	public static void init() {
		try {
			Class.forName("org.wso2.javascript.xmlimpl.XMLLibImpl", true, JavaScriptE4XHelper.class.getClassLoader());
			JavaScriptE4XAxiomHelper.init();
		} catch (ClassNotFoundException e) {
		}
	}

	public static XMLHelper getArgHelper(ScriptEngine engine) {
		// TODO: better discovery mechanisim than hardcoded class names
		if (engine == null) {
			return null;
		}
		String language = engine.getFactory().getLanguageName();
		if ("ECMAScript".endsWith(language)) {
			return JavaScriptE4XHelper.getXMLHelper(engine);
		} else if ("ruby".endsWith(language)) {
			return new JRubyReXMLHelper(engine);
		} else {
			return new DefaultXMLHelper();
		}
	}
	
	public abstract Object toScriptXML(OMElement om) throws ScriptException;
	public abstract OMElement toOMElement(Object scriptXML) throws ScriptException;

	public abstract Object toScriptXML(XMLStreamReader reader) throws ScriptException;
	public abstract XMLStreamReader toXMLStreamReader(Object scriptXML) throws ScriptException;

	public abstract Object toScriptXML(Node node) throws ScriptException;
	public abstract Node toDOMNode(Object scriptXML) throws ScriptException;
}
