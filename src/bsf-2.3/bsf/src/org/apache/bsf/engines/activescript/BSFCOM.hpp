/* *****************************************************************
IBM LICENSES THE SOFTWARE TO YOU ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY
KIND. IBM HEREBY EXPRESSLY DISCLAIMS ALL WARRANTIES OR CONDITIONS, EITHER
EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OR
CONDITIONS OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. You are
solely responsible for determining the appropriateness of using this Software
and assume all risks associated with the use of this Software, including but
not limited to the risks of program errors, damage to or loss of data, programs
or equipment, and unavailability or interruption of operations. Some
jurisdictions do not allow for the exclusion or limitation of implied
warranties, so the above limitations or exclusions may not apply to you.

IBM will not be liable for any direct damages or for any special, incidental,
or indirect damages or for any economic consequential damages (including lost
profits or savings), even if IBM has been advised of the possibility of such
damages. IBM will not be liable for the loss of, or damage to, your records or
data, or any damages claimed by you based on a third party claim. Some
jurisdictions do not allow for the exclusion or limitation of incidental or
consequential damages, so the above limitations or exclusions may not apply to
you.
*******************************************************************/
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


