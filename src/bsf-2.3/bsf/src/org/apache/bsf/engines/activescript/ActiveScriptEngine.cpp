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
#pragma warning(disable:4786)  /*Very annoying warning that symbol exceeds debug info size*/
#include <cstdlib>
#include <sstream>

#include <string.h>
#include <iostream>
#include <deque>
#include <string>
#include <cassert>
#include <new>
#include <new.h>
#include <algorithm>

#include <jni.h>
#include "JNIUtils.h"
#include <process.h>
#include <windows.h>
#include "ActiveScriptEngine.h"
#include "ActiveScriptEngine.hpp"
#include "JavaCOM.hpp"
#include "BSFCOM.hpp"
#include "CreateBeanCOM.hpp"
#include "CASHostSite.hpp"
#include "com_ibm_bsf_engines_activescript_ActiveScriptEngine.h"

using namespace std;

DLLInit DLLInit::dllInit; //Do some simple initialization before all else.
jobject  variant2object (JNIEnv *jenv, VARIANT &var, bool *localRefCreated= NULL);


char ModuleName[MAX_PATH *2 ]={0};
HANDLE thisModule;
BOOL WINAPI DllMain(HINSTANCE hinstDll, DWORD fdwReason, LPVOID fImpLoad)
{
  if(fdwReason == DLL_PROCESS_ATTACH)
  { 
    thisModule= hinstDll;
    GetModuleFileName( hinstDll,  ModuleName, sizeof ModuleName);
  }


  return true;
  
}

/*Fix provided by Nev Wylie, The fuction below is use as a semaphore instead of
 * WaitForSingleObject because if the thread is a message queue thread it allows
 * window messages to still be processed.
 */

DWORD MsgWaitForSingleObject(HANDLE handle, DWORD timeOut)
{
 DWORD dwRet;
 MSG msg;

 while(1)
 {
   dwRet = MsgWaitForMultipleObjects(1, &handle, FALSE, timeOut, QS_SENDMESSAGE);
   if (dwRet == WAIT_OBJECT_0 + 1) { // some type of message occurred
     //
     // There is one or more window message available.  Dispatch them
     //
     while(PeekMessage(&msg,NULL,NULL,NULL,PM_REMOVE)) {
       TranslateMessage(&msg);
       DispatchMessage(&msg);
       //
       // Check to see if the object is signalled
       //
       if ((dwRet = WaitForSingleObject(handle, 0)) == WAIT_OBJECT_0) {
       break;
       }
     }
   } else {
     break;
   }
 }

 return dwRet;
}

/*Function get a C pointer from a Java Byte array*/
template <class T> T*   getCPtr(JNIEnv *env, jbyteArray peer, T* &r)
{
 T  *dp;
 jboolean iscopy;
 jbyte *jpeer= env->GetByteArrayElements(peer, &iscopy);
 memcpy(&dp, jpeer, sizeof dp);
 if(iscopy == JNI_TRUE) env->ReleaseByteArrayElements(peer,jpeer,0);
 r=dp;
 return dp;
}

VARTYPE variantType( SAFEARRAY v,  VARTYPE &vartype, const char * &classname)
{
  if(v.cDims >1) 
  {
   int dimSize=v.cDims;
   int sdimArraySize= sizeof(SAFEARRAY) + (dimSize-1) * sizeof(SAFEARRAYBOUND);
   SAFEARRAY *sdimArray= (SAFEARRAY*)_alloca(sdimArraySize);
   void* pvData=v.pvData;
   for(int dim=0; dim <dimSize; ++dim)
   {
    VARTYPE dimVartype= vartype;
    memset(sdimArray, 0, sdimArraySize);
    sdimArray->cDims= dimSize-1;
    sdimArray->fFeatures= v.fFeatures;
    sdimArray->cbElements= v.cbElements;
    for(int i=0; i < dimSize-1; ++i)
    {
     sdimArray->rgsabound[i]= v.rgsabound[i+1]; //Peel top layer array.
    }
    pvData= (void*) (((byte *) pvData) + v.rgsabound[0].cElements *v.cbElements);
    sdimArray->pvData= pvData;
    variantType(*sdimArray, dimVartype, classname);
    if(dimVartype == VT_VARIANT) return vartype= dimVartype;
    if (vartype != VT_EMPTY && vartype != dimVartype) return VT_VARIANT;
    vartype= dimVartype;
   }
  }
  else
  {
    for(int i=0; i<v.rgsabound[0].cElements;++i)
    {
     VARTYPE thistype;
     thistype= (reinterpret_cast<VARIANT*>(v.pvData)[i]).vt ;
     thistype&= (~VT_BYREF);
     if (vartype != VT_EMPTY && vartype != thistype) return VT_VARIANT;
     vartype= thistype;
    }
  }  
  return vartype;
}

jobject  NewVariantArray(JNIEnv *jenv, VARTYPE type, jsize size)
{
 jobject result = NULL;
 switch (type) {
 case VT_ERROR: 
 case VT_EMPTY:
 case VT_NULL:
  result = NULL;
  break;
 case VT_I1:
   result = jenv->NewByteArray(size); 
  break;
 case VT_I2:
   result = jenv->NewShortArray(size); 
  break;
 case VT_I4:
   result = jenv->NewIntArray(size); 
  break;
 case VT_I8:
   result = jenv->NewLongArray(size); 
  break;
 case VT_R4:
   result = jenv->NewFloatArray(size); 
  break;
 case VT_R8:
   result = jenv->NewDoubleArray(size); 
  break;
 case VT_BSTR:
  result= jenv->NewObjectArray(size, jenv->FindClass ("java/lang/String"), NULL);
  break;
 case VT_BOOL:
   result = jenv->NewBooleanArray(size); 
  break;
 case VT_VARIANT:
  result= jenv->NewObjectArray(size, jenv->FindClass ("java/lang/Object"), NULL);
  break;
 case VT_UI1:
   result = jenv->NewShortArray(size); 
  break;
 case VT_UI2:
   result = jenv->NewIntArray(size); 
  break;
 case VT_UI4:
   result = jenv->NewLongArray(size); 
  break;
 case VT_UI8:
   result = jenv->NewLongArray(size); 
  break;
 case VT_INT:
   result = jenv->NewIntArray(size); 
  break;
 case VT_UINT:
   result = jenv->NewLongArray(size); 
  break;
 case VT_ARRAY:
  assert(0);
  break;
 case VT_DISPATCH:
  break;
 case VT_UNKNOWN:
  break;
 default:
  assert(0);
 }//Endof switch
 return result;
}

