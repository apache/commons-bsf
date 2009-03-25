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
import java.io.Writer;
import java.util.List;

/**
 * See Javadoc of <a href="http://java.sun.com/javase/6/docs/api/javax/script/package-summary.html">Java Scripting API</a>
 */
public interface ScriptContext {

    /** defines an integer for the level of scope, ENGINE_SCOPE */
    public static final int ENGINE_SCOPE = 100;

    /** defines an integer for the level of scope, GLOBAL_SCOPE */
    public static final int GLOBAL_SCOPE = 200;

    /**
     * Retrieves the value of the getAttribute(String, int) for the 
     * lowest scope in which it returns a non-null value. Returns 
     * null if there is no such value exists in any scope. 
     * 
     * 
     * @param name the name of the attribute
     * @return the associated value with the specified name 
     * @throws IllegalArgumentException if the name is empty 
     * @throws NullPointerException if the name is null
     */
    public Object getAttribute(String name);

    /**
     * Retrieves the value of an attribute in the specified scope. 
     * Returns null if the no such value exists in the specified 
     * scope.
     * 
     * @param name  the name of the attribute
     * @param scope the value of the scope
     * @return the associated value for the specified name
     * @throws IllegalArgumentException if the name is empty or the 
     *         scope is invalid
     * @throws NullPointerException if the name is null 
     */
    public Object getAttribute(String name, int scope);

    /**
     * Retrieves the lowest value of the scope for which the 
     * attribute is defined.
     * 
     * @param  name the name of attribute
     * @return the value corresponding to lowest value of the scope 
     *         or -1 if no associated value exist in any scope  
     * @throws NullPointerException if name is null. 
     * @throws IllegalArgumentException if name is empty.
     */
    public int getAttributesScope(String name);

    /**
     * Retrieves the Bindings instance associated with the gieve
     * scope. Returns null if no namespace is assoicited with 
     * specified level of scope. 
     * 
     * @param scope the level of the scope
     * @return the Bindings associated with the specified scope
     * @throws IllegalArgumentException
     *         If no Bindings is defined for the specified scope value in ScriptContext of this type.
     */
    public Bindings getBindings(int scope);

    /**
     * Retrieves an instance of java.io.Writer which can be used by 
     * scripts to display their output.
     * 
     * @return an instance of java.io.Writer
     */
    public Writer getWriter();

    /**
     * Returns the Writer to be used to display error output. 
     * @return the error writer
     */
    public Writer getErrorWriter();

    /**
     * Sets the Writer for scripts to use when displaying output.
     * 
     * @param writer the new writer.
     */
    public void setWriter(Writer writer);

    /**
     * Sets the Writer for scripts to use when displaying error output.
     * 
     * @param writer the new writer.
     */
    public void setErrorWriter(Writer writer);

    /**
     * Returns a Reader to be used by the script to read input.
     * 
     * @return the reader
     */
    public Reader getReader();

    /**
     * Sets the Reader to be used by the script to read input.
     * 
     * @param reader the new reader
     */
    public void setReader(Reader reader);

    /**
     * Removes the given attribute form the specified scope. Returns 
     * the removed object or null if no value is associated with the 
     * specified key in specified level of scope. 
     * 
     * @param  name  the name of the attribute
     * @param  scope the level of scope which inherit the attribute
     * @return previous value associated with specified name
     * 
     * @throws NullPointerException if the name is null 
     * @throws IllegalArgumentException if the name is empty or if the 
     *         scope is invalid
     */
    public Object removeAttribute(String name, int scope);

    /**
     * Associates a specified value with the specifed name in the
     * specified scope. 
     * 
     * @param key   the name of the attribute
     * @param value the value of the attribute
     * @param scope the level of the scope
     * @throws IllegalArgumentException if the name is null or the
     *         scope is invalid
     * @throws NullPointerException if the name is null.
     */
    public void setAttribute(String key,Object value,int scope);

    /**
     * Associates the specified Bindings with the specified scope. 
     *  
     * @param bindings the Bindings to be associated with the
     *        specified level of scope
     * @param scope     the scope 
     * @throws IllegalArgumentException if the scope is invalid
     * @throws NullPointerException if the bindings is null and the 
     *          scope is ScriptEngine.ENGINE_SCOPE
     */
    public void setBindings(Bindings bindings,int scope);

    /**
     * Returns immutable List of all the valid values for scope in the ScriptContext.
     *  
     * @return the list
     */
    public List getScopes();

}
