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
 * This is a testcase for NetRexx Script Engine
 */

//@Ignore("How do we tell Maven to depend on Netrexx")
public class NetrexxTest_IGNORE extends BSFEngineTestCase {
    private BSFEngine netrexxEngine;
    private final String lineSeparatorStr = System.getProperty("line.separator");

    public NetrexxTest_IGNORE(final String name) {
        super(name);
    }

    public void ignore_setUp() {
        super.setUp();

        try {
            netrexxEngine = bsfManager.loadScriptingEngine("netrexx");
        } catch (final BSFException bsfe) {
            fail(failMessage("fail while attempting to load netrexx", bsfe));
        }
    }

    public void ignore_tearDown() {
        super.tearDown();
    }

    public void ignore_testDeclareBean() {
        final Integer foo = Integer.valueOf(0);
        Integer bar = null;

        try {
            bsfManager.declareBean("foo", foo, Integer.class);
            bar = Integer.valueOf((netrexxEngine.eval("Test.nrx", 0, 0, "foo.intValue() + 1")).toString());
        } catch (final Exception ex) {
            fail(failMessage("declaredBean() test failed", ex));
        }

        assertEquals(bar, Integer.valueOf(1));
    }

    public void ignore_testRegisterBean() {
        final Integer foo = Integer.valueOf(0);
        Integer bar = null;

        try {
            bsfManager.registerBean("foo", foo);
            bar = Integer.valueOf((netrexxEngine.eval("Test.nrx", 0, 0, "bsf.lookupBean(\"foo\")").toString()));
        } catch (final Exception ex) {
            fail(failMessage("registerBean() test fail", ex));
        }

        assertEquals(bar, Integer.valueOf(0));
    }

    public void ignore_testExec() {
        try {
            netrexxEngine.exec("Test.nrx", 0, 0, "say \"PASSED\"");
        } catch (final BSFException bsfe) {
            fail(failMessage("exec() test fail", bsfe));
        }

        assertEquals("PASSED" + lineSeparatorStr, getTmpOutStr());
    }

    public void ignore_testUndeclareBean() {
        // FIXME: Netrexx is a little chatty about the missing variable...
        final Integer foo = Integer.valueOf(0);
        Object bar = null;

        try {
            bsfManager.declareBean("foo", foo, Integer.class);
            bsfManager.undeclareBean("foo");
            bar = netrexxEngine.eval("Test.nrx", 0, 0, "foo + 1");
        } catch (final BSFException bsfe) {
            // don't do anything .. this is the expected case
        } catch (final Exception ex) {
            fail(failMessage("undeclareBean() test failed", ex));
        }

        assertNull(bar);
    }

    public void ignore_testUnregisterBean() {
        final Integer foo = Integer.valueOf(0);
        Object retValue = null;

        try {
            bsfManager.registerBean("foo", foo);
            bsfManager.unregisterBean("foo");
            retValue = netrexxEngine.eval("Test.nrx", 0, 0, "bsf.lookupBean(\"foo\")");
        } catch (final Exception ex) {
            fail(failMessage("unregisterBean() test fail", ex));
        }

        assertNull(retValue);
    }

    public void ignore_testBSFManagerAvailability() {
        Object retValue = null;

        try {
            retValue = bsfManager.eval("netrexx", "Test.nrx", 0, 0, "bsf.lookupBean(\"foo\")");
        } catch (final Exception ex) {
            fail(failMessage("BSFManagerAvailability() test failed", ex));
        }

        assertNull(retValue);
    }

    public void ignore_testBSFManagerEval() {
        Object retValue = null;

        try {
            retValue = Integer.valueOf((bsfManager.eval("netrexx", "Test.nrx", 0, 0, "1 + (-1)")).toString());
        } catch (final Exception ex) {
            fail(failMessage("BSFManagerEval() test failed", ex));
        }

        assertEquals(retValue, Integer.valueOf(0));
    }

    public void testAllIgnoredUntilNetrexxIsSetUp() {
        // empty
    }
}
