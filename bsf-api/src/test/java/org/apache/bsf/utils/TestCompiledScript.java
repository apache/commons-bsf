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

package org.apache.bsf.utils;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 * Minimal CompiledScript implementation.
 */
public class TestCompiledScript extends CompiledScript {

    private final ScriptEngine engine;
    private final String compiled;

    public TestCompiledScript(TestScriptEngine testScriptEngine, String script) {
        engine = testScriptEngine;
        compiled = script;
    }

    public Object eval(ScriptContext context) throws ScriptException {
        return context.getAttribute(compiled);
    }

    public ScriptEngine getEngine() {
        return engine;
    }

}
