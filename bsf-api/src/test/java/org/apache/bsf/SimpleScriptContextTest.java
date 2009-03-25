/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.apache.bsf;

import java.util.List;

import javax.script.*;

import junit.framework.TestCase;

public class SimpleScriptContextTest extends TestCase {

    ScriptContext context;
    
    public void setUp(){
        context = new SimpleScriptContext();
    }
    
    public void testCtor(){
        assertNotNull(context);
    }
    
    public void testInvalidGetAttribute(){
        try {
            context.getAttribute(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            context.getAttribute("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            context.getAttribute(null, ScriptContext.ENGINE_SCOPE);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            context.getAttribute("", 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            context.getAttribute("OK", 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            context.getAttribute(null, ScriptContext.ENGINE_SCOPE);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            context.getAttribute("", ScriptContext.ENGINE_SCOPE);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }
    
    public void testInvalidRemoveAttribute(){
        try {
            context.removeAttribute(null, ScriptContext.ENGINE_SCOPE);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            context.removeAttribute("", 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            context.removeAttribute("OK", 0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            context.removeAttribute(null, ScriptContext.ENGINE_SCOPE);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            context.removeAttribute("", ScriptContext.ENGINE_SCOPE);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }        
    }
    public void testBindings(){
        try {
            context.getBindings(0);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        assertNotNull(context.getBindings(ScriptContext.ENGINE_SCOPE));
        assertNull(context.getBindings(ScriptContext.GLOBAL_SCOPE)); // no global default
        context.setBindings(null, ScriptContext.GLOBAL_SCOPE); // OK
        try {
            context.setBindings(null, ScriptContext.ENGINE_SCOPE); // Not OK
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    
    public void testScopes(){
        List l = (List) context.getScopes();
        assertNotNull(l);
        assertTrue(l.size() >=2);
        try {
            l.remove(0);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
        }
    }
}
