/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.apache.bsf;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Hashtable;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * This is the main driver for BSF to be run on the command line
 * to eval/exec/compile scripts directly.
 *
 * @author   Sanjiva Weerawarana
 * @author   Matthew J. Duftler
 * @author   Sam Ruby
 */
public class Main {
    private static final String ARG_IN = "-in";
    private static final String ARG_LANG = "-lang";
    private static final String ARG_OUT = "-out";
    private static final String DEFAULT_IN_FILE_NAME = "<STDIN>";
    private static final String DEFAULT_CLASS_NAME = "Test";

    /**
     * Static driver to be able to run BSF scripts from the command line.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) throws IOException {
        if ((args.length == 0) || (args.length % 2 != 0)) {
            printHelp();
            System.exit(1);
        }

        Hashtable argsTable = new Hashtable();

        argsTable.put(ARG_OUT, DEFAULT_CLASS_NAME);

        for (int i = 0; i < args.length; i += 2) {
            argsTable.put(args[i], args[i + 1]);
        }

        String inFileName = (String) argsTable.get(ARG_IN);
        String language = (String) argsTable.get(ARG_LANG);


        if (language == null && inFileName != null) {
            int i = inFileName.lastIndexOf('.');
            if (i > 0) {
                language = inFileName.substring(i+1);
            }
            if (language == null) {
                throw new IllegalArgumentException("unable to determine language");
            }
        }

        ScriptEngineManager mgr = new ScriptEngineManager();
        final List engineFactories = mgr.getEngineFactories();
        if (engineFactories.isEmpty()){
            throw new RuntimeException("Could not find any engine factories");
        }

        Reader in;

        if (inFileName != null) {
            in = new FileReader(inFileName);
        } else {
            in = new InputStreamReader(System.in);
            inFileName = DEFAULT_IN_FILE_NAME;
        }

        try {
            ScriptEngine engine = mgr.getEngineByExtension(language);
            if (engine == null){
                throw new IllegalArgumentException("unable to find engine using Extension: "+language);
            }
            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put("args", args);
            Object obj = engine.eval(in);
            System.err.println("Result: " + obj);
        } catch (ScriptException e) {
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
        System.err.println();
        System.err.println(
            "    Additional args used only if -mode is " + "set to \"compile\":");
        System.err.println();
        System.err.println(
            "      [-out              className]   default: " + DEFAULT_CLASS_NAME);
    }
}