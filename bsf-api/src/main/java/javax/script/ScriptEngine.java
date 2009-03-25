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
 * See Javadoc of <a href="http://java.sun.com/javase/6/docs/api/javax/script/package-summary.html">Java Scripting API</a>
 */
public interface ScriptEngine {

    /** 
     * Reserved key associated with an object array which is used to 
     * pass set of positional parameters to the ScriptEngines.
     */
    public static final String ARGV="javax.script.argv";

    /** 
     * Reserved key associated with name of the file which contains 
     * the source of the script.
     */
    public static final String FILENAME = "javax.script.filename";

    /** 
     * Reserved key associated with the name of the Java 
     * ScriptEngine 
     */    
    public static final String ENGINE = "javax.script.engine";

    /** 
     * Reserved key associated with the version of the Java 
     * ScriptEngine 
     */
    public static final String ENGINE_VERSION = "javax.script.engine_version";

    /**
     * Reserved key associated with the name of the supported 
     * scripting language
     */
    public static final String LANGUAGE = "javax.script.language";

    /** 
     * Reserved key associated with the version of the supported 
     * scripting language 
     */
    public static final String LANGUAGE_VERSION = "javax.script.language_version";

    /** 
     * Reserved key associated with the named value which identifies 
     * the short name of the supported language
     */
    public static final String NAME = "javax.script.name";

    /**
     * Retrieves an uninitialized Bindings which can be used as the scope of 
     * the ScriptEngine.
     *   
     * @return a Bindings which can be used to replace the state 
     *         of the ScriptEngine
     */
    public Bindings createBindings();

    /**
     * Evaluates a script obtained using the specified 
     * reader as the script source. 
     * Returns <tt>null</tt> for scripts that don't return a value.
     * 
     * @param reader the source of the script
     * @return the value of the evaluated script 
     * @throws ScriptException if an error occurs
     * @throws NullPointerException if argument is <tt>null</tt> 
     */
    public Object eval(Reader reader) throws ScriptException;

    /**
     * Evaluates a script obtained using a reader as the 
     * script source and using the specified namespace as the 
     * ENGINE_SCOPE.
     * Returns <tt>null</tt> for scripts that don't return a value.
     * 
     * @param reader    the script source used to obtained the script 
     * @param bindings the bindings to be used as ENGINE_SCOPE
     * @return the value of the evaluated script 
     * @throws ScriptException if an error occurs
     * @throws NullPointerException if either argument is <tt>null</tt> 
     */
    public Object eval(Reader reader, Bindings bindings) 
            throws ScriptException;

    /**
     * Evaluates a script obtained using the specified reader as the
     * script source and using the bindings in the specifed 
     * ScriptContext. 
     * Returns <tt>null</tt> for scripts that don't return a value.
     * 
     * @param reader  the script source
     * @param context the context contianing different bindings for
     *                script evaluation
     * @return the value of the evaluated script 
     * @throws ScriptException if an error occurs
     * @throws NullPointerException if either argument is <tt>null</tt>
     */
    public Object eval(Reader reader, ScriptContext context) 
            throws ScriptException;

    /**
     * Evaluates a script contained in a String and returns the resultant object. 
     * Returns <tt>null</tt> for scripts that don't return a value.
     * 
     * @param script the String representation of the script
     * @return the value of the evaluated script
     * @throws ScriptException if an error occurs
     * @throws NullPointerException if argument is <tt>null</tt> 
     */
    public Object eval(String script) throws ScriptException;

    /**
     * Evaluates a piece of script using the specified namespace as 
     * the ENGINE_SCOPE.
     * Returns <tt>null</tt> for scripts that don't return a value.
     *  
     * @param script    the String representation of the script
     * @param bindings the bindings to be used as the ENGINE_SCOPE
     * @return the value of the evaluated script
     * @throws ScriptException if an error occurs
     * @throws NullPointerException if either argument is <tt>null</tt>
     */
    public Object eval(String script ,Bindings bindings) 
            throws ScriptException;

    /**
     * Evaluates a script using the bindings in the specifed 
     * ScriptContext.
     * Returns <tt>null</tt> for scripts that don't return a value.
     *  
     * @param script  the String representation of the script
     * @param context tbe ScriptContext containing bindings for the
     *                script evaluation 
     * @return the value of the evaluated script
     * @throws ScriptException if an error occurs
     * @throws NullPointerException if either argument is <tt>null</tt>
     */
    public Object eval(String script, ScriptContext context) 
            throws ScriptException;

    /**
     * Retrieves the value which is associated with the specified key
     * in the state of the ScriptEngine.
     * 
     * @param key the key associated with value.
     * @return an object value which is associated with the key
     * @throws IllegalArgumentException if argument is empty
     * @throws NullPointerException if argument is <tt>null</tt>
     */
    public Object get(String key);

    /**
     * Returns a ScriptEngineFactory for the class to which this ScriptEngine belongs.
     * 
     * @return The ScriptEngineFactory
     */
    public ScriptEngineFactory getFactory();

    /**
     * Retrieves a reference to the associated bindings for the 
     * specified scope.
     * 
     * Possible scopes are:
     * 
     * GLOBAL_SCOPE :
     * if the ScriptEngine was created by ScriptingEngineManager 
     * then GLOBAL_SCOPE of it is returned (or <tt>null</tt> if there is no
     * GLOBAL_SCOPE stored in the ScriptEngine).
     * 
     * ENGINE_SCOPE : 
     * the set of key-value pairs stored in the ScriptEngine is 
     * returned.
     * 
     * @param scope the specified scope
     * @return associated bindings for the specified scope
     * @throws IllegalArgumentException if the scope is invalid
     */
    public Bindings getBindings(int scope);

    /**
     * Associates a key and a value in the ScriptEngine ENGINE_SCOPE bindings.
     * 
     * @param key   the specified key associated with the value
     * @param value value which is to be associated with the 
     *              specified key
     * @throws IllegalArgumentException if the key is empty
     * @throws NullPointerException if key is <tt>null</tt>
     */
    public void put(String key, Object value);

    /**
     * Associates the specified bindings with the specified scope.
     * 
     * @param bindings bindings to be associated with the specified scope 
     * @param scope scope which the bindings should be associated with
     * @throws IllegalArgumentException 
     *         if the scope is invalid
     * @throws NullPointerException 
     *         if the bindings is <tt>null</tt> and the scope is ScriptContext.ENGINE_SCOPE
     */
    public void setBindings(Bindings bindings, int scope);

    /**
     * Returns the default ScriptContext of the ScriptEngine whose Bindings, Readers
     * and Writers are used for script executions when no ScriptContext is specified.
     *   
     * @return The default ScriptContext of the ScriptEngine
     */
    public ScriptContext getContext();

    /**
     * Sets the default ScriptContext of the ScriptEngine whose Bindings, Readers and
     * Writers are used for script executions when no ScriptContext is specified.
     * 
     * @param context 
     *    scriptContext that will replace the default ScriptContext in the ScriptEngine.
     * @throws NullPointerException if the context is <tt>null</tt>
     */
    public void setContext(ScriptContext context);

}