void SetVariantArray(JNIEnv *jenv, VARIANT &var, jobject array, jsize pos)
{
 jobject element=NULL;
 switch (var.vt) {
 case VT_ERROR: 
 case VT_EMPTY:
 case VT_NULL:
  break;
 case VT_I1:
   jenv->SetByteArrayRegion(reinterpret_cast<jbyteArray>(array), pos, 1, reinterpret_cast< signed char*>(&var.bVal));
  break;
 case VT_I2:
   jenv->SetShortArrayRegion(reinterpret_cast<jshortArray>(array), pos, 1, &var.iVal);
  break;
 case VT_I4:
 case VT_INT:
   jenv->SetIntArrayRegion(reinterpret_cast<jintArray>(array), pos, 1, &var.lVal);
  break;
 case VT_I8:
   {
    __int64 x= var.lVal;
    jenv->SetLongArrayRegion(reinterpret_cast<jlongArray>(array), pos, 1, &x);
   }
  break;
 case VT_R4:
   jenv->SetFloatArrayRegion(reinterpret_cast<jfloatArray>(array), pos, 1, &var.fltVal);
  break;
 case VT_R8:
   jenv->SetDoubleArrayRegion(reinterpret_cast<jdoubleArray>(array), pos, 1, &var.dblVal);
  break;
 case VT_BSTR:
 case VT_DISPATCH:
 case VT_UNKNOWN:
 case VT_VARIANT:
  {
   bool localRefCreated= false;
   element= variant2object(jenv, var, &localRefCreated);
   jenv->SetObjectArrayElement(reinterpret_cast<jobjectArray>(array), pos, element );
   if( element && localRefCreated) jenv->DeleteLocalRef(element); 
  } 
  break;
 case VT_BOOL:
   {
    jboolean jb = var.boolVal ? JNI_TRUE : JNI_FALSE;
    jenv->SetBooleanArrayRegion(reinterpret_cast<jbooleanArray>(array), pos, 1, &jb);
   }
  break;
 case VT_UI1:
  {
   short x= V_UI1(&var);
   jenv->SetShortArrayRegion(reinterpret_cast<jshortArray>(array), pos, 1, &x);
  } 
  break;
 case VT_UI2:
  {
   long x= V_UI2(&var);
   jenv->SetIntArrayRegion(reinterpret_cast<jintArray>(array), pos, 1, &x);
  } 
  break;
 case VT_UINT:
 case VT_UI4:
  {
   __int64 x= V_UI4(&var);
   jenv->SetLongArrayRegion(reinterpret_cast<jlongArray>(array), pos, 1, &x);
  } 
  break;
 case VT_UI8:
  {
   __int64 x= V_UI4(&var);
   jenv->SetLongArrayRegion(reinterpret_cast<jlongArray>(array), pos, 1, &x);
  } 
  break;
 case VT_ARRAY:
 case VT_ARRAY | VT_BYREF:
  assert(0);
  break;
 default:
  if (V_ISBYREF (&var))
  {
   SetVariantArray(jenv, *reinterpret_cast<VARIANT*>(var.byref), array, pos);
  }
  else
    assert(0); //code error.
 }//Endof switch
}


