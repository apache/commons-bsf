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
