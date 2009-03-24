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

import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * See Javadoc of <a href="http://java.sun.com/javase/6/docs/api/javax/script/package-summary.html">Java Scripting API</a>
 */
public class SimpleScriptContext implements ScriptContext {
	
	/** namespace of the scope of level GLOBAL_SCOPE */
    protected Bindings globalScope = new SimpleBindings();
    
	/** namespace of the scope of level ENGINE_SCOPE */
	protected Bindings engineScope = new SimpleBindings();

	/** The reader to be used for input from scripts. */
	protected Reader reader;

	/** The writer to be used for displaying output from scripts */
	protected Writer writer;

    /** The writer to be used for displaying error output from scripts */
	protected Writer errorWriter;

	private static final List SCOPES = 
	    Collections.unmodifiableList(
	        Arrays.asList(new Integer[] { new Integer(ENGINE_SCOPE), new Integer(GLOBAL_SCOPE) })
	     );
	
	public SimpleScriptContext() {
        reader = new InputStreamReader(System.in);
        writer = new PrintWriter(System.out, true);
        errorWriter = new PrintWriter(System.err, true);
	}
    
    /**
     * Retrieves the value for getAttribute(String, int) for the 
     * lowest scope in which it returns a non-null value.
     * 
     * @param name the name of the attribute 
     * @return the value of the attribute
     */
    public Object getAttribute(String name) {
      
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        
        if (engineScope.get(name) != null) {
            return engineScope.get(name);
        } else if (globalScope.get(name) != null) {
            return globalScope.get(name);
        } else {
            return null;            
        }
    }
    
    /**
     * Retrieves the value associated with specified name in the 
     * specified level of scope. Returns null if no value is 
     * associated with specified key in specified level of scope.
     *  
     * @param name   the name of the attribute
     * @param scope the level of scope
     * @return the value value associated with the specified name in
     *         specified level of scope
     */
    public Object getAttribute(String name, int scope) {
    	
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        
        switch (scope) {
        	case ENGINE_SCOPE:
        		return engineScope.get(name);
        	case GLOBAL_SCOPE:
        		return globalScope.get(name);
        	default:
        		throw new IllegalArgumentException("invalid scope");
        }
    }

    /**
     * Retrieves the lowest value of scopes for which the attribute 
     * is defined. If there is no associate scope with the given 
     * attribute (-1) is returned.
     * 
     * @param  name the name of attribute
     * @return the value of level of scope  
     */
    public int getAttributesScope(String name) {
     
    	if (engineScope.containsKey(name)) {
            return ENGINE_SCOPE;
        } else if(globalScope.containsKey(name)) {
            return GLOBAL_SCOPE;
        }
        
        return -1;
    }
    
    /**
     * Retrieves the Namespace instance associated with the specified
     * level of scope.
     * 
     * @param scope the level of the scope
     * @return the namespace associated with the specified level of 
     *         scope
     */
    public Bindings getBindings(int scope) {
        
    	switch (scope) {
        	case ENGINE_SCOPE:
        		return engineScope;
        	case GLOBAL_SCOPE:
        		return globalScope;
        	default:
        		return null; // shouldn't I throw an IllegalArgumentException
        }
    }
    
    /**
     * Removes the specified attribute form the specified level of 
     * scope.
     * 
     * @param name the name of the attribute
     * @param scope the level of scope 
     * @return value which is removed
     * @throws IllegalArgumentException
     */
    public Object removeAttribute(String name, int scope) { 
       
    	if (name == null) {
            throw new IllegalArgumentException("name is null");
        }
        
        switch (scope) {
        	case ENGINE_SCOPE:
        		return engineScope.remove(name);
        	case GLOBAL_SCOPE:
        		return globalScope.remove(name);
        	default:
        		throw new IllegalArgumentException("invalid scope");
        }        
    }
    
    /**
     * Sets an attribute specified by the name in specified level of 
     * scope.
     *  
     * @param name   the name of the attribute
     * @param value the value of the attribute
     * @param scope the level of the scope
     * @throws IllegalArgumentException if the name is null scope is
     *         invalid
     */
    public void setAttribute(String name, Object value, int scope) {
       
    	if (name == null) {
            throw new IllegalArgumentException("name is null");
        }
        
        switch (scope) {
        	case ENGINE_SCOPE:
        		engineScope.put(name, value);
        		break;
        	case GLOBAL_SCOPE:
        		globalScope.put(name, value);
        		break;
        	default:
        		throw new IllegalArgumentException("invaild scope");
        }
    }
	
	/**
	 * Associates the specified namespace with specified level of 
     * scope.
	 * 
	 * @param namespace the namespace to be associated with specified
     *                  level of scope
     * @param scope     the level of scope 
	 */	
	public void setBindings(Bindings namespace, int scope) {
	
		switch (scope) {
			case ENGINE_SCOPE:
                if (namespace == null) {
                    throw new NullPointerException("binding is null for ENGINE_SCOPE scope");
                }
				engineScope = namespace;
				break;
			case GLOBAL_SCOPE:
				globalScope = namespace;
				break;
			default:
				throw new IllegalArgumentException("invaild scope");
		}
    }

	public List getScopes() {
		return SCOPES;
	}

	public Reader getReader() {
		return reader;
	}

	public void setReader(Reader reader) {
		this.reader = reader;
	}

	public Writer getWriter() {
		return writer;
	}

	public void setWriter(Writer writer) {
		this.writer = writer;
	}

	public Writer getErrorWriter() {
		return errorWriter;
	}

	public void setErrorWriter(Writer writer) {
		this.errorWriter = writer;
	}

}
