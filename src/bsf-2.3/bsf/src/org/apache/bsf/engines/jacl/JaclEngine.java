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

package org.apache.bsf.engines.jacl;

import java.util.*;
import java.io.*;

import tcl.lang.*;

import org.apache.bsf.*;
import org.apache.bsf.util.*;

/**
 * This is the interface to Scriptics's Jacl (Tcl) from the
 * Bean Scripting Framework.
 * <p>
 *
 * @author   Sanjiva Weerawarana
 */

public class JaclEngine extends BSFEngineImpl {
  /* the Jacl interpretor object */
  private Interp interp;

  /**
   * 
   * @param method The name of the method to call.
   * @param args an array of arguments to be
   * passed to the extension, which may be either
   * Vectors of Nodes, or Strings.
   */
  public Object call (Object obj, String method, Object[] args) 
														throws BSFException {
	StringBuffer tclScript = new StringBuffer (method);
	if (args != null) {
	  for( int i = 0 ; i < args.length ; i++ ) {
	tclScript.append (" ");
	tclScript.append (args[i].toString ());
	  }
	}
	return eval ("<function call>", 0, 0, tclScript.toString ());
  }
  /**
   * Declare a bean
   */
  public void declareBean (BSFDeclaredBean bean) throws BSFException {
	String expr = "set " + bean.name + " [bsf lookupBean \"" + bean.name +
	  "\"]";
	eval ("<declare bean>", 0, 0, expr);
  }
  /**
   * This is used by an application to evaluate a string containing
   * some expression.
   */
  public Object eval (String source, int lineNo, int columnNo, 
		      Object oscript) throws BSFException {
	String script = oscript.toString ();
	try {
	  interp.eval (script);
	  TclObject result = interp.getResult();
	  Object internalRep = result.getInternalRep();

	  // if the object has a corresponding Java type, unwrap it
	  if (internalRep instanceof ReflectObject)
		return ReflectObject.get(interp,result);
	  if (internalRep instanceof TclString)
		return result.toString();
	  if (internalRep instanceof TclDouble)
		return new Double(TclDouble.get(interp,result));
	  if (internalRep instanceof TclInteger)
		return new Integer(TclInteger.get(interp,result));

	  return result;
	} catch (TclException e) { 
	  throw new BSFException (BSFException.REASON_EXECUTION_ERROR,
			      "error while eval'ing Jacl expression: " + 
			      interp.getResult (), e);
	}
  }
  /**
   * Initialize the engine.
   */
  public void initialize (BSFManager mgr, String lang,
			  Vector declaredBeans) throws BSFException {
	super.initialize (mgr, lang, declaredBeans);

	// create interpreter
	interp = new Interp();

	// register the extension that user's can use to get at objects
	// registered by the app
	interp.createCommand ("bsf", new BSFCommand (mgr, this));

	// Make java functions be available to Jacl
        try {
   		interp.eval("jaclloadjava");
	} catch (TclException e) {
		throw new BSFException (BSFException.REASON_OTHER_ERROR,
					"error while loading java package: " +
					interp.getResult (), e);
	}

	int size = declaredBeans.size ();
	for (int i = 0; i < size; i++) {
	  declareBean ((BSFDeclaredBean) declaredBeans.elementAt (i));
	}
  }

  /**
   * Undeclare a previously declared bean.
   */
  public void undeclareBean (BSFDeclaredBean bean) throws BSFException {
	eval ("<undeclare bean>", 0, 0, "set " + bean.name + " \"\"");
  }
}
