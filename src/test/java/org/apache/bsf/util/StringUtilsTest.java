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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.Vector;

import org.apache.bsf.util.StringUtils;

import junit.framework.TestCase;

/**
 * This is a testcase for org.apache.bsf.util.StringUtils.java
 *
 * @author Sanka Samaranayake <sanka_hdins@yahoo.com>
 * @author Nilupa Bandara     <nilupa@opensource.lk>
 *
 */
public class StringUtilsTest extends TestCase {

    static private final String lineSeparator =
        System.getProperty("line.separator", "\n");

    /**
     * Constructor for StringUtilsTest.
     * @param arg0
     */
    public StringUtilsTest (final String arg0) {
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

    public void testGetContentAsReader()
        throws MalformedURLException, IOException {

            Reader reader;

            final File myFile = File.createTempFile("Test", "txt");

            final FileWriter fw = new FileWriter(myFile);
            final PrintWriter pw = new PrintWriter(fw);
            pw.println("file name : Test.txt");
            pw.flush();

            reader = StringUtils.getContentAsReader(myFile.toURL());
            final BufferedReader bf = new BufferedReader(reader);
            assertTrue(bf.readLine().equals(
                                            "file name : Test.txt"));

        }

    public void testGetContentAsString() throws IOException{

        String result;

        final File myFile = File.createTempFile("Test", "txt");

        final FileWriter fw = new FileWriter(myFile);
        final PrintWriter pw = new PrintWriter(fw);
        pw.println("file name : Test.txt");
        pw.flush();

        result = StringUtils.getContentAsString(myFile.toURL());
        assertTrue(result.equals(("file name : Test.txt" +
                                            lineSeparator)));

    }

    public void testGetSafeString() {
        String result;

        result = StringUtils.getSafeString("test-string");
        assertTrue(result.equals(("\"test-string\"" +
                                            lineSeparator)));
        //checks for an empty string ..
        result = StringUtils.getSafeString("");
        assertTrue(result.equals(("\"\"" +
                                            lineSeparator)));

        result = StringUtils.getSafeString("\n");
        assertTrue(result.equals(("\"\"" +
                                            lineSeparator)));

        result = StringUtils.getSafeString("\r");
        assertTrue(result.equals(("\"\"" +
                                            lineSeparator)));

        result = StringUtils.getSafeString("\\n");
        assertTrue(result.equals(("\"\\\\n\"" +
                                            lineSeparator)));

        result = StringUtils.getSafeString("\\r");
        assertTrue(result.equals(("\"\\\\r\"" +
                                            lineSeparator)));

    }

    public void testGetValidIdentifierName(){

        assertTrue((StringUtils.getValidIdentifierName("identifier")).equals(
                                                                             "identifier"));

        assertTrue((StringUtils.getValidIdentifierName("0identifier")).equals(
                                                                              "_identifier"));

        assertTrue((StringUtils.getValidIdentifierName("i0dentifier")).equals(
                                                                              "i0dentifier"));

        assertTrue((StringUtils.getValidIdentifierName("$identifier")).equals(
                                                                              "$identifier"));

        assertTrue((StringUtils.getValidIdentifierName("identi$fier")).equals(
                                                                              "identi$fier"));

        assertTrue((StringUtils.getValidIdentifierName(" identifier")).equals(
                                                                              "_identifier"));

        assertTrue((StringUtils.getValidIdentifierName("identi fier")).equals(
                                                                              "identi_fier"));

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
