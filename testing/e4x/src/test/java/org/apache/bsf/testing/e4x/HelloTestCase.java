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
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextHelper;

/**
 * Tests a basic JavaScript/E4X invocation
 */
public class HelloTestCase extends TestCase {

//    private ScriptEngine engine;
//    private XMLHelper xmlHelper;

//    public void testInvokeFunction() throws ScriptException, XMLStreamException, FactoryConfigurationError {
//        engine.eval("function isXML(xml) { return typeof xml.b == xml; }" );
//        engine.eval("function hello(xml) { return <foo>{xml.b}</foo>; }" );
//        assertTrue(engine instanceof Invocable);
//        Invocable invocableScript = (Invocable) engine;
//
//        Object xmlIn = xmlHelper.toScriptXML(createOm("<a><b>petra</b></a>"));
//
//        Object o = invocableScript.invokeFunction("isXML", new Object[]{xmlIn});
//        assertTrue(o instanceof Boolean);
//        assertTrue(((Boolean)o).booleanValue());
//
//        Object xmlOut = invocableScript.invokeFunction("hello", new Object[]{xmlIn});
//
//        OMElement omOut = xmlHelper.toOMElement(xmlOut);
//        assertEquals("<foo><b>petra</b></foo>", omOut.toString());
//    }

    public void testE4X() throws ScriptException, XMLStreamException, FactoryConfigurationError {
        // The default Rhino implementation provided by Java 1.6 does not support E4X,
        // so use the unique name supported by the 1.6R7 version factory.
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("rhino-nonjdk");
        XMLHelper convertor = XMLHelper.getArgHelper(engine);
        Object o = convertor.toScriptXML(createOMElement("<a><b>petra</b></a>"));
        OMElement om = convertor.toOMElement(o);
        assertEquals("<a><b>petra</b></a>", om.toString());

        Bindings bindings = engine.createBindings();
        bindings.put("o", o);
        Object x = engine.eval("typeof o", bindings);
        assertEquals("xml", x);
    }

    protected OMElement createOMElement(String s) throws XMLStreamException, FactoryConfigurationError {
        XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(s));
        StAXOMBuilder builder = new StAXOMBuilder(parser);
        OMElement om = builder.getDocumentElement();
        return om;
    }

    protected void setUp() {
//        ScriptEngineManager manager = new ScriptEngineManager();
//        engine = manager.getEngineByExtension("js");
//        xmlHelper = XMLHelper.getArgHelper(engine);
        Context cx = Context.enter();
        try {
            ContextHelper.setTopCallScope(cx, cx.initStandardObjects());
        } finally {
            Context.exit();
        }
    }
}
