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
 * See Javadoc of <a href="http://java.sun.com/javase/6/docs/api/javax/script/package-summary.html">Java Scripting API</a>
 */
public interface Compilable {

    /**
     * Returns a CompileScript implementation for the given piece
     * of script which is a abstraction for the intermediate code 
     * produced by the compilation.
     * 
     * @param script the source of the script represented as String
     * @return an implementation of CompileScript which can be used 
     *         to re-execute intermediate code produced by the 
     *         compilation of script
     * @throws ScriptException if the compilation fials due to any 
     *         reason
     */
    public CompiledScript compile(String script) throws ScriptException;
    
    /**
     * Retruns a CompileScript implementation for the script 
     * obtained using java.io.Reader as the script source.
     * 
     * @param reader the reader form which the script source is 
     *        obtained
     * @return an implementation of CompileScript which can be used 
     *         to re-execute intermediate code produced by the 
     *         compilation of script
     * @throws ScriptException if the compilation fials due to any 
     *         reason
     */
    public CompiledScript compile(Reader reader) throws ScriptException;
}