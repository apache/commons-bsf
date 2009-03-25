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

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * See Javadoc of <a href="http://java.sun.com/javase/6/docs/api/javax/script/package-summary.html">Java Scripting API</a>
 */
public class SimpleBindings implements Bindings {

    /**
     * In which the key-value pairs are stored.
     */
    private final Map map;

    /**
     * Constructs a SimpleBindings.
     */
    public SimpleBindings(){
        map = new HashMap();
    }

    /**
     * Constructs a SimpleBindings and initializes it using a 
     * specified map. 
     * 
     * @param map a map which is used to initialize the 
     *            SimpleBindings
     */
    public SimpleBindings(Map map){
        this.map = map;
    }



    /**
     * Check the conditions which keys need to satisfy:
     * <br/>
     * + String<br/>
     * + non-null<br/>
     * + non-empty<br/>
     * 
     * @param key key to be checked
     * 
     * @throws NullPointerException if key is <tt>null</tt> 
     * @throws ClassCastException if key is not String 
     * @throws llegalArgumentException if key is empty String
     */
    private void validateKey(Object key){
        if (key == null) {
            throw new NullPointerException("key must not be null");
        }
        if (!(key instanceof String)) {
            throw new ClassCastException("key must be a String");
        }
        if (((String)key).length() == 0) {
            throw new IllegalArgumentException("key must not be the empty string");
        }
    }

    /** {@inheritDoc} */
    public Object put(Object key, Object value) {
        validateKey(key);
        return put((String) key, value);
    }

    /**
     * Associates the specified value with the specified key in a 
     * java.util.Map. If the map previously contained a mapping for 
     * this key, the old value is replaced.
     * 
     * @param key the String value which uniquely identifies the 
     *            object
     * @param value the object to be stored.
     * 
     * @return the previous value for the mapping (may be <tt>null</tt>), or <tt>null</tt> if there was none.
     * 
     * @throws NullPointerException if the key is <tt>null</tt>
     * @throws IllegalArgumentException if the key is empty
     */
    public Object put(String key, Object value) {      
        validateKey(key);
        return map.put(key,value);
    }

    /** {@inheritDoc} */
    public void putAll(Map toMerge) {

        Set keySet= toMerge.keySet();
        Iterator keys= keySet.iterator();

        while (keys.hasNext()) {
            validateKey(keys.next());
        }

        map.putAll(toMerge);    
    }

    /** {@inheritDoc} */
    public int size() {
        return map.size();
    }

    /** {@inheritDoc} */
    public void clear() {
        map.clear();    
    }

    /** {@inheritDoc} */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /** {@inheritDoc} */
    public boolean containsKey(Object key) {
        validateKey(key);
        return map.containsKey(key);
    }
    /** {@inheritDoc} */
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    /** {@inheritDoc} */
    public Collection values() {
        return map.values();
    }

    /** {@inheritDoc} */
    public Set entrySet() {
        return map.entrySet();
    }

    /** {@inheritDoc} */
    public Object get(Object key) {
        validateKey(key);
        return map.get(key);
    }

    /** {@inheritDoc} */
    public Set keySet() {
        return map.keySet();
    }

    /** {@inheritDoc} */
    public Object remove(Object key) {
        validateKey(key);
        return map.remove(key);
    }

}
