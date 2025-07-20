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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.bsf.engines;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFEngineTestCase;
import org.apache.bsf.BSFException;

/**
 * Test class for the jython language engine.
 */
public class JythonTest extends BSFEngineTestCase {
    private BSFEngine jythonEngine;

    public JythonTest(final String name) {
        super(name);
    }

    public void setUp() {
        super.setUp();

        try {
            jythonEngine = bsfManager.loadScriptingEngine("jython");
        } catch (final Exception e) {
            fail(failMessage("Failure attempting to load jython", e));
        }
    }

    public void testExec() {
        try {
            jythonEngine.exec("Test.py", 0, 0, "print \"PASSED\",");
        } catch (final Exception e) {
            fail(failMessage("exec() test failed", e));
        }

        assertEquals("PASSED", getTmpOutStr());
    }

    public void testEval() {
        Integer retval = null;

        try {
            retval = Integer.valueOf((jythonEngine.eval("Test.py", 0, 0, "1 + 1")).toString());
        } catch (final Exception e) {
            fail(failMessage("eval() test failed", e));
        }

        assertEquals(Integer.valueOf(2), retval);
    }

    public void testCall() {
        final Object[] args = { Integer.valueOf(1) };
        Integer retval = null;

        try {
            jythonEngine.exec("Test.py", 0, 0, "def addOne(f):\n\t return f + 1\n");
            retval = Integer.valueOf((jythonEngine.call(null, "addOne", args).toString()));
        } catch (final Exception e) {
            fail(failMessage("call() test failed", e));
        }

        assertEquals(Integer.valueOf(2), retval);
    }

    public void testIexec() {
        // iexec() differs from exec() in this engine, primarily
        // in that it only executes up to the first newline.
        try {
            jythonEngine.iexec("Test.py", 0, 0, "print \"PASSED\",\nprint \"FAILED\",");
        } catch (final Exception e) {
            fail(failMessage("iexec() test failed", e));
        }

        assertEquals("PASSED", getTmpOutStr());
    }

    public void testBSFManagerEval() {
        Integer retval = null;

        try {
            retval = Integer.valueOf((bsfManager.eval("jython", "Test.py", 0, 0, "1 + 1")).toString());
        } catch (final Exception e) {
            fail(failMessage("BSFManager eval() test failed", e));
        }

        assertEquals(Integer.valueOf(2), retval);
    }

    public void testBSFManagerAvailability() {
        Object retval = null;

        try {
            retval = jythonEngine.eval("Test.py", 0, 0, "bsf.lookupBean(\"foo\")");
        } catch (final Exception e) {
            fail(failMessage("Test of BSFManager availability failed", e));
        }

        assertEquals("None", retval.toString());
    }

    public void testRegisterBean() {
        final Integer foo = Integer.valueOf(1);
        Integer bar = null;

        try {
            bsfManager.registerBean("foo", foo);
            bar = Integer.valueOf((jythonEngine.eval("Test.py", 0, 0, "bsf.lookupBean(\"foo\")")).toString());
        } catch (final Exception e) {
            fail(failMessage("registerBean() test failed", e));
        }

        assertEquals(foo, bar);
    }

    public void testUnregisterBean() {
        final Integer foo = Integer.valueOf(1);
        Object bar = null;

        try {
            bsfManager.registerBean("foo", foo);
            bsfManager.unregisterBean("foo");
            bar = jythonEngine.eval("Test.py", 0, 0, "bsf.lookupBean(\"foo\")");
        } catch (final Exception e) {
            fail(failMessage("unregisterBean() test failed", e));
        }

        assertEquals("None", bar.toString());
    }

    public void testDeclareBean() {
        final Integer foo = Integer.valueOf(1);
        Integer bar = null;

        try {
            bsfManager.declareBean("foo", foo, Integer.class);
            bar = Integer.valueOf((jythonEngine.eval("Test.py", 0, 0, "foo + 1")).toString());
        } catch (final Exception e) {
            fail(failMessage("declareBean() test failed", e));
        }

        assertEquals(Integer.valueOf(2), bar);
    }

    public void testUndeclareBean() {
        final Integer foo = Integer.valueOf(1);
        Integer bar = null;

        try {
            bsfManager.declareBean("foo", foo, Integer.class);
            bsfManager.undeclareBean("foo");
            bar = Integer.valueOf((jythonEngine.eval("Test.py", 0, 0, "foo + 1")).toString());
        } catch (final BSFException bsfE) {
            // Do nothing. This is the expected case.
        } catch (final Exception e) {
            fail(failMessage("undeclareBean() test failed", e));
        }

        assertNull(bar);
    }
}
