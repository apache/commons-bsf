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

import java.io.IOException;
import java.io.Reader;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

/**
 * Minimal Script engine used for JUnit tests.
 */
public class TestScriptEngine extends AbstractScriptEngine implements Compilable, Invocable {

    public TestScriptEngine() {
        super();
    }

    public TestScriptEngine(Bindings n) {
        super(n);
    }

    // Allow test access to protected method
    public ScriptContext getScriptContext(Bindings bindings){
        return super.getScriptContext(bindings);
    }

    public Bindings createBindings() {
        return new SimpleBindings();
    }

    public Object eval(Reader reader, ScriptContext context)
            throws ScriptException {
        return eval(readerToString(reader), context);
    }

    /**
     * @param reader
     * @return
     * @throws ScriptException
     */
    private String readerToString(Reader reader) throws ScriptException {
        StringBuffer sb = new StringBuffer();
        char cbuf[] = new char[1024];
        try {
            while(reader.read(cbuf) != -1){
                sb.append(cbuf);
            }
        } catch (IOException e) {
            throw new ScriptException(e);
        }
        return sb.toString();
    }

    // Very simple evaluator - just return the context value for the script
    public Object eval(String script, ScriptContext context)
            throws ScriptException {
        return context.getAttribute(script);
    }

    public ScriptEngineFactory getFactory() {
        return new TestScriptEngineFactory();
    }

    // Compilable methods
    
    public CompiledScript compile(String script) throws ScriptException {
        return new TestCompiledScript(this, script);
    }

    public CompiledScript compile(Reader reader) throws ScriptException {
        String script = readerToString(reader);
        return new TestCompiledScript(this, script);
    }

    // Invokable methods
    
    public Object getInterface(Class clasz) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object getInterface(Object thiz, Class clasz) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object invokeFunction(String name, Object[] args)
            throws ScriptException, NoSuchMethodException {
        // TODO Auto-generated method stub
        return null;
    }

    public Object invokeMethod(Object thiz, String name, Object[] args)
            throws ScriptException, NoSuchMethodException {
        // TODO Auto-generated method stub
        return null;
    }

}
