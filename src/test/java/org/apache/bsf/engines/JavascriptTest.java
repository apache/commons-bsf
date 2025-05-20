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
 * Test class for the Rhino language engine.
 */
public class JavascriptTest extends BSFEngineTestCase {
    protected BSFEngine engine;

    public JavascriptTest(final String name) {
        super(name);
    }

    protected BSFEngine createEngine() throws Exception{
        return bsfManager.loadScriptingEngine("javascript");
    }

    public void setUp() {
        super.setUp();
        try {
            engine = createEngine();
        } catch (final Exception e) {
            fail(failMessage("Failure attempting to load javascript engine", e));
        }
    }

    public void testExec() {
        try {
            engine.exec("Test.js", 0, 0, "java.lang.System.out.print " + "(\"PASSED\");");
        } catch (final Exception e) {
            fail(failMessage("exec() test failed", e));
        }
        assertEquals("PASSED", getTmpOutStr());
    }

    public void testEval() {
        Double retval = null;
        try {
            retval = Double.valueOf((engine.eval("Test.js", 0, 0, "1 + 1").toString()));
        } catch (final Exception e) {
            fail(failMessage("eval() test failed", e));
        }
        assertEquals(2.0, retval);
    }

    public void testCall() {
        final Object[] args = {1.0};
        Double retval = null;
        try {
            engine.exec("Test.js", 0, 0, "function addOne (f) {\n return f + 1;\n}");
            retval = Double.valueOf((engine.call(null, "addOne", args).toString()));
        } catch (final Exception e) {
            fail(failMessage("call() test failed", e));
        }
        assertEquals(2.0, retval);
    }

    public void testIexec() {
        try {
            engine.iexec("Test.js", 0, 0, "java.lang.System.out.print " + "(\"PASSED\")");
        } catch (final Exception e) {
            fail(failMessage("iexec() test failed", e));
        }
        assertEquals("PASSED", getTmpOutStr());
    }

    public void testBSFManagerEval() {
        Double retval = null;
        try {
            retval = Double.valueOf((bsfManager.eval("javascript", "Test.js", 0, 0, "1 + 1")).toString());
        } catch (final Exception e) {
            fail(failMessage("BSFManager eval() test failed", e));
        }
        assertEquals(2.0, retval);
    }

    public void testBSFManagerAvailability() {
        Object retval = null;
        try {
            retval = engine.eval("Test.js", 0, 0, "bsf.lookupBean(\"foo\")");
        } catch (final Exception e) {
            fail(failMessage("Test of BSFManager availability failed", e));
        }
        assertNull(retval);
    }

    public void testRegisterBean() {
        final Double foo = 1.0;
        Double bar = null;
        try {
            bsfManager.registerBean("foo", foo);
            bar = (Double) engine.eval("Test.js", 0, 0, "bsf.lookupBean(\"foo\")");
        } catch (final Exception e) {
            fail(failMessage("registerBean() test failed", e));
        }
        assertEquals(foo, bar);
    }

    public void testUnregisterBean() {
        final Double foo = 1.0;
        Double bar = null;
        try {
            bsfManager.registerBean("foo", foo);
            bsfManager.unregisterBean("foo");
            bar = (Double) engine.eval("Test.js", 0, 0, "bsf.lookupBean(\"foo\")");
        } catch (final Exception e) {
            fail(failMessage("unregisterBean() test failed", e));
        }
        assertNull(bar);
    }

    public void testDeclareBean() {
        final Double foo = 1.0;
        Double bar = null;
        try {
            bsfManager.declareBean("foo", foo, Double.class);
            bar = (Double) engine.eval("Test.js", 0, 0, "foo + 1");
        } catch (final Exception e) {
            fail(failMessage("declareBean() test failed", e));
        }
        assertEquals(2.0, bar);
    }

    public void testUndeclareBean() {
        final Double foo = 1.0;
        Double bar = null;
        try {
            bsfManager.declareBean("foo", foo, Double.class);
            bsfManager.undeclareBean("foo");
            bar = (Double) engine.eval("Test.js", 0, 0, "foo + 1");
        } catch (final BSFException bsfE) {
            // Do nothing. This is the expected case.
        } catch (final Exception e) {
            fail(failMessage("undeclareBean() test failed", e));
        }
        assertNull(bar);
    }
}
