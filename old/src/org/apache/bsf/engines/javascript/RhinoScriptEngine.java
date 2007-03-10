
package org.apache.bsf.engines.javascript;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.GenericScriptEngine;
import javax.script.Invocable;
import javax.script.Namespace;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.apache.bsf.util.BSFInvocation;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.WrappedException;

/**
 * This is interface to Netscape's Rhino (JavaScript) form Web Scripting 
 * Framework. 
 * NOTE : Some of the code were taken form JavaScript engine used in 
 * Jakarta Apache Beans Scritping Framework. 
 * 
 * @author Nandika Jayawardana <nandika@opensource.lk>
 * @author Sanka Samaranayake  <sanka@opensource.lk>
 */
public class RhinoScriptEngine extends GenericScriptEngine implements Compilable, Invocable{
    private RhinoNamespaceBridge rhinospace;
    private BSFInvocation invoker;
    
    
    /**
     * Constructs an instance of RhinoScriptEngine. 
     */
    public RhinoScriptEngine(){
        super();
        // setting up the Engine Namespace ..
        rhinospace = new RhinoNamespaceBridge(this);
        invoker = new BSFInvocation(this);
    }
    
    public RhinoScriptEngine(Namespace namespace){
        super(namespace);
        rhinospace = new RhinoNamespaceBridge(this);
    }
  
    public Object call(String methodName, Object thiz, Object[] args)
            throws ScriptException {
    	
        Context cx;
        Scriptable scope;
        Object retValue = null;
        
        if (thiz != null && !(thiz instanceof Scriptable)) {
             throw new IllegalArgumentException(thiz 
                              + " is not a valid JavaScript object");
        }
        
        scope = (thiz == null) ? rhinospace : (Scriptable) thiz;
        
        try {
        	
            cx = Context.enter();
            Object func = scope.get(methodName, scope);
            
            if (func == null || !(func instanceof Function)) {
                throw new EvaluatorException("Function " + methodName 
                          + " not found");
            }
            
            cx.setGeneratingDebug(false);
            cx.setGeneratingSource(false);
            cx.setOptimizationLevel(-1);
            retValue = ((Function) func).call(cx, scope,
                               (Scriptable) thiz, args);
            
        } catch (Throwable throwable) {
            errorHandler(throwable);
        } finally {
            Context.exit();
        }
        
        return unwrap(retValue);
    }
    
    public Object call(String methodName, Object[] args)
            throws ScriptException {
        return call(methodName, null, args);
    }
    
    public CompiledScript compile(Reader reader) throws ScriptException {
    	
        Context cx;
        Script script = null;
        
        try {
        	
            cx = Context.enter();
            String source =
                ((String) rhinospace.get("javax.script.filename") != null)
                ? (String) rhinospace.get("javax.script.filename")
                : "<unknown>";
                
                script = cx.compileReader(reader, source, 0, null);
                
        } catch (Throwable throwable) {
            errorHandler(throwable);
        } finally {
            Context.exit();
        }
        
        return new RhinoCompiledScript(script, this);
    }
    
    public CompiledScript compile(String script) throws ScriptException {
        return compile(new StringReader(script));
    }
    
    public Namespace createNamespace(){
        return new RhinoNamespaceBridge(this);
    }
        
    public Object eval(Reader reader, ScriptContext context) 
            throws ScriptException{
     
        Context cx;
        Object retVal = null;
        RhinoNamespaceBridge oldSpace = rhinospace;
        String source = (get("javax.script.filename") != null) 
                ? get("javax.script.filename").toString() 
                : "<unknown>";
        
        try {
        	
            cx = Context.enter();
            cx.setGeneratingDebug(false);
            cx.setGeneratingSource(false);
            cx.setOptimizationLevel(-1);
            Scriptable sc = getScope(context);
            retVal = cx.evaluateReader(sc, reader, source, 0, null);
 
        } catch (JavaScriptException ex) {
            errorHandler(ex);
        } catch (IOException ioe) {
            errorHandler(ioe);
        } finally {
            rhinospace = oldSpace;
            Context.exit();
        }
        
        return unwrap(retVal);
    }
    
    public Object eval(String script, ScriptContext context) 
            throws ScriptException {
        return eval(new StringReader(script), context);
    }
        
    public ScriptEngineFactory getFactory() {
        return new RhinoScriptEngineFactory();
    }
    
    public Object getInterface(Class clasz) { 
        return invoker.getInterface(clasz);
    }
    
    public Object unwrap(Object value){
        value = (value instanceof NativeJavaObject) ? 
                ((NativeJavaObject)value).unwrap() : value;
        return !(value instanceof Undefined) ? value : null;
    }
    
    public Scriptable getScope(ScriptContext context){
        RhinoNamespaceBridge targetScope;
        
        Namespace enginespace =  context.getNamespace(ScriptContext.ENGINE_SCOPE),
		          globalsapce = context.getNamespace(ScriptContext.GLOBAL_SCOPE);
        
        if (enginespace != null && enginespace != rhinospace) {
        	if (enginespace instanceof RhinoNamespaceBridge) {
                targetScope = (RhinoNamespaceBridge)enginespace;
            } else {
            	targetScope = new RhinoNamespaceBridge(enginespace);
            }        	
        } else {
        	targetScope = rhinospace;
        }
        
        
        if (globalspace != null) {
        	targetScope.setGlobalNamespace(globalspace);        	
        }
        
        targetScope.put("context", context);
        return targetScope;
    }

    public void errorHandler(Throwable throwable) throws ScriptException{
        String message = null;
        String source = null;
        int lineNumber = -1;
        int columnNumber = -1;
        
        // gets the wrapped exception if the argument is a wrapper 
        if(throwable instanceof WrappedException){
            throwable = ((WrappedException)throwable).getWrappedException();
        }
        /*
        if(throwable instanceof JavaScriptException){
            message = throwable.getLocalizedMessage();
            source = ((JavaScriptException)throwable).getSourceName();
            
            if(((JavaScriptException)throwable).getLineNumber() != 0){
                lineNumber = ((JavaScriptException)throwable).getLineNumber();
            }
            
        }else if(throwable instanceof EvaluatorException){
            message = ((EvaluatorException)throwable).getLocalizedMessage();
            source = ((EvaluatorException)throwable).getSourceName();
            
            if(((EvaluatorException)throwable).getLineNumber() != 0){
                lineNumber = ((EvaluatorException)throwable).getLineNumber();
            }
            if(((EvaluatorException)throwable).getColumnNumber() != 0){
                columnNumber = ((EvaluatorException)throwable).getColumnNumber();
            }
        }else if(throwable instanceof StackOverflowError){
            message = "STACK OVERFLOW :";
        }
        */
        if(message == null){
            message = throwable.toString();
        }
        
        if(throwable instanceof Error && 
                !(throwable instanceof StackOverflowError)){
            throw (Error)throwable;
        }else{
            throw new ScriptException(message, source, lineNumber, columnNumber);
        }        
    }
}
