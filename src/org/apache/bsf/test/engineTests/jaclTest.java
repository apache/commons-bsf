package org.apache.bsf.test.engineTests;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;
import org.apache.bsf.test.BSFEngineTestTmpl;

/**
 * Test class for the jacl language engine.
 * @author   Victor J. Orlikowski <vjo@us.ibm.com>
 */
public class jaclTest extends BSFEngineTestTmpl {
    private BSFEngine jaclEngine;

    public jaclTest(String name) {
        super(name);
    }

    public void setUp() {
        super.setUp();

        try {
            jaclEngine = bsfManager.loadScriptingEngine("jacl");
        }
        catch (Exception e) {
            fail(failMessage("Failure attempting to load jacl", e));
        }
    }

    public void testExec() {
        try {
            jaclEngine.exec("Test.jacl", 0, 0,
                            "puts -nonewline \"PASSED\"");
        }
        catch (Exception e) {
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
        catch (Exception e) {
            fail(failMessage("eval() test failed", e));
        }

        assertEquals(new Integer(2), retval);
    }

    public void testCall() {
        Object[] args = { new Integer(1) };
        Integer retval = null;

        try {
            jaclEngine.exec("Test.jacl", 0, 0,
                            "proc addOne {f} {\n return [expr $f + 1]\n}");
            retval = (Integer) jaclEngine.call(null, "addOne", args);
        }
        catch (Exception e) {
            fail(failMessage("call() test failed", e));
        }

        assertEquals(new Integer(2), retval);
    }

    public void testIexec() {
        try {
            jaclEngine.iexec("Test.jacl", 0, 0,
                             "puts -nonewline \"PASSED\"");
        }
        catch (Exception e) {
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
        catch (Exception e) {
            fail(failMessage("BSFManager eval() test failed", e));
        }

        assertEquals(new Integer(2), retval);
    }

    public void testRegisterBean() {
        Integer foo = new Integer(1);
        Integer bar = null;

        try {
            bsfManager.registerBean("foo", foo);
            bar = (Integer) jaclEngine.eval("Test.jacl", 0, 0,
                                            "bsf lookupBean \"foo\"");
        }
        catch (Exception e) {
            fail(failMessage("registerBean() test failed", e));
        }

        assertEquals(foo, bar);
    }

    public void testUnregisterBean() {
        Integer foo = new Integer(1);
        Integer bar = null;

        try {
            bsfManager.registerBean("foo", foo);
            bsfManager.unregisterBean("foo");
            bar = (Integer) jaclEngine.eval("Test.jacl", 0, 0,
                                            "bsf lookupBean \"foo\"");
        }
        catch (BSFException bsfE) {
            // Do nothing. This is the expected case.
        }
        catch (Exception e) {
            fail(failMessage("unregisterBean() test failed", e));
        }

        assertNull(bar);
    }
    
    public void testDeclareBean() {
        Integer foo = new Integer(1);
        Integer bar = null;

        try {
            bsfManager.declareBean("foo", foo, Integer.class);
            bar = (Integer)
                jaclEngine.eval("Test.jacl", 0, 0,
                                "proc ret {} {\n upvar 1 foo lfoo\n " +
                                "return $lfoo\n }\n ret");
        }
        catch (Exception e) {
            fail(failMessage("declareBean() test failed", e));
        }

        assertEquals(foo, bar);
    }

    public void testUndeclareBean() {
        Integer foo = new Integer(1);
        Integer bar = null;

        try {
            bsfManager.declareBean("foo", foo, Integer.class);
            bsfManager.undeclareBean("foo");
            bar = (Integer)
                jaclEngine.eval("Test.jacl", 0, 0,
                                "expr $foo + 1");
        }
        catch (Exception e) {
            fail(failMessage("undeclareBean() test failed", e));
        }

        assertEquals(foo, bar);
    }
}
