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

import java.util.Vector;
import org.apache.bsf.*;
import org.apache.bsf.debug.util.DebugLog;

public final class COMIDispatchBean implements Cloneable
{

   
 private byte[] IDispatchInterface; //Byte array containing point to a COM IDispatch Interface.

 private COMIDispatchBean(){return;} //Should not be called.
 public  COMIDispatchBean( byte[] pInterface)
 {
   IDispatchInterface= new byte[pInterface.length]; 
   System.arraycopy(pInterface,0,IDispatchInterface,0, pInterface.length);
 }
 public Object clone() throws CloneNotSupportedException
 {
   COMIDispatchBean d = null;
   DebugLog.stdoutPrintln("cloning: "  + this, DebugLog.BSF_LOG_L3);
   try
   {

	 d= (COMIDispatchBean) this.getClass().newInstance();
	 d.IDispatchInterface= new byte[this.IDispatchInterface.length];
	 System.arraycopy(this.IDispatchInterface,0,d.IDispatchInterface,0, this.IDispatchInterface.length);
	 ActiveScriptEngine.nativeIdispatchAddRef(d.IDispatchInterface); 
   } catch( Exception e)
   {
	 throw new CloneNotSupportedException();
   }
   DebugLog.stdoutPrintln("cloning: returned : " + d, DebugLog.BSF_LOG_L3);
   return d;
 }
 public static COMIDispatchBean COMIDispatchBeanFactory( byte[] s) //Convient for c side to construct.
 {
   return new COMIDispatchBean(s);

 }
 protected void finalize() throws Throwable
 {
   
  if(null != IDispatchInterface)
  {
	byte[]  x= IDispatchInterface ;
	IDispatchInterface = null;
	ActiveScriptEngine.nativeIdispatchDeleteRef(x); 
  }  
 }
 public byte[] getIDispatchInterface() throws BSFException
 {
  ActiveScriptEngine.nativeIdispatchAddRef(IDispatchInterface); 
  return IDispatchInterface;
 }
 public String toString()
 {
   return this.getClass().toString() + ":" + this.hashCode() + IDispatchInterface[3] + IDispatchInterface[2] + IDispatchInterface[1] + IDispatchInterface[0];
 }
}
