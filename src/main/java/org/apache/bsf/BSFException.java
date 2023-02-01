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

package org.apache.bsf;

/**
 * If something goes wrong while doing some scripting stuff, one of these
 * is thrown. The integer code indicates what's wrong and the message
 * may give more details. The reason one exception with multiple meanings
 * (via the code) [instead of multiple exception types] is used is due to
 * the interest to keep the run-time size small.
 */
public class BSFException extends Exception {
  public static final int REASON_INVALID_ARGUMENT = 0;
  public static final int REASON_IO_ERROR = 10;
  public static final int REASON_UNKNOWN_LANGUAGE = 20;
  public static final int REASON_EXECUTION_ERROR = 100;
  public static final int REASON_UNSUPPORTED_FEATURE = 499;
  public static final int REASON_OTHER_ERROR = 500;

  final int reason;
  Throwable targetThrowable;

  public BSFException (final int reason, final String msg) {
    super (msg);
    this.reason = reason;
  }
  public BSFException (final int reason, final String msg, final Throwable t) {
    this (reason, msg);
    targetThrowable = t;
  }
  public BSFException (final String msg) {
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
      final String msg = getMessage ();

      if (msg != null && !msg.equals (targetThrowable.getMessage ())) {
        System.err.print (msg + ": ");
      }

      targetThrowable.printStackTrace ();
    } else {
      super.printStackTrace ();
    }
  }
}
