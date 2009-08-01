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

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A simple implementation of {@link ScriptContext}.
 * 
 * This class is not synchronized.
 * 
 * See Javadoc of <a href="http://java.sun.com/javase/6/docs/api/javax/script/package-summary.html">Java Scripting API</a>
 */
public class SimpleScriptContext implements ScriptContext {

    /**
     * This is the scope bindings for GLOBAL_SCOPE. 
     * By default, a <tt>null</tt> value (which means no global scope) is used. 
     * */
    protected Bindings globalScope = null;

    /**
     * This is the scope bindings for ENGINE_SCOPE .
     * By default, a SimpleBindings is used.
     */
    protected Bindings engineScope = new SimpleBindings();

    /** The reader to be used for input from scripts. */
    protected Reader reader;

    /** The writer to be used for displaying output from scripts */
    protected Writer writer;

    /** The writer to be used for displaying error output from scripts */
    protected Writer errorWriter;

    // N.B. These fields are mandated as protected by the JSR-223 spec.
    
    /** Immutable list of scopes returned by {@link #getScopes()}*/
    private static final List SCOPES = 
        Collections.unmodifiableList(
            Arrays.asList(new Integer[] { new Integer(ENGINE_SCOPE), new Integer(GLOBAL_SCOPE) })
         );

    /**
     * Create a new instance, 
     * setting the Reader and Writers from the corresponding System streams.
     */
    public SimpleScriptContext() {
        reader = new InputStreamReader(System.in);
        writer = new PrintWriter(System.out, true);
        errorWriter = new PrintWriter(System.err, true);
    }

    /**
     * Check if name is <tt>null</tt> or empty string
     * @param name to be checked
     * @throws NullPointerException if the name is <tt>null</tt>
     * @throws IllegalArgumentException if the name is the empty string
     */
    private void checkName(String name){
        if (name == null){
            throw new NullPointerException("name must not be null");
        }
        if (name.length() == 0){
            throw new IllegalArgumentException("name must not be an empty string");
        }
    }


    /** {@inheritDoc} */
    public Object getAttribute(String name) {

        checkName(name);

        final Object engineObject = engineScope.get(name);
        if (engineObject != null) {
            return engineObject;
        } else if (globalScope != null) {
            return globalScope.get(name);
        } else {
            return null;            
        }
    }

    /** {@inheritDoc} */
    public Object getAttribute(String name, int scope) {

        checkName(name);

        switch (scope) {
            case ENGINE_SCOPE:
                return engineScope.get(name);
            case GLOBAL_SCOPE:
                return globalScope != null ? globalScope.get(name) : null;
            default:
                throw new IllegalArgumentException("invalid scope");
        }
    }

    /** {@inheritDoc} */
    public int getAttributesScope(String name) {

        checkName(name);

        if (engineScope.containsKey(name)) {
            return ENGINE_SCOPE;
        } else if(globalScope != null && globalScope.containsKey(name)) {
            return GLOBAL_SCOPE;
        }

        return -1;
    }

    /** {@inheritDoc} */
    public Bindings getBindings(int scope) {

        switch (scope) {
            case ENGINE_SCOPE:
                return engineScope;
            case GLOBAL_SCOPE:
                return globalScope;
            default:
                throw new IllegalArgumentException("invalid scope");
        }
    }

    /** {@inheritDoc} */
    public Object removeAttribute(String name, int scope) { 

        checkName(name);

        switch (scope) {
            case ENGINE_SCOPE:
                return engineScope.remove(name);
            case GLOBAL_SCOPE:
                return globalScope != null ? globalScope.remove(name) : null;
            default:
                throw new IllegalArgumentException("invalid scope");
        }        
    }

    /** {@inheritDoc} */
    public void setAttribute(String name, Object value, int scope) {

        checkName(name);

        switch (scope) {
            case ENGINE_SCOPE:
                engineScope.put(name, value);
                break;
            case GLOBAL_SCOPE:
                if (globalScope != null) {
                    globalScope.put(name, value);
                }
                break;
            default:
                throw new IllegalArgumentException("invalid scope");
        }
    }

    /** {@inheritDoc} */
    public void setBindings(Bindings bindings, int scope) {

        switch (scope) {
            case ENGINE_SCOPE:
                if (bindings == null) {
                    throw new NullPointerException("binding is null for ENGINE_SCOPE scope");
                }
                engineScope = bindings;
                break;
            case GLOBAL_SCOPE:
                globalScope = bindings;
                break;
            default:
                throw new IllegalArgumentException("invalid scope");
        }
    }

    /** {@inheritDoc} */
    public List getScopes() {
        return SCOPES;
    }

    /** {@inheritDoc} */
    public Reader getReader() {
        return reader;
    }

    /** {@inheritDoc} */
    public void setReader(Reader reader) {
        this.reader = reader;
    }

    /** {@inheritDoc} */
    public Writer getWriter() {
        return writer;
    }

    /** {@inheritDoc} */
    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    /** {@inheritDoc} */
    public Writer getErrorWriter() {
        return errorWriter;
    }

    /** {@inheritDoc} */
    public void setErrorWriter(Writer writer) {
        this.errorWriter = writer;
    }

}
