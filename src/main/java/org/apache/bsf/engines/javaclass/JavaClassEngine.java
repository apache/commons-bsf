/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.bsf.engines.javaclass;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.bsf.BSFException;
import org.apache.bsf.util.BSFEngineImpl;
import org.apache.bsf.util.MethodUtils;

/**
 * This is the interface to scripts consisting of Java objects from the Bean Scripting Framework.
 */
public class JavaClassEngine extends BSFEngineImpl {
    /**
     * call the named method of the given object. If object is an instance of Class, then the call is a static call on that object. If not, its an instance
     * method call or a static call (as per Java) on the given object.
     */
    public Object call(final Object object, final String method, final Object[] args) throws BSFException {
        // determine arg types
        Class[] argTypes = null;
        if (args != null) {
            argTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                argTypes[i] = (args[i] != null) ? args[i].getClass() : null;
            }
        }

        // now find method with the right signature, call it and return result
        try {
            final Method m = MethodUtils.getMethod(object, method, argTypes);
            return m.invoke(object, args);
        } catch (final Exception e) {
            // something went wrong while invoking method
            final Throwable t = (e instanceof InvocationTargetException) ? ((InvocationTargetException) e).getTargetException() : null;
            throw new BSFException(BSFException.REASON_OTHER_ERROR, "method invocation failed: " + e + ((t == null) ? "" : (" target exception: " + t)), t);
        }
    }

    /**
     * This is used by an application to evaluate an object containing some expression - clearly not possible for compiled code ..
     */
    public Object eval(final String source, final int lineNo, final int columnNo, final Object oscript) throws BSFException {
        throw new BSFException(BSFException.REASON_UNSUPPORTED_FEATURE, "Java bytecode engine can't evaluate expressions");
    }
}
