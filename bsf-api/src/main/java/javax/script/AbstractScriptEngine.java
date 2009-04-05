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
package javax.script;

import java.io.Reader;

/**
 * Provides a standard implementation for various versions of the eval() methods.
 * Stores the ScriptContext, and provides access to the Bindings stored in the context.
 * 
 * The class is not synchronized.
 * The default context - {@link SimpleScriptContext} - is not synchronized either. 
 * Sub-classes must provide synchronization if necessary.
 * 
 * See Javadoc of <a href="http://java.sun.com/javase/6/docs/api/javax/script/package-summary.html">Java Scripting API</a>
 */
public abstract class AbstractScriptEngine implements ScriptEngine {

    /** The default ScriptContext of this AbstractScriptEngine */
    protected ScriptContext context;
    // This field is defined as protected in JSR-223

    /**
     * Constructs a ScriptEngine using an uninitialized 
     * SimpleScriptContext.
     */
    public AbstractScriptEngine() {
        this.context = new SimpleScriptContext();
    }

    /**
     * Constructs a ScriptEngine using the specified Bindings as its 
     * ENGINE_SCOPE.
     * 
     * @param bindings the Bindings to be used as the ENGINE_SCOPE
     * 
     * @throws NullPointerException if the parameter is <tt>null</tt>
     */
    public AbstractScriptEngine(Bindings bindings){
        this();
        if (bindings == null) {
            throw new NullPointerException("bindings is null");
        }
        context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
    }

    /** {@inheritDoc} */
    public Object eval(Reader reader) throws ScriptException{
        return eval(reader, context);
    }

    /** {@inheritDoc} */
    public Object eval(Reader reader, Bindings bindings) 
            throws ScriptException{
        return eval(reader,getScriptContext(bindings));
    }

    /** {@inheritDoc} */
    public Object eval(String script) throws ScriptException{
        return eval(script, context);
    }

    /** {@inheritDoc} */
    public Object eval(String script, Bindings bindings) throws ScriptException{
        return eval(script,getScriptContext(bindings));
    }

    /** {@inheritDoc} */
    public Object get(String key) {
        return getBindings(ScriptContext.ENGINE_SCOPE).get(key);
    }

    /** {@inheritDoc} */
    public Bindings getBindings(int scope) {
        if (scope == ScriptContext.GLOBAL_SCOPE || scope == ScriptContext.ENGINE_SCOPE) {
            return context.getBindings(scope);
        } else {
            throw new IllegalArgumentException("invalid scope");
        }
    }


    /**
     * Returns a {@link SimpleScriptContext} which:
     * <ul>
     * <li>uses the specified {@link Bindings} as the {@link ENGINE_SCOPE}</li>
     * <li>has the same GLOBAL_SCOPE Bindings as the current context</li>
     * <li>uses the same Reader, Writer and Error Writer as the current context</li>
     * </ul>
     * 
     * @param bindings the bindings to be associated with ENGINE_SCOPE 
     * @return an instance of SimpleScriptContext
     * 
     * @throws NullPointerException if bindings is <tt>null</tt>
     */
    protected ScriptContext getScriptContext(Bindings bindings){
        if (bindings == null) {
            throw new NullPointerException("ENGINE_SCOPE bindings cannot be null");
        }

        ScriptContext scriptContext = new SimpleScriptContext();

        scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        scriptContext.setBindings(this.getBindings(ScriptContext.GLOBAL_SCOPE), ScriptContext.GLOBAL_SCOPE);

        scriptContext.setReader(this.context.getReader());
        scriptContext.setWriter(this.context.getWriter());
        scriptContext.setErrorWriter(this.context.getErrorWriter());

        return scriptContext;
    }

    /** {@inheritDoc} */
    public void put(String key, Object value) {

        if (key == null) { 
            throw new NullPointerException("name is null");
        }

        if (key.length() == 0) { 
            throw new IllegalArgumentException("name is empty");
        }

        getBindings(ScriptContext.ENGINE_SCOPE).put(key,value);
    }

    /** {@inheritDoc} */
    public void setBindings(Bindings bindings, int scope) {

        if (scope == ScriptContext.GLOBAL_SCOPE || scope == ScriptContext.ENGINE_SCOPE) {
             context.setBindings(bindings, scope);
        } else {
            throw new IllegalArgumentException("invalid scope");
        }
    }

    /** {@inheritDoc} */
    public ScriptContext getContext() {
        return this.context;
    }

    /** {@inheritDoc} */
    public void setContext(ScriptContext context) {
        if (context == null) {
            throw new NullPointerException("context is null");
        }
        this.context = context;
    }
}
