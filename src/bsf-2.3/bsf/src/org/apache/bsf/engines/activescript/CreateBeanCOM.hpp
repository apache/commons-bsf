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

/*Class to wrapper BSF object.  This COM objects implements for the script all functionality
 *that is provided by the BSF object.
 */
#if !defined (I588e0360939e11d381ee24dd75000000)
#define I588e0360939e11d381ee24dd75000000
#include "activescriptengine.h"

#include "javacom.hpp"
class CreateBeanCOM : public JavaCOM
{ 
public:
 CreateBeanCOM(ActiveScriptEngine &_ase ): JavaCOM( _ase, "< CreateBean >") { }


 HRESULT STDMETHODCALLTYPE CreateBeanCOM::Invoke(DISPID dispIdMember, REFIID riid, LCID lcid,
						 WORD wFlags, DISPPARAMS __RPC_FAR *pDispParams, VARIANT __RPC_FAR *result,
						 EXCEPINFO __RPC_FAR *pExcepInfo, UINT __RPC_FAR *puArgErr)
  {
   JNIEnv *jenv= getJenv(); 
   HRESULT hr= E_FAIL;

   //Special case for a com object that implement a default member to create other beans.
   //This allows for:  Set foo= CreateBean("BarBean", "parm1", ... "parmN") //vbs does not supp. var args!
   if( dispIdMember != 0)
   {
    if(pExcepInfo)
    {
     pExcepInfo->scode=DISP_E_EXCEPTION; //?
     pExcepInfo->bstrSource=SysAllocString(L"CreateBean");
     pExcepInfo->bstrDescription=SysAllocString(L"No methods on CreateBean suppoted.");
    }
    return DISP_E_EXCEPTION;
   }
   assert(pDispParams);
   if(!pDispParams || pDispParams->cArgs < 1)
   {
    if(pExcepInfo)
    {
     pExcepInfo->scode=DISP_E_EXCEPTION; //?
     pExcepInfo->bstrSource=SysAllocString(L"CreateBean");
     pExcepInfo->bstrDescription=SysAllocString(L"CreateBean requires at least a single string parameter.");
    }
    return DISP_E_EXCEPTION;
   }
   if( VT_BSTR != pDispParams->rgvarg[pDispParams->cArgs-1].vt )
   {
    if(pExcepInfo)
    {
     pExcepInfo->scode=DISP_E_EXCEPTION; //?
     pExcepInfo->bstrSource=SysAllocString(L"CreateBean");
     pExcepInfo->bstrDescription=SysAllocString(L"CreateBean requires at least a single string parameter.");
    }
    return DISP_E_EXCEPTION;
   }
   return  ASE.dispatchJavaMethodA( "createBean", pDispParams, result, pExcepInfo, puArgErr);
  }
};//endof class CreateBeanCOM
#endif /* #define I588e0360939e11d381ee24dd75000000*/
