package org.apache.bsf.engines.testscript;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;


/**
 * @author Sanka Samaranayake <ssanka@gmail.com>
 *
 */
public class TestScriptEngineFactory implements ScriptEngineFactory {
	

	public ScriptEngine getScriptEngine() {
		return new TestScriptEngine();
	}
	
	public String getEngineName() {
		return "TestScriptEngine";
	}
	public String getEngineVersion() {
		return "1.0";
	}
	public String[] getExtensions() {
		return new String[]{"tEst","teSt"};
	}
	public String getLanguageName() {
		return "TestScript";
	}
	public String getLanguageVersion() {
		return "1.0";
	}
	public String[] getMimeTypes() {
		return new String[]{};
	}
	public String[] getName() {
		return new String[]{"TestScript", "JUnit"};
		
	}
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
        } 
        return null;
	}
}
