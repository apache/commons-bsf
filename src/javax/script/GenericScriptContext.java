
package javax.script;

import java.io.PrintWriter;
import java.io.Writer;

/**
 * The GenericScriptContext is a simple implementation of ScriptContext.
 * 
 * @author Nandika Jayawardana <nandika@opensource.lk>
 * @author Sanka Samaranayake  <sanka@opensource.lk> 
 */
public class GenericScriptContext implements ScriptContext {
	
	/** namespace of the scope of level GLOBAL_SCOPE */
    protected Namespace globalScope = new SimpleNamespace();
    
	/** namespace of the scope of level SCRIPT_SCOPE */
	protected Namespace scriptScope = new SimpleNamespace();
    
	/** namespace of the scope of level ENGINE_SCOPE */
	protected Namespace engineScope = new SimpleNamespace();
	
	public GenericScriptContext() {
	}
    
    /**
     * Retrieves the value for getAttribute(String, int) for the 
     * lowest scope in which it returns a non-null value.
     * 
     * @param name the name of the attribute 
     * @return the value of the attribute
     */
    public Object getAttribute(String name) throws IllegalArgumentException{
      
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        
        if (scriptScope.get(name) != null) {
            return scriptScope.get(name);
        } else if (engineScope.get(name) != null) {
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
     * @param key   the name of the attribute
     * @param scope the level of scope
     * @return the value value associated with the specified name in
     *         specified level of scope
     */
    public Object getAttribute(String name, int scope) 
            throws IllegalArgumentException{
    	
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        
        switch (scope) {
        	case SCRIPT_SCOPE:
        		return scriptScope.get(name);
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
     
    	if (scriptScope.containsKey(name)) {
            return SCRIPT_SCOPE;
        } else if (engineScope.containsKey(name)) {
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
    public Namespace getNamespace(int scope) {
        
    	switch (scope) {
        	case SCRIPT_SCOPE:
        		return scriptScope;
        	case ENGINE_SCOPE:
        		return engineScope;
        	case GLOBAL_SCOPE:
        		return globalScope;
        	default:
        		return null; // shouldn't I throw an IllegalArgumentException
        }
    }
    
    /**
     * Retrieves an instance of java.io.Writer which can be used by 
     * scripts to display their output.
     * 
     * @return an instance of java.io.Writer
     */
    public Writer getWriter() {
        // autoflush is ture so that I can see the output immediately
        return new PrintWriter(System.out, true); 
    }
    
    /**
     * Removes the specified attribute form the specified level of 
     * scope.
     * 
     * @param name the name of the attribute
     * @param scope the level of scope 
     * @return value which is removed
     * @throws
     */
    public Object removeAttribute(String name, int scope) 
            throws IllegalArgumentException{ 
       
    	if (name == null) {
            throw new IllegalArgumentException("name is null");
        }
        
        switch (scope) {
        	case SCRIPT_SCOPE:
        		return scriptScope.remove(name);
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
     * @param key   the name of the attribute
     * @param value the value of the attribute
     * @param scope the level of the scope
     * @return value the value associated with the specified key in 
     *         specified scope
     * @throws IllegalArguementException if the name is null scope is
     *         invlaid
     */
    public void setAttribute(String name, Object value, int scope) 
            throws IllegalArgumentException{
       
    	if (name == null) {
            throw new IllegalArgumentException("name is null");
        }
        
        switch (scope) {
        	case SCRIPT_SCOPE:
        		scriptScope.put(name, value);
        		break;
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
	public void setNamespace(Namespace namespace, int scope) 
            throws IllegalArgumentException {
	
		switch (scope) {
			case SCRIPT_SCOPE:
				scriptScope = namespace;
				break;
			case ENGINE_SCOPE:
				engineScope = namespace;
				break;
			case GLOBAL_SCOPE:
				globalScope = namespace;
				break;
			default:
				throw new IllegalArgumentException("invaild scope");
		}
    }
}
