/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
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

/* Class to dynamically wrapper a Java object into a COM object
 */
#if _MSC_VER > 1000
#pragma warning(disable:4786)  /*Very annoying warning that symbol exceeds debug info size*/
#pragma warning(disable:4503)  /*Very annoying warning that symbol exceeds debug info size*/
#endif // _MSC_VER > 1000/
#include <string>
#include <cstring>
#include <iostream>
#include <direct.h>
#include <io.h>
#include <TCHAR.H>
#include "ActiveScriptEngine.h"
#include "ActiveScriptEngine.hpp"
#include "JavaCOM.hpp"
#include "JNIUtils.h"
#include "dbgutils.h"
//////////////////////////////////////////////////////////////////////////
// GUIDs
//////////////////////////////////////////////////////////////////////////

// {9A197388-5B41-11d3-B2B4-D6E25F000000}
static const GUID IID_IJavaPeer = 
{ 0x9a197388, 0x5b41, 0x11d3, { 0xb2, 0xb4, 0xd6, 0xe2, 0x5f, 0x0, 0x0, 0x0 } };

JavaCOM::JavaCOM(ActiveScriptEngine  &_ase, jobject _o, JNIEnv *o_jnienv ):
 refCounter(0), ASE(_ase)
{
 o_jnienv=  o_jnienv ? o_jnienv : getJenv();
 peerBean= o_jnienv->NewGlobalRef(_o); //Make it a global ref so we support returning back beanland
 o_jnienv->DeleteLocalRef(_o);
 javaClass= ASE.getClass(o_jnienv, peerBean);
}//Endof JavaCOM
/*
JavaCOM::JavaCOM(ActiveScriptEngine  &_ase, jobject o ) : refCounter(0), ASE(_ase)
{
 JNIEnv *jenv= getJenv();
 peerBean= jenv->NewGlobalRef(o); //Make it a global ref so we support returning back beanland
 jenv->DeleteLocalRef(o);
 javaClass= ASE.getClass(jenv, peerBean);
}
*/
JavaCOM::JavaCOM(ActiveScriptEngine  &_ase, const char *className ):
 refCounter(0), ASE(_ase)
{
 JNIEnv *jenv= getJenv();
 peerBean=  NULL; 
 javaClass= ASE.getClass(className);
    
}//Endof JavaCOM

JNIEnv * JavaCOM::getJenv(){return ASE.getJenv();}

//////////////////////////////////////////////////////////////////////////
// IUnknown interfaces
//////////////////////////////////////////////////////////////////////////

HRESULT STDMETHODCALLTYPE JavaCOM::QueryInterface(REFIID riid, void **ppv)
{
 if (riid == IID_IUnknown  )  *ppv = static_cast<JavaCOM*>(this); 
 else if (riid == IID_IDispatch)*ppv = static_cast<IDispatch*>(this); 
 else if ( IID_IJavaPeer == riid) *ppv= static_cast<IJavaPeer*>(this);
 else
 {
  *ppv = NULL;
  return E_NOINTERFACE;
 }
 static_cast<IUnknown*>((*ppv))->AddRef();
 return S_OK;
}

ULONG STDMETHODCALLTYPE JavaCOM::AddRef(void)
{
#if 0 //!defined(NDEBUG)
 std::cerr << "JavaCOM:Addref:" << this << ", RefCounter:" << refCounter+1 << std::endl <<std::flush;
#endif   
 return InterlockedIncrement(&refCounter);
}

ULONG STDMETHODCALLTYPE JavaCOM::Release(void)
{
#if 0 //!defined(NDEBUG)
 std::cerr << "JavaCOM:Release:" << this << ", RefCounter:" << refCounter-1 << std::endl <<std::flush;
#endif   
 if(InterlockedDecrement(&refCounter)) return refCounter;
 delete this;
 return 0;
}

/////////////////////////////////////////////////////////////////////// 
// IDispatch
/////////////////////////////////////////////////////////////////////// 

HRESULT  STDMETHODCALLTYPE JavaCOM::GetTypeInfoCount( UINT __RPC_FAR *pctinfo)
{ //The name is misleading.  If you provide typinfo (via GetTypeInfo) return 1 else 0;
// CRTDBGBRK
 *pctinfo = 0;
 return S_OK;
}
     
HRESULT STDMETHODCALLTYPE JavaCOM::GetTypeInfo( UINT iTInfo, LCID lcid,
						ITypeInfo __RPC_FAR *__RPC_FAR *ppTInfo)
{
 //assert(0 /*GetTypeInfo called*/);
 // CRTDBGBRK
 return E_NOTIMPL;
}

HRESULT STDMETHODCALLTYPE JavaCOM::GetIDsOfNames( REFIID riid, LPOLESTR __RPC_FAR *rgszNames,
						  UINT cNames, LCID lcid, DISPID __RPC_FAR *rgDispId)
{
 //riid is always null and not used.
 //lcid  is localization code 
 assert(rgszNames);
 if(rgszNames == NULL) return E_POINTER; 
 assert(1== cNames);
 if(cNames != 1) return E_INVALIDARG;  
 assert(rgszNames[0]);
 if(rgszNames[0] == NULL) return E_POINTER; 
 // assert(lcid == LOCALE_SYSTEM_DEFAULT);  //JUST IGNORE IS BETTER per MS DOCS
 // if(lcid != LOCALE_SYSTEM_DEFAULT) return E_INVALIDARG ; 
 assert(rgDispId);
 if(rgDispId == NULL) return E_POINTER;
   
 addMethod(rgszNames[0], rgDispId); // for dynamic methods return type info is never used.
                                          
 if(!*rgDispId)
 {
  return E_NOTIMPL;
 }
  
 return S_OK;
}
     
