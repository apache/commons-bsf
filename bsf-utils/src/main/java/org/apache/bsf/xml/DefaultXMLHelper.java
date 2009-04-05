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

import javax.script.ScriptException;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.w3c.dom.Node;

/**
 * Default XMLHelper for when no specific XMLHelper is implemented
 * for a script engine. Just converts XML to text Strings.
 */
public class DefaultXMLHelper extends XMLHelper {

    public OMElement toOMElement(Object scriptXML) throws ScriptException {
        try {

            StAXOMBuilder builder = new StAXOMBuilder(
                new ByteArrayInputStream(scriptXML.toString().getBytes()));
            return builder.getDocumentElement();

        } catch (XMLStreamException e) {
            throw new ScriptException(e);
        }
    }

    public Object toScriptXML(OMElement om) throws ScriptException {
        return om.toString();
    }

    public Node toDOMNode(Object scriptXML) throws ScriptException {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public Object toScriptXML(XMLStreamReader reader) throws ScriptException {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public Object toScriptXML(Node node) throws ScriptException {
        throw new UnsupportedOperationException("not implemented yet");
    }

    public XMLStreamReader toXMLStreamReader(Object scriptXML) throws ScriptException {
        throw new UnsupportedOperationException("not implemented yet");
    }

}
