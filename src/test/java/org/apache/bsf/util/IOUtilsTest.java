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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */


package org.apache.bsf.util;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.bsf.util.IOUtils;

import junit.framework.TestCase;

/**
 *
 * This is testcase for the org.apache.bsf.util.IOUtils
 *
 * @author Thusitha Perera <pererawt@yahoo.com>
 *
 */

public class IOUtilsTest extends TestCase {

    static private final String lineSeparator =
        System.getProperty("line.separator","/n");

    public IOUtilsTest(final String name) {
        super(name);
    }

    public void testGetStringFromReader() throws IOException {
        String result;
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);

        pw.println("IOUtilsTest");
        pw.flush();

        final StringReader sr = new StringReader(sw.toString());
        result = IOUtils.getStringFromReader(sr);

        assertTrue(result.equals(("IOUtilsTest" + lineSeparator)));

        final File myFile = File.createTempFile("Test", "txt");

        final FileWriter fw = new FileWriter(myFile);
        final PrintWriter npw = new PrintWriter(fw);
        npw.println("file name : Test.txt");
        npw.flush();

        final FileReader fr = new FileReader(myFile);
        result = IOUtils.getStringFromReader(fr);

        assertTrue(result.equals(("file name : Test.txt" +
                                            lineSeparator)));
    }
}
