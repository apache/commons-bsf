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

package org.apache.bsf.engines.activescript;

import java.util.*;
import java.io.*;
import java.lang.reflect.*;

import org.apache.bsf.*;
import org.apache.bsf.util.*;



/**
 * This is the interface to active scripting engines from the Bean 
 * Scripting Framework. This code uses John Ponzo's IBM Active Scripting
 * Toolkit to tie in active scripting engines to BSF. 
 * This class implements Runnable to create a thread.  This thread is to 
 * exclusively access the scripting engine.  All methods from this class to
 * the engines is proxied over to the engine thread for execution. Why? 
 * Because, MS engines are implemented to only be accessed from ONE thread.
 *
 * @author   Sanjiva Weerawarana
 */
public class ActiveScriptEngine extends BSFEngineImpl  implements JavaBeanAddEventListener
{

 class ArrayInfo
 {
  protected Object arrayObject=null;
  protected boolean investigated= false;
  public int maxDepth=0;
  // int curDepth=0;
  public char type=0;
  public int maxDimArray[]= null;
  public int dimOffset[]= null;
  public String toString()
  {
   if(0== maxDepth) return "Not an array";
   String ret= "type="+type+",maxDepth="+ maxDepth+ "maxDimArray=\n"; 
   for(int i=0; i < maxDepth; ++i) ret+= "["+ i+ "]"+"=" + maxDimArray[i] + ";\n";
   return ret;
  }

  protected void investigate(Object o, int curDepth)
  {
   if(investigated) return;
   if( null == o) return; //Safety

   ++curDepth;
   int thisDepth= curDepth;
   if(thisDepth == maxDepth)
   { //The reason the last dimension needs to be type specific to avoid getting runtime class cast exceptions on primatives!
	 switch( type)
	 {
	  case 'Z' :
	   {
		boolean[] larray= (boolean []) o;
		 if(larray.length > maxDimArray[thisDepth-1]) maxDimArray[thisDepth-1]= larray.length;
	   } 
	  break;	
	  case 'B' :
	   {
		byte[] larray= (byte []) o;
		 if(larray.length > maxDimArray[thisDepth-1]) maxDimArray[thisDepth-1]= larray.length;
	   } 
	  break;	
	  case 'C' :
	   {
		char[] larray= (char []) o;
		 if(larray.length > maxDimArray[thisDepth-1]) maxDimArray[thisDepth-1]= larray.length;
	   } 
	  break;	
	  case 'S' :
	   {
		short[] larray= (short []) o;
		 if(larray.length > maxDimArray[thisDepth-1]) maxDimArray[thisDepth-1]= larray.length;
	   } 
	  break;
	  case 'I' :
	   {
		int[] larray= (int []) o;
		 if(larray.length > maxDimArray[thisDepth-1]) maxDimArray[thisDepth-1]= larray.length;
	   } 
	  break;
	  case 'J' :
	   {
		long[] larray= (long []) o;
		 if(larray.length > maxDimArray[thisDepth-1]) maxDimArray[thisDepth-1]= larray.length;
	   } 
	  break;
	  case 'F' :
	   {
		float[] larray= (float []) o;
		 if(larray.length > maxDimArray[thisDepth-1]) maxDimArray[thisDepth-1]= larray.length;
	   } 
	  break;
	  case 'D' :
	   {
		double[] larray= (double[]) o;
		 if(larray.length > maxDimArray[thisDepth-1]) maxDimArray[thisDepth-1]= larray.length;
	   } 
	  break;
	  default:
	   { //Should be an object
		 Object[] larray= (Object  []) o;
		 if(larray.length > maxDimArray[thisDepth-1]) maxDimArray[thisDepth-1]= larray.length;
	   } 
	  break;
	 }
   }
   else
   { //Still a multi-dim array
	 Object[] larray= (Object  []) o;
	  if(larray.length > maxDimArray[curDepth-1]) maxDimArray[curDepth-1]= larray.length;

	 for(int i=0; i < larray.length ; ++i)
	 {
	  if(larray[i].getClass().isArray() )
	  {
		investigate(larray[i], thisDepth);
	  }
	 }
   }
   if(1== curDepth) investigated= true;
  }//Endof investigate


