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
#if !defined (ID418D7D1800111d3B2E2984512000000)
#define ID418D7D1800111d3B2E2984512000000 
#include "activescriptengine.h"
#include "BSFException.hpp"
#include "activescriptengine.hpp"


#include "javacom.hpp"
class ActiveScriptEngine;
class BSFCOM : public JavaCOM
{ 
public:
 BSFCOM(ActiveScriptEngine &_ase): JavaCOM( _ase, "< BSF >") 
  {
   addMethod(L"createBean",  NULL);      //DISPID=1
   addMethod(L"lookupBean",  NULL);      //DISPID=2 
   addMethod(L"callBeanMethod",  NULL); //DISPID=3 
   addMethod(L"addEventListener", NULL); //DISPID=4 
  }


 HRESULT STDMETHODCALLTYPE BSFCOM::Invoke(DISPID dispIdMember, REFIID riid, LCID lcid,
					  WORD wFlags, DISPPARAMS __RPC_FAR *pDispParams, VARIANT __RPC_FAR *result,
					  EXCEPINFO __RPC_FAR *pExcepInfo, UINT __RPC_FAR *puArgErr)
  {
   HRESULT hr= E_FAIL;
   assert( dispIdMember <= numberOfMethods()); //Warning script does call with DISPID 0 must return E_NOTIMPL
   if( dispIdMember < 1 || dispIdMember >numberOfMethods() ) return E_NOTIMPL;

   assert(pDispParams);

   switch( dispIdMember )
   {
   case 1: //createBean
   {
    if(pDispParams->cArgs < 1)
    {
     if(pExcepInfo)
     {
      pExcepInfo->scode=DISP_E_EXCEPTION; //?
      pExcepInfo->bstrSource=SysAllocString(L"BSF class");
      pExcepInfo->bstrDescription=SysAllocString(L"CreateBean requires at least one string parameter for class to create");
     }
	 
	   
     return DISP_E_EXCEPTION;
    }
    jobjectArray args = NULL;
    VARIANT vClassname=pDispParams->rgvarg[pDispParams->cArgs-1];
    if(vClassname.vt != VT_BSTR)
    {
     if(pExcepInfo)
     {
      pExcepInfo->scode=DISP_E_EXCEPTION; //?
      pExcepInfo->bstrSource=SysAllocString(L"BSF class");
      pExcepInfo->bstrDescription=SysAllocString(L"CreateBean first parameter to be a string");
     }
     return DISP_E_EXCEPTION; 
    }
	 
    return  ASE.dispatchJavaMethodA( "createBean", pDispParams, result, pExcepInfo, puArgErr);
   }
   break;
   case 2: //lookupBean
   { 
    HRESULT hr= S_OK;
    if(pDispParams->cArgs != 1)
    { 
     if(pExcepInfo)
     {
      pExcepInfo->scode=DISP_E_EXCEPTION; //?
      pExcepInfo->bstrSource=SysAllocString(L"BSF class");
      pExcepInfo->bstrDescription=SysAllocString(L"lookupBean first parameter to be a string");
     }
     return DISP_E_EXCEPTION;
    }
    //We now need to get the arguments set up for the call back into java via bsf
    VARIANT vBeanname=pDispParams->rgvarg[0];
    if(vBeanname.vt != VT_BSTR)
    {
     hr= DISP_E_EXCEPTION;
    }
    if( FAILED(hr))
    {
     if(pExcepInfo)
     {
      pExcepInfo->scode=DISP_E_EXCEPTION; //?
      pExcepInfo->bstrSource=SysAllocString(L"BSF class");
      pExcepInfo->bstrDescription=SysAllocString(L"lookupBean parameter needs to be a string");
     }
     return hr;
    }
    return  ASE.dispatchJavaMethod( "lookupBean",pDispParams, result, pExcepInfo, puArgErr);
	 
   }	 
   break;
   case 3: //callBeanMethod
   {
    if(pDispParams->cArgs < 2)
    {
     if(pExcepInfo)
     {
      pExcepInfo->scode=DISP_E_EXCEPTION; //?
      pExcepInfo->bstrSource=SysAllocString(L"BSF class");
      pExcepInfo->bstrDescription=SysAllocString(L"callBeanMethod needs bean and method name.");
     }
     return E_FAIL;
    }
    jobjectArray args = NULL;
    VARIANT vMethodName=pDispParams->rgvarg[pDispParams->cArgs-2];
    if(vMethodName.vt != VT_BSTR)
    {
     if(pExcepInfo)
     {
      pExcepInfo->scode=DISP_E_EXCEPTION; //?
      pExcepInfo->bstrSource=SysAllocString(L"BSF class");
      pExcepInfo->bstrDescription=SysAllocString(L"callBeanMethod second parameter needs to be a string..");
     }
     return E_FAIL; 
    }

    VARIANT *vTarget=&pDispParams->rgvarg[pDispParams->cArgs-1];
    IDispatch *TargetDisp= NULL;
    if(vTarget->vt == VT_DISPATCH)
    {
     TargetDisp= vTarget->pdispVal;
    }
    else if(vTarget->vt == (VT_VARIANT | VT_BYREF))
    {
     TargetDisp= vTarget->pvarVal->pdispVal;
    }
    else
    {
     if(pExcepInfo)
     {
      pExcepInfo->scode=DISP_E_EXCEPTION; //?
      pExcepInfo->bstrSource=SysAllocString(L"BSF class");
      pExcepInfo->bstrDescription=SysAllocString(L"callBeanMethod first parameter needs to be a javabean");
     }
	  
     return  DISP_E_EXCEPTION;
    } 

    return  ASE.dispatchJavaMethodA( "callMethodViaBSF",pDispParams, result, pExcepInfo, puArgErr);
   }
   break;
   default:
    return ASE.dispatchJavaMethod( ws2mbs(methodNameById(dispIdMember )),pDispParams, result, pExcepInfo, puArgErr);
   }
  }
};//endof class BSFCOM
#endif /* #define ID418D7D1800111d3B2E2984512000000 */


