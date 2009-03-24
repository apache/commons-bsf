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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.imageio.spi.ServiceRegistry;

/**
 * See Javadoc of <a href="http://java.sun.com/javase/6/docs/api/javax/script/package-summary.html">Java Scripting API</a>
 */
public class ScriptEngineManager {

	/** Stores all instances of classes which implements 
     * ScriptEngineFactory which are found in resources 
     * META-INF/services/javax.script.ScriptEngineFactory
     */
    private final HashSet engineSpis = new HashSet();
	
    /**
     * Stores language names with an associated 
     * ScriptEngineFactory 
     */     
    private final HashMap nameAssociations = new HashMap();
	
    /** 
     * Stores file extensions with an associated 
     * ScriptEngineFactory 
     */
    private final HashMap extensionAssocitions = new HashMap();
	
    /** Stores MIME types with an associated ScriptEngineFactory */
    private final HashMap mimeTypeAssociations = new HashMap();
	
    /** Stores the namespace associated with GLOBAL_SCOPE */
    private Bindings globalscope = new SimpleBindings();
	
    /**
     * Constructs ScriptEngineManager and initializes it.
     */
	public ScriptEngineManager() {
        this(Thread.currentThread().getContextClassLoader());
	}
    
    public ScriptEngineManager(ClassLoader loader) {
        Iterator iterator = ServiceRegistry.lookupProviders(ScriptEngineFactory.class, loader);
        
        while (iterator.hasNext()) {
            ScriptEngineFactory factory;
            try {
                factory = (ScriptEngineFactory) iterator.next();
            } catch (Error ignored) {
                continue;
            }
            engineSpis.add(factory);
            
            List data = factory.getNames();
            // gets all descriptinve names for Scripting Engine
            for (int i=0; i<data.size(); i++) {
                nameAssociations.put(data.get(i), factory);
            }
            // gets all supported extensions 
            data = factory.getExtensions();
            for (int i=0; i<data.size(); i++) {
                extensionAssocitions.put(data.get(i), factory);
            }
            // gets all supported MIME types
            data = factory.getMimeTypes();
            for (int i=0; i<data.size(); i++) {
                mimeTypeAssociations.put(data.get(i), factory);
            }            
        }
        
        initXMLHelper(loader);
    }

    /**
     * Initialise the xml helper here so BSF clients don't have to.
     * (Temporary approach for beta2 release)
     */
    private void initXMLHelper(ClassLoader loader) {
    	try {
			Class xmlHelperClass = Class.forName("org.apache.bsf.xml.XMLHelper", true, loader);
			Method initMethod = xmlHelperClass.getMethod("init", new Class[]{});
			initMethod.invoke(null, new Object[]{});
		} catch (Throwable e) {
			// ignore
		}
	}

	/**
     * Retrieves the associated value for the spefied key in the 
     * GLOBAL_SCOPE
     *  
     * @param key the associated key of the value stored in the 
     *        GLOBAL_SCOPE
     * @return the value associated with the specifed key 
     */
    public Object get(String key){
        return globalscope.get(key);    
    }

	/**
     * Retrieves a new instance of a ScriptingEngine for the 
     * specified extension of a scirpt file. Returns null if no 
     * suitable ScriptingEngine is found.
     * 
	 * @param extension the specified extension of a script file
	 * @return a new instance of a ScriptingEngine which supports the
     *         specified script file extension
	 */
    public ScriptEngine getEngineByExtension(String extension){
        
        ScriptEngine engine = null;
        
        ScriptEngineFactory factory = 
                (ScriptEngineFactory) extensionAssocitions.get(extension);
        
		if (factory != null) {
            // gets a new instance of the Scripting Engine
			engine = factory.getScriptEngine();
            // sets the GLOBAL SCOPE
			engine.setBindings(globalscope,ScriptContext.GLOBAL_SCOPE);
        }
        
		return engine;
	}
	
