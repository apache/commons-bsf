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