  private int setVariantData( Object o,  byte v[], int os,  int curDepth, int[] index) throws BSFException
  {
	  ++curDepth;
	  if(curDepth != maxDepth)
	  {
		Object[] larray= (Object  []) o;
		int i;
		for( i=0; i< larray.length ; ++i)
		{
		  if(larray[i] == null)
		  {
			
		  }
		  else
		  {
	   index[curDepth-1]=i;
	   for(int k= curDepth; k< maxDepth; index[k++]=0);
		   setVariantData( larray[i], v, os , curDepth, index );
		  } 
		}
	  }
	  else
	  {
		switch( type)
		{
		 case 'Z' :
		  {

		   boolean[] larray= (boolean[]) o;
		   int i;
	   int pos=os;
	   for(int k=0; k < curDepth-1; ++k) pos+= index[k]*dimOffset[k]*16; 
		   for( i=0; i < larray.length; ++i)
		   {
	     int fpos= pos+ i*dimOffset[curDepth-1]*16;
			 v[fpos] = 11; //VT_BOOL
			 v[fpos+1] = 0;
	   
			 byte x= (byte)( (larray[i]) ? 0xff: 0);
			 v[fpos+2]= x;
			 v[fpos+3]= x;
			 v[fpos+4]= x;
			 v[fpos+5]= x;
		   }
		   for(; i< maxDimArray[curDepth-1]; ++i )
		   {
	     int fpos= pos+ i*dimOffset[curDepth-1]*16;
			 v[fpos] = v[fpos+1]= 0;  //VT_EMPTY
		   }
	   
		  } 
		 break;	
		 case 'B' :
		  {
		   byte[] larray= (byte[]) o;
		   int i;
	   int pos=os;
	   for(int k=0; k < curDepth-1; ++k) pos+= index[k]*dimOffset[k]*16; 
		   for( i=0; i < larray.length; ++i)
		   {
	     int fpos= pos+ i*dimOffset[curDepth-1]*16;
			 v[fpos] = 17; //VT_UI1
			 v[fpos+1] = 0;
	   
			 int x= larray[i];
			 v[fpos+8]= (byte)x;
			 v[fpos+9]= v[fpos+10]= v[fpos+11]= 0;
		   }
		   for(; i< maxDimArray[curDepth-1]; ++i )
		   {
	     int fpos= pos+ i*dimOffset[curDepth-1]*16;
			 v[fpos] = v[fpos+1]= 0;  //VT_EMPTY
	   
		   }
		  } 
		 break;	
		 case 'C' :
		  {
		   char[] larray= (char[]) o;
		   int i;
	   int pos=os;
	   for(int k=0; k < curDepth-1; ++k) pos+= index[k]*dimOffset[k]*16; 
		   for( i=0; i < larray.length; ++i)
	   {
	      int fpos= pos+ i*dimOffset[curDepth-1]*16;
	      v[fpos] = 17; //VT_UI1
	      v[fpos+1] = 0;
	   
	      byte x= (byte) ((Character)o).charValue();
	      v[fpos+8]= x;
			  v[fpos+9]= v[fpos+10]= v[fpos+11]= 0;
		   }
		   for(; i< maxDimArray[curDepth-1]; ++i )
		   {
	     int fpos= pos+ i*dimOffset[curDepth-1]*16;
			 v[fpos] = v[fpos+1]= 0;  //VT_EMPTY
	   
		   }
		  } 
		 break;	
		 case 'S' :
		  {
		   short[] larray= (short[]) o;
		   int i;
	   int pos=os;
	   for(int k=0; k < curDepth-1; ++k) pos+= index[k]*dimOffset[k]*16; 
		   for( i=0; i < larray.length; ++i)
	   {
	      int fpos= pos+ i*dimOffset[curDepth-1]*16;
	      v[fpos] = 2; //VT_I2
	      v[fpos+1] = 0;
	   
	      int x= (int)larray[i];
	      v[fpos+8]= (byte)x;
	      v[fpos+9]= (byte)((x>>>8) & 0xff);
	      v[fpos+10]= (byte)((x>>>16) & 0xff);
	      v[fpos+11]= (byte)((x>>>24) & 0xff);
		   }
		   for(; i< maxDimArray[curDepth-1]; ++i )
		   {
	     int fpos= pos+ i*dimOffset[curDepth-1]*16;
			 v[fpos] = v[fpos+1]= 0;  //VT_EMPTY
	   
		   }
		  } 
		 break;
		 case 'I' :
		  {
		   int[] larray= (int[]) o;
		   int i;
	   int pos=os;
	   for(int k=0; k < curDepth-1; ++k) pos+= index[k]*dimOffset[k]*16; 
		   for( i=0; i < larray.length; ++i)
		   {
	     int fpos= pos+ i*dimOffset[curDepth-1]*16;
			 v[fpos] = 3; //VT_I4
			 v[fpos+1] = 0;
	   
			  int x= larray[i];
			 v[fpos+8]= (byte)x;
			 v[fpos+9]= (byte)((x>>>8) & 0xff);
			 v[fpos+10]= (byte)((x>>>16) & 0xff);
			 v[fpos+11]= (byte)((x>>>24) & 0xff);
		   }
		   for(; i< maxDimArray[curDepth-1]; ++i )
		   {
	     int fpos= pos+ i*dimOffset[curDepth-1]*16;
			 v[fpos] = v[fpos+1]= 0;  //VT_EMPTY
	   
		   }
		  } 
		 break;
		 case 'J' :
		  {
		   long[] larray= (long[]) o;
		   int i;
	   int pos=os;
	   for(int k=0; k < curDepth-1; ++k) pos+= index[k]*dimOffset[k]*16; 
		   for( i=0; i < larray.length; ++i)
	   {
	     int fpos= pos+ i*dimOffset[curDepth-1]*16;
	     v[fpos] = 5; //VT_R8
	     v[fpos+1] = 0;

	     long x= Double.doubleToLongBits((double)(larray[i]));
			 v[fpos+8]= (byte)x;
			 v[fpos+9]= (byte)((x>>>8) & 0xff);
			 v[fpos+10]= (byte)((x>>>16) & 0xff);
			 v[fpos+11]= (byte)((x>>>24) & 0xff);
			 v[fpos+12]= (byte)((x>>>32) & 0xff);
			 v[fpos+13]= (byte)((x>>>40) & 0xff);
			 v[fpos+14]= (byte)((x>>>48) & 0xff);
			 v[fpos+15]= (byte)((x>>>56) & 0xff);
	   
		   }
		   for(; i< maxDimArray[curDepth-1]; ++i )
		   {
	     int fpos= pos+ i*dimOffset[curDepth-1]*16;
			 v[fpos] = v[fpos+1]= 0;  //VT_EMPTY
		   }
		  } 
		 break;
		 case 'F' :
		  {
		   float[] larray= (float[]) o;
		   int i;
	   int pos=os;
	   for(int k=0; k < curDepth-1; ++k) pos+= index[k]*dimOffset[k]*16; 
		   for( i=0; i < larray.length; ++i)
	   {
	      int fpos= pos+ i*dimOffset[curDepth-1]*16;
	      v[fpos] = 4; //VT_R4
	      v[fpos+1] = 0;
	   
	      int x=  Float.floatToIntBits(larray[i]);
	      v[fpos+8]= (byte)x;
	      v[fpos+9]= (byte)((x>>>8) & 0xff);
	      v[fpos+10]= (byte)((x>>>16) & 0xff);
	      v[fpos+11]= (byte)((x>>>24) & 0xff);
		   }
		   for(; i< maxDimArray[curDepth-1]; ++i )
		   {
	     int fpos= pos+ i*dimOffset[curDepth-1]*16;
			 v[fpos] = v[fpos+1]= 0;  //VT_EMPTY
	   
		   }
		  } 
		 break;
		 case 'D' :
		  {
		   double[] larray= (double[]) o;
		   int i;
	   int pos=os;
	   for(int k=0; k < curDepth-1; ++k) pos+= index[k]*dimOffset[k]*16; 
		   for( i=0; i < larray.length; ++i)
	   {
	      int fpos= pos+ i*dimOffset[curDepth-1]*16;
	      v[fpos] = 5; //VT_R8
	      v[fpos+1] = 0;
	   
	      long x=  Double.doubleToLongBits(larray[i]);
	      v[fpos+8]= (byte)x;
	      v[fpos+9]= (byte)((x>>>8) & 0xff);
	      v[fpos+10]= (byte)((x>>>16) & 0xff);
	      v[fpos+11]= (byte)((x>>>24) & 0xff);
			v[fpos+12]= (byte)((x>>>32) & 0xff);
			  	v[fpos+13]= (byte)((x>>>40) & 0xff);
			  	v[fpos+14]= (byte)((x>>>48) & 0xff);
	      v[fpos+15]= (byte)((x>>>56) & 0xff);
			} 
			for(; i< maxDimArray[curDepth-1]; ++i )
			{
	     int fpos= pos+ i*dimOffset[curDepth-1]*16;
			 v[fpos] = v[fpos+1]= 0;  //VT_EMPTY
	   
			}
	   } 
		 break;
		 default:
		  { //Should be an object array.

			 Object[] larray= (Object[]) o;
			 int i;
			 int pos=os;
			 for(int k=0; k < curDepth-1; ++k) pos+= index[k]*dimOffset[k]*16; 
			 for( i=0; i < larray.length; ++i)
			 {
			   int fpos= pos+ i*dimOffset[curDepth-1]*16;
	      
	        if(larray[i] == null)
		{
				  v[fpos] = 1; //VT_NULL
	          v[fpos+1] = 0;
		}
				else if(larray[i]  instanceof java.lang.Boolean)
				{
				 v[fpos] = 11; //VT_BOOL
				 v[fpos+1] = 0;
				 byte x= (byte)( (((Boolean) larray[i]).booleanValue()) ? 0xff: 0);
				 v[fpos+8]= x;
				 v[fpos+9]= x;
				 v[fpos+10]= x;
				 v[fpos+11]= x;
				}
				else if(larray[i]  instanceof java.lang.Integer) //VT_R8
				{
				 v[fpos] = 3; //VT_I4
				 v[fpos+1] = 0;
				 int x= ((Integer)larray[i]).intValue();
				 v[fpos+8]= (byte)x;
				 v[fpos+9]= (byte)((x>>>8) & 0xff);
				 v[fpos+10]= (byte)((x>>>16) & 0xff);
				 v[fpos+11]= (byte)((x>>>24) & 0xff);
				}
				else if(larray[i] instanceof String)
		{
				   byte[] cppref= nativeStingToBString((String) larray[i]);

			  	      v[fpos] = 8; //VT_BSTR
			  	      v[fpos+1] = 0;
		 
			  	      v[fpos+8]= cppref[0];
			  	      v[fpos+9]= cppref[1];
			  	      v[fpos+10]= cppref[2];
			  	      v[fpos+11]= cppref[3];
		}
				else if(larray[i]  instanceof java.lang.Long) //VT_R8
				{ //COM has no long type so promote it to double which can contain it.
				 v[fpos] = 5; //VT_R8
				 v[fpos+1] = 0;
				 long x= Double.doubleToLongBits((double)(((Long)larray[i]).longValue()));
				 v[fpos+8]= (byte)x;
				 v[fpos+9]= (byte)((x>>>8) & 0xff);
				 v[fpos+10]= (byte)((x>>>16) & 0xff);
				 v[fpos+11]= (byte)((x>>>24) & 0xff);
				 v[fpos+12]= (byte)((x>>>32) & 0xff);
				 v[fpos+13]= (byte)((x>>>40) & 0xff);
				 v[fpos+14]= (byte)((x>>>48) & 0xff);
				 v[fpos+15]= (byte)((x>>>56) & 0xff);
				}
				else if(larray[i] instanceof java.lang.Short)
				{
				 v[fpos] = 2; //VT_I2
				 v[fpos+1] = 0;
				 // int x= Float.floatToIntBits((larray[i]));
				 int x= ((Short)larray[i]).intValue();
				 v[fpos+8]= (byte)x;
				 v[fpos+9]= (byte)((x>>>8) & 0xff);
				 v[fpos+10]= (byte)((x>>>16) & 0xff);
				 v[fpos+11]= (byte)((x>>>24) & 0xff);
				}
				else if(larray[i] instanceof java.lang.Float)
				{
				 v[fpos] = 4; //VT_R4
				 v[fpos+1] = 0;
				 int x= Float.floatToIntBits(((Float)larray[i]).floatValue());
				 v[fpos+8]= (byte)x;
				 v[fpos+9]= (byte)((x>>>8) & 0xff);
				 v[fpos+10]= (byte)((x>>>16) & 0xff);
				 v[fpos+11]= (byte)((x>>>24) & 0xff);
				}
				else if(larray[i]  instanceof java.lang.Double) //VT_R8
				{
				 v[fpos] = 5; //VT_R8
				 v[fpos+1] = 0;
				 long x= Double.doubleToLongBits(((Double)larray[i]).doubleValue());
				 v[fpos+8]= (byte)x;
				 v[fpos+9]= (byte)((x>>>8) & 0xff);
				 v[fpos+10]= (byte)((x>>>16) & 0xff);
				 v[fpos+11]= (byte)((x>>>24) & 0xff);
				 v[fpos+12]= (byte)((x>>>32) & 0xff);
				 v[fpos+13]= (byte)((x>>>40) & 0xff);
				 v[fpos+14]= (byte)((x>>>48) & 0xff);
				 v[fpos+15]= (byte)((x>>>56) & 0xff);
				}
				else if(larray[i]  instanceof java.lang.Byte) 
				{
				 v[fpos] = 17; //VT_UI1
				 v[fpos+1] = 0;
				 byte x= ((Byte)larray[i] ).byteValue();
				 v[fpos+8]= x;
				}
				else if( larray[i]  instanceof java.lang.Character) 
				{
				 v[fpos] = 17; //VT_UI1
				 v[fpos+1] = 0;
				 byte x= (byte) ((Character)larray[i] ).charValue();
				 v[fpos+8]= x;
				}
				else if( larray[i]  instanceof java.lang.Void) 
				{
				 v[fpos] = 1; //VT_NULL
				 v[fpos+1] = 0;
				}
		else
		{ //Really some non-primative rep. object

				  byte[] cppref= null; // nativeObjectToVariant(css, larray[i]); 

			  	  v[fpos] = 9; //VT_DISPATCH
			  	  v[fpos+1] = 0;

				  if(o  instanceof org.apache.bsf.engines.activescript.COMIDispatchBean ) 
				  {
					cppref = ((org.apache.bsf.engines.activescript.COMIDispatchBean )larray[i]).getIDispatchInterface();
				  }
				  else
				  {
					cppref= nativeObjectToVariant(css, larray[i]); 
				  }
				  System.arraycopy(cppref,0,v,fpos+8, cppref.length);
		}     
			 } 
			 for(; i< maxDimArray[curDepth-1]; ++i )
			 {
	      int fpos= pos+ i*dimOffset[curDepth-1]*16;
			  v[fpos] = v[fpos+1]= 0;  //VT_EMPTY
			 }
		  }//endof default 
		 break;
		}

		
	  }

	return os;
  }

