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
 * 4. The names "BSF", "Apache", and "Apache Software Foundation"
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

package org.apache.bsf.engines.jython;

import java.util.Vector;

import org.python.util.*;
import org.python.core.*;

import org.apache.bsf.*;
import org.apache.bsf.util.BSFEngineImpl;
import org.apache.bsf.util.BSFFunctions;

/**
 * This is the interface to Jython (http://www.jython.org/) from BSF.
 * It's derived from the JPython 1.x engine
 *
 * @author   Sanjiva Weerawarana
 * @author   Finn Bock <bckfnn@worldonline.dk>
 * @author   Chuck Murcko
 */

public class JythonEngine extends BSFEngineImpl {
  PythonInterpreter interp;

  /**
   * call the named method of the given object.
   */
  public Object call (Object object, String method, Object[] args) 
														throws BSFException {
	
	PyObject[] pyargs = Py.EmptyObjects;
	if (args != null) {
	  pyargs = new PyObject[args.length];
	  for (int i = 0; i < pyargs.length; i++)
		pyargs[i] = Py.java2py(args[i]);
	}

	if (object != null) {
	  PyObject o = Py.java2py(object);
	  return unwrap(o.invoke(method, pyargs));
	}
	PyObject m = interp.get(method);
	if (m == null)
		m = interp.eval(method);
	if (m != null) {
	return unwrap(m.__call__(pyargs));
	}
	return null;
  }
  /**
   * Declare a bean
   */
  public void declareBean (BSFDeclaredBean bean) throws BSFException {
	interp.set (bean.name, bean.bean);
  }
  /**
   * Evaluate an expression.
   */
  public Object eval (String source, int lineNo, int columnNo, 
		      Object script) throws BSFException {
	try {
	  Object result = interp.eval (script.toString ());
	  if (result != null && result instanceof PyJavaInstance)
		result = ((PyJavaInstance)result).__tojava__(Object.class);
	  return result;
	} catch (PyException e) {
	  e.printStackTrace ();
	  throw new BSFException (BSFException.REASON_EXECUTION_ERROR,
			      "exception from Jython: " + e, e);
	}
  }
  /**
   * Execute a script. 
   */
  public void exec (String source, int lineNo, int columnNo,
		    Object script) throws BSFException {
	try {
	  interp.exec (script.toString ());
	} catch (PyException e) {
	  e.printStackTrace ();
	  throw new BSFException (BSFException.REASON_EXECUTION_ERROR,
			      "exception from Jython: " + e, e);
	}
  }
  /**
   * Initialize the engine.
   */
  public void initialize (BSFManager mgr, String lang,
						  Vector declaredBeans) throws BSFException {
	super.initialize (mgr, lang, declaredBeans);

	// create an interpreter
	interp = new PythonInterpreter ();

	// register the mgr with object name "bsf"
	interp.set ("bsf", new BSFFunctions (mgr, this));

	int size = declaredBeans.size ();
	for (int i = 0; i < size; i++) {
	  declareBean ((BSFDeclaredBean) declaredBeans.elementAt (i));
	}
  }

  /**
   * Undeclare a previously declared bean.
   */
  public void undeclareBean (BSFDeclaredBean bean) throws BSFException {
	interp.set (bean.name, null);
  }
  public Object unwrap(PyObject result) {
	if (result != null) {
	   Object ret = result.__tojava__(Object.class);
	   if (ret != Py.NoConversion)
		  return ret;
	}
	return result;
  }
}
