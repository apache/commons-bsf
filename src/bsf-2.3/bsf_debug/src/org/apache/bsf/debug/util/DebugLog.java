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

package org.apache.bsf.debug.util;

import java.security.*;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.String;
import java.lang.Integer;
import java.util.Hashtable;

public class DebugLog {
    public static final int BSF_LOG_L0 = 0;
    public static final int BSF_LOG_L1 = 1;
    public static final int BSF_LOG_L2 = 2;
    public static final int BSF_LOG_L3 = 3;
    public static Hashtable logLevels;
    
    private static int loglevel = 0;
    private static PrintStream debugStream = System.err;

    static {
        Integer logprop = Integer.getInteger("org.apache.bsf.debug.logLevel", 0);
        setLogLevel(logprop.intValue());

        logLevels = new Hashtable();
        logLevels.put("BSF_LOG_L0", new Integer(BSF_LOG_L0));
        logLevels.put("BSF_LOG_L1", new Integer(BSF_LOG_L1));
        logLevels.put("BSF_LOG_L2", new Integer(BSF_LOG_L2));
        logLevels.put("BSF_LOG_L3", new Integer(BSF_LOG_L3));
    }

    public static void stdoutPrint(Object obj, int prio) {
        streamPrint(obj, System.out, prio);
    }

    public static void stdoutPrintln(Object obj, int prio) {
        streamPrintln(obj, System.out, prio);
    }

    public static void stderrPrint(Object obj, int prio) {
        streamPrint(obj, System.err, prio);
    }

    public static void stderrPrintln(Object obj, int prio) {
        streamPrintln(obj, System.err, prio);
    }

    public static void debugPrint(Object obj, int prio) {
        streamPrint(obj, debugStream, prio);
    }

    public static void debugPrintln(Object obj, int prio) {
        streamPrintln(obj, debugStream, prio);
    }

    public static void setDebugStream(PrintStream dbgStream) {
        debugStream = dbgStream;
    }

    public static PrintStream getDebugStream() {
        return debugStream;
    }

    public static void setLogLevel(int loglvl) {
        if (loglvl != loglevel) {
            if (loglvl >= 0) {
                try {
                    final int loglvlf = loglvl;
                    AccessController.doPrivileged(new PrivilegedExceptionAction() {
                            public Object run() throws Exception {
                                System.setProperty("org.apache.bsf.debug.logLevel", String.valueOf(loglvlf));
                                return null;
                            }
                        });
                    loglevel = loglvlf;
                }
                catch (PrivilegedActionException prive) {
                    Exception e = prive.getException();
                    System.err.println("Unable to set loglevel: "
                                       + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public static int getLogLevel() {
        return loglevel;
    }

    public static void streamPrint(Object obj, 
                                   OutputStream ostrm, 
                                   int prio) {
        if (loglevel >= prio) {
            PrintStream prs = 
                (ostrm instanceof PrintStream) ? (PrintStream) ostrm 
                : new PrintStream (ostrm, true);
            prs.print(obj);
        }
    }

    public static void streamPrintln(Object obj, 
                                     OutputStream ostrm, 
                                     int prio) {
        if (loglevel >= prio) {
            PrintStream prs = 
                (ostrm instanceof PrintStream) ? (PrintStream) ostrm
                : new PrintStream (ostrm, true);
            prs.println(obj);
        }
    }
}