  byte [] toVariant()throws BSFException
  {
	investigate(arrayObject, 0);
	int totalDimSize= maxDimArray[0];
	for(int i=1; i <  maxDepth; ++i) totalDimSize *= maxDimArray[i];
	int mallocSize=16 + // Size of variant.
				 16 + // The size of a safeArray that follows.
	   		 8*maxDepth+ //The size of SAFEARRAYBOUND by the no of dim.
	   		 totalDimSize* 16;

	byte[] v= new byte[mallocSize  ]; //Size of each data item as a Variant.
	   v[0]= 12;   //VT_ARRAY | VT_VARIANT
	   v[1]= 0x20; //VT_ARRAY
	   		 
	   //SAFEARRAY IS PACKED AFTER THE VARIANT
	   v[16]= (byte)maxDepth;                  //cDims;
	   v[17]= (byte)((maxDepth>>>8) & 0xff);

	   v[18]= (byte) 0X92;                               //fFeatures = FADF_VARIANT
	   v[19]= 0X8;
	   //cbElements= 16 the size of a variant
	   v[20]= 16;
	   v[21]=v[22]=v[23]= 0;
	   //cLocks  ??
	   v[24]=v[25]=v[26]=v[27]= 0;
	   int i=0, j=32;

	   //rgsabound[]  one for each dimension.  Has max no of elements for dim followed by staring base of index.
	   for(i=maxDepth-1; i >= 0 ; --i) //Kinda stored backward from what I expected.
	   {
		 v[j++]= (byte)( maxDimArray[i]);
		 v[j++]= (byte)((maxDimArray[i]>>>8) & 0xff);
		 v[j++]= (byte)((maxDimArray[i]>>>16) & 0xff);
		 v[j++]= (byte)((maxDimArray[i]>>>24) & 0xff);
		 
		 v[j++]= 0; //Only support starting address of zero
		 v[j++]= 0;
		 v[j++]= 0;
		 v[j++]= 0; 
	   }
	   //pvData  addjusted on C++ side.
	   v[28]=v[29]=v[30]=v[31]= 0;
		 v[28]= (byte)(j);
		 v[29]= (byte)((j>>>8) & 0xff);
		 v[30]= (byte)((j>>>16) & 0xff);
		 v[31]= (byte)((j>>>24) & 0xff);

	   //THE DATA FOR THE SAFEARRAY IS PACKED AFTER IT
	   //Now for the data which will be a variant for each element.  Note strings and true objects will need to be called back.
	   setVariantData(  arrayObject,   v, j,  0, new int[ maxDepth]);


	return v;
  }

   
  ArrayInfo( Object o)
  {
	if( o.getClass().isArray())
	{
	 String arrayClass= o.getClass().toString();
	 maxDepth= arrayClass.indexOf('[');
	 if(-1 != maxDepth)
	 {
	   arrayObject= o; 
	   arrayClass= arrayClass.substring(maxDepth);
	   for(maxDepth=0; arrayClass.charAt(maxDepth) == '['; ++maxDepth); 
	   type= arrayClass.charAt(maxDepth);
	   maxDimArray= new int[maxDepth];
	   dimOffset= new int[maxDepth];
	   investigate(o,0);
	   dimOffset[0]=1;
	   for(int i=1; i < maxDepth; ++i)
	   {
		dimOffset[i]= dimOffset[i-1] * maxDimArray[i-1];
	   }
	 }
	
	} 
  }
 }
  static BSFException dllLoadException= null; //Can hold an exception on from the loading of the c++ dll.
  static final String libName= "bsfactivescriptengine";  //C++ dll name.
  
