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
 * ComplieScript interface is an abstraction for the intermediate 
 * code produced by the compilation and contains methods which allow
 * the re-execution of the intermediate code retained.
 * 
 * @author Nandika Jayawardana <nandika@opensource.lk>
 * @author Sanka Samaranayake  <ssanka@gmail.com>
 */
public abstract class CompiledScript {
    
    public CompiledScript(){
    }
    
    /**
     * Re-evaluates the pre-compiled script stored using the 
     * ENGINE_SCOPE and the GLOBAL_SCOPE of the associated 
     * ScriptEngine and returns the resultant object.
     * 
     * @return the resultant object after the re-evaluation of the 
     *         script
     * @throws ScriptException if re-evaluation fails due to any 
     *         reason
     */ 
    public Object eval() throws ScriptException {
        
        GenericScriptContext context = new GenericScriptContext();
        
        context.setNamespace(getEngine().getNamespace(ScriptContext.ENGINE_SCOPE),
                        ScriptContext.ENGINE_SCOPE);
        context.setNamespace(getEngine().getNamespace(ScriptContext.GLOBAL_SCOPE),
                        ScriptContext.GLOBAL_SCOPE);
        
        return eval(context);
    }    
    
    /**
     * Re-evaluates the pre-compiled script using the specified 
     * namespace as the SCRIPT_SCOPE and using ENGINE_SCOPE, 
     * GLOBAL_SCOPE of the associated ScriptEngine.
     *   
     * @param namespace the namespace to be used as the SCRIPT_SCOPE
     * @return resultant object after the re-evaluation
     * @throws ScriptException if the re-evaluation fails due to any
     *         reason
     */
    public Object eval(Namespace namespace) throws ScriptException{
        
        GenericScriptContext context = new GenericScriptContext();
        
        context.setNamespace(namespace, ScriptContext.SCRIPT_SCOPE);
        context.setNamespace(getEngine().getNamespace(ScriptContext.ENGINE_SCOPE),
                        ScriptContext.ENGINE_SCOPE);
        context.setNamespace(getEngine().getNamespace(ScriptContext.GLOBAL_SCOPE),
                        ScriptContext.GLOBAL_SCOPE);
        
        return eval(context);
    }
    
    /**
     * Re-evaluates the recompiled script using the specified 
     * ScriptContext. 
     * 
     * @param context A ScriptContext to be used in the re-evalution
     *        of the script
     * @return resultant object after the re-evaluation
     * @throws ScriptException if the re-evaluation fails due to any
     *         reason
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
