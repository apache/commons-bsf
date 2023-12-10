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

package org.apache.bsf.engines.jython;

import java.beans.PropertyChangeEvent;
import java.io.ByteArrayInputStream;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.bsf.BSFDeclaredBean;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.bsf.util.BSFEngineImpl;
import org.apache.bsf.util.BSFFunctions;
import org.python.core.Py;
import org.python.core.PyException;
import org.python.core.PyJavaInstance;
import org.python.core.PyObject;
import org.python.core.PySystemState;
import org.python.util.InteractiveInterpreter;

/**
 * This is the interface to Jython (http://www.jython.org/) from BSF. It's derived from the JPython 1.x engine
 */

public class JythonEngine extends BSFEngineImpl {
    BSFPythonInterpreter interp;
    private final static Pattern fromRegExp = Pattern.compile("from ([.^\\S]*)");

    /**
     * call the named method of the given object.
     */
    public Object call(final Object object, final String method, final Object[] args) throws BSFException {
        try {
            PyObject[] pyargs = Py.EmptyObjects;

            if (args != null) {
                pyargs = new PyObject[args.length];
                for (int i = 0; i < pyargs.length; i++) {
                    pyargs[i] = Py.java2py(args[i]);
                }
            }

            if (object != null) {
                final PyObject o = Py.java2py(object);
                return unwrap(o.invoke(method, pyargs));
            }

            PyObject m = interp.get(method);

            if (m == null) {
                m = interp.eval(method);
            }
            if (m != null) {
                return unwrap(m.__call__(pyargs));
            }

            return null;
        } catch (final PyException e) {
            throw new BSFException(BSFException.REASON_EXECUTION_ERROR, "exception from Jython:\n" + e, e);
        }
    }

    /**
     * Declare a bean
     */
    public void declareBean(final BSFDeclaredBean bean) throws BSFException {
        interp.set(bean.name, bean.bean);
    }

    /**
     * Evaluate an anonymous function (differs from eval() in that apply() handles multiple lines).
     */
    public Object apply(final String source, final int lineNo, final int columnNo, final Object funcBody, final Vector paramNames, final Vector arguments)
            throws BSFException {
        try {
            /*
             * We wrapper the original script in a function definition, and evaluate the function. A hack, no question, but it allows apply() to pretend to work
             * on Jython.
             */
            final StringBuilder script = new StringBuilder(byteify(funcBody.toString()));
            int index = 0;
            script.insert(0, "def bsf_temp_fn():\n");

            while (index < script.length()) {
                if (script.charAt(index) == '\n') {
                    script.insert(index + 1, '\t');
                }
                index++;
            }

            final String scriptStr = script.toString();
            importPackage(scriptStr);
            interp.exec(scriptStr);

            Object result = interp.eval("bsf_temp_fn()");

            if (result instanceof PyJavaInstance) {
                result = ((PyJavaInstance) result).__tojava__(Object.class);
            }
            return result;
        } catch (final PyException e) {
            throw new BSFException(BSFException.REASON_EXECUTION_ERROR, "exception from Jython:\n" + e, e);
        }
    }

    /**
     * Evaluate an expression.
     */
    public Object eval(final String source, final int lineNo, final int columnNo, final Object script) throws BSFException {
        try {
            final String scriptStr = byteify(script.toString());
            importPackage(scriptStr);
            Object result = interp.eval(scriptStr);
            if (result instanceof PyJavaInstance) {
                result = ((PyJavaInstance) result).__tojava__(Object.class);
            }
            return result;
        } catch (final PyException e) {
            throw new BSFException(BSFException.REASON_EXECUTION_ERROR, "exception from Jython:\n" + e, e);
        }
    }

    /**
     * Execute a script.
     */
    public void exec(final String source, final int lineNo, final int columnNo, final Object script) throws BSFException {
        try {
            final String scriptStr = byteify(script.toString());
            importPackage(scriptStr);
            interp.exec(scriptStr);
        } catch (final PyException e) {
            throw new BSFException(BSFException.REASON_EXECUTION_ERROR, "exception from Jython:\n" + e, e);
        }
    }

    private void importPackage(final String script) {
        final Matcher matcher = fromRegExp.matcher(script);
        while (matcher.find()) {
            final String packageName = matcher.group(1);
            PySystemState.add_package(packageName);
        }
    }

    /**
     * Execute script code, emulating console interaction.
     */
    public void iexec(final String source, final int lineNo, final int columnNo, final Object script) throws BSFException {
        String scriptStr = byteify(script.toString());
        importPackage(scriptStr);
        final int newline = scriptStr.indexOf("\n");

        if (newline > -1) {
            scriptStr = scriptStr.substring(0, newline);
        }

        try {
            if (interp.buffer.length() > 0) {
                interp.buffer.append("\n");
            }
            interp.buffer.append(scriptStr);
            if (!(interp.runsource(interp.buffer.toString()))) {
                interp.resetbuffer();
            }
        } catch (final PyException e) {
            interp.resetbuffer();
            throw new BSFException(BSFException.REASON_EXECUTION_ERROR, "exception from Jython:\n" + e, e);
        }
    }

    /**
     * Initialize the engine.
     */
    public void initialize(final BSFManager mgr, final String lang, final Vector declaredBeans) throws BSFException {
        super.initialize(mgr, lang, declaredBeans);

        // create an interpreter
        interp = new BSFPythonInterpreter();

        // ensure that output and error streams are re-directed correctly
        interp.setOut(System.out);
        interp.setErr(System.err);

        // register the mgr with object name "bsf"
        interp.set("bsf", new BSFFunctions(mgr, this));

        // Declare all declared beans to the interpreter
        final int size = declaredBeans.size();
        for (int i = 0; i < size; i++) {
            declareBean((BSFDeclaredBean) declaredBeans.elementAt(i));
        }
    }

    /**
     * Undeclare a previously declared bean.
     */
    public void undeclareBean(final BSFDeclaredBean bean) throws BSFException {
        interp.set(bean.name, null);
    }

    public Object unwrap(final PyObject result) {
        if (result != null) {
            final Object ret = result.__tojava__(Object.class);
            if (ret != Py.NoConversion) {
                return ret;
            }
        }
        return result;
    }

    private String byteify(final String orig) {
        // Ugh. Jython likes to be fed bytes, rather than the input string.
        final ByteArrayInputStream bais = new ByteArrayInputStream(orig.getBytes());
        final StringBuilder s = new StringBuilder();
        int c;

        while ((c = bais.read()) >= 0) {
            s.append((char) c);
        }

        return s.toString();
    }

    private class BSFPythonInterpreter extends InteractiveInterpreter {

        public BSFPythonInterpreter() {
        }

        // Override runcode so as not to print the stack dump
        public void runcode(final PyObject code) {
            try {
                this.exec(code);
            } catch (final PyException exc) {
                throw exc;
            }
        }
    }

    public void propertyChange(final PropertyChangeEvent e) {
        super.propertyChange(e);
        final String name = e.getPropertyName();
        final Object value = e.getNewValue();
        if (name.equals("classLoader")) {
            Py.getSystemState().setClassLoader((ClassLoader) value);
        }

    }
}
