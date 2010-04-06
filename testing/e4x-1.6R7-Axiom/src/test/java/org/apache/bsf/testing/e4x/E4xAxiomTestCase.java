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

import javax.script.Bindings;
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
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.bsf.xml.XMLHelper;

/**
 * Tests a basic JavaScrip/E4X invocation
 */
public class E4xAxiomTestCase extends TestCase {

	private XMLHelper xmlHelper;
	private ScriptEngine engine;
	
	public void testInvokeFunctionInXML() throws ScriptException, XMLStreamException, FactoryConfigurationError, NoSuchMethodException {
        if (!engineSupportsE4X(engine, "testInvokeFunctionInXML()")){
            return;
        }

		engine.eval("function isXML(xml) { return typeof xml == 'xml'; }" );
		assertTrue(engine instanceof Invocable);
		Invocable invocableScript = (Invocable) engine;

		Object xmlIn = xmlHelper.toScriptXML(createOMElement("<a><b>petra</b></a>"));

		Object o = invocableScript.invokeFunction("isXML", new Object[]{xmlIn});
		assertTrue(o instanceof Boolean);
		assertTrue(((Boolean)o).booleanValue());
	}
	
	public void testInvokeFunctionOutXML() throws ScriptException, XMLStreamException, FactoryConfigurationError, NoSuchMethodException {
        if (!engineSupportsE4X(engine, "testInvokeFunctionOutXML()")){
            return;
        }
		engine.eval("function hello(xml) { return <foo>{xml.b}</foo>; }" );
		assertTrue(engine instanceof Invocable);
		Invocable invocableScript = (Invocable) engine;

		Object xmlIn = xmlHelper.toScriptXML(createOMElement("<a><b>petra</b></a>"));

		Object xmlOut = invocableScript.invokeFunction("hello", new Object[]{xmlIn});
		OMElement omOut = xmlHelper.toOMElement(xmlOut);
		assertEquals("<foo><b>petra</b></foo>", omOut.toString());
	}

	public void testE4X() throws ScriptException, XMLStreamException, FactoryConfigurationError {
        Object o = xmlHelper.toScriptXML(createOMElement("<a><b>petra</b></a>"));
        OMElement om = xmlHelper.toOMElement(o);
        assertEquals("<a><b>petra</b></a>", om.toString());
        
        Bindings bindings = engine.createBindings();
        bindings.put("o", o);
        Object x = engine.eval("typeof o", bindings);
        if (!engineSupportsE4X(engine, "the rest of testE4X()")){
            return;
        }
        assertEquals("xml", x);
	}
	
	protected OMElement createOMElement(String s) throws XMLStreamException, FactoryConfigurationError {
		XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(s));
		StAXOMBuilder builder = new StAXOMBuilder(parser);
		OMElement om = builder.getDocumentElement();
		return om;
	}

    protected void setUp() {
        engine = new ScriptEngineManager().getEngineByExtension("js");
        assertNotNull("Could not load js engine",engine);
        xmlHelper = XMLHelper.getArgHelper(engine);
    }

    private static boolean engineSupportsE4X(ScriptEngine engine, String message){
        final String name = engine.getClass().getName();
        // This engine does not support E4X
        if (name.equals("com.sun.script.javascript.RhinoScriptEngine")){
            System.out.println("*** "+name + " does not support E4X, skipping "+message);
            return false;
        }
        return true;
    }
}
