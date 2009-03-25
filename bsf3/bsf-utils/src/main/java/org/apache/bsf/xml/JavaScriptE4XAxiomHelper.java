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

import org.apache.axiom.om.OMElement;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ContextHelper;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.xml.XMLLib;
import org.mozilla.javascript.xml.XMLObject;

/**
 * XMLHelper for JavaScript E4X using Axiom
 */
public class JavaScriptE4XAxiomHelper extends DefaultXMLHelper {

    private final Scriptable scope;

    JavaScriptE4XAxiomHelper(ScriptEngine engine) {

        // tell Rhino to use Axiom E4X impl
        if (!ContextFactory.hasExplicitGlobal()) {
             ContextFactory.initGlobal(new AxiomE4XContextFactory());
        }

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

        Object o = ScriptableObject.callMethod( (Scriptable) scriptXML, "getXmlObject", new Object[0]);
        return (OMElement) o;
//        return (OMElement) ScriptableObject.callMethod( (Scriptable) scriptXML, "getXmlObject", new Object[0]);
    }

    public Object toScriptXML(OMElement om) throws ScriptException {
        if (om == null) {
            return null;
        }
        Context cx = Context.enter();
        try {

            // TODO: why is this needed? A bug in axiom-e4x?
              ContextHelper.setTopCallScope(cx, scope);

           return cx.newObject(scope, "XML", new Object[]{om});

        } finally {
            Context.exit();
        }
    }

    public static void init() {
        ContextFactory.initGlobal(new AxiomE4XContextFactory());
    }
}

class AxiomE4XContextFactory extends ContextFactory {

    protected XMLLib.Factory getE4xImplementationFactory() {
        return org.mozilla.javascript.xml.XMLLib.Factory.create(
                "org.wso2.javascript.xmlimpl.XMLLibImpl"
        );
    }
}


