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

package org.apache.bsf.util.event.generator;

/**
 * Byte handling utilities
 *
 * 5 April 1999 - functions to append standard types to byte arrays
 *                functions to produce standard types from byte arrays
 *
 * @author Richard F. Boehme
 *
 */
public class ByteUtility
{
  public static byte[] addBytes(byte[] array,byte[] value)
  {
	if( null != array )
	{
	  byte newarray[] = new byte[array.length + value.length];
	  System.arraycopy(array,0,newarray,0,array.length);
	  System.arraycopy(value,0,newarray,array.length,value.length);
	  array = newarray;
	}
	else
	{
	  array = value;
	}
	return array;
  }
  public static byte[] addBytes(byte[] array, byte value)
  {
	if( null != array )
	{
	  byte newarray[] = new byte[array.length + 1];
	  System.arraycopy(array,0,newarray,0,array.length);
	  newarray[newarray.length-1] = value;
	  array = newarray;
	}
	else
	{
	  array = new byte[1];
	  array[0] = value;
	}
	return array;
  }
  public static byte[] addBytes(byte[] array, int value)
  {
	if( null != array )
	{
	  byte newarray[] = new byte[array.length + 3];
	  System.arraycopy(array,0,newarray,0,array.length);
	  newarray[newarray.length-3] = (byte) (( value >> 16 ) & 0xFF);
	  newarray[newarray.length-2] = (byte) (( value >>  8 ) & 0xFF);
	  newarray[newarray.length-1] = (byte) (  value         & 0xFF);
	  array = newarray;
	}
	else
	{
	  array = new byte[3];
	  array[0] = (byte) (( value >> 16 ) & 0xFF);
	  array[1] = (byte) (( value >>  8 ) & 0xFF);
	  array[2] = (byte) (  value         & 0xFF);
	}
	return array;
  }
  public static byte[] addBytes(byte[] array, long value)
  {
	if( null != array )
	{
	  byte newarray[] = new byte[array.length + 4];
	  System.arraycopy(array,0,newarray,0,array.length);
	  newarray[newarray.length-4] = (byte) (( value >> 24 ) & 0xFF);
	  newarray[newarray.length-3] = (byte) (( value >> 16 ) & 0xFF);
	  newarray[newarray.length-2] = (byte) (( value >>  8 ) & 0xFF);
	  newarray[newarray.length-1] = (byte) (  value         & 0xFF);
	  array = newarray;
	}
	else
	{
	  array = new byte[4];
	  array[0] = (byte) (( value >> 24 ) & 0xFF);
	  array[1] = (byte) (( value >> 16 ) & 0xFF);
	  array[2] = (byte) (( value >>  8 ) & 0xFF);
	  array[3] = (byte) (value & 0xFF);
	}
	return array;
  }
  public static byte[] addBytes(byte[] array,String value)
  {
	if( null != value )
	{
	  if( null != array)
	  {
		byte newarray[] = new byte[array.length + value.length()];
		System.arraycopy(array,0,newarray,0,array.length);
		System.arraycopy(value.getBytes(),0,newarray,array.length,value.length());
		array = newarray;
	  }
	  else
	  {
		array = value.getBytes();
	  }
	}
	return array;
  }
  public static byte[] addBytes(byte[] array, short value)
  {
	if( null != array)
	{
	  byte newarray[] = new byte[array.length + 2];
	  System.arraycopy(array,0,newarray,0,array.length);
	  newarray[newarray.length-2] = (byte) (( value >> 8 ) & 0xFF);
	  newarray[newarray.length-1] = (byte) (  value        & 0xFF);
	  array = newarray;
	}
	else
	{
	  array = new byte[2];
	  array[0] = (byte) (( value >> 8 ) & 0xFF);
	  array[1] = (byte) (  value        & 0xFF);
	}
	return array;
  }
  public static double byteArrayToDouble(byte high[], byte low[])
  {
	double temp = 0;
	// high bytes
	temp += (((long)high[0]) & 0xFF) << 56;
	temp += (((long)high[1]) & 0xFF) << 48;
	temp += (((long)high[2]) & 0xFF) << 40;
	temp += (((long)high[3]) & 0xFF) << 32;
	// low bytes
	temp += (((long)low[0]) & 0xFF) << 24;
	temp += (((long)low[1]) & 0xFF) << 16;
	temp += (((long)low[2]) & 0xFF) << 8;
	temp += (((long)low[3]) & 0xFF);
	return temp;
  }
  public static double byteArrayToDounle(byte value[])
  {
	byte high[] = new byte[4];
	byte low[] = new byte[4];
	high[0] = value[0];
	high[1] = value[1];
	high[2] = value[2];
	high[3] = value[3];
	low[0] = value[4];
	low[1] = value[5];
	low[2] = value[6];
	low[3] = value[7];
	return byteArrayToDouble(high,low);
  }
  public static float byteArrayToFloat(byte value[])
  {
	float temp = 0;
	temp += (((int)value[0]) & 0xFF) << 24;
	temp += (((int)value[1]) & 0xFF) << 16;
	temp += (((int)value[2]) & 0xFF) << 8;
	temp += (((int)value[3]) & 0xFF);
	return temp;
  }
  public static int byteArrayToInt(byte value[])
  {
	int temp = 0;
	temp += (((int)value[0]) & 0xFF) << 24;
	temp += (((int)value[1]) & 0xFF) << 16;
	temp += (((int)value[2]) & 0xFF) << 8;
	temp += (((int)value[3]) & 0xFF);
	return temp;
  }
  public static long byteArrayToLong(byte value[])
  {
	byte high[] = new byte[4];
	byte low[] = new byte[4];
	high[0] = value[0];
	high[1] = value[1];
	high[2] = value[2];
	high[3] = value[3];
	low[0] = value[4];
	low[1] = value[5];
	low[2] = value[6];
	low[3] = value[7];
	return byteArrayToLong(high,low);
  }
  public static long byteArrayToLong(byte high[], byte low[])
  {
	long temp = 0;
	// high bytes
	temp += (((long)high[0]) & 0xFF) << 56;
	temp += (((long)high[1]) & 0xFF) << 48;
	temp += (((long)high[2]) & 0xFF) << 40;
	temp += (((long)high[3]) & 0xFF) << 32;
	// low bytes
	temp += (((long)low[0]) & 0xFF) << 24;
	temp += (((long)low[1]) & 0xFF) << 16;
	temp += (((long)low[2]) & 0xFF) << 8;
	temp += (((long)low[3]) & 0xFF);
	return temp;
  }
  // make the following loops with check on array length *****************
  public static short byteArrayToShort(byte value[])
  {
	short temp = 0;
	temp += (((int)value[0]) & 0xFF) << 8;
	temp += (((int)value[1]) & 0xFF);
	return temp;
  }
  public static String byteToHexString(byte value)
  {
	String temp = null;

	switch( (value & 0xF0) >> 4 )
	{
	  case 0:
		temp = "0";
		break;
	  case 1:
		temp = "1";
		break;
	  case 2:
		temp = "2";
		break;
	  case 3:
		temp = "3";
		break;
	  case 4:
		temp = "4";
		break;
	  case 5:
		temp = "5";
		break;
	  case 6:
		temp = "6";
		break;
	  case 7:
		temp = "7";
		break;
	  case 8:
		temp = "8";
		break;
	  case 9:
		temp = "9";
		break;
	  case 10:
		temp = "A";
		break;
	  case 11:
		temp = "B";
		break;
	  case 12:
		temp = "C";
		break;
	  case 13:
		temp = "D";
		break;
	  case 14:
		temp = "E";
		break;
	  case 15:
		temp = "F";
		break;
	}
	switch( (value & 0x0F) )
	{
	  case 0:
		temp += "0";
		break;
	  case 1:
		temp += "1";
		break;
	  case 2:
		temp += "2";
		break;
	  case 3:
		temp += "3";
		break;
	  case 4:
		temp += "4";
		break;
	  case 5:
		temp += "5";
		break;
	  case 6:
		temp += "6";
		break;
	  case 7:
		temp += "7";
		break;
	  case 8:
		temp += "8";
		break;
	  case 9:
		temp += "9";
		break;
	  case 10:
		temp += "A";
		break;
	  case 11:
		temp += "B";
		break;
	  case 12:
		temp += "C";
		break;
	  case 13:
		temp += "D";
		break;
	  case 14:
		temp += "E";
		break;
	  case 15:
		temp += "F";
		break;
	}
	return temp;
  }
}
