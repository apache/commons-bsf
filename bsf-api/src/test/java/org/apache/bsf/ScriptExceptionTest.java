/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.apache.bsf;

import javax.script.ScriptException;

import junit.framework.TestCase;

public class ScriptExceptionTest extends TestCase {

    public void testException1(){
        ScriptException ex = new ScriptException("");
        try {
            throw ex;
        } catch (ScriptException e) {
            assertEquals(-1, e.getLineNumber());
            assertEquals(-1, e.getColumnNumber());
            assertNull(e.getFileName()); // this is not defined by the spec. or the Java 6 API
        }
    }

    public void testException2(){
        ScriptException ex = new ScriptException(new Exception());
        try {
            throw ex;
        } catch (ScriptException e) {
            assertEquals(-1, e.getLineNumber());
            assertEquals(-1, e.getColumnNumber());
            assertNull(e.getFileName()); // this is not defined by the spec. or the Java 6 API
        }
    }

    public void testException3(){
        final String fileName = "file";
        ScriptException ex = new ScriptException("test", fileName, 10);
        try {
            throw ex;
        } catch (ScriptException e) {
            assertEquals(10, e.getLineNumber());
            assertEquals(-1, e.getColumnNumber());
            assertEquals(fileName, e.getFileName());
            final String message = e.getMessage();
            assertFalse(-1 == message.indexOf("test"));
            assertFalse(-1 == message.indexOf(fileName));
            assertFalse(-1 == message.indexOf(""+10));
        }
    }

    public void testException4(){
        final String fileName = "file";
        ScriptException ex = new ScriptException("test", fileName, 10, 30);
        try {
            throw ex;
        } catch (ScriptException e) {
            assertEquals(10, e.getLineNumber());
            assertEquals(30, e.getColumnNumber());
            assertEquals(fileName, e.getFileName());
            final String message = e.getMessage();
            assertFalse(-1 == message.indexOf("test"));
            assertFalse(-1 == message.indexOf(fileName));
            assertFalse(-1 == message.indexOf(""+10));
            assertFalse(-1 == message.indexOf(""+30));
        }
    }

    public void testBSF29(){
        final Exception exception = new Exception("exception message");
        final String expectedMessage =exception.toString();
        final Exception scriptException = new ScriptException(exception);
        assertEquals(expectedMessage, scriptException.getMessage());
    }
}
