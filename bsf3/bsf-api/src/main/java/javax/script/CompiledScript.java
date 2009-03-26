/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package javax.script;

/**
 * Base for classes that store the results of compilations.
 * 
 * This class is immutable.
 * 
 * See Javadoc of <a href="http://java.sun.com/javase/6/docs/api/javax/script/package-summary.html">Java Scripting API</a>
 */
public abstract class CompiledScript {

    public CompiledScript(){
    }

    /**
     * Executes the program stored in the CompiledScript object.
     * The default ScriptContext of the associated ScriptEngine is used.
     * The effect of calling this method is same as that of
     * eval(getEngine().getContext()). 
     * 
     * @return the resultant object after the evaluation of the 
     *         script (may be <tt>null</tt>)
     * @throws ScriptException if evaluation fails for any reason
     */ 
    public Object eval() throws ScriptException {
        return eval(getEngine().getContext());
    }    

    /**
     * Executes the program stored in the CompiledScript object
     * using the supplied Bindings of attributes as the ENGINE_SCOPE
     * of the associated ScriptEngine during script execution.
     *  
     * If bindings is <tt>null</tt>, then the effect of calling this method is
     * same as that of eval(getEngine().getContext()).
     * <br/>
     * The GLOBAL_SCOPE Bindings, Reader and Writer associated
     * with the default ScriptContext of the associated ScriptEngine
     * are used. 
     *   
     * @param bindings the bindings to be used as the ENGINE_SCOPE
     * @return resultant object after the re-evaluation (may be <tt>null</tt>)
     * @throws ScriptException if the evaluation fails for any reason
     */
    public Object eval(Bindings bindings) throws ScriptException{
        ScriptContext context;
        if (bindings == null) {
            context = getEngine().getContext();
        } else {
            // same code as ((AbstractScriptEngine) getEngine()).getScriptContext(bindings);
            context = new SimpleScriptContext();
            context.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
            ScriptContext oldContext = getEngine().getContext();
            context.setBindings(oldContext.getBindings(ScriptContext.GLOBAL_SCOPE), ScriptContext.GLOBAL_SCOPE);
            context.setReader(oldContext.getReader());
            context.setWriter(oldContext.getWriter());
            context.setErrorWriter(oldContext.getErrorWriter());
        }
        return eval(context);
    }

    /**
     * Evaluates the compiled script using the specified 
     * {@link ScriptContext}. 
     * 
     * @param context A ScriptContext to be used in the evalution
     *        of the script
     * @return resultant object after the evaluation (may be <tt>null</tt>)
     * @throws ScriptException if the evaluation fails for any reason
     * @throws NullPointerException if context is <tt>null</tt>
     */
    public abstract Object eval(ScriptContext context) throws ScriptException;

    /**
     * Retrieves a reference to the ScriptEngine whose methods 
     * created this CompiledScript object.
     * 
     * @return the ScriptEngine which created this CompiledScript
     *         object
     */
    public abstract ScriptEngine getEngine();

}