jobject  variant2object (JNIEnv *jenv, VARIANT &var, bool *localRefCreated)
{
 IJavaPeer *peerInterface= NULL;
 jobject result = NULL;
 char *buf= NULL;
 if(localRefCreated) *localRefCreated= true; //this is mostly the case.
 switch (V_VT (&var)) {
 case VT_ERROR:    //Perl returns this for a variable that has not been assigned a value. For Java equate this to null
 case VT_EMPTY:
 case VT_NULL:
  if(localRefCreated) *localRefCreated= false; 
  result = NULL;
  break;
 case VT_I1:
  result = bsf_makeByte (jenv, (int) V_I1 (&var));
  break;
 case VT_I2:
  result = bsf_makeShort (jenv, (int) V_I2 (&var));
  break;
 case VT_I4:
  result = bsf_makeInteger (jenv, (int) V_I4 (&var));
  break;
 case VT_I8:
  /* where's V_I8??? */
  result = bsf_makeLong (jenv, (long) V_I4 (&var));
  break;
 case VT_R4:
  result = bsf_makeFloat (jenv, (float) V_R4 (&var));
  break;
 case VT_R8:
  result = bsf_makeDouble (jenv, (double) V_R8 (&var));
  break;
 case VT_BSTR:
  /* if its a string with the right stuff, retract the object */
  buf= ws2mbs(var.bstrVal);
  result = jenv->NewStringUTF (buf);
  break;
 case VT_BOOL:
  result = bsf_makeBoolean (jenv, (int) V_BOOL (&var));
  break;
 case VT_VARIANT:
  result = variant2object (jenv, *(V_VARIANTREF (&var)), localRefCreated);
  break;
 case VT_UI1:
  result = bsf_makeShort (jenv, (int) V_UI1 (&var));
  break;
 case VT_UI2:
  result = bsf_makeInteger (jenv, (int) V_UI2 (&var));
  break;
 case VT_UI4:
  result = bsf_makeLong (jenv, (long) V_UI4 (&var));
  break;
 case VT_UI8:
  /* where's V_UI8??? */
  result = bsf_makeLong (jenv, (long) V_UI4 (&var));
  break;
 case VT_INT:
  result = bsf_makeInteger (jenv, V_INT (&var));
  break;
 case VT_UINT:
  result = bsf_makeLong (jenv, (long) V_UINT (&var));
  break;
 case VT_ARRAY:
  if(var.parray->cDims >1) 
  {
   int dimSize=var.parray->cDims;
   int sdimArraySize= sizeof(SAFEARRAY) + (dimSize-1) * sizeof(SAFEARRAYBOUND);
   SAFEARRAY *sdimArray= (SAFEARRAY*)_alloca(sdimArraySize);
   jclass oclass=jenv->FindClass ("java/lang/Object"); 
   jobjectArray resultArray= jenv->NewObjectArray(var.parray->rgsabound[0].cElements, oclass , NULL);
   jenv->DeleteLocalRef(oclass);
   void* pvData=var.parray->pvData;
   int elementoffset=0;
   int elementInc=0;
   for(int dim=0; dim < dimSize -1; ++dim)
   {
     elementInc= var.parray->rgsabound[dim+1].cElements;
   }
   for(dim=0; dim < var.parray->rgsabound[0].cElements; ++dim)
   {
    VARIANT dimArray;
    ZeroIt(dimArray);
    V_VT (&dimArray)= VT_ARRAY;
    V_ARRAY(&dimArray)= sdimArray;
    memset(sdimArray, 0, sdimArraySize);
    sdimArray->cDims= dimSize-1;
    sdimArray->fFeatures= var.parray->fFeatures;
    sdimArray->cbElements= var.parray->cbElements;
    for(int i=0; i < dimSize-1; ++i)
    {
     sdimArray->rgsabound[i]= var.parray->rgsabound[i+1]; //Peel top layer array.
    }
    pvData= (void*) (((byte *) pvData) + elementoffset  *var.parray->cbElements);
    elementoffset += elementInc;
    sdimArray->pvData= pvData;
    jobject dimResult=variant2object(jenv, dimArray);   
    jenv->SetObjectArrayElement(reinterpret_cast<jobjectArray>(resultArray), dim, dimResult); 
    jenv->DeleteLocalRef(dimResult);
   }
   result= resultArray;
  }
  else
  {
   if(var.parray->fFeatures & FADF_BSTR)
   {
    //ARRAY OF STRINGS
    result= jenv->NewObjectArray(var.parray->rgsabound[0].cElements, jenv->FindClass ("java/lang/String"), NULL);
    for(int i=0; i<var.parray->rgsabound[0].cElements;++i)
    {
     jstring element;
     char *buf= ws2mbs((reinterpret_cast<BSTR *>(var.parray->pvData)[i]));
     element = jenv->NewStringUTF (buf);
     jenv->SetObjectArrayElement(reinterpret_cast<jobjectArray>(result), i, element);
     jenv->DeleteLocalRef(element);
    }
   }
   else if(var.parray->fFeatures & FADF_VARIANT)
   {
    const char *classname;
    VARTYPE vt= VT_EMPTY;
    vt= variantType( *var.parray,  vt, classname);
    result=  NewVariantArray(jenv, vt, var.parray->rgsabound[0].cElements);
    for(int i=0; i<var.parray->rgsabound[0].cElements;++i)
    {
     if( vt== VT_VARIANT)
     { //Mixed types
      bool localRefCreated=false;
      jobject element= variant2object(jenv, (reinterpret_cast<VARIANT*>(var.parray->pvData)[i]), &localRefCreated);
      jenv->SetObjectArrayElement(reinterpret_cast<jobjectArray>(result), i, element);
      if( element && localRefCreated ) jenv->DeleteLocalRef(element); 
     }
     else
      SetVariantArray(jenv,reinterpret_cast<VARIANT*>(var.parray->pvData)[i], result, i);
    }
   }
   else if((var.parray->fFeatures & FADF_UNKNOWN) ||(var.parray->fFeatures& FADF_DISPATCH) )
   {
    result= jenv->NewObjectArray(var.parray->rgsabound[0].cElements, jenv->FindClass ("java/lang/Object"), NULL);
    for(int i=0; i<var.parray->rgsabound[0].cElements;++i)
    {
     VARIANT V;
     ZeroIt(V);
     V_VT(&V)= (var.parray->fFeatures & FADF_UNKNOWN) ? VT_UNKNOWN : VT_DISPATCH;
     V_UNKNOWN(&V)= reinterpret_cast<IUnknown**>(var.parray->pvData)[i];
     jobject jelement= variant2object(jenv, V);
     jenv->SetObjectArrayElement(reinterpret_cast<jobjectArray>(result), i, jelement);
    }
   }
   else
   {
    assert(0); //
    throwBSFException << "Unknown Variant array type " << var.parray->fFeatures  << BSFException::THROWIT;
   } 
  } 
  break;
 case VT_DISPATCH:
  if(V_DISPATCH(&var) == NULL){  result= NULL;} //null object was placed in insted
  else if(FAILED((V_DISPATCH(&var))->QueryInterface(IID_IJavaPeer, reinterpret_cast<void**>(&peerInterface))))
  {
    //Must be a real COM object.
   IUnknown *iunk; //Querying the IUnkown interface does an addref for the pointer passed to java
                   //So we don't release it!
   result= NULL;
   if(!ASSERT_FAILED((V_DISPATCH(&var))->QueryInterface(IID_IUnknown, reinterpret_cast<void**>(&iunk))))
   {
     void *x =  reinterpret_cast<void*>( V_DISPATCH(&var));
     jboolean iscopy= JNI_FALSE;
     jbyteArray jByteArray= jenv->NewByteArray(sizeof x);
     jbyte *jr= jenv->GetByteArrayElements(jByteArray, &iscopy);
     memcpy(jr, &x, sizeof x);
     if(iscopy == JNI_TRUE) jenv->ReleaseByteArrayElements(jByteArray,jr,0);
 
 
     jclass objectClass = jenv->FindClass ("com/ibm/bsf/engines/activescript/COMIDispatchBean");
     assert(objectClass);
     if(objectClass)
     {
      jmethodID mid=jenv->GetStaticMethodID(objectClass,"COMIDispatchBeanFactory","([B)Lcom/ibm/bsf/engines/activescript/COMIDispatchBean;");
      assert(NULL != mid);
      if(mid)
      {
       result= jenv->CallStaticObjectMethod(objectClass,mid, jByteArray);
      }
      jenv->DeleteLocalRef(objectClass);
     }
   }
  }
  else
  { //This is a com object wrappered as a java object.
   if(ASSERT_FAILED(peerInterface->getPeerBean(&result))) result= NULL;
   peerInterface->Release();
   if(localRefCreated)  *localRefCreated=   false;
  }
  break;
 case VT_UNKNOWN:
  if(V_UNKNOWN(&var) == NULL){ result= NULL;} //null object was placed in insted
  else if(ASSERT_FAILED((V_UNKNOWN(&var))->QueryInterface(IID_IJavaPeer, reinterpret_cast<void**>(&peerInterface))))
  {
   result= NULL;
   if(localRefCreated)  *localRefCreated=   false;
  }
  else
  {
   if(ASSERT_FAILED(peerInterface->getPeerBean(&result))) result= NULL;
   peerInterface->Release();
   if(localRefCreated)  *localRefCreated=   false;
  }
  break;
 default:
  if (V_ISBYREF (&var))
  {
   if((var.vt & (VT_BYREF|VT_ARRAY)) == (VT_BYREF|VT_ARRAY))
   {
    //var.vt&= (~VT_BYREF);
    VARIANT arraybyref;
    ZeroIt(arraybyref);
    arraybyref.vt = VT_ARRAY;
    arraybyref.parray= *var.pparray;
    result= variant2object(jenv, arraybyref, localRefCreated);
   }
   else
   {
    result = variant2object (jenv, *((VARIANT *)var.byref), localRefCreated);
   }
  }
  else
  {
   assert(0);
   throwBSFException << "Unknown Variant array type " << V_VT (&var)  << BSFException::THROWIT;
   break;
  }
 }
 if(!result && localRefCreated) *localRefCreated= false;
 return result;
}




class DeleteIt
{
public:
 DeleteIt(){}
 void operator()( pair<string, JavaClass*> x) { delete x.second;}
};

