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
#ifndef I310fad60d72f11d38283607bdb000000 
#define I310fad60d72f11d38283607bdb000000

#if _MSC_VER > 1000
#pragma once
#pragma warning(disable:4786)  /*Very annoying warning that symbol exceeds debug info size*/
#endif // _MSC_VER > 1000

#include <iostream>
#include <string>
#include <cstring>
#include <limits>
#include <vector>
#include <map>
#include <cassert>
#include <windows.h>
#include <tchar.h> 
#include <COMDEF.H>
// #include <oleauto.h>
#include <jni.h>
#include "ActiveScriptEngine.h"
#include "javacom.hpp"

class JavaOLEENUMVAR : public JavaCOM,  public IEnumVARIANT 
{
private:
 LONG refCounter;
protected:
 DISPID dispidClone;
 DISPID dispidNext;
 DISPID dispidReset;
 DISPID dispidSkip;
public:
 JavaOLEENUMVAR(ActiveScriptEngine  &_ase, jobject o, JNIEnv * o_jnienv ): JavaCOM(_ase,o,o_jnienv), refCounter(0L)
 {
  addMethod(L"Clone", &dispidClone);
  addMethod(L"Next", &dispidNext);
  addMethod(L"Reset", &dispidReset);
  addMethod(L"Skip", &dispidSkip);
 };
 /////////////////////////////////////////////////////////////////////// 
 // IUnknown
 /////////////////////////////////////////////////////////////////////// 
  HRESULT STDMETHODCALLTYPE QueryInterface( REFIID riid, void **ppv);

  ULONG   STDMETHODCALLTYPE AddRef(void);

  ULONG   STDMETHODCALLTYPE Release(void);
 /////////////////////////////////////////////////////////////////////// 
 // IEnumVARIANT
 /////////////////////////////////////////////////////////////////////// 
  HRESULT STDMETHODCALLTYPE Clone( IEnumVARIANT FAR* FAR*  ppEnum  );
   
  HRESULT STDMETHODCALLTYPE Next( unsigned long  celt, VARIANT FAR*  rgVar,  unsigned long FAR*  pCeltFetched  );
	  
  HRESULT STDMETHODCALLTYPE Reset();

  HRESULT STDMETHODCALLTYPE Skip( unsigned long  celt  );
 
};
#endif  //I310fad60d72f11d38283607bdb000000 
