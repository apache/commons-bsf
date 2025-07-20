/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.bsf;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Hashtable;

import org.apache.bsf.util.CodeBuffer;
import org.apache.bsf.util.IOUtils;

/**
 * This is the main driver for BSF to be run on the command line to eval/exec/compile scripts directly.
 */
public class Main {
    private static final String ARG_IN = "-in";
    private static final String ARG_LANG = "-lang";
    private static final String ARG_MODE = "-mode";
    private static final String ARG_OUT = "-out";
    private static final String ARG_VAL_EVAL = "eval";
    private static final String ARG_VAL_EXEC = "exec";
    private static final String ARG_VAL_COMPILE = "compile";
    private static final String DEFAULT_IN_FILE_NAME = "<STDIN>";
    private static final String DEFAULT_MODE = ARG_VAL_EVAL;
    private static final String DEFAULT_CLASS_NAME = "Test";

    /**
     * Static driver to be able to run BSF scripts from the command line.
     *
     * @param args command line arguments
     * @exception IOException if any I/O error while loading script
     */
    public static void main(final String[] args) throws IOException {
        try {
            if ((args.length == 0) || (args.length % 2 != 0)) {
                printHelp();
                System.exit(1);
            }

            final Hashtable argsTable = new Hashtable();

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
                    throw new BSFException(BSFException.REASON_OTHER_ERROR, "unable to determine language");
                }
            }

            Reader in;

            if (inFileName != null) {
                in = new FileReader(inFileName);
            } else {
                in = new InputStreamReader(System.in);
                inFileName = DEFAULT_IN_FILE_NAME;
            }

            final BSFManager mgr = new BSFManager();
            final String mode = (String) argsTable.get(ARG_MODE);

            if (mode.equals(ARG_VAL_COMPILE)) {
                final String outClassName = (String) argsTable.get(ARG_OUT);
                final FileWriter out = new FileWriter(outClassName + ".java");
                final PrintWriter pw = new PrintWriter(out);

                final CodeBuffer cb = new CodeBuffer();
                cb.setClassName(outClassName);
                mgr.compileScript(language, inFileName, 0, 0, IOUtils.getStringFromReader(in), cb);
                cb.print(pw, true);
                out.close();
            } else {
                if (mode.equals(ARG_VAL_EXEC)) {
                    mgr.exec(language, inFileName, 0, 0, IOUtils.getStringFromReader(in));
                } else { /* eval */
                    final Object obj = mgr.eval(language, inFileName, 0, 0, IOUtils.getStringFromReader(in));

                    // Try to display the result.

                    if (obj instanceof java.awt.Component) {
                        Frame f;
                        if (obj instanceof Frame) {
                            f = (Frame) obj;
                        } else {
                            f = new Frame("BSF Result: " + inFileName);
                            f.add((java.awt.Component) obj);
                        }
                        // Add a window listener to quit on closing.
                        f.addWindowListener(new WindowAdapter() {
                            public void windowClosing(final WindowEvent e) {
                                System.exit(0);
                            }
                        });
                        f.pack();
                        // f.show(); // javac 1.5 warns to use f.show(), Apache build scripts abort as a result :(
                        f.setVisible(true); // available since Java 1.1
                    } else {
                        System.err.println("Result: " + obj);

                    }

                    System.err.println("Result: " + obj);
                }
            }
        } catch (final BSFException e) {
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
        System.err.println("      [-in                fileName]   default: " + DEFAULT_IN_FILE_NAME);
        System.err.println("      [-lang          languageName]   default: <If -in is specified and -lang");
        System.err.println("                                                is not, attempt to determine");
        System.err.println("                                                language from file extension;");
        System.err.println("                                                otherwise, -lang is required.>");
        System.err.println("      [-mode   (eval|exec|compile)]   default: " + DEFAULT_MODE);
        System.err.println();
        System.err.println("    Additional args used only if -mode is set to \"compile\":");
        System.err.println();
        System.err.println("      [-out              className]   default: " + DEFAULT_CLASS_NAME);
    }
}
