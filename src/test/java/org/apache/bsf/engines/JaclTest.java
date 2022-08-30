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

package org.apache.bsf.engines;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFEngineTestTmpl;
import org.apache.bsf.BSFException;

/**
 * Test class for the jacl language engine.
 */
public class JaclTest extends BSFEngineTestTmpl {
    private BSFEngine jaclEngine;

    public JaclTest(final String name) {
        super(name);
    }

    public void setUp() {
        super.setUp();

        try {
            jaclEngine = bsfManager.loadScriptingEngine("jacl");
        }
        catch (final Exception e) {
            fail(failMessage("Failure attempting to load jacl", e));
        }
    }

    public void testExec() {
        try {
            jaclEngine.exec("Test.jacl", 0, 0,
                            "puts -nonewline \"PASSED\"");
        }
        catch (final Exception e) {
            fail(failMessage("exec() test failed", e));
        }

        assertEquals("PASSED", getTmpOutStr());
    }

    public void testEval() {
        Integer retval = null;

        try {
            retval =  (Integer) jaclEngine.eval("Test.jacl", 0, 0,
                                                "expr 1 + 1");
        }
        catch (final Exception e) {
            fail(failMessage("eval() test failed", e));
        }

        assertEquals(new Integer(2), retval);
    }

    public void testCall() {
        final Object[] args = { new Integer(1) };
        Integer retval = null;

        try {
            jaclEngine.exec("Test.jacl", 0, 0,
                            "proc addOne {f} {\n return [expr $f + 1]\n}");
            retval = (Integer) jaclEngine.call(null, "addOne", args);
        }
        catch (final Exception e) {
            fail(failMessage("call() test failed", e));
        }

        assertEquals(new Integer(2), retval);
    }

    public void testIexec() {
        try {
            jaclEngine.iexec("Test.jacl", 0, 0,
                             "puts -nonewline \"PASSED\"");
        }
        catch (final Exception e) {
            fail(failMessage("iexec() test failed", e));
        }

        assertEquals("PASSED", getTmpOutStr());
    }

    public void testBSFManagerEval() {
        Integer retval = null;

        try {
            retval = (Integer) bsfManager.eval("jacl", "Test.jacl", 0, 0,
                                               "expr 1 + 1");
        }
        catch (final Exception e) {
            fail(failMessage("BSFManager eval() test failed", e));
        }

        assertEquals(new Integer(2), retval);
    }

    public void testRegisterBean() {
        final Integer foo = new Integer(1);
        Integer bar = null;

        try {
            bsfManager.registerBean("foo", foo);
            bar = (Integer) jaclEngine.eval("Test.jacl", 0, 0,
                                            "bsf lookupBean \"foo\"");
        }
        catch (final Exception e) {
            fail(failMessage("registerBean() test failed", e));
        }

        assertEquals(foo, bar);
    }

    public void testUnregisterBean() {
        final Integer foo = new Integer(1);
        Integer bar = null;

        try {
            bsfManager.registerBean("foo", foo);
            bsfManager.unregisterBean("foo");
            bar = (Integer) jaclEngine.eval("Test.jacl", 0, 0,
                                            "bsf lookupBean \"foo\"");
        }
        catch (final BSFException bsfE) {
            // Do nothing. This is the expected case.
        }
        catch (final Exception e) {
            fail(failMessage("unregisterBean() test failed", e));
        }

        assertNull(bar);
    }

    public void testDeclareBean() {
        final Integer foo = new Integer(1);
        Integer bar = null;

        try {
            bsfManager.declareBean("foo", foo, Integer.class);
            bar = (Integer)
                jaclEngine.eval("Test.jacl", 0, 0,
                                "proc ret {} {\n upvar 1 foo lfoo\n " +
                                "return $lfoo\n }\n ret");
        }
        catch (final Exception e) {
            fail(failMessage("declareBean() test failed", e));
        }

        assertEquals(foo, bar);
    }

    public void testUndeclareBean() {
        final Integer foo = new Integer(1);
        Integer bar = null;

        try {
            bsfManager.declareBean("foo", foo, Integer.class);
            bsfManager.undeclareBean("foo");
            bar = (Integer)
                jaclEngine.eval("Test.jacl", 0, 0,
                                "expr $foo + 1");
        }
        catch (final Exception e) {
            fail(failMessage("undeclareBean() test failed", e));
        }

        assertEquals(foo, bar);
    }
}
