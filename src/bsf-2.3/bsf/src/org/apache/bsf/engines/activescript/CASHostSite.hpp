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

