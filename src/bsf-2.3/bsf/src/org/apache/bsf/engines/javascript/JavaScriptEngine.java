/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Apache BSF", "Apache", and "Apache Software Foundation"
 *    must not be used to endorse or promote products derived from
 *    this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many individuals
 * on behalf of the Apache Software Foundation and was originally created by
 * Sanjiva Weerawarana and others at International Business Machines
 * Corporation. For more information on the Apache Software Foundation,
 * please see <http://www.apache.org/>.
 */

package org.apache.bsf.engines.javascript;

import java.rmi.RemoteException;
import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import org.mozilla.javascript.Script;
import org.mozilla.javascript.ClassDefinitionException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.PropertyException;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.WrappedException;
import org.mozilla.javascript.Wrapper;
import org.mozilla.javascript.ImporterTopLevel;

import org.mozilla.javascript.debug.*;

import org.apache.bsf.*;
import org.apache.bsf.util.*;

/**
 * This is the interface to Netscape's Rhino (JavaScript) from the
 * Bean Scripting Framework.
 * <p>
 * The original version of this code was first written by Adam Peller
 * for use in LotusXSL. Sanjiva took his code and adapted it for BSF.
 *
 * @author   Adam Peller <peller@lotus.com>
 * @author   Sanjiva Weerawarana
 * @author   Matthew J. Duftler
 * @author   Norris Boyd
 */
public class JavaScriptEngine extends BSFEngineImpl {
    /**
     * The global script object, where all embedded functions are defined,
     * as well as the standard ECMA "core" objects.
     */
    private Scriptable global;

    private RhinoEngineDebugger m_rhinoDbg;

    public void disconnectedDebuggerNotify() {
        m_rhinoDbg.disconnectedDebuggerNotify();
    }

    BSFDebugManagerImpl getDebugManager() {
        return dbgmgr;
    }

    public void placeBreakpointAtLine(int brkptid, String docname, int lineno)
        throws BSFException {
        m_rhinoDbg.placeBreakpointAtLine(brkptid, docname, lineno);
    }

    public void placeBreakpointAtOffset(int brkptid, String docname,
                                        int offset) throws BSFException {
        m_rhinoDbg.placeBreakpointAtOffset(brkptid, docname, offset);
    }

    public void removeBreakpoint(String docname, int brkptid)
        throws BSFException {
        m_rhinoDbg.removeBreakpoint(docname, brkptid);
    }

    public void setEntryExit(String docname, boolean on)
        throws BSFException {
        m_rhinoDbg.setEntryExit(docname, on);
    }

    /**
     * Return an object from an extension.
     * @param object Object on which to make the call (ignored).
     * @param method The name of the method to call.
     * @param args an array of arguments to be
     * passed to the extension, which may be either
     * Vectors of Nodes, or Strings.
     */
    public Object call(Object object, String method, Object[] args)
        throws BSFException {
        Object theReturnValue = null;
        Context cx;
        try {

            cx = Context.enter();

            //REMIND: convert arg list Vectors here?

            Object fun = global.get(method, global);
            if (fun == Scriptable.NOT_FOUND) {
                throw new JavaScriptException("function " + method + " not found.");
            }
            if (dbgmgr != null) {
                // Force interpretive mode---otherwise
                // debugging is not supported by Rhino.
                cx.setGeneratingDebug(true);
                cx.setGeneratingSource(true);

                cx.setOptimizationLevel(-1);

                cx.setDebugger(m_rhinoDbg, new RhinoContextProxy(m_rhinoDbg));

                theReturnValue = ScriptRuntime.call(cx, fun, global, args,
                                                    null);

            }
            else {
                cx.setOptimizationLevel(-1);

                cx.setGeneratingDebug(false);
                cx.setGeneratingSource(false);

                cx.setOptimizationLevel(0);

                cx.setDebugger(null, null);

                theReturnValue = ScriptRuntime.call(cx, fun, global, args,
                                                    null);
            }
            if (theReturnValue instanceof Wrapper) {
                theReturnValue = ((Wrapper) theReturnValue).unwrap();
            }
        } catch (Throwable t) {
            handleError(t);
        } finally {
            Context.exit();
        }
        return theReturnValue;
    }

    public void declareBean(BSFDeclaredBean bean) throws BSFException {
        if ((bean.bean instanceof Number) ||
            (bean.bean instanceof String) ||
            (bean.bean instanceof Boolean)) {
            global.put(bean.name, global, bean.bean);
        } else {
            // Must wrap non-scriptable objects before presenting to Rhino
            Scriptable wrapped = Context.toObject(bean.bean, global);
            global.put(bean.name, global, wrapped);
        }
    }