  static final String  LANG_VBSCRIPT = "vbscript";
  static final String  LANG_PERLSCRIPT = "perlscript";
  static final String  LANG_JSCRIPT = "jscript";
  
  static {
	try
	{
	  System.loadLibrary (libName);
	}
	catch(java.lang.SecurityException e)
	{
	  dllLoadException= new BSFException(BSFException.REASON_OTHER_ERROR, "SecurityException loading library:" + libName + " " + e.getMessage(),e); 
	}
	catch(java.lang.UnsatisfiedLinkError e)
	{
	  dllLoadException= new BSFException(BSFException.REASON_OTHER_ERROR, "UnsatisfiedLinkError loading library:" + libName + " " + e.getMessage(),e); 
	}
	
  }
  byte [] css= null;// c++ active script engine pointer, saved as an object and passed into the 
  BSFManager bsfmgr=null; //Used by other methods during JNI callbacks.
  private Hashtable evalRet= null; //Used by languages which don't support expressions.
  protected String lang= null; //The script language this engine is running.

   /**
	* add an event listener
	*/
   public void addEventListener( Object bean, String event, String filter, String script) throws BSFException
   {
	 EngineUtils.addEventListener(bean, event, filter, this, bsfmgr, "ActiveScriptEngine", 0, 0, script);
   }
   /**	
   * Binds a method to an integer so it can be later referenced to invoke the method via callMethod. 
   *
   */
   public  final int bindMember(Object target, String name, short bindType) throws Exception
   {
	 return  JavaBean.bindMember( target.getClass(), name, bindType);
   }
  /**
   * Return an object from an extension.
   * @param method The name of the method to call.
   * @param args an array of arguments to be
   * passed to the extension, which may be either
   * Vectors of Nodes, or Strings.
   */
  public Object call (Object object, String method, Object[] args) 
														throws BSFException {
	StringBuffer sb = new StringBuffer (300);

	sb.append (object.toString());
	sb.append (".");
	sb.append (method);
	sb.append ("(");
	if (args != null) {
	  for (int i = 0; i < args.length; i++) {
	sb.append (args[i].toString ());
		if (i < args.length-1) {
		  sb.append (",");
		}
	  }
	}
	sb.append (")");
	return eval ("<internal>", -1, -1, sb.toString ());
  }
   /**
   *  This function and (BSFCOM) should be eliminated once support in BSF to call methods with arrays is present.
   *  Java does not support variable number of arguments so the arguments to these are packed in an array in C++ land.
   *  The same holds true for the two functions following this one. ALSO BSFCOM c++ object might be able to be eliminated in
   *  the process.
   */
   private final Object callBeanMethod(Object target, String methodName, Object[] args ) throws org.apache.bsf.BSFException 
   {
	  if(target.equals(this) && methodName.equals("callMethodViaBSF")){  return callMethodViaBSF((Object [])args[0]);} //We know this is the only funtion for this method.
	  if(target.equals(this) && methodName.equals("createBean")){  return createBean((Object [])args[0]);} //We know this is the only funtion for this method.
	  return EngineUtils.callBeanMethod(target, methodName, args);
   }
   /**	
   * Invokes the method assocaited with methodID on the bean with parameters in the array args. 
   *
   */
   public  final Object callMethod( Object bean, int methodID, Object[] args) throws Exception
   {
		return JavaBean.callMethod(this, bean, methodID, args);
   }
   /**
   *
   */
   final Object callMethodViaBSF(Object []args)  throws org.apache.bsf.BSFException 
   {
	  Object [] bsfargs= new Object[args.length-2];
	  if(args.length >2)System.arraycopy(args,2,bsfargs,0, args.length-2);
	  return EngineUtils.callBeanMethod(args[0], (String) args[1], bsfargs );
   }
   /**
   * createBean 
   *
   */
   public final Object createBean(Object []args)  throws org.apache.bsf.BSFException 
   {
	  Object [] bsfargs= new Object[args.length-1];
	  if(args.length >1)System.arraycopy(args,1,bsfargs,0, args.length-1);
	  return EngineUtils.createBean((String) args[0], bsfargs );
   }
   public static final Throwable  createBSFException( int reason, String msg, Throwable t)
   {
	 if(t != null) return new BSFException(reason, msg, t);
	 else return new BSFException(reason,msg);
   }
  /**
   * Declare a bean after the engine has been started. Declared beans
   * are beans that are named and which the engine must make available
   * to the scripts it runs in the most first class way possible.
   *
   * @param bean the bean to declare
   *
   * @exception BSFException if the engine cannot do this operation
   */
   public final void declareBean (BSFDeclaredBean bean) throws BSFException
   {
	if(isVBScript()) exec("<declareBean>", 0, 0, "SET " + bean.name + "=bsf.lookupBean(\"" + bean.name + "\") 'via declareBean");
	else if(isJScript()) exec("<declareBean>", 0, 0,"var " + bean.name + "=bsf.lookupBean(\"" + bean.name + "\"); //via declareBean");
	else if(isPerlScript()) exec("<declareBean>", 0, 0, "$"+bean.name + "=$bsf->lookupBean('" + bean.name + "'); #via declareBean");
	else throw new BSFException(BSFException.REASON_OTHER_ERROR, lang + " does not support declareBean."); 
   }
  /**
   * This is used by an application to evaluate a string containing
   * some expression. ActiveScript engines don't return anything .. so
   * the return value is awlays null.
   */
  public Object eval (String source, int lineNo, int columnNo, Object oscript) throws BSFException
  {

	if(!isPerlScript()) return nativeEval (css, source, lineNo, columnNo, oscript.toString (), true);
	else
	{  //ActiveState's Perl implementation does not seem to support this so this was added to make it work.
	   Integer key=new Integer(Thread.currentThread().hashCode());
	   nativeEval (css, "<bsf perl declare>",lineNo, columnNo, "$bsf->setEvalRet(" + oscript.toString () + "); #via eval", false); 
	   Object ret= evalRet.get(key);
	   if(ret== evalRet) ret = null;
	   evalRet.put(key, evalRet); //loose reference to whatever.
	   return ret;
	}
  }
  /**
   * This is used by an application to execute a string containing
   * a script to execute. ActiveScript engines don't return anything .. so
   * the return value is awlays null.
   */
  public void exec (String source, int lineNo, int columnNo, Object script) throws BSFException
  {
	//Run the script throw away any return code.
	synchronized(this)
	{
	 if(terminated()) throw new BSFException(BSFException.REASON_OTHER_ERROR, "Exec or eval called after engine termination!");
	}
	 nativeEval (css, source,  lineNo, columnNo, script.toString (), false);
  }
   protected void finalize() throws Throwable
   {

	 terminate();
	 super.finalize();
   }
  public void initialize (BSFManager mgr, String language, Vector declaredBeans) throws BSFException
  {
	if(null != dllLoadException) throw dllLoadException;
	synchronized(this)
	{
	 if(null != lang)
	 { //Been called before... this is bad.
	  lang=language;
	  throw new BSFException(BSFException.REASON_OTHER_ERROR, "Engine " + this + " initialized again"); 
	 }
	 lang= language;
	}

	super.initialize (mgr, language, declaredBeans);
	if(isPerlScript()) evalRet=  new Hashtable(); //Used by languages which don't support expressions.
	bsfmgr= mgr; //Save away so we can use duing JNI callback.


	nativeInit (lang, null , null ); //Does not return unless exception or this engine is terminated.
									 //Delared beans are not declared using ActiveX Script's AddItem anymore since
	   			      // this does not allow to undeclare these beans later.
	if(css == null)
	{ //Double check: nativeInit should have set this field!
	  throw  new BSFException(BSFException.REASON_OTHER_ERROR, "Engine " + this
	   + " failed to initialize native interface properly."); 
	}

	//Run a little script that sets up the declared beans.
	// NOTE: this is done this way as opposed to doing it with MS com addnamedItem because
	//       this allows for undeclare of these beans to work.
	if( 0 !=declaredBeans.size())
	{
	  String prefix= "";
	  String bsf= "";
	  String suffix= "";
	  String eos="";
	  String eosLast="";
	 if(isVBScript())
	 {
	   prefix= "SET ";
	   bsf= "=bsf.lookupBean(\"";
	   suffix= "\")";
	   eos=":";
	   eosLast="";
	 }
	 else if(isJScript())
	 {
	   prefix= "var ";
	   bsf= "=bsf.lookupBean(\"";
	   suffix= "\")";
	   eos=";";
	   eosLast=eos;
	 }
	 else if(isPerlScript())
	 {
	   prefix= "$";
	   bsf= "=$bsf->lookupBean('";
	   suffix= "')";
	   eos=";";
	   eosLast=eos;
	 }
	 else throw new BSFException(BSFException.REASON_OTHER_ERROR, lang + " does not support undeclareBean."); 

	 StringBuffer startup= new StringBuffer("");
	
	 int numDeclaredBeans= declaredBeans.size();
	 for(int i=0; i < numDeclaredBeans; ++i)
	 {
	   BSFDeclaredBean b=(BSFDeclaredBean)declaredBeans.elementAt(i);
	   startup.append(prefix+ b.name + bsf+ b.name + suffix + (i< (numDeclaredBeans-1)?  eos : eosLast));  
	 }
	 exec("<declareBean>", 0, 0, startup.toString());
	} 
	

  }
  protected final boolean isCaseSensitive() { return  isVBScript();}
  protected final boolean isJScript(){ return  lang.equalsIgnoreCase( LANG_JSCRIPT);}
  protected final boolean isPerlScript(){ return  lang.equalsIgnoreCase( LANG_PERLSCRIPT);}
  /*Unfortunately language identifiers necessary to handle language specific issues.*/
  protected final boolean isVBScript(){ return  lang.equalsIgnoreCase( LANG_VBSCRIPT);}
   /**
   * lookupBean 
   */
   public final Object lookupBean(String name) //  throws org.apache.bsf.BSFException 
   {
	 return bsfmgr.lookupBean(name);
   }
  private native Object nativeEval(byte[] css, String Source, int lineNo, int columnNo, String script, boolean evaluate) throws BSFException;
  /*Native COM support routines */  //should go else where but easier here for now... lazy
  static native void nativeIdispatchAddRef (byte[]IdispatchInterface) throws BSFException;
  static native void nativeIdispatchDeleteRef (byte[]IdispatchInterface) throws BSFException;
  /*Native routines*/
  private native void nativeInit (String lang, String declaredBeanNames, Object[]declaredBeans) throws BSFException; //If all goes well sets css
  private native byte[] nativeObjectToVariant(byte[] css, Object o) throws BSFException;
  private native byte[] nativeStingToBString(String s) throws BSFException;
  private native void nativeTerminate(byte[] css);
  /**
   * objectToVariant converts a java object to it's equivalent MS variant
   * representation.  Primitives are converted, objects and strings only have
   * their types set.  
   *
   * @param o the object which is to be converted to a variant.
   * @return a byte array that has the image of a variant. SEE MS docs. 
   *
   */
   private final byte[] objectToVariant( Object o) throws BSFException
   {
	 byte[] v= new byte[16]; //Size of a variant
	   
	   if( null== o)
	   { //to be safe.
		v[0] = 1; //VT_NULL
	v[1] = 0;
	   }
	   else if(o  instanceof java.lang.Boolean) //VT_R8
	   {
		v[0] = 11; //VT_BOOL
	v[1] = 0;
	byte x= (byte)( (((Boolean) o).booleanValue()) ? 0xff: 0);
	v[8]= x;
	v[9]= x;
	v[10]= x;
	v[11]= x;
	   }
	   else if(o  instanceof java.lang.Integer) //VT_R8
	   {
		v[0] = 3; //VT_I4
	v[1] = 0;
	int x= ((Integer)o).intValue();
	v[8]= (byte)x;
	v[9]= (byte)((x>>>8) & 0xff);
	v[10]= (byte)((x>>>16) & 0xff);
	v[11]= (byte)((x>>>24) & 0xff);
	   }
	   else if( o instanceof java.lang.String)
	   {
		 v[0] = 8; //VT_BSTR
		 v[1] = 0;
	   }
	   else if(o  instanceof java.lang.Long) //VT_R8
	   { //COM has no long type so promote it to double which can contain it.
		v[0] = 5; //VT_R8
	v[1] = 0;
	long x= Double.doubleToLongBits((double)(((Long)o).longValue()));
	v[8]= (byte)x;
	v[9]= (byte)((x>>>8) & 0xff);
	v[10]= (byte)((x>>>16) & 0xff);
	v[11]= (byte)((x>>>24) & 0xff);
	v[12]= (byte)((x>>>32) & 0xff);
	v[13]= (byte)((x>>>40) & 0xff);
	v[14]= (byte)((x>>>48) & 0xff);
	v[15]= (byte)((x>>>56) & 0xff);
	   }
	   else if(o instanceof java.lang.Short)
	   {
		v[0] = 2; //VT_I2
	v[1] = 0;
		int x= ((Short)o).intValue();
	v[8]= (byte)x;
	v[9]= (byte)((x>>>8) & 0xff);
	v[10]= (byte)((x>>>16) & 0xff);
	v[11]= (byte)((x>>>24) & 0xff);
	   }
	   else if(o instanceof java.lang.Float)
	   {
		v[0] = 4; //VT_R4
	v[1] = 0;
	int x= Float.floatToIntBits(((Float)o).floatValue());
	v[8]= (byte)x;
	v[9]= (byte)((x>>>8) & 0xff);
	v[10]= (byte)((x>>>16) & 0xff);
	v[11]= (byte)((x>>>24) & 0xff);
	   }
	   else if(o  instanceof java.lang.Double) //VT_R8
	   {
		v[0] = 5; //VT_R8
	v[1] = 0;
	long x= Double.doubleToLongBits(((Double)o).doubleValue());
	v[8]= (byte)x;
	v[9]= (byte)((x>>>8) & 0xff);
	v[10]= (byte)((x>>>16) & 0xff);
	v[11]= (byte)((x>>>24) & 0xff);
	v[12]= (byte)((x>>>32) & 0xff);
	v[13]= (byte)((x>>>40) & 0xff);
	v[14]= (byte)((x>>>48) & 0xff);
	v[15]= (byte)((x>>>56) & 0xff);
	   }
	   else if(o  instanceof java.lang.Byte) 
	   {
		v[0] = 17; //VT_UI1
	v[1] = 0;
	byte x= ((Byte)o).byteValue();
	v[8]= x;
	   }
	   else if(o  instanceof java.lang.Character) 
	   {
		v[0] = 17; //VT_UI1
	v[1] = 0;
	byte x= (byte) ((Character)o).charValue();
	v[8]= x;
	   }
	   else if(o  instanceof java.lang.Void) 
	   {
		v[0] = 1; //VT_NULL
	v[1] = 0;
	   }
	   else if( o.getClass().isArray())
	   {
		ArrayInfo ai= new ArrayInfo(o);
	v= ai.toVariant();
	   }
	   else
	   { //Anything else just pray it's an object
		v[0] = 9; //VT_DISPATCH for object
	v[1] = 0;

		byte[] cppref= null; 

		if(o  instanceof org.apache.bsf.engines.activescript.COMIDispatchBean ) 
	{
		  cppref = ((org.apache.bsf.engines.activescript.COMIDispatchBean )o).getIDispatchInterface();
		  System.arraycopy(cppref,0,v,8, cppref.length);
	}
		else if(o  instanceof org.apache.bsf.engines.activescript.vbEmpty) //Specific request to return back empty 
	{
		  v[0] = 0; //VT_EMPTY
		  v[1] = 0;
	}
	else
	{
		  cppref= nativeObjectToVariant(css, o); 
		  System.arraycopy(cppref,0,v,8, cppref.length);
	}
	
	   }
	 return v; 
   }
  public final void setEvalRet( Object ret)
  {
	evalRet.put( new Integer(Thread.currentThread().hashCode())  , ret != null ? ret : evalRet); //Had some problems setting the value to null.
  }
   public synchronized void terminate() 
   {	
	if(!terminated())
	{
	  byte[] css= this.css;
	  this.css= null;

	  bsfmgr=null; //Used by other methods during JNI callbacks.
	  evalRet= null; //Used by languages which don't support expressions.
	  lang=null;
	  nativeTerminate(css);  //Let c++ objects cleanup too.
	  super.terminate();
	}
   }
  private final boolean terminated() {return null== css;} //Indicates object has offically terminated.
   /**
	* Undeclare a previously declared bean.
	*
	* @param bean the bean to undeclare
	*
	* @exception BSFException if the engine cannot do this operation
	*/
   public void undeclareBean (BSFDeclaredBean bean) throws BSFException
   {
	if(isVBScript()) exec("<undeclareBean>", 0, 0, "SET " + bean.name + "=Nothing 'via undeclareBean");
	else if(isJScript()) exec("<undeclareBean>", 0, 0, bean.name + "=null; // via undeclareBean");
	else if(isPerlScript()) exec("<undeclareBean>", 0, 0, "undef " + bean.name + " ; #via undeclareBean");
	else throw new BSFException(BSFException.REASON_OTHER_ERROR, lang + " does not support undeclareBean."); 
 
   }
}
