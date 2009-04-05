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
 * A mapping of key/value pairs, all of whose keys are non-empty Strings.
 * 
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
     * 
     * @return the previous value for the mapping, or <tt>null</tt> if there was none.
     * 
     * @throws NullPointerException if key is <tt>null</tt>
     * @throws ClassCastException if the key is not a String
     * @throws IllegalArgumentException if the key is an empty string
     */
    public Object put(Object key,Object value);

    /**
     * Copies all of the mappings from the specified map to this map.
     * These mappings will replace any mappings that this map had for
     * any of the keys currently in the specified map.
     * 
     * @param toMerge mappings to be stored in the map.
     * 
     * @throws NullPointerException
     *         if toMerge map is <tt>null</tt> or if some key in the map is <tt>null</tt>. 
     * @throws IllegalArgumentException
     *         if some key in the map is an empty String
     * @throws ClassCastException if some key in the map is not a String 
     */
    public void putAll(Map toMerge);

     /**
      * Returns true if this map contains a mapping for the specified key.
      * 
      * @param key key whose presence in this map is to be tested.
      * @return <tt>true</tt> if this map contains a mapping for the specified key.
      * 
      * @throws NullPointerException if key is <tt>null</tt>
      * @throws ClassCastException if key is not a String 
      * @throws IllegalArgumentException if key is empty String
      */
    public boolean containsKey(Object key);

    /**
     * Returns the value to which this map maps the specified key.
     * Returns <tt>null</tt> if the map contains no mapping for this key. 
     * A return value of <tt>null</tt> does not necessarily indicate that the map contains no mapping for the key; 
     * it's also possible that the map explicitly maps the key to <tt>null</tt>.
     * The containsKey  operation may be used to distinguish these two cases.
     *  
     * @param key key whose presence in this map is to be tested.
     * 
     * @return the value to which this map maps the specified key,
     * or <tt>null</tt> if the map contains no mapping for this key.
     *  
     * @throws NullPointerException if key is <tt>null</tt>
     * @throws ClassCastException if key is not a String 
     * @throws IllegalArgumentException if key is empty String
     */
    public Object get(Object key);

    /**
     * Removes the mapping for this key from this map if it is 
     * present (optional operation). 
     * 
     * Returns the value to which the map previously associated the
     * key, or <tt>null</tt> if the map contained no mapping for this key. (A 
     * <tt>null</tt> return can also indicate that the map previously 
     * associated <tt>null</tt> with the specified key if the implementation 
     * supports <tt>null</tt> values.) The map will not contain a mapping for 
     * the specified key once the call returns.
     *  
     * @param key key of entry to be removed.
     * 
     * @return the previous value associated with the key,
     * or <tt>null</tt> if the map contained no mapping for this key.
     *  
     * @throws NullPointerException if key is <tt>null</tt>
     * @throws ClassCastException if key is not a String 
     * @throws IllegalArgumentException if key is empty String
     */
    public Object remove(Object key);
}
