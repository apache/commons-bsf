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

import java.util.List;

import javax.script.ScriptEngineManager;

import junit.framework.TestCase;

/**
 * Check that the basic factories can be found;
 * also reports runtime details
 */
public class FactoryTest extends TestCase {
	
	
    private void show(String key){
        System.out.println(key+"="+System.getProperty(key));
    }

    public void testEngines() {
        show("java.version");
        show("java.vendor");
        show("java.vm.version");
        show("java.vm.vendor");
        show("java.vm.name");
        ScriptEngineManager sem = new ScriptEngineManager();
        assertNotNull(sem);
        Boolean isBSFClass = null;
        try {
            // Check for a method that's unlikely to be in any but the BSF implementation
            sem.getBindings().getClass().getDeclaredMethod("validateKey", new Class[]{Object.class});
            isBSFClass = Boolean.TRUE;
        } catch (SecurityException e) {
            // Leave as null - we don't know the answer
        } catch (NoSuchMethodException e) {
            isBSFClass = Boolean.FALSE;
        }
        if (isBSFClass == null) {
            System.out.println("ScriptEngineManager class - implementation unknown");
        } else if (isBSFClass.booleanValue()) {
            System.out.println("ScriptEngineManager class is from Apache BSF");            
        } else {
            System.out.println("ScriptEngineManager class is not Apache BSF");            
        }
        final int count = sem.getEngineFactories().size();
        assertTrue("Must find some factories",count>0);
        List l = sem.getEngineFactories();
        System.out.println("Factory count: "+count);
        for(int i=0; i < count; i++){
            System.out.println(l.get(i).getClass().getName());
        }
    }
}
