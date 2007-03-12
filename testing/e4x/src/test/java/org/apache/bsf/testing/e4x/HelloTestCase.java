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
package org.apache.bsf.testing.e4x;

import java.io.StringReader;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.bsf.e4x.E4XHelper;
import org.mozilla.javascript.xml.XMLObject;

/**
 * Tests a basic JavaScrip/E4X invocation
 */
public class HelloTestCase extends TestCase {
	
	private E4XHelper e4xHelper;

	public void testInvokeFunction() throws ScriptException, XMLStreamException, FactoryConfigurationError {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByExtension("js");
		engine.eval("function hello(xml) { return <foo>{xml.b}</foo>; }" );
		assertTrue(engine instanceof Invocable);
		Invocable invocableScript = (Invocable) engine;

		XMLObject xmlIn = e4xHelper.toE4X(createOm("<a><b>petra</b></a>"));

		Object xmlOut = invocableScript.invokeFunction("hello", new Object[]{xmlIn});

		OMNode omOut = e4xHelper.fromE4X((XMLObject) xmlOut);
		assertEquals("<foo><b>petra</b></foo>", omOut.toString());
	}
	
	protected OMElement createOm(String s) throws XMLStreamException, FactoryConfigurationError {
		XMLStreamReader parser = 
			XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(s));
		StAXOMBuilder builder = new StAXOMBuilder(parser);
		OMElement om = builder.getDocumentElement();
		return om;
	}

    protected void setUp() {
    	e4xHelper = new E4XHelper();
    }
}
