/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.bsf.util;

import org.apache.bsf.BSFException;
import junit.framework.TestCase;

/**
 * This is a testcase for org.apache.bsf.util.EngineUtils
 */

public class EngineUtilsTest extends TestCase {

    Object result = null;

    public EngineUtilsTest(final String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testCallBeanMethod() {

        Object[] args = new Object[] { "MoreConfirmation" };
        final TestBean bean = new TestBean("TestBean");

        try {
            result = EngineUtils.callBeanMethod(bean, "getStringValue", null);
        } catch (final BSFException bsfe) {
            fail("createBean method failed" + bsfe);
        }

        assertEquals("TestBean", (String) result);

        try {
            EngineUtils.callBeanMethod(bean, "setValue", args);
        } catch (final BSFException bsfe) {
            fail("createBean method failed" + bsfe);
        }

        assertEquals("MoreConfirmation", bean.getStringValue());

        args = new Object[] { "aString", Integer.valueOf(1) };

        try {
            EngineUtils.callBeanMethod(bean, "setValue", args);
        } catch (final BSFException bsfe) {
            fail("createBean method failed" + bsfe);
        }

        assertEquals("aString", bean.getStringValue());
        assertEquals(Integer.valueOf(1), (Integer) bean.getNumericValue());

        // try to invoke a method which does not exist ...
        // should throw a BSFException
        try {
            result = EngineUtils.callBeanMethod(bean, "nonExistentMethod", args);
            fail();
        } catch (final BSFException bsfe) {
        }
    }

    public void testCreateBean() throws BSFException {

        final Object[] args = new Object[] { "test" };

        try {
            result = EngineUtils.createBean("org.apache.bsf.util.TestBean", args);
        } catch (final BSFException bsfe) {
            fail("createBean method failed" + bsfe);
        }

        assertNotNull(result);
        assertEquals("test", ((TestBean) result).getStringValue());

        // try to create a bean by passing a wrong string ...
        // should throw a BSFException
        try {
            EngineUtils.createBean("nonExsitentClass", null);
            fail();
        } catch (final BSFException bsfe) {
        }

    }

    public void testGetTypeSignatureString() {
        // test for a non primitive type
        final Integer int1 = Integer.valueOf(10);
        final Object obj = EngineUtils.getTypeSignatureString(int1.getClass());

        assertEquals("Ljava/lang/Integer;", (String) obj);
        assertEquals("I", EngineUtils.getTypeSignatureString(int.class));
    }
}