ActiveScriptEngine::ActiveScriptEngine( JNIEnv *_jenv, jobject thisobj,
					const char* lang, HANDLE &wait ): jenv(_jenv), javaActiveScriptEngine(thisobj),
	 g_langstr(lang), terminateRequest(false),
  engineThreadId( ::GetCurrentThreadId() ), dispatchedMethods(0),
  engineRuntimeError()
{
 HRESULT hr= E_FAIL;
 currentProxyThread= NULL;						
 jclass  ActiveScriptEngineClass= jenv->GetObjectClass(javaActiveScriptEngine);
 if(NULL==ActiveScriptEngineClass)throwBSFException << "Internal error Java script engine class not found!" << BSFException::THROWIT;

 InitializeCriticalSection( &EngineControl );
 engineWork=CreateEvent(NULL, FALSE, FALSE, NULL);
 waitEngineTerminate=CreateEvent(NULL, FALSE, FALSE, NULL);

 hr = CoInitializeEx (0,::COINIT_MULTITHREADED );
 if(ASSERT_FAILED(hr)) throwBSFException << "CoInitialize Failed="<< hr << BSFException::THROWIT;
    
 _respectCase= (_stricmp(g_langstr, "vbscript")) ? true : false;

 /*Create the Script Engine appropriate for desired language.*/
 CLSID clsid; //CLSID assoc. with lang.
 hr= CLSIDFromProgID(mbs2ws(g_langstr), &clsid);
 if(ASSERT_FAILED(hr)) throwBSFException << "Script Language not found."<< hr << BSFException::THROWIT;

 //Create a active script host site.
 css=new CASHostSite(*this, jenv); //Our script site
 if(!css) throwBSFException <<"Allocation of CSCriptSite failed" <<BSFException::THROWIT;
 css->AddRef();


 // IActiveScriptParse *pasp= NULL;
 hr= CoCreateInstance(clsid,0,CLSCTX_ALL,IID_IActiveScriptParse,reinterpret_cast<void**>(&pIasp));
 if(ASSERT_FAILED(hr)) throwBSFException << "Failed to create script engine.."<< hr << BSFException::THROWIT;
 hr= pIasp->QueryInterface(IID_IActiveScript, reinterpret_cast<void**>(&pIas));
 if(ASSERT_FAILED(hr)) throwBSFException << "Script engine initialization failure."<< hr << BSFException::THROWIT;

 hr= pIasp->InitNew(); //Put Script in SCRIPTSTATE_INITIALIZED
 if(ASSERT_FAILED(hr)) throwBSFException << "Script engine initialization failure."<< hr << BSFException::THROWIT;
 hr= pIas->SetScriptSite( css ); //Tell script engine of our script site
 if(ASSERT_FAILED(hr)) throwBSFException << "Script engine initialization failure."<< hr << BSFException::THROWIT;

 css->addDeclaredBeans(pIas, pIasp, NULL, NULL);
  
 //Set Script to connected state, so all outbound events are connected.
 hr=pIas->SetScriptState(SCRIPTSTATE_CONNECTED);
 if(ASSERT_FAILED(hr))throwBSFException<<"Script Engine could not enter connected state." <<hr<<BSFException::THROWIT;

   
 //Create a new byte array and stuff the pointer to our newly created object into it.
 jboolean iscopy;
 jbyteArray jByteArray= jenv->NewByteArray(sizeof this);
 jbyte *jr= jenv->GetByteArrayElements(jByteArray, &iscopy);
 void *x =this;
 memcpy(jr, &x, sizeof x);
 if(iscopy == JNI_TRUE) jenv->ReleaseByteArrayElements(jByteArray,jr,0);
  
 jfieldID jFcc= jenv->GetFieldID(ActiveScriptEngineClass, "css", "[B"); 
 jenv->SetObjectField(javaActiveScriptEngine,jFcc, jByteArray);
 jenv->DeleteLocalRef(ActiveScriptEngineClass);

 //Release thread waiting on engine start.

 jenv->DeleteGlobalRef(javaActiveScriptEngine);//Don't stop it from being grabage collected.
 javaActiveScriptEngine=NULL; 
 SetEvent(wait); //Stop engine from doing more work than necessary
 wait=0; //Dont need it anymore.

 try
 {
  run();
 } 
 catch(exception e)
 {

  BSFException b(__FILE__, __LINE__);
  b << e.what();
  engineRuntimeError= b;
  jenv->ExceptionClear(); //Let a calling thread to invoke a call record the error.
  terminateRequest=true;
 }
 catch(BSFException &e)
 { 
  engineRuntimeError = e;
  jenv->ExceptionClear(); //Let a calling thread to invoke a call record the error.
  terminateRequest=true;
 }
#ifdef NDEBUG  //during debug this even catches the exception to start the debugger :-(
 catch(...)
 {
  BSFException b(__FILE__, __LINE__);
  b << L"Unhandled C++ exception"; 
  engineRuntimeError = b;
  jenv->ExceptionClear(); //Let a calling thread to invoke a call record the error.
  terminateRequest=true;
 }
#endif

 while(!requestList.empty())
 {
  ThreadRequest *rq=requestList[0];
  requestList.pop_front();
  rq->tooLate(mbs2ws(engineRuntimeError.what()));
 }

 pIasp->Release();
 pIasp= NULL;
 css->Release();
 css= NULL;
 pIas->Close();
 pIas->Release(); 
 pIas= NULL;
 CoUninitialize();
 if(javaActiveScriptEngine )
 {
  jenv->DeleteGlobalRef(javaActiveScriptEngine); 
  javaActiveScriptEngine = NULL;
 }

 for_each(javaClasses.begin(), javaClasses.end(), DeleteIt() );
 JavaVM *vm;
 jenv->GetJavaVM(&vm);
 vm->DetachCurrentThread();
 SetEvent(waitEngineTerminate);
 _endthread();

}

void ActiveScriptEngine::run()
{
 for(;!terminateRequest;) //Engine loop
 {
  MsgWaitForSingleObject(engineWork, INFINITE);
  if(!terminateRequest) processPendingWork();
 }

 while(!requestList.empty())
 {
  ThreadRequest *rq=requestList[0];
  requestList.pop_front();
  rq->tooLate();
 }

}//Endof: ActiveScriptEngine::run()

void ActiveScriptEngine::processPendingWork(ThreadRequest *pending) //Try to do work while method is running in java.
{
 if(terminateRequest) return;  //Don't start something we don't want to finish.
 do //Engine loop
 {
  processPendingWork();
  if(pending->isJavaPending())MsgWaitForSingleObject(engineWork, 1000L);
 }while(pending->isJavaPending() && !terminateRequest);
 if(!terminateRequest) processPendingWork(); //Java method for this thread is done or engine is in termination mode.
}
void ActiveScriptEngine::processPendingWork()
{
 if(terminateRequest) return;  //Don't start something we don't want to finish.
 ThreadRequest *rq;
 do
 {
  EnterCriticalSection(&EngineControl);
  rq= NULL;
  if(!terminateRequest)
  {
   if(!requestList.empty())
   {
    rq=requestList[0];
    requestList.pop_front();
   }
   else
   {
    if(javaActiveScriptEngine && !dispatchedMethods)
    { //There are no request to execute a script and no calls back into Java so we 
     // delete our reference to the Java activescript engine.
     jenv->DeleteGlobalRef(javaActiveScriptEngine); //Delete it so it can be garbaged.
     javaActiveScriptEngine=NULL; //Delete it so it can be garbaged.
    } 
    ResetEvent(engineWork); //Stop engine from doing more work than necessary
   }
  }

  LeaveCriticalSection(&EngineControl);

  if(rq && !terminateRequest)
  {
   VARIANT rc;
   ZeroIt(rc);
   ThreadRequest *prev_rq= currentProxyThread;
   currentProxyThread= rq; //We are now working for a new request.
   if(NULL==javaActiveScriptEngine)
   {
    javaActiveScriptEngine= rq->jenv->NewGlobalRef(rq->jASE); //Switch to a global ref since ultimately we need to pass to another thread. 
   }  
   //Finally, let the language engine do its job.
   HRESULT hr=pIasp->ParseScriptText(rq->script,0,0,0,0,0,SCRIPTTEXT_ISVISIBLE | (rq->evaluate?SCRIPTTEXT_ISEXPRESSION : 0 ),rq->evaluate ? &rc : NULL ,&rq->scriptExcepInfo);
   currentProxyThread->Done(hr, rc); //Done executing this.
   currentProxyThread= prev_rq; //Go back to processing old.
  }
 } while(rq && !terminateRequest);
}

