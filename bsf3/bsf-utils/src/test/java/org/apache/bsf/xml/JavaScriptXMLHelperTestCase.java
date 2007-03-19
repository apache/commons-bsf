package org.apache.bsf.xml;
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
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import junit.framework.TestCase;

import org.apache.axiom.om.OMElement;
import org.apache.bsf.xml.XMLHelper;
import org.mozilla.javascript.xml.XMLObject;

/**
 * Tests for converting btw OMElement and E4X
 */
public class JavaScriptXMLHelperTestCase extends TestCase {

	private ScriptEngine engine;

	private XMLHelper xmlHelper;

	public void testToOmElement() throws ScriptException {
		Object scriptXML = engine.eval("<a><b>petra</b></a>");
		assertTrue(scriptXML instanceof XMLObject);

		OMElement om = xmlHelper.toOMElement(scriptXML);
		assertNotNull(om);
		assertEquals("<a><b>petra</b></a>", om.toString());
	}

	public void testToScriptXML() throws ScriptException {
		OMElement om = xmlHelper.toOMElement(engine.eval("<a><b>petra</b></a>"));

		Bindings bindings = engine.createBindings();
		bindings.put("xml", xmlHelper.toScriptXML(om));

		assertEquals("xml", engine.eval("typeof xml", bindings));
		assertEquals("petra", engine.eval("xml.b.toString()", bindings));
	}

	public void setUp() {
		ScriptEngineManager manager = new ScriptEngineManager();
		engine = manager.getEngineByExtension("js");
		xmlHelper = XMLHelper.getArgHelper(engine);
	}

}