/* Flags for IDispatch Invoke 
#define DISPATCH_METHOD          0x01
#define DISPATCH_PROPERTYGET     0x02
#define DISPATCH_PROPERTYPUT     0x04
#define DISPATCH_PROPERTYPUTREF  0x08
*/
#define DISPATCH_CASESENSITIVE   0x40
HRESULT STDMETHODCALLTYPE JavaCOM::Invoke( DISPID dispIdMember, REFIID riid, LCID lcid,
   WORD wFlags, DISPPARAMS __RPC_FAR *pDispParams, VARIANT __RPC_FAR *pVarResult,
    EXCEPINFO __RPC_FAR *pExcepInfo, UINT __RPC_FAR *puArgErr)
{
 assert(dispIdMember==0 || javaClass->validDispid(dispIdMember)); //Warning script does call with DISPID 0 must return E_NOTIMPL

 if( !javaClass->validDispid(dispIdMember)) return E_NOTIMPL;

 assert(pDispParams);

 if(!pDispParams) return E_INVALIDARG;
 // assert(pVarResult);                        //It appears it is set to null if no result needed!
 // if(!pVarResult) return E_INVALIDARG;

 //  assert(pDispParams->cNamedArgs==0); //I thought this should be the case, but for some strange reason
 //  if(pDispParams->cNamedArgs !=0) return E_NOTIMPL;  //vbscript does set this with some useless info
 // when property put meth is being done :-(
 if(wFlags &(DISPATCH_PROPERTYGET |DISPATCH_PROPERTYPUT|DISPATCH_PROPERTYPUTREF)   ) wFlags &= (DISPATCH_PROPERTYGET |DISPATCH_PROPERTYPUT|DISPATCH_PROPERTYPUTREF); 						       
 if((wFlags &DISPATCH_PROPERTYGET) /* && pDispParams->cArgs > 0*/  )
 {
  LPCOLESTR methodName=methodNameById(dispIdMember); 
  if(wcslen(methodName) >2 &&! _wcsnicmp(L"is", methodName,2)) wFlags=DISPATCH_METHOD ;
 }
 
 wFlags &= ~DISPATCH_CASESENSITIVE;
 wFlags |= (ASE.respectCase() ? DISPATCH_CASESENSITIVE : 0);  
 wFlags |= (ASE.isPerlScript() ? DISPATCH_PROPERTYGET|DISPATCH_PROPERTYPUT|DISPATCH_METHOD : 0);  
 
 HRESULT hr;
 hr= ASE.dispatchJavaMethod(peerBean, getMethodById(dispIdMember), wFlags,   pDispParams, pVarResult, pExcepInfo, puArgErr);

 return hr;
}

/////////////////////////////////////////////////////////////////////// 
// IJavaPeer
/////////////////////////////////////////////////////////////////////// 

HRESULT STDMETHODCALLTYPE JavaCOM::getPeerBean(jobject* pJobj)
{ 
// CRTDBGBRK
 assert(pJobj);
 if(pJobj == NULL) return E_POINTER;
 *pJobj=  peerBean;
 assert(*pJobj);
 return  *pJobj ? S_OK : E_FAIL;
}
/*NOTE: in getIDSofNames properties are called absent for their "get" or "set" prefix */
HRESULT STDMETHODCALLTYPE JavaClass::addMethod(LPCOLESTR name,  DISPID *dispid)
{
 long found=0;
 int (*caseComp)(const wchar_t*, const wchar_t *)= ASE.respectCase() ? wcscmp : _wcsicmp;
 //First check for methods that have been specifically assigned a DISPID


 for(int i= 0; i < assignedMethods.size() ; ++i)
 {
  if( 0==caseComp(assignedMethods[i]->getName(), name))
  {
   if(dispid) *dispid= assignedMethods[i]->getDISPID();
   return S_OK;
  }
 }

 for( i= 1; i < methods.size() && !found  ; ++i)
 {
  if( 0==caseComp(methods[i]->name, name)) found= i;
 }
  
 if(!found )
 {
  for(i=0; i < assignedMethods.size(); ++i)
  {
    if( assignedMethods[i]->getDISPID() == methods.size())
    {
     methods.push_back( assignedMethods[i]);
     i=0;
    }
  }

  methods.push_back( new Method( name, methods.size()));
  found= methods.size()-1;
 }
 if(dispid) *dispid= found;

 return S_OK;
}//endof javaCOM::addMethod

JavaCOM * JavaClass::createObject(ActiveScriptEngine  &_ase, jobject peerBean, JNIEnv *o_jnienv)
{
 JavaClass *objectsClass= _ase.getClass((o_jnienv ? o_jnienv : _ase.getJenv()), peerBean);
 return objectsClass->instantiate(_ase, peerBean, o_jnienv );
}
 JavaCOM * JavaClass::instantiate(ActiveScriptEngine  &_ase, jobject peerBean, JNIEnv *o_jnienv )
 {
   if(factory) return factory(_ase, peerBean, o_jnienv );
   else new JavaCOM(_ase, peerBean, o_jnienv );
 }
//Endof file
