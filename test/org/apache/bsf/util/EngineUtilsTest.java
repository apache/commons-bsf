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

package org.apache.bsf.util;

import org.apache.bsf.BSFException;
import org.apache.bsf.util.EngineUtils;

import junit.framework.TestCase;

/**
 *
 * This is a testcase for org.apache.bsf.util.EngineUtils
 *
 * @author Nandika Jayawardana <jayawark@yahoo.com>
 *
 */

public class EngineUtilsTest extends TestCase {

    Object result=null;

    public EngineUtilsTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testCallBeanMethod() {

        Object[] args = new Object[]{new String("MoreConfirmation")};
        TestBean bean = new TestBean("TestBean");

        try {
            result = EngineUtils.callBeanMethod(bean, "getStringValue", null);
        }
        catch (BSFException bsfe) {
            fail("createBean method failed"+bsfe);
        }

        assertEquals("TestBean",(String)result);

        try {
            EngineUtils.callBeanMethod(bean,"setValue",args);
        }
        catch (BSFException bsfe) {
            fail("createBean method failed"+bsfe);
        }

        assertEquals("MoreConfirmation",bean.getStringValue());

        args = new Object[]{new String("aString"),new Integer(1)};

        try {
            EngineUtils.callBeanMethod(bean,"setValue",args);
        }
        catch (BSFException bsfe) {
            fail("createBean method failed"+bsfe);
        }

        assertEquals("aString",bean.getStringValue());
        assertEquals(new Integer(1),(Integer)bean.getNumericValue());

        // try to invoke a method which does not exist ...
        // should throw a BSFException
            try {
                result= EngineUtils.callBeanMethod(bean, "nonExistentMethod",
                                                   args);
                fail();
            }
            catch (BSFException bsfe) {
            }
    }

    public void testCreateBean() throws BSFException {

        Object args[] = new Object[]{ new String("test") };

        try {
            result = EngineUtils.createBean("org.apache.bsf.util.TestBean", args);
        }
        catch (BSFException bsfe) {
            fail("createBean method failed"+bsfe);
        }

        assertNotNull(result);
        assertEquals("test",((TestBean)result).getStringValue());

        // try to create a bean by passing a wrong string ...
        // should throw a BSFException
            try {
                Object obj1 = EngineUtils.createBean("nonExsitentClass",null);
                fail();
            }
            catch (BSFException bsfe) {
            }

    }

    public void testGetTypeSignatureString() {
        //test for a non primitive type
        Integer int1 = new Integer(10);
        Object obj = EngineUtils.getTypeSignatureString(int1.getClass());

        assertEquals("Ljava/lang/Integer;",(String)obj);
        assertEquals("I",
                     (String)(EngineUtils.getTypeSignatureString(int.class)));
    }
}
