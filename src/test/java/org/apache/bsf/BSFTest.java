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

import org.apache.bsf.engines.JavascriptTest;
import org.apache.bsf.engines.JythonTest;
import org.apache.bsf.engines.NetrexxTest_IGNORE;
import org.apache.bsf.util.EngineUtilsTest;
import org.apache.bsf.util.IOUtilsTest;
import org.apache.bsf.util.StringUtilsTest;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

/**
 * Primary test class and testing front end for BSF.
 */
public class BSFTest extends BSFEngineTestCase {
    public static String[] testNames;

    public BSFTest(final String name) {
        super(name);
    }

    public static void main(final String args[]) {
        final TestRunner runner = new TestRunner();
        final TestSuite suite = (TestSuite) suite();
        TestResult results;

        for (int i = 0; i < suite.testCount(); i++) {
            System.out.print(testNames[i]);
            results = runner.doRun(suite.testAt(i), false);
            System.out.println("Results: " + results.runCount() + " tests run, " + results.failureCount() + " failures, " + results.errorCount() + " errors.");
            System.out.print("\n----------------------------------------\n");
        }
    }

    public static Test suite() {
        /*
         * Please add testcases here as needed.
         */
        final TestSuite suite = new TestSuite();
        testNames = new String[7];

        suite.addTestSuite(BSFTest.class);
        testNames[0] = "BSFManager Base Tests";
        suite.addTestSuite(JavascriptTest.class);
        testNames[1] = "Rhino Engine Tests";
        suite.addTestSuite(JythonTest.class);
        testNames[2] = "Jython Engine Tests";
        suite.addTestSuite(NetrexxTest_IGNORE.class);
        testNames[3] = "NetRexx Engine Tests";
        suite.addTestSuite(StringUtilsTest.class);
        testNames[4] = "StringUtils Test";
        suite.addTestSuite(IOUtilsTest.class);
        testNames[5] = "IOUtils Test";
        suite.addTestSuite(EngineUtilsTest.class);
        testNames[6] = "EngineUtils Test";

        return suite;
    }

    public void setUp() {
        super.setUp();
        BSFManager.registerScriptingEngine("fakeEngine", FakeEngine.class.getName(), new String[] { "fakeEng", "fE" });
    }

    public void testRegisterEngine() {
        assertTrue(bsfManager.isLanguageRegistered("fakeEngine"));
    }

    public void testGetLangFromFileName() {
        try {
            assertEquals("fakeEngine", BSFManager.getLangFromFilename("Test.fE"));
        } catch (final Exception e) {
            fail(failMessage("getLangFromFilename() test failed", e));
        }
    }

    public void testExec() {
        try {
            bsfManager.exec("fakeEngine", "Test.fE", 0, 0, "Fake syntax");
        } catch (final Exception e) {
            fail(failMessage("exec() test failed", e));
        }

        assertEquals("PASSED", getTmpOutStr());
    }

    public void testEval() {
        Boolean retval = Boolean.FALSE;

        try {
            retval = (Boolean) bsfManager.eval("fakeEngine", "Test.fE", 0, 0, "Fake Syntax");
        } catch (final Exception e) {
            fail(failMessage("eval() test failed", e));
        }

        assertTrue(retval.booleanValue());
    }

    public void testIexec() {
        try {
            bsfManager.iexec("fakeEngine", "Test.fE", 0, 0, "Fake syntax");
        } catch (final Exception e) {
            fail(failMessage("iexec() test failed", e));
        }

        assertEquals("PASSED", getTmpOutStr());
    }

    public void testDeclareBean() {
        try {
            bsfManager.declareBean("foo", Integer.valueOf(1), Integer.class);
        } catch (final Exception e) {
            fail(failMessage("declareBean() test failed", e));
        }

        assertEquals(Integer.valueOf(1), (Integer) bsfManager.lookupBean("foo"));
    }

    public void testUndeclareBean() {
        try {
            bsfManager.declareBean("foo", Integer.valueOf(1), Integer.class);
            bsfManager.undeclareBean("foo");
        } catch (final Exception e) {
            fail(failMessage("undeclareBean() test failed", e));
        }

        assertNull(bsfManager.lookupBean("foo"));
    }

    public void testTerminate() throws Exception {
        try {
            bsfManager.loadScriptingEngine("fakeEngine");
            bsfManager.terminate();
        } catch (final Exception e) {
            fail(failMessage("terminate() test failed", e));
        }

        assertEquals("PASSED", getTmpOutStr());
    }
}
