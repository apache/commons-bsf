/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
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

package org.apache.bsf;

/**
 * If something goes wrong while doing some scripting stuff, one of these
 * is thrown. The integer code indicates what's wrong and the message
 * may give more details. The reason one exception with multiple meanings
 * (via the code) [instead of multiple exception types] is used is due to
 * the interest to keep the run-time size small.
 * 
 * @author   Sanjiva Weerawarana
 */
public class BSFException extends Exception {
  public static int REASON_INVALID_ARGUMENT = 0;
  public static int REASON_IO_ERROR = 10;
  public static int REASON_UNKNOWN_LANGUAGE = 20;
  public static int REASON_EXECUTION_ERROR = 100;
  public static int REASON_UNSUPPORTED_FEATURE = 499;
  public static int REASON_OTHER_ERROR = 500;

  int reason;
  Throwable targetThrowable;

  public BSFException (int reason, String msg) {
	super (msg);
	this.reason = reason;
  }
  public BSFException (int reason, String msg, Throwable t) {
	this (reason, msg);
	targetThrowable = t;
  }
  public BSFException (String msg) {
	this (REASON_OTHER_ERROR, msg);
  }
  public int getReason () {
	return reason;
  }
  public Throwable getTargetException () {
	return targetThrowable;
  }
  public void printStackTrace () {
	if (targetThrowable != null) {
	  String msg = getMessage ();

	  if (msg != null && !msg.equals (targetThrowable.getMessage ())) {
		System.err.print (msg + ": ");
	  }

	  targetThrowable.printStackTrace ();
	} else {
	  super.printStackTrace ();
	}
  }
}