void ActiveScriptEngine::ThreadRequest::dispatchJavaMethod(jobject _bean, char* _methodName, WORD notused, DISPPARAMS __RPC_FAR *_pDispParams,
VARIANT __RPC_FAR *_pVarResult, EXCEPINFO __RPC_FAR *_pExcepInfo, UINT __RPC_FAR *_puArgErr)
{ //Called only by engine.
 bean =_bean;
 methodName = _methodName;
 method= NULL;
 flags= notused;
 pDispParams= _pDispParams;
 pVarResult= _pVarResult;
 javaExcepInfo= _pExcepInfo;
 puArgErr= _puArgErr; 
 javaPending=true;
 SetEvent(Block);//Get request thread running.
  
}
void ActiveScriptEngine::ThreadRequest::dispatchJavaMethod(jobject _bean, JavaClass::Method *m, WORD _flags ,DISPPARAMS __RPC_FAR *_pDispParams,
VARIANT __RPC_FAR *_pVarResult, EXCEPINFO __RPC_FAR *_pExcepInfo, UINT __RPC_FAR *_puArgErr)
{ //Called only by engine.
 bean =_bean;
 methodName = NULL;
 method= m;
 flags= _flags;
 pDispParams= _pDispParams;
 pVarResult= _pVarResult;
 javaExcepInfo= _pExcepInfo;
 puArgErr= _puArgErr; 
 javaPending=true;
 SetEvent(Block);//Get request thread running.
  
}

jobject ActiveScriptEngine::eval(ThreadRequest &tr)
{ //called only by request thread.
 jobject retObject= NULL;
 BSFException termException;
 EnterCriticalSection(&EngineControl);
 termException= engineRuntimeError;
 if(!termException)
 {
  requestList.push_back(&tr);
  wakeUpEngine();
 } 
 LeaveCriticalSection(&EngineControl);
  
  
 if(termException) throw termException;
 else  retObject= tr.Wait();
 return retObject;
}

jobject ActiveScriptEngine::ThreadRequest::Wait()
{ //Run by requesting thread.
 jobject jobjectReturn=NULL;
 MsgWaitForSingleObject(Block, INFINITE);
 ResetEvent(Block);//Get request thread running.

 while(!done)
 { 

  jobject resobj;
  //Must need to process a java method. see:ThreadRequest::dispatchJavaMethod
  if( this->throwObj) //Incase it was left over from before.
  {
    jenv->DeleteGlobalRef(this->throwObj);
    this->throwObj=NULL; 
  }
  jobjectArray args = NULL;
  //We now need to get the arguments set up for the call back into java via bsf
  jclass objectClass = jenv->FindClass ("java/lang/Object");
  args= jenv->NewObjectArray(pDispParams->cArgs, objectClass, NULL);
  jenv->DeleteLocalRef(objectClass);
  for(int i=0 ; i < pDispParams->cArgs; ++i)
  {  //DISPPARMS are stored reversed
   IJavaPeer *peerInterface= NULL;
   int ii= pDispParams->cArgs-1-i;
   bool localRefCreated=false; 
   jobject arg=  variant2object(jenv, pDispParams->rgvarg[ii], &localRefCreated);
   jenv->SetObjectArrayElement (args, i,  arg);
   if( arg && localRefCreated)
   {
      jenv->DeleteLocalRef(arg);
   }
  }

  //Now do jni stuff to invoke method.
  jenv->ExceptionClear();
  if(methodName)
  {
   jclass clazzASE= jenv->GetObjectClass(jASE);
   jstring jMethodName= jenv->NewStringUTF(methodName); 
   jmethodID mid=jenv->GetMethodID(clazzASE,"callBeanMethod" , "(Ljava/lang/Object;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;");
   jenv->DeleteLocalRef(clazzASE);
   resobj= jenv->CallObjectMethod(jASE,mid,bean ,jMethodName,args);
   jenv->DeleteLocalRef(jMethodName);
  }
  else
  {
   resobj= method->invoke(jenv, jASE, bean, flags|0x80, args);
  }
  jenv->DeleteLocalRef(args); 
  jthrowable throwObj;
  if(NULL != (throwObj=jenv->ExceptionOccurred()))
  {
   this->throwObj= (jthrowable) jenv->NewGlobalRef(throwObj);
   hr_java= DISP_E_EXCEPTION;
   jenv->ExceptionClear();
   if(javaExcepInfo)
   {
    string msg("Courtesy of Java: method name:");
    if(!methodName) methodName= ws2mbs(method->getName());
    javaExcepInfo->scode=DISP_E_EXCEPTION; //?
    javaExcepInfo->bstrSource=SysAllocString(mbs2ws((msg+ methodName).c_str()));
   }	 
   jclass throwObjClazz = jenv->GetObjectClass(throwObj);
   if(throwObjClazz && javaExcepInfo)
   {
    string stringMsg;
    jmethodID mid= jenv->GetMethodID(throwObjClazz, "getMessage", "()Ljava/lang/String;");
    jmethodID midToString= jenv->GetMethodID(jenv->GetObjectClass(throwObjClazz), "toString", "()Ljava/lang/String;");
    if(midToString)
    {
     jstring msgClassExcp= (jstring) jenv->CallObjectMethod(throwObjClazz, midToString, NULL);
     if(msgClassExcp)
     {
       jboolean iscopy;
       const char* cmsgClassExcp= jenv->GetStringUTFChars(msgClassExcp, &iscopy);
       if(cmsgClassExcp && *cmsgClassExcp)
       {
        stringMsg.append("Java Exception: ");
        stringMsg.append(cmsgClassExcp);
        stringMsg.append(" ");
       }	
       if(iscopy == JNI_TRUE) jenv->ReleaseStringUTFChars(msgClassExcp, cmsgClassExcp);
     }
    }
    jenv->DeleteLocalRef(throwObjClazz);
    if(mid)
    {
     jstring msg= (jstring) jenv->CallObjectMethod(throwObj, mid, NULL);
     if(msg)
     {
      jboolean isCopy;
      const char* cmsg= jenv->GetStringUTFChars(msg, &isCopy);
      if(cmsg && *cmsg)
      {
       stringMsg.append(cmsg);
      }
      if(isCopy == JNI_TRUE) jenv->ReleaseStringUTFChars(msg, cmsg);
     }
    }
    if(stringMsg.length())
    {
       javaExcepInfo->bstrDescription=SysAllocString(mbs2ws(stringMsg.c_str()));
    }
    else
    {
      javaExcepInfo->bstrDescription=SysAllocString(L"Exception contains no information");
    }
   }

   jenv->ExceptionClear();
  } 
  else hr_java= S_OK;

  if(pVarResult)
  {
   if(resobj)
   {
    jclass jcASE= jenv->FindClass("com/ibm/bsf/engines/activescript/ActiveScriptEngine");
    jmethodID jcMidobjectToVariant= jenv->GetMethodID( jcASE,
							     "objectToVariant","(Ljava/lang/Object;)[B");
     
    jbyteArray jVariant= reinterpret_cast<jbyteArray>(jenv->CallObjectMethod(jASE,
										   jcMidobjectToVariant, resobj));   
    jenv->DeleteLocalRef(jcASE);
    //Get Variant from the byte array returned.
    jboolean iscopy= JNI_FALSE;;
    jbyte *jBarrayVariant= jenv->GetByteArrayElements(jVariant, &iscopy);
    *pVarResult= *(reinterpret_cast<VARIANT*>(jBarrayVariant)); //copy results localy

    /*Do special processing for strings, java objects, and arrays*/
    if(pVarResult->vt== VT_BSTR)
    {//Variant is a string. get the string data
     jboolean iscopy;
     const char *s = jenv->GetStringUTFChars(reinterpret_cast<jstring>(resobj), &iscopy);
     V_BSTR(pVarResult)= SysAllocString(mbs2ws(s));
     if(iscopy == JNI_TRUE) jenv->ReleaseStringUTFChars(reinterpret_cast<jstring>(resobj), s);
    }
    else if(pVarResult->vt== (VT_ARRAY|VT_VARIANT))
    { //An array... yuk
     SAFEARRAY *retArray= reinterpret_cast<SAFEARRAY *>(jBarrayVariant + sizeof *pVarResult);
     int arrayDataOffset=  reinterpret_cast<int>(retArray->pvData);
     int retArraySize= arrayDataOffset - sizeof *pVarResult;
     pVarResult->parray= reinterpret_cast<SAFEARRAY*>(CoTaskMemAlloc( retArraySize));
     memcpy(pVarResult->parray, retArray, retArraySize);
     // *pVarResult->parray= *retArray;
     jsize javaArraySize= jenv->GetArrayLength(jVariant);
     int retArrayDataSize= javaArraySize- arrayDataOffset;
     pVarResult->parray->pvData= reinterpret_cast<BYTE*>(CoTaskMemAlloc( retArrayDataSize));
     VARIANT *s=(VARIANT*)(jBarrayVariant+ arrayDataOffset);
     VARIANT *sdone=(VARIANT*)(jBarrayVariant+ javaArraySize);

     for( VARIANT *d= (VARIANT*)(pVarResult->parray->pvData);
       s < sdone; *d++= *s++);
    
    }
    if(iscopy == JNI_TRUE) jenv->ReleaseByteArrayElements(jVariant,jBarrayVariant,0);
   }
   else
   {
    pVarResult->vt=VT_NULL;
   }
  }

  javaPending=false; 
  ASE.wakeUpEngine();
  if(!done)
  {
   MsgWaitForSingleObject(Block, INFINITE); //Continue our wait.
   ResetEvent(Block);//Get request thread running.
  }
 }

 if(NULL == (jenv->ExceptionOccurred()))
 {  //No registered exception in for java.  See if there are any exceptions in COM or script to report.
  EXCEPINFO *exp= NULL; 
  if(siteExcepInfo.wCode || siteExcepInfo.scode){
   exp=&siteExcepInfo;
   if(siteExcepInfo.pfnDeferredFillIn != NULL){ (*(siteExcepInfo.pfnDeferredFillIn))( &siteExcepInfo);}
  }
  if(scriptExcepInfo.wCode || scriptExcepInfo.scode)
  {
   if(scriptExcepInfo.pfnDeferredFillIn != NULL){ (*(scriptExcepInfo.pfnDeferredFillIn))( &scriptExcepInfo);}
   exp= &scriptExcepInfo;
  }
    
  if(exp)
  {
   
   jthrowable javaException= this->throwObj;
   this->throwObj=NULL; //Have reported
   BSFException  e(throwObj);
     e<< "Scripting engine failure\n"<<
    (siteExcepInfo.bstrSource ? ws2mbs(siteExcepInfo.bstrSource) :(scriptExcepInfo.bstrSource ? ws2mbs(scriptExcepInfo.bstrSource): "")) <<
    ":\t" <<( scriptName? *scriptName : "") << " " 
     << (-1!= linenumber ? linenumber+lineOffset : -1)
     <<":" << (-1 != linepos ? linepos+ colOffset : -1) << "\n" <<
    (exp->bstrDescription ? ws2mbs(exp->bstrDescription): "" )<<  "\n" <<
    ws2mbs((errline ? errline : L"")) << "\n" <<
    "(scode=0x" << std::hex << exp->scode << "\t"<< "wcode=0x" << exp->wCode << ")\n" <<
    javaException <<
    BSFException::THROWIT;
      
  }
  else if(evaluate)
  {
   jobjectReturn= variant2object (jenv, evaluateReturn );
  }
 }
 return jobjectReturn;
}

