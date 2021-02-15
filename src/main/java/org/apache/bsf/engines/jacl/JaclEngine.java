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

package org.apache.bsf.engines.jacl;

import java.util.Vector;

import org.apache.bsf.BSFDeclaredBean;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.apache.bsf.util.BSFEngineImpl;

import tcl.lang.Interp;
import tcl.lang.ReflectObject;
import tcl.lang.TclDouble;
import tcl.lang.TclException;
import tcl.lang.TclInteger;
import tcl.lang.TclObject;
import tcl.lang.TclString;

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
  public Object call (final Object obj, final String method, final Object[] args)
                                                        throws BSFException {
    final StringBuffer tclScript = new StringBuffer (method);
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
  public void declareBean (final BSFDeclaredBean bean) throws BSFException {
    final String expr = "set " + bean.name + " [bsf lookupBean \"" + bean.name +
	  "\"]";
    eval ("<declare bean>", 0, 0, expr);
  }
  /**
   * This is used by an application to evaluate a string containing
   * some expression.
   */
  public Object eval (final String source, final int lineNo, final int columnNo,
              final Object oscript) throws BSFException {
    final String script = oscript.toString ();
    try {
      interp.eval (script);
      final TclObject result = interp.getResult();
      final Object internalRep = result.getInternalRep();

      // if the object has a corresponding Java type, unwrap it
      if (internalRep instanceof ReflectObject) {
        return ReflectObject.get(interp,result);
    }
      if (internalRep instanceof TclString) {
        return result.toString();
    }
      if (internalRep instanceof TclDouble) {
        return new Double(TclDouble.get(interp,result));
    }
      if (internalRep instanceof TclInteger) {
        return new Integer(TclInteger.get(interp,result));
    }

      return result;
    } catch (final TclException e) {
      throw new BSFException (BSFException.REASON_EXECUTION_ERROR,
                  "error while eval'ing Jacl expression: " +
                  interp.getResult (), e);
    }
  }
  /**
   * Initialize the engine.
   */
  public void initialize (final BSFManager mgr, final String lang,
              final Vector declaredBeans) throws BSFException {
    super.initialize (mgr, lang, declaredBeans);

    // create interpreter
    interp = new Interp();

    // register the extension that user's can use to get at objects
    // registered by the app
    interp.createCommand ("bsf", new BSFCommand (mgr, this));

    // Make java functions be available to Jacl
        try {
        interp.eval("jaclloadjava");
    } catch (final TclException e) {
        throw new BSFException (BSFException.REASON_OTHER_ERROR,
                    "error while loading java package: " +
                    interp.getResult (), e);
    }

    final int size = declaredBeans.size ();
    for (int i = 0; i < size; i++) {
      declareBean ((BSFDeclaredBean) declaredBeans.elementAt (i));
    }
  }

  /**
   * Undeclare a previously declared bean.
   */
  public void undeclareBean (final BSFDeclaredBean bean) throws BSFException {
    eval ("<undeclare bean>", 0, 0, "set " + bean.name + " \"\"");
  }
}
