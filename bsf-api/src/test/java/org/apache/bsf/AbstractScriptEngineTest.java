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

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;

import junit.framework.TestCase;

import org.apache.bsf.utils.TestScriptEngine;

public class AbstractScriptEngineTest extends TestCase {

    private final TestScriptEngine engine  = new TestScriptEngine();

    public void testCtor1(){
        ScriptContext b = engine.getContext();
        assertNotNull(b);
    }

    public void testCtor2(){
        try {
            new TestScriptEngine(null);
            fail("Should have thrown NPE");
        } catch (NullPointerException expected) {
        }
        Bindings b = new SimpleBindings();
        new TestScriptEngine(b); // should be OK
    }

    public void testSetBindings(){
        try {
            engine.setBindings(null, -123);
            fail("Should have generated illegal arg exception");
        } catch (IllegalArgumentException e) {
        }
        try {
            engine.setBindings(null, ScriptContext.ENGINE_SCOPE);
            fail("Should have generated NPE");
        } catch (NullPointerException e) {
        }
        engine.setBindings(null, ScriptContext.GLOBAL_SCOPE); // should be OK
        Bindings bindings = new SimpleBindings();
        engine.setBindings(bindings , ScriptContext.ENGINE_SCOPE);
        engine.setBindings(bindings , ScriptContext.GLOBAL_SCOPE);
    }

    public void testGetBindings(){
        try {
            engine.getBindings(-123);
            fail("Should have generated illegal arg exception");
        } catch (IllegalArgumentException e) {
        }
        assertNotNull(engine.getBindings(ScriptContext.ENGINE_SCOPE));
        assertNull(engine.getBindings(ScriptContext.GLOBAL_SCOPE)); // null is the default
        engine.setBindings(null, ScriptContext.GLOBAL_SCOPE);// null is allowed here
        assertNull(engine.getBindings(ScriptContext.GLOBAL_SCOPE));
        engine.setBindings(new SimpleBindings(), ScriptContext.GLOBAL_SCOPE);
        assertNotNull(engine.getBindings(ScriptContext.GLOBAL_SCOPE));
    }

    public void testContext(){
        final ScriptContext initial = engine.getContext();
        assertNotNull(initial);
        final SimpleScriptContext newContext = new SimpleScriptContext();
        assertNotSame(initial, newContext);
        engine.setContext(newContext);
        ScriptContext updated = engine.getContext();
        assertNotNull(updated);
        assertSame(updated, newContext);
    }

    public void testgetScriptContext(){
        try {
            engine.getScriptContext(null);
            fail("Should have caused NPE");
        } catch (NullPointerException e) {
        }
        final SimpleBindings bindings = new SimpleBindings();
        ScriptContext sc = engine.getScriptContext(bindings);
        assertEquals(bindings, sc.getBindings(ScriptContext.ENGINE_SCOPE));
        assertNull(sc.getBindings(ScriptContext.GLOBAL_SCOPE));
    }

    public void testPutGet(){
        try {
            engine.put(null, null);
            fail("Should have caused NPE");
        } catch (NullPointerException e) {
        }
        try {
            engine.put("", null);
            fail("Should have caused IllegalArg");
        } catch (IllegalArgumentException e) {
        }
        engine.put("null", null);
        engine.put("notnull", "");
        try {
            engine.get(null);
            fail("Should have caused NPE");
        } catch (NullPointerException e) {
        }
        try {
            engine.get("");
            fail("Should have caused IllegalArg");
        } catch (IllegalArgumentException e) {
        }
        assertNull(engine.get("null"));
        assertNotNull(engine.get("notnull"));
    }
}
