/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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

package org.apache.bsf.engines.jacl;

import java.util.*;
import java.io.*;

import tcl.lang.*;

import org.apache.bsf.*;
import org.apache.bsf.util.*;

// class used to add "bsf" command to the Jacl runtime
class BSFCommand implements Command {
  BSFManager mgr;
  BSFEngine jengine;

  BSFCommand (BSFManager mgr, BSFEngine jengine) {
	this.mgr = mgr;
	this.jengine = jengine;
  }
  public void cmdProc (Interp interp, 
		       TclObject argv[]) throws TclException {
	if (argv.length < 2) {
	  interp.setResult ("invalid # of args; usage: bsf " +
		"lookupBean|registerBean|unregisterBean|addEventListener args");
	  throw new TclException (TCL.ERROR);
	}

	String op = argv[1].toString ();

	if (op.equals ("lookupBean")) {
	  if (argv.length != 3) {
	interp.setResult ("invalid # of args; usage: bsf " +
			  "lookupBean name-of-bean");
	throw new TclException (TCL.ERROR);
	  }

	  String beanName = argv[2].toString ();
	  Object bean = mgr.lookupBean (beanName);
	  if (bean == null) {
	interp.setResult ("unknown object: " + beanName);
	throw new TclException (TCL.ERROR);
	  }
	  interp.setResult (ReflectObject.newInstance (interp, bean.getClass (), 
						   bean));

	} else if (op.equals ("registerBean")) {
	  if (argv.length != 4) {
	interp.setResult ("invalid # of args; usage: bsf " +
			  "registerBean name-of-bean bean");
	throw new TclException (TCL.ERROR);
	  }
	  mgr.registerBean (argv[2].toString (), 
			ReflectObject.get (interp, argv[3]));
	  interp.setResult ("");

	} else if (op.equals ("unregisterBean")) {
	  if (argv.length != 3) {
	interp.setResult ("invalid # of args; usage: bsf " +
			  "unregisterBean name-of-bean");
	throw new TclException (TCL.ERROR);
	  }
	  mgr.unregisterBean (argv[2].toString ());
	  interp.setResult ("");

	} else if (op.equals ("addEventListener")) {
	  if (argv.length != 6) {
	interp.setResult ("invalid # of args; usage: bsf " +
			  "addEventListener object event-set-name filter " +
			  "script-to-run");
	throw new TclException (TCL.ERROR);
	  }
	  try {
	// usage: bsf addEventListener object event-set filter script
	String filter = argv[4].toString ();
	filter = filter.equals ("") ? null : filter;
	EngineUtils.addEventListener (ReflectObject.get (interp, argv[2]),
				      argv[3].toString (), filter,
				      jengine, mgr, "<event-script>", 0, 0,
				      argv[5].toString ());
	  } catch (BSFException e) {
	e.printStackTrace ();
	interp.setResult ("got BSF exception: " + e.getMessage ());
	throw new TclException (TCL.ERROR);
	  }
	}
  }
}
