/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2002 The Apache Software Foundation.  All rights
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
 * 4. The names "BSF", "Apache", and "Apache Software Foundation"
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

package org.apache.bsf.util.event.generator;

/**
 * Bytecode handling utilities
 *
 * Handle standard byte arrays as defined in Java VM and Class File
 *
 * 5 April 1999 - functions to append Class File byte subarrays
 *                into a Class File byte array
 *
 * @author Richard F. Boehme (<tt>rfboehme@us.ibm.com</tt>)
 *
 */
public class Bytecode
{
  // Constant Pool Item Codes
  public static final byte C_Utf8               = 0x01;   //  1
  public static final byte C_Integer            = 0x03;   //  3
  public static final byte C_Float              = 0x04;   //  4
  public static final byte C_Long               = 0x05;   //  5
  public static final byte C_Double             = 0x06;   //  6
  public static final byte C_Class              = 0x07;   //  7
  public static final byte C_String             = 0x08;   //  8
  public static final byte C_FieldRef           = 0x09;   //  9
  public static final byte C_MethodRef          = 0x0A;   // 10
  public static final byte C_InterfaceMethodRef = 0x0B;   // 11
  public static final byte C_NameAndType        = 0x0C;   // 12

//public static byte[] addDouble(byte[] array,double value)
//{
//  array = ByteUtility.addBytes(array,C_Double);
//  array = ByteUtility.addBytes(array,value);
//  return array;
//}

  public static byte[] addClass(byte[] array,short value)
  { return addRef(C_Class,array,value); }
  public static byte[] addFieldRef(byte[] array,short value1,short value2)
  { return addRef(C_FieldRef,array,value1,value2); }
  public static byte[] addInteger(byte[] array,int value)
  {
	array = ByteUtility.addBytes(array,C_Integer);
	array = ByteUtility.addBytes(array,value);
	return array;
  }
  public static byte[] addInterfaceMethodRef(byte[] array,short value1,short value2)
  { return addRef(C_InterfaceMethodRef,array,value1,value2); }
//public static byte[] addFloat(byte[] array,float value)
//{
//  array = ByteUtility.addBytes(array,C_Float);
//  array = ByteUtility.addBytes(array,value);
//  return array;
//}

  public static byte[] addLong(byte[] array,long value)
  {
	array = ByteUtility.addBytes(array,C_Long);
	array = ByteUtility.addBytes(array,value);
	return array;
  }
  public static byte[] addMethodRef(byte[] array,short value1,short value2)
  { return addRef(C_MethodRef,array,value1,value2); }
  public static byte[] addNameAndType(byte[] array,short value1,short value2)
  { return addRef(C_NameAndType,array,value1,value2); }
  public static byte[] addRef(byte refType,byte[] array,short value)
  {
	array = ByteUtility.addBytes(array,refType);
	array = ByteUtility.addBytes(array,value);
	return array;
  }
  // Generic Bytecode Methods
  public static byte[] addRef(byte refType,byte[] array,short value1,short value2)
  {
	array = ByteUtility.addBytes(array,refType);
	array = ByteUtility.addBytes(array,value1);
	array = ByteUtility.addBytes(array,value2);
	return array;
  }
  public static byte[] addString(byte[] array,short value)
  { return addRef(C_String,array,value); }
  // Constant Pool Item Methods
  public static byte[] addUtf8(byte[] array,String value)
  {
	array = ByteUtility.addBytes(array,C_Utf8);
	array = ByteUtility.addBytes(array,(short)value.length());
	array = ByteUtility.addBytes(array,value);
	return array;
  }
}
