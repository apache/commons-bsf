package org.apache.bsf.test;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import junit.textui.TestRunner;

import org.apache.bsf.BSFManager;

import org.apache.bsf.test.engineTests.*;

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
        testNames = new String [4];

        suite.addTestSuite(BSFTest.class);
        testNames[0] = "BSFManager Base Tests";
        suite.addTestSuite(jaclTest.class);
        testNames[1] = "Jacl Engine Tests";
        suite.addTestSuite(javascriptTest.class);
        testNames[2] = "Rhino Engine Tests";
        suite.addTestSuite(jythonTest.class);
        testNames[3] = "Jython Engine Tests";
        
        return suite;
    }

    public void setUp() {
        super.setUp();
        BSFManager.registerScriptingEngine("fakeEngine", 
                                           fakeEngine.class.getName(), 
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
