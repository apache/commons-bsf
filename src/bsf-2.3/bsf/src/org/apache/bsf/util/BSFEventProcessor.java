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

package org.apache.bsf.util;

import org.apache.bsf.util.event.*;
import org.apache.bsf.*;
import java.io.PrintStream;

/**
 * This is used to support binding scripts to be run when an event
 * occurs. 
 *
 * @author Sanjiva Weerawarana
 */
public class BSFEventProcessor implements EventProcessor {
  BSFEngine engine;
  BSFManager manager;
  String filter;
  String source;
  int lineNo;
  int columnNo;
  Object script;

  /**
   * Package-protected constructor makes this class unavailable for
   * public use.
   */
  BSFEventProcessor (BSFEngine engine, BSFManager manager, String filter,
		     String source, int lineNo, int columnNo, Object script)
	   throws BSFException {
	this.engine = engine;
	this.manager = manager;
	this.filter = filter;
	this.source = source;
	this.lineNo = lineNo;
	this.columnNo = columnNo;
	this.script = script;
  }
  //////////////////////////////////////////////////////////////////////////
  //
  // event is delegated to me by the adapters using this. inFilter is
  // in general the name of the method via which the event was received
  // at the adapter. For prop/veto change events, inFilter is the name
  // of the property. In any case, in the event processor, I only forward
  // those events if for which the filters match (if one is specified).

  public void processEvent (String inFilter, Object[] evtInfo) {
	try {
	  processExceptionableEvent (inFilter, evtInfo);
	} catch (RuntimeException re) {
	  // rethrow this .. I don't want to intercept run-time stuff 
	  // that can in fact occur legit
	  throw re;
	} catch (Exception e) {
	  // should not occur
	  System.err.println ("BSFError: non-exceptionable event delivery " +
			  "threw exception (that's not nice): " + e);
	  e.printStackTrace ();
	}
  }
  //////////////////////////////////////////////////////////////////////////
  //
  // same as above, but used when the method event method may generate
  // an exception which must go all the way back to the source (as in
  // the vetoableChange case)

  public void processExceptionableEvent (String inFilter, Object[] evtInfo) 
														 throws Exception {
	if ((filter != null) && !filter.equals (inFilter)) {
	  // ignore this event
	  return;
	}

	// run the script
	engine.exec (source, lineNo, columnNo, script);
  }
}
