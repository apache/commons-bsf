
package org.apache.bsf.engines.javascript;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.bsf.engines.javascript.RhinoScriptEngine;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;

public class RhinoCompiledScript extends CompiledScript {
    
    RhinoScriptEngine engine;
    Script script;
    
    RhinoCompiledScript(Script script, RhinoScriptEngine engine){
        this.engine = engine;
        this.script = script;
    }

    public Object eval(ScriptContext context) throws ScriptException {
    
        Context cx;
        Object retValue = null;
        
        try {
        	
            cx = Context.enter();
            Scriptable eScope = engine.getScope(context);
            retValue = script.exec(cx, eScope);  
            
        } catch (Throwable throwable) {
            engine.errorHandler(throwable);            
        } finally {        	
            Context.exit();
        }
        
        return engine.unwrap(retValue);
    }

    public ScriptEngine getEngine() {
        return engine;
    }
    
}