//Structure to pass data from one thread to the other.
struct engineThreadRtnData
{
 HANDLE wait;
 JavaVM *vm;
 jobject javaEngine;
 const char* langStr;
 BSFException errorReason;
};

/** Kicks off thread engine  needed by _beginthread.
*/
static void __cdecl engineThreadRtn( void *p)
{
 _PNH _old_new_handler= _set_new_handler(my_new_handler);
 JNIEnv *jenv= NULL;
 engineThreadRtnData *pTd= (reinterpret_cast<engineThreadRtnData*>(p)); 
 HANDLE wait= pTd->wait;
 #ifdef JNI_VERSION_1_2
 pTd->vm->AttachCurrentThread(reinterpret_cast<void**>(&jenv), "");
 #else
 pTd->vm->AttachCurrentThread(&jenv, "");
 #endif
 try
 {
  new ActiveScriptEngine(jenv, pTd->javaEngine, pTd->langStr, wait );
 }   
 catch(exception e)
 {
  BSFException b(__FILE__, __LINE__);
  b << e.what();
  pTd->errorReason = b;
 }
 catch(BSFException &e)
 {
  pTd->errorReason = e;
 }
#ifdef NDEBUG  //during debug this even catches the exception to start the debugger :-(
 catch(...)
 {
  BSFException b(__FILE__, __LINE__);
  pTd->errorReason << "Unhandled C++ exception";
  pTd->errorReason = b;
 }
#endif
 if(wait) SetEvent(wait); //Stop engine from doing more work than necessary
 _endthread();
     
}


extern "C" JNIEXPORT void JNICALL 
Java_com_ibm_bsf_engines_activescript_ActiveScriptEngine_nativeInit
  (JNIEnv *jenv, jobject thisobj, jstring lang, jstring jdeclaredBeanNames,
  jobjectArray jdeclaredBeans)
{
 CRTDBGBRK
  engineThreadRtnData td;
 td.wait=CreateEvent(NULL, TRUE, FALSE, NULL);
 td.javaEngine= jenv->NewGlobalRef(thisobj);
 jint jrc= jenv->GetJavaVM(&td.vm);
 assert(!jrc);

 /* save the language string */
 const char *langstr = jenv->GetStringUTFChars (lang, 0);
 td.langStr = strdup (langstr);
 jenv->ReleaseStringUTFChars (lang, langstr);



 _PNH _old_new_handler= _set_new_handler(my_new_handler);
      
 try
 {
  if(-1 == _beginthread(engineThreadRtn,0, &td))
   throwBSFException << "Could not create a new thread for scripting engine" << BSFException::THROWIT;
  MsgWaitForSingleObject(td.wait, INFINITE);
  CloseHandle(td.wait);
 }
 catch(exception e)
 {
  setBSFExceptionState(jenv, __FILE__, __LINE__, e.what());
 }
 catch(BSFException &e)
 { 
  e.setJNIException(jenv);
 }
#ifdef NDEBUG  //during debug this even catches the exception to start the debugger :-(
 catch(...)
 {
  setBSFExceptionState(jenv, __FILE__, __LINE__, "Unhandled C++ exception");
 }
#endif
 _set_new_handler(_old_new_handler);
 if(td.errorReason)
 {
  td.errorReason.setJNIException(jenv);;
 }
   
 return;
}

