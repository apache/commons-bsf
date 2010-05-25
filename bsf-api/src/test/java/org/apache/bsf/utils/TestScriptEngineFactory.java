/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.bsf.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

/**
 * Minimal ScriptEngineFactory for use in JUnit tests.
 */
public class TestScriptEngineFactory implements ScriptEngineFactory {


    public ScriptEngine getScriptEngine() {
        return new TestScriptEngine();
    }

    public String getEngineName() {
        return "TestScriptEngine";
    }
    public String getEngineVersion() {
        return "1.0";
    }
    public List getExtensions() {
        return Collections.unmodifiableList(Arrays.asList(new String[]{"tEst","teSt"}));
    }
    public String getLanguageName() {
        return "TestScript";
    }
    public String getLanguageVersion() {
        return "1.0";
    }
    public List getMimeTypes() {
        return Collections.unmodifiableList(Arrays.asList(new String[]{"application/junit"}));
    }

    public Object getParameter(String key) {
        if (key == ScriptEngine.ENGINE) {
            return getEngineName();
        } else if (key == ScriptEngine.ENGINE_VERSION) {
            return getEngineVersion();
        } else if (key == ScriptEngine.NAME) {
            return getNames();
        } else if (key == ScriptEngine.LANGUAGE) {
            return getLanguageName();
        } else if(key == ScriptEngine.ENGINE_VERSION) {
            return getLanguageVersion();
        } else if (key == "THREADING") {
            return "MULTITHREADED";
        }
        return null;
    }

    public String getMethodCallSyntax(String obj, String method, String[] args) {
        // TODO Auto-generated method stub
        return "callMethod";
    }

    public List getNames() {
        return Collections.unmodifiableList(Arrays.asList(new String[]{"JUnit"}));
    }

    public String getOutputStatement(String toDisplay) {
        // TODO Auto-generated method stub
        return "outputStatment";
    }

    public String getProgram(String[] statements) {
        // TODO Auto-generated method stub
        return "program";
    }
}
