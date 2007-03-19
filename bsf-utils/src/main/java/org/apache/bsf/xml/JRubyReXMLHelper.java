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

import java.io.ByteArrayInputStream;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;

/**
 * XMLHelper for Ruby ReXML
 * 
 * TODO: Find a more efficent way to do this with JRuby
 */
public class JRubyReXMLHelper extends DefaultXMLHelper {

	private ScriptEngine scriptEngine;

	JRubyReXMLHelper(ScriptEngine scriptEngine) {
		this.scriptEngine = scriptEngine;
	}

	public OMElement toOMElement(Object scriptXML) throws ScriptException {
		if (scriptXML == null) {
			return null;
		}
		try {

			byte[] xmlBytes = scriptXML.toString().getBytes();
			StAXOMBuilder builder = new StAXOMBuilder(new ByteArrayInputStream(xmlBytes));
			return builder.getDocumentElement();

		} catch (Exception e) {
			throw new ScriptException(e);
		}
	}

	public Object toScriptXML(OMElement om) throws ScriptException {
		if (om == null) {
			return null;
		}
		StringBuffer srcFragment = new StringBuffer("Document.new(<<EOF\n");
		srcFragment.append(om.toString());
		srcFragment.append("\nEOF\n");
		srcFragment.append(")");
		return scriptEngine.eval(srcFragment.toString());
	}

}