extern "C" JNIEXPORT jbyteArray JNICALL Java_com_ibm_bsf_engines_activescript_ActiveScriptEngine_nativeObjectToVariant
  (JNIEnv *jenv, jobject thisobj, jbyteArray cssobj, jobject o)
{
  jbyteArray ret= NULL;
  ActiveScriptEngine *pASE = NULL;
  getCPtr(jenv, cssobj, pASE);
  JavaCOM *e= JavaClass::createObject(*pASE, o, jenv);
     
  //Give back to COM the IDispatch interface.
  VOID *pIDispatch= NULL;
  HRESULT hr_java= e->QueryInterface(IID_IDispatch, &pIDispatch);
  if(!ASSERT_FAILED(hr_java))
  {
    jboolean iscopy= JNI_FALSE;
    ret = jenv->NewByteArray(sizeof pIDispatch);
    jbyte *jr= jenv->GetByteArrayElements(ret, &iscopy);
    void *x =pIDispatch;
    memcpy(jr, &x, sizeof x);
    if(iscopy == JNI_TRUE) jenv->ReleaseByteArrayElements(ret,jr,0);

  }
 return ret;
}
extern "C" JNIEXPORT void JNICALL Java_com_ibm_bsf_engines_activescript_ActiveScriptEngine_nativeIdispatchAddRef
(JNIEnv *jenv, jclass, jbyteArray byteArrayIDispatch)
{
  IDispatch *pIDispatchInterface= NULL;
  getCPtr(jenv, byteArrayIDispatch, pIDispatchInterface);
  pIDispatchInterface->AddRef();
}
extern "C" JNIEXPORT void JNICALL Java_com_ibm_bsf_engines_activescript_ActiveScriptEngine_nativeIdispatchDeleteRef
(JNIEnv *jenv, jclass, jbyteArray byteArrayIDispatch)
{
  IDispatch *pIDispatchInterface= NULL;
  getCPtr(jenv, byteArrayIDispatch, pIDispatchInterface);
  pIDispatchInterface->Release();
  
}
extern "C" JNIEXPORT jbyteArray JNICALL Java_com_ibm_bsf_engines_activescript_ActiveScriptEngine_nativeStingToBString
  (JNIEnv *jenv, jobject thisObj, jstring s)
{
  jbyteArray ret= NULL;

  jboolean iscopy=JNI_FALSE;
  const char *d = jenv->GetStringUTFChars(s, &iscopy);
  BSTR dBstr= SysAllocString(mbs2ws(d));
  if(iscopy == JNI_TRUE) jenv->ReleaseStringUTFChars(s,d);

  //Now that we have the Bstring we need a java byte array to return it.
  ret = jenv->NewByteArray(sizeof dBstr);
  jbyte *jr= jenv->GetByteArrayElements(ret, &iscopy);

  void *x =dBstr;
  memcpy(jr, &x, sizeof x);
  if(iscopy == JNI_TRUE) jenv->ReleaseByteArrayElements(ret,jr,0);
  return ret;
}
/*
 * Class:     com_ibm_bsf_engines_activescript_ActiveScriptEngine
 * Method:    nativeEval
 * Signature: (Ljava/lang/Object;Lcom/ibm/bsf/BSFManager;Ljava/lang/String;)Ljava/lang/String;
 */
jobject JNIEXPORT JNICALL
Java_com_ibm_bsf_engines_activescript_ActiveScriptEngine_nativeEval
 (JNIEnv *jenv, jobject thisobj, jbyteArray cssobj, jstring scriptsName, jint lineNo, jint columnNo,
 jstring script, jboolean evaluate)
{
 jobject returnObject= NULL;
 _PNH _old_new_handler= _set_new_handler(my_new_handler);
 try{

  const char *scriptstr;

  ActiveScriptEngine *pASE = NULL;
  getCPtr(jenv, cssobj, pASE);

  /*Get script text from java and place into a wide char str. */
  jboolean isCopy= JNI_FALSE;
  scriptstr = jenv->GetStringUTFChars (script, &isCopy);
  LPOLESTR scriptTextW= mbs2LPOLESTR(scriptstr);
  if(isCopy == JNI_TRUE) jenv->ReleaseStringUTFChars (script, scriptstr);

  const char *scriptsNamestr= NULL;
  string scriptsNameS;
  if(scriptsName)
  {
    isCopy= JNI_FALSE;
    scriptsNamestr = jenv->GetStringUTFChars (scriptsName, &isCopy);
    scriptsNameS= scriptsNamestr;
    if(isCopy == JNI_TRUE)jenv->ReleaseStringUTFChars (scriptsName, scriptsNamestr);
  }
  
  ActiveScriptEngine::ThreadRequest tr( jenv, thisobj, *pASE,  scriptsName ? &scriptsNameS : NULL,  lineNo, columnNo, scriptTextW, JNI_TRUE == evaluate);
  returnObject= pASE->eval(tr);
  free (scriptTextW);

 }
 catch(exception e)
 {
  setBSFExceptionState(jenv, __FILE__, __LINE__, e.what());
 }
 catch(BSFException &e)
 {
  e.setJNIException(jenv);
 }
#ifdef NDEBUG  //during debug this even catch the exception to start the debugger :-(
 catch(...)
 {
  // cerr << "Unhandled C++ exception."<<endl;
  setBSFExceptionState(jenv, __FILE__, __LINE__, "Unhandled C++ exceptiong");
 }
#endif
 _set_new_handler(_old_new_handler);
 return returnObject;
}

ActiveScriptEngine:: ~ActiveScriptEngine()
{
 if(!terminateRequest) terminate();
}

void ActiveScriptEngine::terminate() 
{
 terminateRequest= true;
 if(engineThreadId != GetCurrentThreadId()) //Unlikely, but just to be on the safe side.
 {
  EXCEPINFO e; 
  ZeroIt(e);
  e.scode=DISP_E_EXCEPTION; //?
  e.bstrSource=SysAllocString(L"ActiveScriptEngine");
  e.bstrDescription=SysAllocString(L"Script terminated due to BSF language engine termination.");
  CoInitializeEx (0,::COINIT_MULTITHREADED );
  if(pIas) pIas->InterruptScriptThread(SCRIPTTHREADID_ALL, &e, 0 );
  CoUninitialize();
  wakeUpEngine();  //Wake up the engine thread if necessary
  MsgWaitForSingleObject(waitEngineTerminate, INFINITE);
 }
}
JNIEXPORT void JNICALL Java_com_ibm_bsf_engines_activescript_ActiveScriptEngine_nativeTerminate
  (JNIEnv *jenv, jobject, jbyteArray cssobj)
{ //Note that java side  should make sure this only gets called once.
 ActiveScriptEngine *pASE;
 getCPtr(jenv, cssobj, pASE);
 if(pASE) delete pASE;
}


LPCOLESTR CASHostSite::BSF_NAME= L"bsf";
LPCOLESTR CASHostSite::CREATEBEAN_NAME= L"CreateBean";

