
package javax.script;

import java.util.List;

/**
 * ScriptEngineFactory is used to describe a ScriptEngine instance. 
 * Each ScriptEngine class implementing ScriptEngine has a 
 * corresponding factory which exposes metadata describing the engine
 * class. 
 * 
 * Nandika Jayawardana <nandika@opensource.lk>
 * Sanka Samaranayake  <sanka@opensource.lk>
 */
public interface ScriptEngineFactory {
    
    /**
     * Retrieves an instance of the associated ScriptEngine.
     *  
     * @return an instance of the associated ScriptEngine
     */
    public ScriptEngine getScriptEngine();

	/**
     * Retrieves the full name of the ScriptEngine.
     *  
	 * @return the name of the Script Engine
	 */
	public String getEngineName();
    
	/**
     * Retrieves the version of the Script Engine.
     * 
	 * @return the version of the Script Engine
	 */
    public String getEngineVersion();
    
	/**
     * Retrieves the name of the language supported by the 
     * ScriptEngine.
     * 
	 * @return the name of the supported language
	 */
	public String getLanguageName();
	
    /**
     * Retrieves the version of the language supported by the 
     * ScriptEngine.
     *  
     * @return the version of the supported language
     */
	public String getLanguageVersion();
	
	/**
     * Retrieves an array of Strings which are file extensions 
     * tipically used for files containing scripts written in the
     * language supported by the ScriptEngine.
     *  
	 * @return string array of supported file extensions
	 */	
	public List getExtensions();
	
    /**
     * Retrieves an array of Strings containing MIME types describing
     * the content which can be processed using the Script Engine.
     * 
	 * @return string array of MIME types
	 */
	public List getMimeTypes();
	
	/**
     * Retrieves an array of short descriptive names such as 
     * {"javascript", "rhino"} describing the language supported by 
     * the Script Engine.
     * 
	 * @return an array of short descriptive names describing the 
     *         language supported by the ScriptEngine
	 */
	public List getNames();
	
	/**
     * Retrieves an associated value for the specified key. Returns 
     * null if the ScriptEngine does not have an associated value for
     * the key.
     *  
     * @return associated value for the specified key
	 */
	public Object getParameter(String key);	

	public String getMethodCallSyntax(Object o, String method, String[] args);
	
	public String getOutputStatement(String toDisplay);
	
	public String getProgram(String[] statements);
}
