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

package org.apache.bsf.util;

import java.beans.PropertyChangeEvent;
import java.util.Vector;

import org.apache.bsf.BSFDeclaredBean;
import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;

/**
 * This is a base implementation of the BSFEngine interface which engine implementations may choose to extend to get the basic methods of the interface
 * implemented.
 */

public abstract class BSFEngineImpl implements BSFEngine {

    protected BSFManager mgr; // my manager
    protected String lang; // my language string
    protected Vector declaredBeans; // BSFDeclaredBeans
    protected String classPath;
    protected String tempDir;
    protected ClassLoader classLoader;

    /**
     * Default impl of apply - calls eval ignoring parameters and returns the result.
     */
    public Object apply(final String source, final int lineNo, final int columnNo, final Object funcBody, final Vector paramNames, final Vector arguments)
            throws BSFException {
        return eval(source, lineNo, columnNo, funcBody);
    }

    /**
     * Default impl of compileApply - calls compileExpr ignoring parameters.
     */
    public void compileApply(final String source, final int lineNo, final int columnNo, final Object funcBody, final Vector paramNames, final Vector arguments,
            final CodeBuffer cb) throws BSFException {
        compileExpr(source, lineNo, columnNo, funcBody, cb);
    }

    /**
     * Default impl of compileExpr - generates code that'll create a new manager, evaluate the expression, and return the value.
     */
    public void compileExpr(final String source, final int lineNo, final int columnNo, final Object expr, final CodeBuffer cb) throws BSFException {
        ObjInfo bsfInfo = cb.getSymbol("bsf");

        if (bsfInfo == null) {
            bsfInfo = new ObjInfo(BSFManager.class, "bsf");
            cb.addFieldDeclaration("org.apache.bsf.BSFManager bsf = new org.apache.bsf.BSFManager();");
            cb.putSymbol("bsf", bsfInfo);
        }

        String evalString = bsfInfo.objName + ".eval(\"" + lang + "\", ";
        evalString += "request.getRequestURI(), " + lineNo + ", " + columnNo;
        evalString += "," + StringUtils.lineSeparator;
        evalString += StringUtils.getSafeString(expr.toString()) + ")";

        final ObjInfo oldRet = cb.getFinalServiceMethodStatement();

        if (oldRet != null && oldRet.isExecutable()) {
            cb.addServiceMethodStatement(oldRet.objName + ";");
        }

        cb.setFinalServiceMethodStatement(new ObjInfo(Object.class, evalString));

        cb.addServiceMethodException("org.apache.bsf.BSFException");
    }

    /**
     * Default impl of compileScript - generates code that'll create a new manager, and execute the script.
     */
    public void compileScript(final String source, final int lineNo, final int columnNo, final Object script, final CodeBuffer cb) throws BSFException {
        ObjInfo bsfInfo = cb.getSymbol("bsf");

        if (bsfInfo == null) {
            bsfInfo = new ObjInfo(BSFManager.class, "bsf");
            cb.addFieldDeclaration("org.apache.bsf.BSFManager bsf = new org.apache.bsf.BSFManager();");
            cb.putSymbol("bsf", bsfInfo);
        }

        String execString = bsfInfo.objName + ".exec(\"" + lang + "\", ";
        execString += "request.getRequestURI(), " + lineNo + ", " + columnNo;
        execString += "," + StringUtils.lineSeparator;
        execString += StringUtils.getSafeString(script.toString()) + ")";

        final ObjInfo oldRet = cb.getFinalServiceMethodStatement();

        if (oldRet != null && oldRet.isExecutable()) {
            cb.addServiceMethodStatement(oldRet.objName + ";");
        }

        cb.setFinalServiceMethodStatement(new ObjInfo(void.class, execString));

        cb.addServiceMethodException("org.apache.bsf.BSFException");
    }

    public void declareBean(final BSFDeclaredBean bean) throws BSFException {
        throw new BSFException(BSFException.REASON_UNSUPPORTED_FEATURE, "language " + lang + " does not support declareBean(...).");
    }

    /**
     * Default impl of execute - calls eval and ignores the result.
     */
    public void exec(final String source, final int lineNo, final int columnNo, final Object script) throws BSFException {
        eval(source, lineNo, columnNo, script);
    }

    /**
     * Default impl of interactive execution - calls eval and ignores the result.
     */
    public void iexec(final String source, final int lineNo, final int columnNo, final Object script) throws BSFException {
        eval(source, lineNo, columnNo, script);
    }

    /**
     * initialize the engine; called right after construction by the manager. Declared beans are simply kept in a vector and that's it. Subclasses must do
     * whatever they want with it.
     */
    public void initialize(final BSFManager mgr, final String lang, final Vector declaredBeans) throws BSFException {

        this.mgr = mgr;
        this.lang = lang;
        this.declaredBeans = declaredBeans;

        // initialize my properties from those of the manager. It'll send
        // propagate change events to me
        this.classPath = mgr.getClassPath();
        this.tempDir = mgr.getTempDir();
        this.classLoader = mgr.getClassLoader();
    }

    /**
     * Receive property change events from the manager and update my fields as needed.
     *
     * @param e PropertyChange event with the change data
     */
    public void propertyChange(final PropertyChangeEvent e) {
        final String name = e.getPropertyName();
        final Object value = e.getNewValue();

        if (name.equals("classPath")) {
            classPath = (String) value;
        } else if (name.equals("tempDir")) {
            tempDir = (String) value;
        } else if (name.equals("classLoader")) {
            classLoader = (ClassLoader) value;
        }
    }

    public void terminate() {
        mgr = null;
        declaredBeans = null;
        classLoader = null;
    }

    public void undeclareBean(final BSFDeclaredBean bean) throws BSFException {
        throw new BSFException(BSFException.REASON_UNSUPPORTED_FEATURE, "language " + lang + " does not support undeclareBean(...).");
    }
}