HRESULT STDMETHODCALLTYPE CASHostSite::OnScriptError(IActiveScriptError *pError)
{
 EXCEPINFO pexcepinfo; 
 DWORD notused=0;
 LONG linepos =0;
 LONG lineno;
 BSTR texterr=0;
 HRESULT hr;
 ZeroIt(pexcepinfo);

 hr= pError->GetSourcePosition(&notused, reinterpret_cast<ULONG*>( &lineno), &linepos);
 if(ASSERT_FAILED(hr))
 {
  linepos= 0;
  lineno= -1;
 }
  
 hr= pError->GetSourceLineText(&texterr);
 if(FAILED(hr)) texterr= NULL; //SysAllocString(L""); //If error occurs in java this will return failed
 hr= pError->GetExceptionInfo( &pexcepinfo );
 if(SUCCEEDED(hr)) ASE.throwSiteException(pexcepinfo.bstrSource, pexcepinfo.bstrDescription,
					  pexcepinfo.scode,lineno, linepos, texterr);

 return hr;
}

CASHostSite::CASHostSite(ActiveScriptEngine &ase,JNIEnv *_jenv ):
      refCounter(0L), ASE(ase), declaredObjects(NULL), declaredObjectNames(NULL), jenv(_jenv), jdeclaredBeans (NULL)
{
 comp= ASE.respectCase() ? wcscmp  : _wcsicmp;
};

void CASHostSite::addDeclaredBeans(IActiveScript *pIas,IActiveScriptParse *pIasp, jstring jdeclaredBeanNames, jobjectArray _jdeclaredBeans )
{
 jdeclaredBeans= _jdeclaredBeans;
 //Access the java declared beans and create an associated COM OBJECT for each.
 int numDeclaredBeans= 0; // jenv->GetArrayLength(jdeclaredBeans); CAN NOT USE THIS IN JSP's SINCE IT EXPECTS REDEFINE OR UNDEFINE THE BEAN WHICH MS SCRIPT ENGINES DO NOT ALLOW!!!
 //Create an array of unknown meth ptrs. for each COM obj that represent a declared bean
 declaredObjects=   new IUnknown*[numDeclaredBeans+3];
 memset(declaredObjects, 0, sizeof (IUnknown*)* (numDeclaredBeans+3));
 //Create an array of names for the objects that match previous array
 declaredObjectNames= new LPOLESTR [ numDeclaredBeans+3];
 memset(declaredObjectNames, 0, sizeof LPOLESTR * (numDeclaredBeans +3));


 int i=0;
 if(numDeclaredBeans)
 {
  char *declaredBeanNamesStr;
  /*Get the string that has all the declared bean names sep. by a space*/
  jboolean isCopy;   
  const char *cs=jenv->GetStringUTFChars(jdeclaredBeanNames,&isCopy); 
  declaredBeanNamesStr= _strdup(cs);
  if(JNI_TRUE==isCopy) jenv->ReleaseStringUTFChars(jdeclaredBeanNames,cs); 

  HRESULT hr;
  char *declaredName= declaredBeanNamesStr;
  char *end;
  //loop through declared beans.*/
  for(;i<numDeclaredBeans; ++i, declaredName=++end)
  {
   end= strchr(declaredName, ' ');
   assert(end); 
   if(!end)
    throwBSFException << "internal error unbalanced declared bean names "<< i << BSFException::THROWIT;
   *end='\0';
   declaredObjectNames[i]=mbs2LPOLESTR(declaredName);
   //Let Script engine become aware of our declared object
   hr=pIas->AddNamedItem(declaredObjectNames[i], SCRIPTITEM_ISVISIBLE); //|SCRIPTITEM_ISSOURCE
   if(ASSERT_FAILED(hr)) throwBSFException << "Adding bean " << declaredName << "failed. "<< hr << BSFException::THROWIT;
  }
  free(declaredBeanNamesStr);
 }
  
 declaredObjectNames[i]= _wcsdup( BSF_NAME); 
 //Let Script engine become aware of our object
 HRESULT hr= pIas->AddNamedItem( declaredObjectNames[i], SCRIPTITEM_ISVISIBLE);
 if(ASSERT_FAILED(hr)) throwBSFException << "Adding bean CreateBean failed. "<< hr << BSFException::THROWIT;
 ++i;

 if(ASE.isPerlScript())
 { //ActiveState's PerlScript does not seem to support objects that use dispid 0
  EXCEPINFO scriptExcepInfo;
  HRESULT hrp=pIasp->ParseScriptText(L"sub CreateBean { return $bsf->createBean(@_); }",
				     0,0,0,0,0,SCRIPTTEXT_ISVISIBLE,0,&scriptExcepInfo);
 }
 else
 {  //CreateBean is an object that has dispid 0 implemented this allows for creating object thru CreateBean() method.
     
  declaredObjectNames[i]= _wcsdup( CREATEBEAN_NAME); 
  //Let Script engine become aware of our object
  hr= pIas->AddNamedItem( declaredObjectNames[i], SCRIPTITEM_ISVISIBLE);
  if(ASSERT_FAILED(hr)) throwBSFException << "Adding bean CreateBean failed. "<< hr << BSFException::THROWIT;
  ++i;
 }

 declaredObjectNames[i]= NULL;
};
  //////////////////////////////////////////////////////////////////////////
  // IActieveScriptSite interfaces
  //////////////////////////////////////////////////////////////////////////

HRESULT STDMETHODCALLTYPE CASHostSite::GetItemInfo( 
             LPCOLESTR pstrName,
             DWORD dwReturnMask,
             IUnknown __RPC_FAR *__RPC_FAR *ppiunkItem,
             ITypeInfo __RPC_FAR *__RPC_FAR *ppti)
{

 if (dwReturnMask & SCRIPTINFO_ITYPEINFO) return E_NOTIMPL; //Without using connection points doing this appears to be ok.
 if (dwReturnMask & ~SCRIPTINFO_IUNKNOWN) return S_FALSE; //Don't know what is wanted.
 if(ppiunkItem == NULL) return E_POINTER;
 *ppiunkItem= NULL;
 for(LPOLESTR *fname=declaredObjectNames; fname && *fname; ++fname)
 {
  if(!comp(*fname, pstrName))
  {//found it
   int index=  fname - declaredObjectNames;
   if(declaredObjects[index])
   {
    *ppiunkItem=declaredObjects[index]; 
    declaredObjects[index]->AddRef(); 
    return S_OK;
   }
      
   JavaCOM *bean= NULL;
   if(!comp(*fname, CREATEBEAN_NAME))
    bean= new CreateBeanCOM(ASE); //Special bean with dispid 0 implemented to create other beans.
   else if(!comp(*fname, BSF_NAME))
    bean= new BSFCOM(ASE);
   else
   {
    bean= JavaClass::createObject( ASE, jenv->GetObjectArrayElement(jdeclaredBeans,index));
   }

   HRESULT hr = bean->QueryInterface(IID_IUnknown, reinterpret_cast<void**>(&declaredObjects[index]));
   if(! ASSERT_FAILED(hr))
   {
    *ppiunkItem=declaredObjects[index]; 
    declaredObjects[index]->AddRef(); 
   }
   return hr;

	
  }
 }
 assert(0 /*shouldn't be here*/);

 return S_FALSE; 
}

extern "C" 
{
 JavaCOM *STDMETHODCALLTYPE oleautomationobject( ActiveScriptEngine  &_ase, jobject peerBean, JNIEnv *o_jnienv )
{
  return new JavaCOM(_ase, peerBean, o_jnienv );
}
}
