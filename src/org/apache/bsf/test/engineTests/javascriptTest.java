package org.apache.bsf.test.engineTests;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;
import org.apache.bsf.test.BSFEngineTestTmpl;

/**
 * Test class for the Rhino language engine.
 * @author   Victor J. Orlikowski <vjo@us.ibm.com>
 */
public class javascriptTest extends BSFEngineTestTmpl {
    private BSFEngine javascriptEngine;

    public javascriptTest(String name) {
        super(name);
    }

    public void setUp() {
        super.setUp();

        try {
            javascriptEngine = bsfManager.loadScriptingEngine("javascript");
        }
        catch (Exception e) {
            fail(failMessage("Failure attempting to load Rhino", e));
        }
    }

    public void testExec() {
        try {
            javascriptEngine.exec("Test.js", 0, 0,
                                  "java.lang.System.out.print " + 
                                  "(\"PASSED\");");
        }
        catch (Exception e) {
            fail(failMessage("exec() test failed", e));
        }

        assertEquals("PASSED", getTmpOutStr());
    }
    
    public void testEval() {
        Double retval = null;

        try {
            retval = new Double((javascriptEngine.eval("Test.js", 0, 0,
                                                       "1 + 1;").toString()));
        }
        catch (Exception e) {
            fail(failMessage("eval() test failed", e));
        }

        assertEquals(new Double(2), retval);
    }

    public void testCall() {
        Object[] args = { new Double(1) };
        Double retval = null;

        try {
            javascriptEngine.exec("Test.js", 0, 0,
                                  "function addOne (f) {\n return f + 1;\n}");
            retval = 
                new Double((javascriptEngine.call(null, "addOne",
                                                  args).toString()));
        }
        catch (Exception e) {
            fail(failMessage("call() test failed", e));
        }

        assertEquals(new Double(2), retval);
    }

    public void testIexec() {
        try {
            javascriptEngine.iexec("Test.js", 0, 0,
                                   "java.lang.System.out.print " + 
                                   "(\"PASSED\");");
        }
        catch (Exception e) {
            fail(failMessage("iexec() test failed", e));
        }

        assertEquals("PASSED", getTmpOutStr());
    }

    public void testBSFManagerEval() {
        Double retval = null;

        try {
            retval = new Double((bsfManager.eval("javascript", "Test.js", 0,
                                                 0, "1 + 1;")).toString());
        }
        catch (Exception e) {
            fail(failMessage("BSFManager eval() test failed", e));
        }

        assertEquals(new Double(2), retval);
    }
    
    public void testBSFManagerAvailability() {
        Object retval = null;

        try {
            retval = javascriptEngine.eval("Test.js", 0, 0,
                                           "bsf.lookupBean(\"foo\");");
        }
        catch (Exception e) {
            fail(failMessage("Test of BSFManager availability failed", e));
        }

        assertNull(retval);
    }

    public void testRegisterBean() {
        Double foo = new Double(1);
        Double bar = null;

        try {
            bsfManager.registerBean("foo", foo);
            bar = (Double)
                javascriptEngine.eval("Test.js", 0, 0,
                                      "bsf.lookupBean(\"foo\");");
        }
        catch (Exception e) {
            fail(failMessage("registerBean() test failed", e));
        }

        assertEquals(foo, bar);
    }

    public void testUnregisterBean() {
        Double foo = new Double(1);
        Double bar = null;

        try {
            bsfManager.registerBean("foo", foo);
            bsfManager.unregisterBean("foo");
            bar = (Double) 
                javascriptEngine.eval("Test.js", 0, 0,
                                      "bsf.lookupBean(\"foo\");");
        }
        catch (Exception e) {
            fail(failMessage("unregisterBean() test failed", e));
        }

        assertNull(bar);
    }
    
    public void testDeclareBean() {
        Double foo = new Double(1);
        Double bar = null;

        try {
            bsfManager.declareBean("foo", foo, Double.class);
            bar = (Double) javascriptEngine.eval("Test.js", 0, 0, "foo + 1;");
        }
        catch (Exception e) {
            fail(failMessage("declareBean() test failed", e));
        }

        assertEquals(new Double(2), bar);
    }

    public void testUndeclareBean() {
        Double foo = new Double(1);
        Double bar = null;

        try {
            bsfManager.declareBean("foo", foo, Double.class);
            bsfManager.undeclareBean("foo");
            bar = (Double) javascriptEngine.eval("Test.js", 0, 0,
                                                 "foo + 1");
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
