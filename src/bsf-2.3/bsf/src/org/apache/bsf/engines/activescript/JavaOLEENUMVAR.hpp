
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
