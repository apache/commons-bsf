package org.apache.bsf.test.engineTests;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;
import org.apache.bsf.test.BSFEngineTestTmpl;

/**
 * Test class for the jython language engine.
 * @author   Victor J. Orlikowski <vjo@us.ibm.com>
 */
public class jythonTest extends BSFEngineTestTmpl {
    private BSFEngine jythonEngine;

    public jythonTest(String name) {
        super(name);
    }

    public void setUp() {
        super.setUp();
        
        try {
            jythonEngine = bsfManager.loadScriptingEngine("jython");
        }
        catch (Exception e) {
            fail(failMessage("Failure attempting to load jython", e));
        }
    }

    public void testExec() {
        try {
            jythonEngine.exec("Test.py", 0, 0,
                              "print \"PASSED\",");
        }
        catch (Exception e) {
            fail(failMessage("exec() test failed", e));
        }

        assertEquals("PASSED", getTmpOutStr());
    }

    public void testEval() {
        Integer retval = null;

        try {
            retval = new Integer((jythonEngine.eval("Test.py", 0, 0,
                                                    "1 + 1")).toString());
        }
        catch (Exception e) {
            fail(failMessage("eval() test failed", e));
        }

        assertEquals(new Integer(2), retval);
    }

    public void testCall() {
        Object[] args = { new Integer(1) };
        Integer retval = null;

        try {
            jythonEngine.exec("Test.py", 0, 0,
                              "def addOne(f):\n\t return f + 1\n");
            retval = 
                new Integer((jythonEngine.call(null, "addOne",
                                               args).toString()));
        }
        catch (Exception e) {
            fail(failMessage("call() test failed", e));
        }

        assertEquals(new Integer(2), retval);
    }

    public void testIexec() {
        // iexec() differs from exec() in this engine, primarily
        // in that it only executes up to the first newline.
        try {
            jythonEngine.iexec("Test.py", 0, 0,
                               "print \"PASSED\"," + "\n" + "print \"FAILED\",");
        }
        catch (Exception e) {
            fail(failMessage("iexec() test failed", e));
        }
        
        assertEquals("PASSED", getTmpOutStr());
    } 

    public void testBSFManagerEval() {
        Integer retval = null;

        try {
            retval = new Integer((bsfManager.eval("jython", "Test.py", 0, 0,
                                                  "1 + 1")).toString());
        }
        catch (Exception e) {
            fail(failMessage("BSFManager eval() test failed", e));
        }

        assertEquals(new Integer(2), retval);
    }

    public void testBSFManagerAvailability() {
        Object retval = null;

        try {
            retval = jythonEngine.eval("Test.py", 0, 0,
                                       "bsf.lookupBean(\"foo\")");
        }
        catch (Exception e) {
            fail(failMessage("Test of BSFManager availability failed", e));
        }

        assertEquals("None", retval.toString());
    }

    public void testRegisterBean() {
        Integer foo = new Integer(1);
        Integer bar = null;

        try {
            bsfManager.registerBean("foo", foo);
            bar = new Integer((jythonEngine.eval("Test.py", 0, 0,
                                                 "bsf.lookupBean(\"foo\")"))
                              .toString());
        }
        catch (Exception e) {
            fail(failMessage("registerBean() test failed", e));
        }

        assertEquals(foo, bar);
    }

    public void testUnregisterBean() {
        Integer foo = new Integer(1);
        Object bar = null;

        try {
            bsfManager.registerBean("foo", foo);
            bsfManager.unregisterBean("foo");
            bar = jythonEngine.eval("Test.py", 0, 0,
                                    "bsf.lookupBean(\"foo\")");
        }
        catch (Exception e) {
            fail(failMessage("unregisterBean() test failed", e));
        }

        assertEquals("None", bar.toString());
    }

    public void testDeclareBean() {
        Integer foo = new Integer(1);
        Integer bar = null;

        try {
            bsfManager.declareBean("foo", foo, Integer.class);
            bar = new Integer((jythonEngine.eval("Test.py", 0, 0,
                                                 "foo + 1")).toString());
        }
        catch (Exception e) {
            fail(failMessage("declareBean() test failed", e));
        }

        assertEquals(new Integer(2), bar);
    }

    public void testUndeclareBean() {
        Integer foo = new Integer(1);
        Integer bar = null;

        try {
            bsfManager.declareBean("foo", foo, Integer.class);
            bsfManager.undeclareBean("foo");
            bar = new Integer((jythonEngine.eval("Test.py", 0, 0,
                                                 "foo + 1")).toString());
        }
        catch (BSFException bsfE) {
            // Do nothing. This is the expected case.
        }
        catch (Exception e) {
            fail(failMessage("undeclareBean() test failed", e));
        }

        assertNull(bar);
    }
}
