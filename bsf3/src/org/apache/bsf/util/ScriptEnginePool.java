
package org.apache.bsf.util;

import java.util.LinkedList;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

public class ScriptEnginePool {

    static private final int DEFAULT_SIZE = 10;
    private LinkedList pool = new LinkedList();
    private int engines = 0;
    private int capacity = 0;
    private ScriptEngineFactory factory;
    private boolean isMultithreaded = false;
    
    public ScriptEnginePool(ScriptEngineFactory factory, int capacity){
    	this.factory = factory;
        this.capacity = capacity;
        
        String param = (String)factory.getParameter("THREADING");
        if (param != null && param.equals("MULTITHREADED")) {
            this.isMultithreaded = true;
            pool.add(factory.getScriptEngine());
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
                    waiting();
                }
                return (ScriptEngine) pool.removeFirst();
            }
        }
    }
    
    public boolean isMultithreadingSupported(){
        return this.isMultithreaded;
    }
    
    public void waiting(){
        try{
            wait();            
        }catch(InterruptedException ie){
        }
    }
    
}
