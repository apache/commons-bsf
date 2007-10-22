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

import java.util.Map;

/**
 * See Javadoc of <a href="http://java.sun.com/javase/6/docs/api/javax/script/package-summary.html">Java Scripting API</a>
 */
public interface Bindings extends Map{

	/**
     * Associates the specified value with the specified key in a 
     * java.util.Map. If the map previously contained a mapping for 
     * this key, the old value is replaced.
     * 
	 * @param key the String value which uniquely identifies the 
     *            object
	 * @param value the object to be stored.
	 * @throws IllegalArgumentException if the key is null not an 
     *         instance of java.lang.String
     * */
	public Object put(Object key,Object value);

	
    /**
     * Copies all of the mappings from the specified map to this map.
     * These mappings will replace any mappings that this map had for
     * any of the keys currently in the specified map.
     * 
     * @param toMerge mappings to be stored in the map.
     * @throws IllegalArgumentException if a key is null or is not an
     *         instance of java.lang.String
     */
    public void putAll(Map toMerge);

    public boolean containsKey(Object key);
    public Object get(Object key);
    public Object remove(Object key);
}
