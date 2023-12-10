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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.Vector;

import junit.framework.TestCase;

/**
 * This is a testcase for org.apache.bsf.util.StringUtils.java
 */
public class StringUtilsTest extends TestCase {

    static private final String lineSeparator = System.getProperty("line.separator", "\n");

    /**
     * Constructor for StringUtilsTest.
     * 
     * @param arg0
     */
    public StringUtilsTest(final String arg0) {
        super(arg0);
    }

    public void testClassNameToVarName() {

        assertTrue((StringUtils.classNameToVarName("int")).equals("int"));
        assertTrue((StringUtils.classNameToVarName("int[][][]")).equals("int_3D"));
        assertNull((StringUtils.classNameToVarName("")));
    }

    public void testCleanString() {

        String result;

        result = StringUtils.cleanString("\"");
        assertTrue(result.equals("\\\""));

        result = StringUtils.cleanString("\\");
        assertTrue(result.equals("\\\\"));

        result = StringUtils.cleanString("\n");
        assertTrue(result.equals("\\n"));

        result = StringUtils.cleanString("\r");
        assertTrue(result.equals("\\r"));

    }

    public void testGetChars() {

        String result;

        result = StringUtils.getChars(1, 'a');
        assertTrue(result.equals("a"));

        result = StringUtils.getChars(1, ' ');
        assertTrue(result.equals(" "));

        result = StringUtils.getChars(10, ' ');
        assertTrue(result.equals("          "));

        result = StringUtils.getChars(-1, 'a');
        assertTrue(result.equals(""));

    }

    public void testGetClassName() {
        String result;

        result = StringUtils.getClassName((new Byte("0")).getClass());
        assertTrue(result.equals("java.lang.Byte"));

        result = StringUtils.getClassName((new Byte[0][0][0]).getClass());
        assertTrue(result.equals("java.lang.Byte[][][]"));

        result = StringUtils.getClassName(("").getClass());
        assertTrue(result.equals("java.lang.String"));

        result = StringUtils.getClassName((new String[0][0][0]).getClass());
        assertTrue(result.equals("java.lang.String[][][]"));

    }

    public void testGetCommaListFromVector() {

        String result;

        final Vector vector = new Vector();
        vector.add(Character.valueOf('a'));
        vector.add(Character.valueOf('b'));

        result = StringUtils.getCommaListFromVector(vector);
        assertTrue(result.equals("a, b"));

        result = StringUtils.getCommaListFromVector(new Vector());
        assertTrue(result.equals(""));

    }

    public void testGetContentAsReader() throws MalformedURLException, IOException {

        Reader reader;

        final File myFile = File.createTempFile("Test", "txt");

        final FileWriter fw = new FileWriter(myFile);
        final PrintWriter pw = new PrintWriter(fw);
        pw.println("file name : Test.txt");
        pw.flush();

        reader = StringUtils.getContentAsReader(myFile.toURL());
        final BufferedReader bf = new BufferedReader(reader);
        assertTrue(bf.readLine().equals("file name : Test.txt"));

    }

    public void testGetContentAsString() throws IOException {

        String result;

        final File myFile = File.createTempFile("Test", "txt");

        final FileWriter fw = new FileWriter(myFile);
        final PrintWriter pw = new PrintWriter(fw);
        pw.println("file name : Test.txt");
        pw.flush();

        result = StringUtils.getContentAsString(myFile.toURL());
        assertTrue(result.equals(("file name : Test.txt" + lineSeparator)));

    }

    public void testGetSafeString() {
        String result;

        result = StringUtils.getSafeString("test-string");
        assertTrue(result.equals(("\"test-string\"" + lineSeparator)));
        // checks for an empty string ..
        result = StringUtils.getSafeString("");
        assertTrue(result.equals(("\"\"" + lineSeparator)));

        result = StringUtils.getSafeString("\n");
        assertTrue(result.equals(("\"\"" + lineSeparator)));

        result = StringUtils.getSafeString("\r");
        assertTrue(result.equals(("\"\"" + lineSeparator)));

        result = StringUtils.getSafeString("\\n");
        assertTrue(result.equals(("\"\\\\n\"" + lineSeparator)));

        result = StringUtils.getSafeString("\\r");
        assertTrue(result.equals(("\"\\\\r\"" + lineSeparator)));

    }

    public void testGetValidIdentifierName() {

        assertTrue((StringUtils.getValidIdentifierName("identifier")).equals("identifier"));

        assertTrue((StringUtils.getValidIdentifierName("0identifier")).equals("_identifier"));

        assertTrue((StringUtils.getValidIdentifierName("i0dentifier")).equals("i0dentifier"));

        assertTrue((StringUtils.getValidIdentifierName("$identifier")).equals("$identifier"));

        assertTrue((StringUtils.getValidIdentifierName("identi$fier")).equals("identi$fier"));

        assertTrue((StringUtils.getValidIdentifierName(" identifier")).equals("_identifier"));

        assertTrue((StringUtils.getValidIdentifierName("identi fier")).equals("identi_fier"));

        // checks for a empty string which should return null
        assertNull(StringUtils.getValidIdentifierName(""));

        // checks for a null value which should return null
        assertNull(StringUtils.getValidIdentifierName(null));
    }

    public void testIsValidIdentifierName() {

        assertTrue(StringUtils.isValidIdentifierName("identifier"));

        assertTrue(!StringUtils.isValidIdentifierName("0identifier"));

        assertTrue(StringUtils.isValidIdentifierName("i0dentifier"));

        assertTrue(StringUtils.isValidIdentifierName("$identifier"));

        assertTrue(StringUtils.isValidIdentifierName("identi$fier"));

        assertTrue(!StringUtils.isValidIdentifierName(" identifier"));

        assertTrue(!StringUtils.isValidIdentifierName("identi fier"));

        // checks for a null value .. should return null
        assertNull(StringUtils.getValidIdentifierName(null));

        // checks for an empty string .. should return null
        assertNull(StringUtils.getValidIdentifierName(""));

    }

    public void testIsValidPackageName() {

        assertTrue(StringUtils.isValidPackageName("org"));

        assertTrue(StringUtils.isValidPackageName("org.apache.bsf"));

        // checks whether the package name ends with a '.'
        assertTrue(!StringUtils.isValidPackageName("org.apache.bsf."));

        // checks whether the package name includes '..'
        assertTrue(!StringUtils.isValidPackageName("org.apache.bsf.."));

        // checks for an empty string which is ok ..
        assertTrue(StringUtils.isValidPackageName(""));

        // checks for a null value
        assertTrue(!StringUtils.isValidPackageName(null));
    }
}
