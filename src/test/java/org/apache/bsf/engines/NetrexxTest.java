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

package org.apache.bsf.engines;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFEngineTestTmpl;
import org.apache.bsf.BSFException;

/**
 * This is a testcase for NetRexx Script Engine
 *
 * @author Sanka Samaranayake <sanka@opensource.lk>
 * @author Nilupa Bandara     <nilupa@opensoruce.lk>
 */

public class NetrexxTest extends BSFEngineTestTmpl {
    private BSFEngine netrexxEngine;
    private final String lineSeparatorStr = System.getProperty("line.separator");

    public NetrexxTest(final String name) {
        super(name);
    }

    public void setUp() {
        super.setUp();

        try {
            netrexxEngine = bsfManager.loadScriptingEngine("netrexx");
        }
        catch (final BSFException bsfe) {
            fail(failMessage("fail while attempting to load netrexx", bsfe));
        }
    }

    public void tearDown() {
        super.tearDown();
    }

    public void testDeclareBean() {
        final Integer foo = new Integer(0);
        Integer bar = null;

        try {
            bsfManager.declareBean("foo", foo, Integer.class);
            bar = new Integer((netrexxEngine.eval("Test.nrx", 0, 0,
                                                  "foo.intValue() + 1")).toString());
        }
        catch (final Exception ex) {
            fail(failMessage("declaredBean() test failed", ex));
        }

        assertEquals(bar , new Integer(1));
    }

    public void testRegisterBean() {
        final Integer foo = new Integer(0);
        Integer bar = null;

        try {
            bsfManager.registerBean("foo", foo);
            bar = new Integer((netrexxEngine.eval("Test.nrx", 0, 0,
                                                  "bsf.lookupBean(\"foo\")").toString()));
        }
        catch (final Exception ex) {
            fail(failMessage("registerBean() test fail", ex));
        }

        assertEquals(bar, new Integer(0));
    }

    public void testExec() {
        try {
            netrexxEngine.exec("Test.nrx", 0, 0,
                               "say \"PASSED\"");
        }
        catch (final BSFException bsfe) {
            fail(failMessage("exec() test fail", bsfe));
        }

        assertEquals("PASSED"+lineSeparatorStr, getTmpOutStr());
    }

    public void testUndeclareBean() {
        // FIXME: Netrexx is a little chatty about the missing variable...
        final Integer foo = new Integer(0);
        Object  bar = null;
        
        try {
            bsfManager.declareBean("foo", foo, Integer.class);
            bsfManager.undeclareBean("foo");
            bar = netrexxEngine.eval("Test.nrx", 0, 0,
                                     "foo + 1");
        }
        catch (final BSFException bsfe) {
            // don't do anything .. this is the expected case
        }
        catch (final Exception ex) {
            fail(failMessage("undeclareBean() test failed", ex));
        }

        assertNull(bar);
    }

    public void testUnregisterBean(){
        final Integer foo = new Integer(0);
        Object retValue  = null;

        try {
            bsfManager.registerBean("foo", foo);
            bsfManager.unregisterBean("foo");
            retValue = netrexxEngine.eval("Test.nrx", 0, 0,
                                          "bsf.lookupBean(\"foo\")");
        }
        catch (final Exception ex) {
            fail(failMessage("unregisterBean() test fail", ex));
        }

        assertNull(retValue);
    }

    public void testBSFManagerAvailability(){
        Object retValue = null;

        try {
            retValue = bsfManager.eval("netrexx", "Test.nrx", 0, 0,
                                       "bsf.lookupBean(\"foo\")");
        }
        catch (final Exception ex) {
            fail(failMessage("BSFManagerAvailability() test failed", ex));
        }

        assertNull(retValue);
    }

    public void testBSFManagerEval(){
        Object retValue = null;

        try {
            retValue = new Integer((bsfManager.eval("netrexx", "Test.nrx", 0, 0,
                                                    "1 + (-1)")).toString());
        }
        catch (final Exception ex) {
            fail(failMessage("BSFManagerEval() test failed", ex));
        }

        assertEquals(retValue, new Integer(0));
    }
}
