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
