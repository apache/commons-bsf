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
/* Active Script Host Site */
#ifndef I3E8457C87BF111d3B2DB1A8321000000
#define I3E8457C87BF111d3B2DB1A8321000000
#include <cassert>
#include <windows.h>
#include <ACTIVSCP.H>

class ActiveScriptEngine;

class CASHostSite :  public IActiveScriptSite
{
protected:
 LONG refCounter;
 IUnknown **declaredObjects; // Interface to objects added to script at start up.
 LPOLESTR *declaredObjectNames; //Names of objectes added to script at start up.
 jobjectArray jdeclaredBeans;
 JNIEnv *jenv;
 ActiveScriptEngine &ASE; //The active script engine that created this scripting site.
 int (*comp)( const wchar_t *, const wchar_t *);
 static LPCOLESTR BSF_NAME;
 static LPCOLESTR CREATEBEAN_NAME;
public:
 CASHostSite(ActiveScriptEngine &ase, JNIEnv *_jenv );
 void addDeclaredBeans(IActiveScript *pIas, IActiveScriptParse *pIasp, jstring jdeclaredBeanNames, jobjectArray jdeclaredBeans  );
 ~CASHostSite()
  {
   assert(refCounter == 0); 
   for(LPOLESTR *dp= declaredObjectNames; *dp; ++dp)
   {
    free(*dp);
   }
   for( ; *declaredObjects; ++ declaredObjects)
   {
    (*declaredObjects)->Release();
   }
  }
 //////////////////////////////////////////////////////////////////////////
 // IUnknown interfaces
 //////////////////////////////////////////////////////////////////////////

 HRESULT STDMETHODCALLTYPE QueryInterface(REFIID riid, void **ppv)
  {
   if      (IID_IUnknown == riid)		*ppv= static_cast<IActiveScriptSite*>(this); 
   else if (IID_IActiveScriptSite == riid)	*ppv= static_cast<IActiveScriptSite*>(this); 
   else
   {
    *ppv = NULL;
    return E_NOINTERFACE;
   }
   static_cast<IUnknown*>((*ppv))->AddRef();
   return S_OK;
  }

 ULONG STDMETHODCALLTYPE AddRef(void)
  {
#if 0 //!defined(NDEBUG)
   std::cerr << "CASHostSite:Addref:" << this << ", RefCounter:" << refCounter+1 << std::endl <<std::flush;
#endif     
   return InterlockedIncrement(&refCounter);
  }

 ULONG STDMETHODCALLTYPE Release(void)
  {
#if 0 //!defined(NDEBUG)
   std::cerr << "CASHostSite:Release:" << this << ", RefCounter:" << refCounter-1 << std::endl <<std::flush;
#endif     
   if(InterlockedDecrement(&refCounter)) return refCounter;
   delete this;
   return 0;
  }

 //////////////////////////////////////////////////////////////////////////
 // IActieveScriptSite interfaces
 //////////////////////////////////////////////////////////////////////////

 HRESULT STDMETHODCALLTYPE GetItemInfo( 
  LPCOLESTR pstrName,
  DWORD dwReturnMask,
  IUnknown __RPC_FAR *__RPC_FAR *ppiunkItem,
  ITypeInfo __RPC_FAR *__RPC_FAR *ppti);

 HRESULT STDMETHODCALLTYPE OnScriptError(IActiveScriptError *pError);
 HRESULT STDMETHODCALLTYPE GetLCID(LCID *plcid)
  {
   *plcid= LOCALE_SYSTEM_DEFAULT; // 9;
   return S_OK;
  }
 HRESULT STDMETHODCALLTYPE GetDocVersionString(BSTR *pVersion)
  {
   *pVersion= SysAllocString(L"Rick did this"); 
   return S_OK;
  }
 HRESULT STDMETHODCALLTYPE OnScriptTerminate(const VARIANT *pv, const EXCEPINFO *pe)
  {
#if 0 // !defined(NDEBUG)
   std::cerr << "Script Terminated" << std::endl <<std::flush;
#endif     
   return S_OK;
  }
 HRESULT STDMETHODCALLTYPE OnStateChange(SCRIPTSTATE ScriptState)
  {
#if 0 // !defined(NDEBUG)
   std::cerr << "Script state change:" << ScriptState <<std::endl <<std::flush;
#endif     
   return S_OK;
  }
 HRESULT STDMETHODCALLTYPE OnEnterScript(void)
  {
  
#if 0 // !defined(NDEBUG)
   std::cerr << "Entering the Script" << std::endl <<std::flush;
#endif     
   return S_OK;
  }
 HRESULT STDMETHODCALLTYPE OnLeaveScript(void)
  {
#if 0 //!defined(NDEBUG)
   std::cerr << "Leaving the Script" << std::endl <<std::flush;
#endif     
   return S_OK;
  }


};//endof class CASHostSite.

#endif /*  I3E8457C87BF111d3B2DB1A8321000000*/