	/**
     * Retrieves new instance the ScriptingEngine for a specifed MIME
     * type. Returns null if no suitable ScriptingEngine is found.
     * 
	 * @param mimeType the specified MIME type
	 * @return a new instance of a ScriptingEngine which supports the
     *         specified MIME type  
	 */
    public ScriptEngine getEngineByMimeType(String mimeType){
        
        ScriptEngine engine = null;
		ScriptEngineFactory factory = 
                (ScriptEngineFactory) mimeTypeAssociations.get(mimeType);
		
		if (factory != null) {
			// gets a new instance of the Scripting Engine
            engine = factory.getScriptEngine();
            // sets the GLOBAL SCOPE
			engine.setBindings(globalscope,ScriptContext.GLOBAL_SCOPE);
        }
        
		return engine;
	}
	
    /**
     * Retrieves a new instance of a ScriptEngine the specified 
     * descriptieve name. Returns null if no suitable ScriptEngine is
     * found.
     * 
     * @param name the descriptive name 
     * @return a new instance of a ScriptEngine which supports the 
     *         specifed descriptive name
     */
	public ScriptEngine getEngineByName(String name){
		
        ScriptEngine engine = null;
        ScriptEngineFactory factory =
                (ScriptEngineFactory) nameAssociations.get(name);
		
		if (factory != null) {
            engine = factory.getScriptEngine();
			engine.setBindings(globalscope,ScriptContext.GLOBAL_SCOPE);
		}
        
		return engine; 
	}
    
    /**
     * Retrieves an array of instances of ScriptEngineFactory class 
     * which are found by the discovery mechanism.
     * 
     * @return a list of all discovered ScriptEngineFactory 
     *         instances 
     */
    public List getEngineFactories(){
    	ArrayList factories = new ArrayList();
    	Iterator iter = engineSpis.iterator();
    	
    	while(iter.hasNext()) {
    		factories.add(iter.next());
    	}
    	return factories;
    }
    
    /**
     * Retrieves the bindings corresponds to GLOBAL_SCOPE.
     * 
     * @return the bindings of GLOBAL_SCOPE
     */
    public Bindings getBindings(){
            return globalscope;
    }
    
    /**
     * Associates the specifed value with the specified key in 
     * GLOBAL_SCOPE.
     * 
     * @param key the associated key for specified value 
     * @param value the associated value for the specified key
     * 
     * @throws NullPointerException if key is null
     * @throws IllegalArgumentException if key is the empty String
     */
    public void put(String key, Object value){
        if (key == null) {
            throw new NullPointerException("key must not be null");
        }
        if (key.length() == 0) {
            throw new IllegalArgumentException("key must not be the empty string");
        }
        globalscope.put(key, value);
    }
	
    /**
     * Register a extension with a ScriptEngineFactory. It 
     * overrides any such association discovered previously.
     * 
     * @param extension the extension associated with the specified
     *        ScriptEngineFactory class
     * @param factory the ScriptEngineFactory associated with
     *        the specified extension
     */
    public void registerEngineExtension(String extension, ScriptEngineFactory factory){
        extensionAssocitions.put(extension, factory);        
    }
    
    /**
     * Registers descriptive name with a ScriptEngineFactory. 
     * It overrides any associations discovered previously.
     * 
     * @param name a descriptive name associated with the specifed 
     *        ScriptEngineFactory class
     * @param factory the ScriptEngineFactory associated with
     *        the specified descriptive name
     */
    public void registerEngineName(String name, ScriptEngineFactory factory){
        nameAssociations.put(name, factory);
    }
    
    /**
     * Registers a MIME type with a ScriptEngineFactory. It 
     * overrides any associations discovered previously.
     *  
     * @param mimeType the MIME type associated with specified 
     *        ScriptEngineFactory class 
     * @param factory the ScriptEngineFactory associated with the
     *        specified MIME type
     */
	public void registerEngineMimeType(String mimeType,ScriptEngineFactory factory){
		mimeTypeAssociations.put(mimeType,factory);
	}
		
    /**
     * Sets the GLOBAL_SCOPE value to the specified namespace.
     * 
     * @param bindings the namespace to be stored in GLOBAL_SCOPE
     * @throws IllegalArgumentException if bindings is null
     */
	public void setBindings(Bindings bindings){
	    if (bindings == null){
	        throw new IllegalArgumentException("bindings must not be null");
	    }
		globalscope = bindings;
	}
} 