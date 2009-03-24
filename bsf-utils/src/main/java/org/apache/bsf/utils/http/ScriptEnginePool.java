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

package org.apache.bsf.utils.http;

import java.util.LinkedList;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

public class ScriptEnginePool {

    static private final int DEFAULT_SIZE = 10;
    private final LinkedList pool = new LinkedList();
    //GuardedBy("this")
    private int engines = 0;
    private final int capacity;
    private final ScriptEngineFactory factory;
    private final boolean isMultithreaded;
    
    public ScriptEnginePool(ScriptEngineFactory factory, int capacity){
    	this.factory = factory;
        this.capacity = capacity;
        
        String param = (String)factory.getParameter("THREADING");
        if (param != null && param.equals("MULTITHREADED")) {
            this.isMultithreaded = true;
            pool.add(factory.getScriptEngine());
        } else {
            this.isMultithreaded = false;
        }
    }
    
    public ScriptEnginePool(ScriptEngineFactory factory){
        this(factory,DEFAULT_SIZE);
     }
       
    public synchronized void free(ScriptEngine eng){
        pool.add(eng); // should I clear the engine namespaces .. 
        notifyAll();
    }
    
    public synchronized ScriptEngine get(){
        if (isMultithreadingSupported()) {
            return (ScriptEngine) pool.getFirst();
        } else {
            if (!pool.isEmpty()) {
                return (ScriptEngine)pool.removeFirst();
            } else {
                if (engines<capacity) {
                    engines++;
                    return factory.getScriptEngine();
                }
                while (!pool.isEmpty()) {
                    try{
                        wait();            
                    }catch(InterruptedException ie){
                    }
                }
                return (ScriptEngine) pool.removeFirst();
            }
        }
    }
    
    public boolean isMultithreadingSupported(){
        return this.isMultithreaded;
    }
    
}