    /**
     * This is used by an application to evaluate a string containing
     * some expression.
     */
    public Object eval(String source, int lineNo, int columnNo, Object oscript)
        throws BSFException {

        String scriptText = oscript.toString();
        Object retval = null;
        DocumentCell cell;
        FnOrScript fnOrScript;
        Script script;
        Context cx;

        try {
            cx = Context.enter();

            cell = m_rhinoDbg.loadDocumentNotify(source);
            fnOrScript = (FnOrScript) cell.registerFnOrScriptLines(scriptText,
                                                                   lineNo,
                                                                   columnNo);

            m_rhinoDbg.setCompilingFnOrScript(fnOrScript);

            if (dbgmgr != null) {
                // Force interpretive mode---otherwise
                // debugging is not supported by Rhino.
                cx.setGeneratingDebug(true);
                cx.setGeneratingSource(true);

                cx.setOptimizationLevel(-1);

                cx.setDebugger(m_rhinoDbg, new RhinoContextProxy(m_rhinoDbg));

                fnOrScript.compile(cx, global);
                m_rhinoDbg.setCompilingFnOrScript(null);
                script = fnOrScript.getScript();

                if (script != null) retval = script.exec(cx, global);
                else retval = null;
            }
            else {
                cx.setOptimizationLevel(-1);

                cx.setGeneratingDebug(false);
                cx.setGeneratingSource(false);

                cx.setOptimizationLevel(0);

                cx.setDebugger(null, null);

                retval = cx.evaluateString(global, scriptText,
                                           source, lineNo,
                                           null);
            }

            if (retval instanceof NativeJavaObject)
                retval = ((NativeJavaObject) retval).unwrap();

        } catch (Throwable t) { // includes JavaScriptException, rethrows Errors
            handleError(t);
        } finally {
            Context.exit();
        }
        return retval;
    }

    public Object getSpecificDebuggingInterface() {
        if (m_rhinoDbg != null) return m_rhinoDbg.getDebugInterface();
        return null;
    }

    private void handleError(Throwable t) throws BSFException {
        if (t instanceof WrappedException) {
            t = (Throwable) ((WrappedException) t).unwrap();
        }

        String message = null;
        Throwable target = t;

        if (t instanceof JavaScriptException) {
            message = t.getLocalizedMessage();

            // Is it an exception wrapped in a JavaScriptException?
            Object value = ((JavaScriptException) t).getValue();
            if (value instanceof Throwable) {
                // likely a wrapped exception from a LiveConnect call.
                // Display its stack trace as a diagnostic
                target = (Throwable) value;
            }
        }
        else if (t instanceof EvaluatorException ||
                 t instanceof SecurityException) {
            message = t.getLocalizedMessage();
        }
        else if (t instanceof RuntimeException) {
            message = "Internal Error: " + t.toString();
        }
        else if (t instanceof StackOverflowError) {
            message = "Stack Overflow";
        }

        if (message == null) {
            message = t.toString();
        }

        //REMIND: can we recover the line number here?  I think
        // Rhino does this by looking up the stack for bytecode
        // see Context.getSourcePositionFromStack()
        // but I don't think this would work in interpreted mode

        if (t instanceof Error && !(t instanceof StackOverflowError)) {
            // Re-throw Errors because we're supposed to let the JVM see it
            // Don't re-throw StackOverflows, because we know we've
            // corrected the situation by aborting the loop and
            // a long stacktrace would end up on the user's console
            throw (Error) t;
        }
        else {
            throw new BSFException(BSFException.REASON_OTHER_ERROR,
                                   "JavaScript Error: " + message,
                                   target);
        }
    }

    /**
     * initialize the engine. put the manager into the context -> manager
     * map hashtable too.
     */
    public void initialize(BSFManager mgr, String lang, Vector declaredBeans)
        throws BSFException {
        try {
            m_rhinoDbg = new RhinoEngineDebugger(this);
        } catch (RemoteException re) {
            m_rhinoDbg = null;
        }
        super.initialize(mgr, lang, declaredBeans);

        // Initialize context and global scope object
        try {
            Context cx = Context.enter();
            global = new ImporterTopLevel(cx);
            Scriptable bsf = cx.toObject(new BSFFunctions(mgr, this), global);
            global.put("bsf", global, bsf);

            int size = declaredBeans.size();
            for (int i = 0; i < size; i++) {
                declareBean((BSFDeclaredBean) declaredBeans.elementAt(i));
            }
        } catch (Throwable t) {

        } finally {
            Context.exit();
        }
    }

    public void undeclareBean(BSFDeclaredBean bean) throws BSFException {
        global.delete(bean.name);
    }
}
