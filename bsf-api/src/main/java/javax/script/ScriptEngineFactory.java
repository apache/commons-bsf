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

import java.util.List;

/**
 * See Javadoc of <a href="http://java.sun.com/javase/6/docs/api/javax/script/package-summary.html">Java Scripting API</a>
 */
public interface ScriptEngineFactory {
    
    /**
     * Retrieves an instance of the associated ScriptEngine.
     *  
     * @return an instance of the associated ScriptEngine
     */
    public ScriptEngine getScriptEngine();

	/**
     * Retrieves the full name of the ScriptEngine.
     *  
	 * @return the name of the Script Engine
	 */
	public String getEngineName();
    
	/**
     * Retrieves the version of the Script Engine.
     * 
	 * @return the version of the Script Engine
	 */
    public String getEngineVersion();
    
	/**
     * Retrieves the name of the language supported by the 
     * ScriptEngine.
     * 
	 * @return the name of the supported language
	 */
	public String getLanguageName();
	
    /**
     * Retrieves the version of the language supported by the 
     * ScriptEngine.
     *  
     * @return the version of the supported language
     */
	public String getLanguageVersion();
	
	/**
     * Retrieves an immutable list of Strings which are file extensions 
     * typically used for files containing scripts written in the
     * language supported by the ScriptEngine.
     *  
	 * @return immutable list of supported file extensions
	 */	
	public List getExtensions();
	
    /**
     * Retrieves an immutable list of Strings containing MIME types describing
     * the content which can be processed using the Script Engine.
     * 
	 * @return immutable list of MIME types
	 */
	public List getMimeTypes();
	
	/**
     * Retrieves an immutable list of short descriptive names such as 
     * {"javascript", "rhino"} describing the language supported by 
     * the Script Engine.
     * 
	 * @return immutable list of short descriptive names describing the 
     *         language supported by the ScriptEngine
	 */
	public List getNames();
	
	/**
     * Retrieves an associated value for the specified key. Returns 
     * null if the ScriptEngine does not have an associated value for
     * the key.
     *  
     * @return associated value for the specified key
	 */
	public Object getParameter(String key);	

	public String getMethodCallSyntax(String obj, String method, String[] args);
	
	public String getOutputStatement(String toDisplay);
	
	public String getProgram(String[] statements);
}
