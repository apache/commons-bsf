/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.apache.bsf;

import java.util.HashMap;
import java.util.Map;

import javax.script.*;

import junit.framework.TestCase;

public class SimpleBindingsTest extends TestCase {

    Bindings bindings;
    
    public void setUp(){
        bindings = new SimpleBindings();
    }
    
    public void testConstruct(){
        assertNotNull(bindings);
        try {
            new SimpleBindings(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        new SimpleBindings(new HashMap());
    }
    
    
    
    public void testBadParams(){
        try {
            bindings.containsKey(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            bindings.containsKey("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            bindings.containsKey(Boolean.FALSE);
            fail("Expected ClassCastException");
        } catch (ClassCastException e) {
        }

        try {
            bindings.get(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            bindings.get("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            bindings.get(Boolean.FALSE);
            fail("Expected ClassCastException");
        } catch (ClassCastException e) {
        }


        try {
            bindings.put(null, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            bindings.put("", null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
//        try {
//            bindings.put(Boolean.FALSE, null);
//            fail("Expected ClassCastException");
//        } catch (ClassCastException e) {
//        }

        try {
            bindings.remove(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            bindings.remove("");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            bindings.remove(Boolean.FALSE);
            fail("Expected ClassCastException");
        } catch (ClassCastException e) {
        }

        try {
            bindings.putAll(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            Map map = new HashMap();
            map.put("OK", null);
            map.put("", null);
            bindings.putAll(map);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
        try {
            Map map = new HashMap();
            map.put("OK", null);
            map.put(Boolean.FALSE, null);
            bindings.putAll(map);
            bindings.remove(Boolean.FALSE);
            fail("Expected ClassCastException");
        } catch (ClassCastException e) {
        }
    }
    
    // Most of these are standard Map tests
    public void testValid(){
        assertFalse(bindings.containsKey("key"));
        assertNull(bindings.get("key"));
        bindings.put("key", Boolean.FALSE);        
        assertNotNull(bindings.get("key"));
        assertTrue(bindings.containsKey("key"));
        assertFalse(bindings.containsKey("null"));
        bindings.put("null", null);
        assertTrue(bindings.containsKey("null"));
        assertNull(bindings.get("null"));
        assertNull(bindings.remove("null"));
        assertNotNull(bindings.remove("key"));
        assertNull(bindings.remove("key"));
    }
}
