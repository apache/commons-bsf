/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package javax.script;

/**
 * Optional interface implemented by {@link ScriptEngine}s which
 * allow the invocation of procedures in scripts that have previously
 * been executed.
 * <p>
 * See Javadoc of <a href="http://java.sun.com/javase/6/docs/api/javax/script/package-summary.html">Java Scripting API</a>
 */
public interface Invocable {

    /**
     * Invokes a scripting procedure with the given name using the 
     * array of objects as its arguments set.
     * 
     * @param name name of the scripting procedure
     * @param args       arguments set for the scripting procedure
     * @return resultant object after the execution of the procedure
     * @throws ScriptException if the invocation of the scripting procedure
     *         fails
     * @throws NoSuchMethodException if method with given name or matching 
     *         argument types cannot be found
     * @throws NullPointerException if the method name is <tt>null</tt>. 
      */
    public Object invokeFunction(String name, Object[] args) 
            throws ScriptException, NoSuchMethodException;

    /**
     * Invokes a procedure on an object which already defined in the
     * script using the array of objects as its arguments set.
     * 
     * @param thiz       object on which the procedure is called
     * @param name       name of the procedure to be invoked
     * @param args       arguments set for the procedure
     * @return           resultant object after the execution of the 
     *                   procedure
     * @throws ScriptException if the invocation of the procedure fails
     * @throws NoSuchMethodException if a method with given name or matching 
     *         argument types cannot be found
     * @throws NullPointerException if the method name is <tt>null</tt>. 
     * @throws IllegalArgumentException if the specified <tt>thiz</tt> 
     *         is <tt>null</tt> or the specified Object is does not represent a scripting object.
     */
    public Object invokeMethod(Object thiz, String name, Object[] args) throws 
            ScriptException, NoSuchMethodException;

    /**
     * Retrieves an instance of a java class whose methods are 
     * implemented using procedures in script which are in the 
     * intermediate code repository in the underlying interpreter.
     * 
     * @param clasz an interface which the returned class must 
     *              implement
     * @return an instance of the class which implements the specified
     *         interface
     * @throws IllegalArgumentException
     *         if the specified Class object is <tt>null</tt> or is not an interface
     */
    public Object getInterface(Class clasz);

    /**
     * Retrieves an instance of a java class whose methods are 
     * implemented using procedures in script which are in the 
     * intermediate code repository in the underlying interpreter.
     * 
     * @param thiz The scripting object whose member functions are used to implement the methods of the interface.
     * @param clasz The Class object of the interface to return. 
     * @return An instance of requested interface.
     *         Will be <tt>null</tt> if the requested interface is unavailable, 
     *         i.e. if compiled methods in the ScriptEngine cannot be found matching the ones in the requested interface.
     *  @throws IllegalArgumentException
     *   if the specified Class object is <tt>null</tt> or is not an interface,
     *   or if the specified Object is <tt>null</tt> or does not represent a scripting object.
     */
    public Object getInterface(Object thiz, Class clasz);
}
