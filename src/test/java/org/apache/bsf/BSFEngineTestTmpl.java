/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.bsf;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import junit.framework.TestCase;

/**
 * Superclass for language engine tests.
 */
public abstract class BSFEngineTestTmpl extends TestCase {
    protected BSFManager bsfManager;
    protected PrintStream sysOut;

    private final PrintStream tmpOut;
    private final ByteArrayOutputStream tmpBaos;

    public BSFEngineTestTmpl(final String name) {
        super(name);

        sysOut = System.out;
        tmpBaos = new ByteArrayOutputStream();
        tmpOut = new PrintStream(tmpBaos);
    }

    public void setUp() {
        bsfManager = new BSFManager();
        System.setOut(tmpOut);
    }

    public void tearDown() {
        System.setOut(sysOut);
        resetTmpOut();
    }

    protected String getTmpOutStr() {
        return tmpBaos.toString();
    }

    protected void resetTmpOut() {
        tmpBaos.reset();
    }

    protected String failMessage(final String failure, final Exception e) {
        String message = failure;
        message += "\nReason:\n";
        message += e.getMessage();
        message += "\n";
        return message;
    }
}
