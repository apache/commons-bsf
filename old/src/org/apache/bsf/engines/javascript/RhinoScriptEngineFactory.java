
package org.apache.bsf.engines.javascript;

import javax.script.GenericScriptContext;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

/**
 * RhinoScriptEngine contains methods which can be used to get 
 * information on describing the RhinoScriptEngine -- an 
 * implementation of ScriptEngine for JavaScripts.
 * 
 * @author Nandika Jayawardana <nandika@opensource.lk>
 * @author Sanka Samaranayake  <sanka@opensource.lk>
 */
public class RhinoScriptEngineFactory implements ScriptEngineFactory {
	
	private ScriptEngine rhinoEngine;
	private ScriptContext scriptContext;

    /**
     * Consturcts a RhinoScriptEngineFactory.
     */
	public RhinoScriptEngineFactory() {
	}

	/**
     * Retrieves an uninitialized ScriptContext which can be used 
     * with all ScriptEngines associated with the Factory.
     *  
	 * @return an uninitialized ScriptContext
	 */
    public ScriptContext getContext() {
		return new GenericScriptContext();
	}
	
    /**
     * Retrieves a new instance of Rhino Engine.
     * 
     * @return a new instance of RhinoScriptEngine 
     */
	public ScriptEngine getScriptEngine()  {
		return new RhinoScriptEngine();
	}
	
    /**
     * Retrieves the full name of the 'Rhino Engine' 
     * 
     * @return the full name of the Rhino Engine
     */
	public String getEngineName() {
		return "RhinoScriptEngine";
	}
    
    /**
     * Retrieves a the version of the Rhino Engine.
     * 
     * @return the version of the Rhino Engine
     */	
	public String getEngineVersion() {
		return "1.0";
	}
	
    /**
     * Retrieves an array of strings discribing the extensions 
     * supported by the Rhino Engine.
     * 
     * @return a String array describing the supported extensions
     */
	public String[] getExtensions() {
        return new String[]{"js", "jss"};
	}
	
    /**
     * Retrieves the name of the language supported by the 
     * Rhino Engine. .. 'javascript' obviously ..
     * 
     * @return the name of the supported language
     */
	public String getLanguageName() {
		return "javascript";
	}
    
    /**
     * Retrieves the javascript version supported by the Rhino Engine.
     * 
     * @return the version of the supported language.
     */
	public String getLanguageVersion() {
		return "1.5";
	}
    
    /**
     * Retrieves an array of Strings describing the MIME types 
     * supported by the Rhino Engine.
     * 
     * @return an array of Strings describing the supported MIME 
     *         types
     */
	public String[] getMimeTypes() {
        return new String[]{};
	}
    
    /**
     * Retrieves an array of Strings which can be used as 
     * descriptives of the Rhino Engine.
     * 
     * @return an array of descriptives of the Rhino Engine 
     */
	public String[] getName() {
		return new String[]{"javascript","rhino"};
	}
    
    /**
     * Retrieves an associated value for the specified key. Returns 
     * null if the ScriptEngine does not have an associated value for
     * the key.
     *  
     * @return associated value for the specified key
     */
	public Object getParameter(Object key) {
        if (key == ScriptEngine.ENGINE) {
            return getEngineName();
        } else if (key == ScriptEngine.ENGINE_VERSION) {
            return getEngineVersion();
        } else if (key == ScriptEngine.NAME) {
            return getName();
        } else if (key == ScriptEngine.LANGUAGE) {
            return getLanguageName();
        } else if(key == ScriptEngine.ENGINE_VERSION) {
            return getLanguageVersion();
        } else if (key == "THREADING") {
        	return "MULTITHREADED";
        } else {
            return null;
        }        
   	}
}
