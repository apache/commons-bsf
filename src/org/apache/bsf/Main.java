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

package org.apache.bsf;

import java.awt.Frame;
import java.awt.event.*;

import java.io.*;
import java.util.Hashtable;

import org.apache.bsf.util.*;

/**
 * This is the main driver for BSF to be run on the command line
 * to eval/exec/compile scripts directly.
 * 
 * @author   Sanjiva Weerawarana
 * @author   Matthew J. Duftler
 * @author   Sam Ruby
 */
public class Main {
	private static String ARG_IN = "-in";
	private static String ARG_LANG = "-lang";
	private static String ARG_MODE = "-mode";
	private static String ARG_OUT = "-out";
	private static String ARG_VAL_EVAL = "eval";
	private static String ARG_VAL_EXEC = "exec";
	private static String ARG_VAL_COMPILE = "compile";
	private static String DEFAULT_IN_FILE_NAME = "<STDIN>";
	private static String DEFAULT_MODE = ARG_VAL_EVAL;
	private static String DEFAULT_CLASS_NAME = "Test";

	/**
	 * Static driver to be able to run BSF scripts from the command line.
	 *
	 * @param args command line arguments
	 *
	 * @exception IOException if any I/O error while loading script
	 */
	public static void main(String[] args) throws IOException {
		try {
			if ((args.length == 0) || (args.length % 2 != 0)) {
				printHelp();
				System.exit(1);
			}

			Hashtable argsTable = new Hashtable();

			argsTable.put(ARG_OUT, DEFAULT_CLASS_NAME);
			argsTable.put(ARG_MODE, DEFAULT_MODE);

			for (int i = 0; i < args.length; i += 2) {
				argsTable.put(args[i], args[i + 1]);
			}

			String inFileName = (String) argsTable.get(ARG_IN);
			String language = (String) argsTable.get(ARG_LANG);

			if (language == null) {
				if (inFileName != null) {
					language = BSFManager.getLangFromFilename(inFileName);
				} else {
					throw new BSFException(
						BSFException.REASON_OTHER_ERROR,
						"unable to determine language");
				}
			}

			Reader in;

			if (inFileName != null) {
				in = new FileReader(inFileName);
			} else {
				in = new InputStreamReader(System.in);
				inFileName = "<STDIN>";
			}

			BSFManager mgr = new BSFManager();
			String mode = (String) argsTable.get(ARG_MODE);

			if (mode.equals(ARG_VAL_COMPILE)) {
				String outClassName = (String) argsTable.get(ARG_OUT);
				FileWriter out = new FileWriter(outClassName + ".java");
				PrintWriter pw = new PrintWriter(out);

				CodeBuffer cb = new CodeBuffer();
				cb.setClassName(outClassName);
				mgr.compileScript(
					language,
					inFileName,
					0,
					0,
					IOUtils.getStringFromReader(in),
					cb);
				cb.print(pw, true);
				out.close();
			} else
				if (mode.equals(ARG_VAL_EXEC)) {
					mgr.exec(language, inFileName, 0, 0, IOUtils.getStringFromReader(in));
				} else /* eval */ {
					Object obj =
                                            mgr.eval(language, inFileName, 0, 0, IOUtils.getStringFromReader(in));
                                        
					// Try to display the result.
                                        
					if (obj instanceof java.awt.Component) {
                                            Frame f;
                                            
                                            if (obj instanceof Frame) {
						f = (Frame) obj;
                                            } else {
						f = new Frame ("BSF Result: " + inFileName);
						f.add ((java.awt.Component) obj);
                                            }
                                            
                                            // Add a window listener to quit on closing.
                                            f.addWindowListener(
                                                                new WindowAdapter () {
                                                                    public void windowClosing (WindowEvent e) {
                                                                        System.exit (0);
                                                                    }
                                                                });
                                            f.pack ();
                                            f.show ();
					} else {
                                            System.err.println("Result: " + 
                                                               obj);
					}
                                        
					System.err.println("Result: " + obj);
				}
		} catch (BSFException e) {
                    e.printStackTrace();
		}
	}
	private static void printHelp() {
		System.err.println("Usage:");
		System.err.println();
		System.err.println("  java " + Main.class.getName() + " [args]");
		System.err.println();
		System.err.println("    args:");
		System.err.println();
		System.err.println(
			"      [-in                fileName]   default: " + DEFAULT_IN_FILE_NAME);
		System.err.println(
			"      [-lang          languageName]   default: "
				+ "<If -in is specified and -lang");
		System.err.println(
			"                                               "
				+ " is not, attempt to determine");
		System.err.println(
			"                                               "
				+ " language from file extension;");
		System.err.println(
			"                                               "
				+ " otherwise, -lang is required.>");
		System.err.println(
			"      [-mode   (eval|exec|compile)]   default: " + DEFAULT_MODE);
		System.err.println();
		System.err.println(
			"    Additional args used only if -mode is " + "set to \"compile\":");
		System.err.println();
		System.err.println(
			"      [-out              className]   default: " + DEFAULT_CLASS_NAME);
	}
}
