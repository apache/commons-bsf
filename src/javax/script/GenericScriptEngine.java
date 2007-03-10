package javax.script;

import java.io.Reader;

/**
 * GenericScriptEngine is an abstarct class which implements the ScriptEngine
 * interface and it contains default implementations for several eval() methods.
 * 
 * @author Nandika Jayawardana <nandika@opensource.lk>
 * @author Sanka Samaranayake  <sanka@opensource.lk>
 */
public abstract class GenericScriptEngine implements ScriptEngine {
		
    /** Namespace associated with associated with the ENGINE_SCOPE */
	protected Namespace namespace;
    
    /** Namespace associated with the GLOBAL_SCOPE */		
	protected Namespace globalspace;
	
    /**
     * Constructs a ScriptEngine using an uninitialized 
     * SimpleNamespace.
     */
    public GenericScriptEngine() {
		namespace = new SimpleNamespace();
	}
	
    /**
     * Constructs a ScriptEngine using the specified namespace as its 
     * ENGINE_SCOPE.
     * 
     * @param namespace the namespace to be used as the ENGINE_SCOPE
     */
	public GenericScriptEngine(Namespace namespace){
		this.namespace = namespace;
	}
    
    /**
     * Retrieves an uninitailized namespace which is associated with
     * the ScriptEngine namespace.
     * 
     * @return an unitialized namespace
     */
    public Namespace createNamespace() {
        return   new SimpleNamespace();
    }
	
    /**
     * Evaluates a piece of script obtained using the specified 
     * reader as the script source. Returns null for non-returning
     * scripts.
     * 
     * @param reader the reader form which the script is obtained
     * @return the value of the evaluated script
     * @throws ScriptException if an error occurs 
     */
    public Object eval(Reader reader) throws ScriptException{
        return eval(reader,getScriptContext(null));
    }
    
    /**
     * Evaluates a piece of scripts obtained using a reader as the 
     * script source and using the specified namespace as the 
     * SCRIPT_SCOPE. Returns null for non-returning scripts.
     * 
     * @param reader    the reader from which the script is obtained 
     * @param namespace the namespace to be used as SCRIPT_SCOPE
     * @return the value of the evaluated script
     * @throws ScriptException if an error occurs 
     */    
    public Object eval(Reader reader, Namespace nameSpace) 
            throws ScriptException{
        return eval(reader,getScriptContext(nameSpace));
    }
    
    /**
     * Evaluates a piece of script and returns the resultant object.
     * Returns null for non-returning scripts.
     * 
     * @param script the String representation of the script
     * @return the value of the evaluated script 
     * @throws ScriptException if an error occurs 
     */
    public Object eval(String script) throws ScriptException{
        return eval(script,getScriptContext(null));
    }
       
    /**
     * Evaluates a piece of script using the specified namespace as 
     * its SCRIPT_SCOPE. Returns null for non-returning scripts.
     *  
     * @param script    the String representation of the script
     * @param namespace the namespace to be used as the SCRIPT_SCOPE
     * @return the value of the evaluated script
     * @throws ScriptException if an error occurs 
     */    
    public Object eval(String script, Namespace nameSpace) throws ScriptException{
        return eval(script,getScriptContext(nameSpace));
    }
        
    /**
     * Retrieves the associated value with the specified key 
     * ScriptEngine namespace. Returns null if no such value exists.
     * 
     * @param key the associated key of the value
     * @return the value associated with the specified key in 
     *         ScriptEngine namespace
     */
	public Object get(String key) {
		return getNamespace(ScriptContext.ENGINE_SCOPE).get(key);
	}
    
    /**
     * Retrieves a reference to the associated namespace for the 
     * specified level of scope.
     *  
     * @param scope the specified level of scope
     * @return associated namespace for the specified level of scope
     * @throws IllegalArgumentException if the scope is invalid
     */    
    public Namespace getNamespace(int scope) {
    	
    	switch (scope) {
    		case ScriptContext.ENGINE_SCOPE:
    			return namespace;
    		case ScriptContext.GLOBAL_SCOPE:
    			return globalspace;
    		default:
    			throw new IllegalArgumentException("invaild scope");
    	}
    }
    
    
    /**
     * Retrieves an instance of ScriptContext with namespaces 
     * associated with all the level of scopes and the specified
     * namespace associated with SCRIPT_SCOPE.
     * 
     * @param namespace the namespace to be associated with 
     *        SCRIPT_SCOPE 
     * @return an instance of ScriptContext with all namespaces of 
     *         all scopes
     */
    protected ScriptContext getScriptContext(Namespace namespace){
        
        ScriptContext scriptContext = new GenericScriptContext();
        
        scriptContext.setNamespace(namespace, ScriptContext.SCRIPT_SCOPE);
        scriptContext.setNamespace(this.namespace,ScriptContext.ENGINE_SCOPE);
        scriptContext.setNamespace(this.globalspace,
                              ScriptContext.GLOBAL_SCOPE);
        
        return scriptContext;
    }

    /**
     * Associates a key and a value in the ScriptEngine namespace.
     * 
     * @param key   String value which uniquely identifies the value
     * @param value value which is to be associated with the 
     *              specified key
     * @throws IllegalArgumentException if the key is null
     */
	public void put(String key, Object value) {
        
		if (key == null) { 
            throw new IllegalArgumentException("name is null");
        }
		
		getNamespace(ScriptContext.ENGINE_SCOPE).put(key,value);
	}

    /**
     * Associates a namespace with a specified level of scope.
     * 
     * @param namespace the namespace to be associated with specified scope
     * @param scope     the level of scope of the specified namespace
     * @throws IllegalArgumentException if scope is invalid
     */
	public void setNamespace(Namespace namespace, int scope)
			throws UnsupportedOperationException {
		
		switch (scope) {
			case ScriptContext.ENGINE_SCOPE:
				this.namespace = namespace;
				break;
			case ScriptContext.GLOBAL_SCOPE:
				globalspace = namespace;
				break;
			default:
				throw new IllegalArgumentException("invalid scope");
		}
    }
}
