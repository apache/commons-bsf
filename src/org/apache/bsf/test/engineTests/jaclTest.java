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
