/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
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

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.bsf.BSFManager;

import org.apache.bsf.engines.JaclTest;
import org.apache.bsf.engines.JavascriptTest;
import org.apache.bsf.engines.JythonTest;
import org.apache.bsf.engines.NetrexxTest;
import org.apache.bsf.test.engineTests.*;
import org.apache.bsf.test.utilTests.*;
import org.apache.bsf.util.EngineUtilsTest;
import org.apache.bsf.util.IOUtilsTest;
import org.apache.bsf.util.StringUtilsTest;

/**
 * Primary test class and testing front end for BSF.
 * @author   Victor J. Orlikowski <vjo@us.ibm.com>
 */
public class BSFTest extends BSFEngineTestTmpl {
    public static String[] testNames;
    
    public BSFTest(String name) {
        super(name);
    }

    public static void main(String args[]) {
        TestRunner runner = new TestRunner();
        TestSuite suite = (TestSuite) suite();
        TestResult results;

        for (int i = 0; i < suite.testCount(); i++) {
            System.out.print(testNames[i]);
            results = runner.doRun(suite.testAt(i), false);
            System.out.println("Results: " + results.runCount() + 
                               " tests run, " + results.failureCount() +
                               " failures, " + results.errorCount() +
                               " errors.");
            System.out.print("\n----------------------------------------\n");
        }
    }

    public static Test suite() { 
        /*
         * Please add testcases here as needed.
         */
        TestSuite suite = new TestSuite(); 
        testNames = new String [8];

        suite.addTestSuite(BSFTest.class);
        testNames[0] = "BSFManager Base Tests";
        suite.addTestSuite(JaclTest.class);
        testNames[1] = "Jacl Engine Tests";
        suite.addTestSuite(JavascriptTest.class);
        testNames[2] = "Rhino Engine Tests";
        suite.addTestSuite(JythonTest.class);
        testNames[3] = "Jython Engine Tests";
        suite.addTestSuite(NetrexxTest.class);
        testNames[4] = "NetRexx Engine Tests";
        suite.addTestSuite(StringUtilsTest.class);
        testNames[5] = "StringUtils Test";
        suite.addTestSuite(IOUtilsTest.class);
        testNames[6] = "IOUtils Test";
        suite.addTestSuite(EngineUtilsTest.class);
        testNames[7] = "EngineUtils Test";
        
        return suite;
    }

    public void setUp() {
        super.setUp();
        BSFManager.registerScriptingEngine("fakeEngine", 
                                           FakeEngine.class.getName(), 
                                           new String[] { "fakeEng", "fE" });
    }

    public void testRegisterEngine() {
        assertTrue(bsfManager.isLanguageRegistered("fakeEngine"));
    }

    public void testGetLangFromFileName() {
        try {
            assertEquals("fakeEngine", 
                         BSFManager.getLangFromFilename("Test.fE"));
        }
        catch (Exception e) {
            fail(failMessage("getLangFromFilename() test failed", e));
        }
    }

    public void testExec() {
        try {
            bsfManager.exec("fakeEngine", "Test.fE", 0, 0, "Fake syntax");
        }
        catch (Exception e) {
            fail(failMessage("exec() test failed", e));
        }

        assertEquals("PASSED", getTmpOutStr());
    }

    public void testEval() {
        Boolean retval = Boolean.FALSE;

        try {
            retval = (Boolean) bsfManager.eval("fakeEngine", 
                                               "Test.fE", 0, 0,
                                               "Fake Syntax");
        }
        catch (Exception e) {
            fail(failMessage("eval() test failed", e));
        }
        
        assertTrue(retval.booleanValue());
    }

    public void testIexec() {
        try {
            bsfManager.iexec("fakeEngine", "Test.fE", 0, 0, "Fake syntax");
        }
        catch (Exception e) {
            fail(failMessage("iexec() test failed", e));
        }

        assertEquals("PASSED", getTmpOutStr());
    }

    public void testDeclareBean() {
        try {
            bsfManager.declareBean("foo", new Integer(1), Integer.class);
        }
        catch (Exception e) {
            fail(failMessage("declareBean() test failed", e));
        }

        assertEquals(new Integer(1), (Integer) bsfManager.lookupBean("foo"));
    }

    public void testUndeclareBean() {
        try {
            bsfManager.declareBean("foo", new Integer(1), Integer.class);
            bsfManager.undeclareBean("foo");
        }
        catch (Exception e) {
            fail(failMessage("undeclareBean() test failed", e));
        }

        assertNull(bsfManager.lookupBean("foo"));
    }

    public void testTerminate() throws Exception {
        try {
            bsfManager.loadScriptingEngine("fakeEngine");
            bsfManager.terminate();
        }
        catch (Exception e) {
            fail(failMessage("terminate() test failed", e));
        }

        assertEquals("PASSED", getTmpOutStr());
    }
}
