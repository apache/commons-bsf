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
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.xmlbeans.XmlObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;
import org.mozilla.javascript.xml.XMLObject;

/**
 * XMLHelper for JavaScript E4X using XmlBeans
 */
public class JavaScriptE4XXmlBeansHelper extends DefaultXMLHelper {

	private final Scriptable scope;

    JavaScriptE4XXmlBeansHelper(ScriptEngine engine) {
	    Context cx = Context.enter();
	    try {
	        this.scope = cx.initStandardObjects();
	    } finally {
	        Context.exit();
	    }
	}

	public OMElement toOMElement(Object scriptXML) throws ScriptException {
        if (scriptXML == null) {
        	return null;
        }

        if (!(scriptXML instanceof XMLObject)) {
            return null;
        }

        // TODO: E4X Bug? Shouldn't need this copy, but without it the outer element gets lost???
        Scriptable jsXML =
            (Scriptable) ScriptableObject.callMethod((Scriptable) scriptXML, "copy", new Object[0]);
        Wrapper wrapper =
            (Wrapper) ScriptableObject.callMethod(jsXML, "getXmlObject", new Object[0]);

        XmlObject xmlObject = (XmlObject) wrapper.unwrap();

        try {
            StAXOMBuilder builder = new StAXOMBuilder(xmlObject.newInputStream());
            return builder.getDocumentElement();

        } catch (XMLStreamException e) {
        	throw new ScriptException(e);
        }
	}

	public Object toScriptXML(OMElement om) throws ScriptException {
        if (om == null) {
        	return null;
        }
        Context cx = Context.enter();
        try {

    		XmlObject xml = null;
            try {
                xml = XmlObject.Factory.parse(om.getXMLStreamReader());
            } catch (Exception e) {
            	throw new ScriptException(e);
            }
            Object wrappedXML = cx.getWrapFactory().wrap(cx, scope, xml, XmlObject.class);
            return cx.newObject(scope, "XML", new Object[]{wrappedXML});

        } finally {
            Context.exit();
        }
	}

}
