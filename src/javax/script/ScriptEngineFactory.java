
package javax.script;

/**
 * ScriptEngineFactory is used to describe a ScriptEngine instance. 
 * Each ScriptEngine class implementing ScriptEngine has a 
 * corresponding factory which exposes metadata describing the engine
 * class. 
 * 
 * Nandika Jayawardana <nandika@opensource.lk>
 * Sanka Samaranayake  <sanka@opensource.lk>
 */
public interface ScriptEngineFactory extends ScriptEngineInfo{
    
    /**
     * Retrieves an instance of the associated ScriptEngine.
     *  
     * @return an instance of the associated ScriptEngine
     */
    public ScriptEngine getScriptEngine();
}
