/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.bsf.engines.jexl;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Vector;

import org.apache.bsf.BSFDeclaredBean;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.bsf.util.BSFEngineImpl;

import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.jexl.Script;
import org.apache.commons.jexl.ScriptFactory;

/**
 * {@link BSFEngine} for Commons JEXL. Requires Commons JEXL version 1.1 or later.
 *
 * @see <a href="http://commons.apache.org/jexl/">Commons JEXL</a>
 *
 */
public class JEXLEngine extends BSFEngineImpl {

    /** The backing JexlContext for this engine. */
    private JexlContext jc;

    /**
     * Initialize the JEXL engine by creating a JexlContext and populating it with the declared beans.
     *
     * @param mgr           The {@link BSFManager}.
     * @param lang          The language.
     * @param declaredBeans The vector of the initially declared beans.
     *
     * @throws BSFException For any exception that occurs while trying to initialize the engine.
     */
    public void initialize(final BSFManager mgr, final String lang, final Vector declaredBeans) throws BSFException {
        super.initialize(mgr, lang, declaredBeans);
        jc = JexlHelper.createContext();
        for (int i = 0; i < declaredBeans.size(); i++) {
            final BSFDeclaredBean bean = (BSFDeclaredBean) declaredBeans.elementAt(i);
            jc.getVars().put(bean.name, bean.bean);
        }
    }

    /**
     * Terminate the JEXL engine by clearing and destroying the backing JEXLContext.
     */
    public void terminate() {
        if (jc != null) {
            jc.getVars().clear();
            jc = null;
        }
    }

    /**
     * Adds this bean to the backing JexlContext.
     *
     * @param bean The {@link BSFDeclaredBean} to be added to the backing context.
     *
     * @throws BSFException For any exception that occurs while trying to declare the bean.
     */
    public void declareBean(final BSFDeclaredBean bean) throws BSFException {
        jc.getVars().put(bean.name, bean.bean);
    }

    /**
     * Removes this bean from the backing JexlContext.
     *
     * @param bean The {@link BSFDeclaredBean} to be removed from the backing context.
     *
     * @throws BSFException For any exception that occurs while trying to undeclare the bean.
     */
    public void undeclareBean(final BSFDeclaredBean bean) throws BSFException {
        jc.getVars().remove(bean.name);
    }

    /**
     * Evaluates the expression as a JEXL Script.
     *
     * @param fileName The file name, if it is available.
     * @param lineNo   The line number, if it is available.
     * @param colNo    The column number, if it is available.
     * @param expr     The expression to be evaluated.
     *
     * @throws BSFException For any exception that occurs while evaluating the expression.
     */
    public Object eval(final String fileName, final int lineNo, final int colNo, final Object expr) throws BSFException {
        if (expr == null) {
            return null;
        }
        try {
            Script jExpr = null;
            if (expr instanceof File) {
                jExpr = ScriptFactory.createScript((File) expr);
            } else if (expr instanceof URL) {
                jExpr = ScriptFactory.createScript((URL) expr);
            } else {
                jExpr = ScriptFactory.createScript((String) expr);
            }
            return jExpr.execute(jc);
        } catch (final Exception e) {
            throw new BSFException(BSFException.REASON_EXECUTION_ERROR, "Exception from Commons JEXL:\n" + e.getMessage(), e);
        }
    }

    /**
     * Executes the script as a JEXL {@link Script}.
     *
     * @param fileName The file name, if it is available.
     * @param lineNo   The line number, if it is available.
     * @param colNo    The column number, if it is available.
     * @param script   The script to be executed.
     *
     * @throws BSFException For any exception that occurs while executing the script.
     */
    public void exec(final String fileName, final int lineNo, final int colNo, final Object script) throws BSFException {
        if (script == null) {
            return;
        }
        try {
            Script jExpr = null;
            if (script instanceof File) {
                jExpr = ScriptFactory.createScript((File) script);
            } else if (script instanceof URL) {
                jExpr = ScriptFactory.createScript((URL) script);
            } else {
                jExpr = ScriptFactory.createScript((String) script);
            }
            jExpr.execute(jc);
        } catch (final Exception e) {
            throw new BSFException(BSFException.REASON_EXECUTION_ERROR, "Exception from Commons JEXL:\n" + e.getMessage(), e);
        }
    }

    /**
     * Same behavior as {@link #exec(String, int, int, Object)} for JEXLEngine.
     *
     * @param fileName The file name, if it is available.
     * @param lineNo   The line number, if it is available.
     * @param colNo    The column number, if it is available.
     * @param script   The script to be executed.
     *
     * @throws BSFException For any exception that occurs while interactively executing the script.
     */
    public void iexec(final String fileName, final int lineNo, final int colNo, final Object script) throws BSFException {
        exec(fileName, lineNo, colNo, script);
    }

    /**
     * Uses reflection to make the call.
     *
     * @param object The object to make the call on.
     * @param name   The call to make.
     * @param args   The arguments to pass.
     *
     * @return The result of the call.
     *
     * @throws BSFException For any exception that occurs while making the call.
     */
    public Object call(final Object object, final String name, final Object[] args) throws BSFException {
        try {
            final Class[] types = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                types[i] = args[i].getClass();
            }
            final Method m = object.getClass().getMethod(name, types);
            return m.invoke(object, args);
        } catch (final Exception e) {
            throw new BSFException(BSFException.REASON_EXECUTION_ERROR, "Exception from JEXLEngine:\n" + e.getMessage(), e);
        }
    }

}
