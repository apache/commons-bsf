
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
#include "JavaOLEENUMVAR.hpp"

HRESULT STDMETHODCALLTYPE JavaOLEENUMVAR::Clone( IEnumVARIANT FAR* FAR*  ppEnum  )
{
  HRESULT hr= S_OK;
  JNIEnv *jenv= getJenv();
  jclass clazzPeerBean= jenv->GetObjectClass(peerBean);
  jmethodID mid=jenv->GetMethodID(clazzPeerBean,"Clone" , "()I");
  if(mid == NULL)
  {
   jenv->DeleteLocalRef(clazzPeerBean);
   hr= E_NOTIMPL;
  }
  else
  {
   jobject resobj= jenv->CallObjectMethod(clazzPeerBean,mid,peerBean , NULL);
  }
  
  return hr;
}
 
HRESULT STDMETHODCALLTYPE JavaOLEENUMVAR::Next( unsigned long  celt, VARIANT FAR*  rgVar,  unsigned long FAR*  pCeltFetched  )
{
  HRESULT hr= S_OK;
  DISPPARAMS dispparams;
  ZeroIt(dispparams);
  EXCEPINFO excepInfo;
  ZeroIt(excepInfo);
  GUID BogusGuid={0};
  if(pCeltFetched ) *pCeltFetched = 0;
  hr= Invoke(dispidNext, BogusGuid, 0, DISPATCH_METHOD, &dispparams, rgVar, &excepInfo, NULL);  
  if(rgVar->vt== VT_DISPATCH)
  {
    IJavaPeer *ipeer; 
    if(SUCCEEDED( rgVar->pdispVal->QueryInterface(IID_IJavaPeer, reinterpret_cast<void**>(&ipeer))))
    {
      jobject returnedBean;
      if(SUCCEEDED(ipeer->getPeerBean( &returnedBean)))
      {
        JNIEnv *jenv= getJenv();
        
        if(jenv->IsSameObject(peerBean, returnedBean)) 
	{
	  ipeer->Release();
	  return S_FALSE; 
	}  
      }
      ipeer->Release();
    }

  }
  if(pCeltFetched && SUCCEEDED(hr))  *pCeltFetched = 1;
  
  return hr;
}
        
HRESULT STDMETHODCALLTYPE JavaOLEENUMVAR::Reset()
{
  HRESULT hr= S_OK;
  DISPPARAMS dispparams;
  ZeroIt(dispparams);
  EXCEPINFO excepInfo;
  ZeroIt(excepInfo);
  GUID BogusGuid={0};
  VARIANT   rgVar; 
  ZeroIt(rgVar);
  hr= Invoke(dispidReset, BogusGuid, 0, DISPATCH_METHOD, &dispparams, &rgVar, &excepInfo, NULL);  
  
  return hr;
}

HRESULT STDMETHODCALLTYPE JavaOLEENUMVAR::Skip( unsigned long  celt  )
{
  HRESULT hr= S_OK;
  DISPPARAMS dispparams;
  ZeroIt(dispparams);
  VARIANTARG args;
  ZeroIt(args);
  EXCEPINFO excepInfo;
  ZeroIt(excepInfo);
  GUID BogusGuid={0};
  VARIANT   rgVar; 
  ZeroIt(rgVar);
  
  dispparams.rgvarg= &args;
  dispparams.cArgs= 1;
  args.vt= VT_I4;
  args.lVal= celt;
  hr= Invoke(dispidSkip, BogusGuid, 0, DISPATCH_METHOD, &dispparams, &rgVar, &excepInfo, NULL);  
  
  return hr;
}
//////////////////////////////////////////////////////////////////////////
// IUnknown interfaces
//////////////////////////////////////////////////////////////////////////

HRESULT STDMETHODCALLTYPE JavaOLEENUMVAR::QueryInterface(REFIID riid, void **ppv)
{
 if (riid == IID_IUnknown  )  *ppv = static_cast<JavaOLEENUMVAR*>(this); 
 else if (riid == IID_IDispatch)*ppv = static_cast<IDispatch*>(this); 
 else if ( IID_IJavaPeer == riid) *ppv= static_cast<IJavaPeer*>(this);
 else if ( IID_IEnumVARIANT == riid) *ppv= static_cast<IEnumVARIANT*>(this);
 else
 {
  *ppv = NULL;
  return E_NOINTERFACE;
 }
 static_cast<IUnknown*>((*ppv))->AddRef();
 return S_OK;
}

ULONG STDMETHODCALLTYPE JavaOLEENUMVAR::AddRef(void)
{
#if 0 //!defined(NDEBUG)
 std::cerr << "JavaOLEENUMVAR:Addref:" << this << ", RefCounter:" << refCounter+1 << std::endl <<std::flush;
#endif   
 return InterlockedIncrement(&refCounter);
}

ULONG STDMETHODCALLTYPE JavaOLEENUMVAR::Release(void)
{
#if 0 //!defined(NDEBUG)
 std::cerr << "JavaOLEENUMVAR:Release:" << this << ", RefCounter:" << refCounter-1 << std::endl <<std::flush;
#endif   
 if(InterlockedDecrement(&refCounter)) return refCounter;
 delete this;
 return 0;
}

/* This routine is used to create a JavaOLEENUMVAR */
extern "C" 
{
 JavaCOM *STDMETHODCALLTYPE createJavaOLEENUMVAR( ActiveScriptEngine  &_ase, jobject peerBean, JNIEnv *o_jnienv )
 {
  return new JavaOLEENUMVAR(_ase, peerBean, o_jnienv );
 }
}
