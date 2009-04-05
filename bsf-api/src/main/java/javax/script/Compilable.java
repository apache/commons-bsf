/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.script;

import java.io.Reader;

/**
 * Optional interface implemented by script engines which
 * can compile scripts to a form that can be executed repeatedly.
 * <p> 
 * See Javadoc of <a href="http://java.sun.com/javase/6/docs/api/javax/script/package-summary.html">Java Scripting API</a>
 */
public interface Compilable {

    /**
     * Compiles the script (sourced from the String) for later execution.
     * 
     * @param script the source of the script represented as String
     * @return an implementation of {@link CompiledScript} to be executed later
     *         using one of its eval() methods.
     *
     * @throws ScriptException if the compilation fails for any reason
     * @throws NullPointerException if script is <tt>null</tt>
     */
    public CompiledScript compile(String script) throws ScriptException;

    /**
     * Compiles the script (source is read from the Reader) for later execution.
     * 
     * @param reader the reader from which the script source is obtained
     * @return an implementation of {@link CompiledScript} to be executed later
     *         using one of its eval() methods.
     *
     * @throws ScriptException if the compilation fails for any reason
     * @throws NullPointerException if reader is <tt>null</tt>
     */
    public CompiledScript compile(Reader reader) throws ScriptException;
}