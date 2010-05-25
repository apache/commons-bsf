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
package org.apache.bsf.testing.javascript;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import junit.framework.TestCase;


public class JRuby112Testcase extends TestCase {

//    public void testEval() throws ScriptException {
//        ScriptEngineManager manager = new ScriptEngineManager();
//        ScriptEngine engine = manager.getEngineByExtension("js");
//        assertTrue(((Boolean)engine.eval("true;")).booleanValue());
//        assertFalse(((Boolean)engine.eval("false;")).booleanValue());
//    }

    public void testInvokeFunction() throws ScriptException, NoSuchMethodException {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByExtension("rb");
        engine.eval("def hello(s)\n   return \"Hello \" + s\nend" );
        assertTrue(engine instanceof Invocable);
        Invocable invocableScript = (Invocable) engine;
        assertEquals("Hello petra", invocableScript.invokeFunction("hello", new Object[]{"petra"}));
    }

//    public void testInvokeMethod() throws ScriptException {
//        ScriptEngineManager manager = new ScriptEngineManager();
//        ScriptEngine engine = manager.getEngineByExtension("js");
//        engine.eval("function hello(s) { return 'Hello ' + s; }" );
//        assertTrue(engine instanceof Invocable);
//        Invocable invocableScript = (Invocable) engine;
//
//        Object thiz = engine.eval("this;");
//        assertEquals("Hello petra", invocableScript.invokeMethod(thiz, "hello", new Object[]{"petra"}));
//    }

}
