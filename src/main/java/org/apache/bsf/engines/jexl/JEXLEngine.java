/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to You under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.bsf.engines.jexl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.bsf.BSFDeclaredBean;
import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.bsf.util.BSFEngineImpl;
import org.apache.bsf.util.BSFFunctions;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.JexlExpression;
import org.apache.commons.jexl3.JexlInfo;
import org.apache.commons.jexl3.JexlScript;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.introspection.JexlPermissions;

/**
 * {@link BSFEngine} for Commons JEXL. Requires Commons JEXL version 1.1 or later.
 *
 * @see <a href="https://commons.apache.org/jexl/">Commons JEXL</a>
 */
public class JEXLEngine extends BSFEngineImpl {
    private static JexlPermissions BSF_PERMISSIONS = JexlPermissions.RESTRICTED;
    /** The engine. */
    private JexlEngine engine;
    /** The declared bean */
    private Map<String, Object> vars;
    /** The backing JexlContext for this engine. */
    private JexlContext jc;

    /**
     * Sets the JEXL engine permissions.
     *
     * @param permissions the permissions
     */
    public static void setPermissions(JexlPermissions permissions) {
        BSF_PERMISSIONS = permissions;
    }

    /**
     * A context sharing the variables.
     */
    private static class BSFContext implements JexlContext {
        private final Map<String, Object> map;

        BSFContext(Map<String, Object> vars) {
            this.map = vars;
        }

        @Override
        public Object get(String name) {
            return map.get(name);
        }

        @Override
        public boolean has(String name) {
            return map.containsKey(name);
        }

        @Override
        public void set(String name, Object value) {
            map.put(name, value);
        }
    }

    /**
     * Initialize the JEXL engine by creating a JexlContext and populating it with the declared beans.
     *
     * @param mgr           The {@link BSFManager}.
     * @param lang          The language.
     * @param declaredBeans The vector of the initially declared beans.
     * @throws BSFException For any exception that occurs while trying to initialize the engine.
     */
    public void initialize(final BSFManager mgr, final String lang, final Vector declaredBeans) throws BSFException {
        super.initialize(mgr, lang, declaredBeans);
        vars = new ConcurrentHashMap<>();
        jc = new BSFContext(vars);
        for (int i = 0; i < declaredBeans.size(); i++) {
            final BSFDeclaredBean bean = (BSFDeclaredBean) declaredBeans.elementAt(i);
            vars.put(bean.name, bean.bean);
        }
        vars.put("java.lang.System.out", System.out);
        vars.put("java.lang.System.in", System.in);
        vars.put("java.lang.System.err", System.err);
        vars.put("bsf", new BSFFunctions(mgr, this));
        engine = new JexlBuilder().cache(32).permissions(BSF_PERMISSIONS).create();
    }

    /**
     * Terminate the JEXL engine by clearing and destroying the backing JEXLContext.
     */
    public void terminate() {
        if (jc != null) {
            vars.clear();
            jc = null;
            engine = null;
        }
    }

    /**
     * Adds this bean to the backing JexlContext.
     *
     * @param bean The {@link BSFDeclaredBean} to be added to the backing context.
     * @throws BSFException For any exception that occurs while trying to declare the bean.
     */
    public void declareBean(final BSFDeclaredBean bean) throws BSFException {
        vars.put(bean.name, bean.bean);
    }

    /**
     * Removes this bean from the backing JexlContext.
     *
     * @param bean The {@link BSFDeclaredBean} to be removed from the backing context.
     * @throws BSFException For any exception that occurs while trying to undeclare the bean.
     */
    public void undeclareBean(final BSFDeclaredBean bean) throws BSFException {
        vars.remove(bean.name);
    }

    /**
     * Evaluates the expression as a JEXL Script.
     *
     * @param fileName The file name, if it is available.
     * @param lineNo   The line number, if it is available.
     * @param colNo    The column number, if it is available.
     * @param expr     The expression to be evaluated.
     * @throws BSFException For any exception that occurs while evaluating the expression.
     */
    public Object eval(final String fileName, final int lineNo, final int colNo, final Object expr) throws BSFException {
        if (expr == null) {
            return null;
        }
        final JexlInfo info = new JexlInfo(
                fileName != null ? fileName : expr.toString(),
                Math.max(lineNo, 1),
                Math.max(colNo, 1));
        try {
            JexlExpression jExpr;
            if (expr instanceof File) {
                jExpr = engine.createExpression(info, readSource(info, (File) expr));
            } else if (expr instanceof URL) {
                jExpr = engine.createExpression(info, readSource(info, (URL) expr));
            } else {
                jExpr = engine.createExpression(info, (String) expr);
            }
            return jExpr.evaluate(jc);
        } catch (final Exception e) {
            throw new BSFException(BSFException.REASON_EXECUTION_ERROR, "Exception from Commons JEXL:\n" + e.getMessage(), e);
        }
    }

    /**
     * Executes the script as a JEXL {@link JexlScript}.
     *
     * @param fileName The file name, if it is available.
     * @param lineNo   The line number, if it is available.
     * @param colNo    The column number, if it is available.
     * @param script   The script to be executed.
     * @throws BSFException For any exception that occurs while executing the script.
     */
    public void exec(final String fileName, final int lineNo, final int colNo, final Object script) throws BSFException {
        if (script == null) {
            return;
        }
        final JexlInfo info = new JexlInfo(
                fileName != null ? fileName : script.toString(),
                Math.max(lineNo, 1),
                Math.max(colNo, 1));
        try {
            JexlScript jExpr;
            if (script instanceof File) {
                jExpr = engine.createScript(info, readSource(info, (File) script));
            } else if (script instanceof URL) {
                jExpr = engine.createScript(info, readSource(info, (URL) script));
            } else {
                jExpr = engine.createScript(info, (String) script);
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
     * @return The result of the call.
     * @throws BSFException For any exception that occurs while making the call.
     */
    public Object call(Object object, final String name, final Object[] args) throws BSFException {
        try {
            if (object == null) {
                object = vars.get(name);
            }
            if (object instanceof JexlScript) {
               return ((JexlScript) object).execute(jc, args);
            }
            return engine.invokeMethod(object, name, args);
        } catch (final Exception e) {
            throw new BSFException(BSFException.REASON_EXECUTION_ERROR, "Exception from JEXLEngine:\n" + e.getMessage(), e);
        }
    }

    /**
     * Reads a JEXL source from a File.
     *
     * @param info the script source info
     * @param file the script file
     * @return the source
     */
    protected String readSource(JexlInfo info, final File file) {
        Objects.requireNonNull(file, "file");
        try (BufferedReader reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            return toString(reader);
        } catch (final IOException xio) {
            throw new JexlException(info, "could not read source File", xio);
        }
    }

    /**
     * Reads a JEXL source from an URL.
     *
     * @param info the script source info
     * @param url the script url
     * @return the source
     */
    protected String readSource(JexlInfo info, final URL url) {
        Objects.requireNonNull(url, "url");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            return toString(reader);
        } catch (final IOException xio) {
            throw new JexlException(info, "could not read source URL", xio);
        }
    }
    /**
     * Creates a string from a reader.
     *
     * @param reader to be read.
     * @return the contents of the reader as a String.
     * @throws IOException on any error reading the reader.
     */
    protected static String toString(final BufferedReader reader) throws IOException {
        final StringBuilder buffer = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line).append('\n');
        }
        return buffer.toString();
    }
}
