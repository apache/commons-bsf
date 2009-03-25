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
	
	/**
	 * This is the scope bindings for GLOBAL_SCOPE. 
	 * By default, a null value (which means no global scope) is used. 
	 * */
    protected Bindings globalScope = null;
    
	/**
	 * This is the scope bindings for ENGINE_SCOPE .
	 * By default, a SimpleBindings is used.
	 */
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
	 * Check if name is null or empty string
	 * @param name to be checked
	 */
	private void checkName(String name){
	    if (name == null){
	        throw new NullPointerException("name must not be null");
	    }
	    if (name.length() == 0){
	        throw new IllegalArgumentException("name must not be an empty string");
	    }
	}
    

	/** {@inheritDoc} */
	public Object getAttribute(String name) {

	    checkName(name);
      
        if (engineScope.get(name) != null) {
            return engineScope.get(name);
        } else if (globalScope != null && globalScope.get(name) != null) {
            return globalScope.get(name);
        } else {
            return null;            
        }
    }
    
    /** {@inheritDoc} */
    public Object getAttribute(String name, int scope) {
    	
        checkName(name);
        
        switch (scope) {
        	case ENGINE_SCOPE:
        		return engineScope.get(name);
        	case GLOBAL_SCOPE:
        		return globalScope != null ? globalScope.get(name) : null;
        	default:
        		throw new IllegalArgumentException("invalid scope");
        }
    }

    /** {@inheritDoc} */
    public int getAttributesScope(String name) {
     
        checkName(name);

        if (engineScope.containsKey(name)) {
            return ENGINE_SCOPE;
        } else if(globalScope != null && globalScope.containsKey(name)) {
            return GLOBAL_SCOPE;
        }
        
        return -1;
    }
    
    /** {@inheritDoc} */
    public Bindings getBindings(int scope) {
        
    	switch (scope) {
        	case ENGINE_SCOPE:
        		return engineScope;
        	case GLOBAL_SCOPE:
        		return globalScope;
        	default:
        		throw new IllegalArgumentException("invalid scope");
        }
    }
    
    /** {@inheritDoc} */
    public Object removeAttribute(String name, int scope) { 
       
        checkName(name);
        
        switch (scope) {
        	case ENGINE_SCOPE:
        		return engineScope.remove(name);
        	case GLOBAL_SCOPE:
        		return globalScope != null ? globalScope.remove(name) : null;
        	default:
        		throw new IllegalArgumentException("invalid scope");
        }        
    }
    
    /** {@inheritDoc} */
    public void setAttribute(String name, Object value, int scope) {
       
        checkName(name);
        
        switch (scope) {
        	case ENGINE_SCOPE:
        		engineScope.put(name, value);
        		break;
        	case GLOBAL_SCOPE:
        	    if (globalScope != null) {
        	        globalScope.put(name, value);
        	    } else {
        	        throw new IllegalArgumentException("Global scope is null");// TODO is this correct?
        	    }
        		break;
        	default:
        		throw new IllegalArgumentException("invalid scope");
        }
    }
	
    /** {@inheritDoc} */
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
				throw new IllegalArgumentException("invalid scope");
		}
    }

    /** {@inheritDoc} */
	public List getScopes() {
		return SCOPES;
	}

    /** {@inheritDoc} */
	public Reader getReader() {
		return reader;
	}

    /** {@inheritDoc} */
	public void setReader(Reader reader) {
		this.reader = reader;
	}

    /** {@inheritDoc} */
	public Writer getWriter() {
		return writer;
	}

    /** {@inheritDoc} */
	public void setWriter(Writer writer) {
		this.writer = writer;
	}

    /** {@inheritDoc} */
	public Writer getErrorWriter() {
		return errorWriter;
	}

    /** {@inheritDoc} */
	public void setErrorWriter(Writer writer) {
		this.errorWriter = writer;
	}

}
