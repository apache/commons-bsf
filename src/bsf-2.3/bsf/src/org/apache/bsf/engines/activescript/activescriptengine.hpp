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
#if !defined (II60B6BF92809E11d3B2E4BA39F8000000 )
#define II60B6BF92809E11d3B2E4BA39F8000000 
#include <deque>
#include<string>
#include <malloc.h>
#include <windows.h>
#include <dbgutils.h>
#include <BSFException.hpp>
#include "javacom.hpp"
#include "CASHostSite.hpp"
class CASHostSite;

class ActiveScriptEngine
{
private:
 ActiveScriptEngine(){};
 ActiveScriptEngine(const ActiveScriptEngine &) {};
public:
 ~ActiveScriptEngine(); //Deletes the space
 void terminate();           //Stops all.
 std::string startup;
 /*A helper class used to track a thread request to execute a script */
 class ThreadRequest
 {
  friend class ActiveScriptEngine; 
  HANDLE Block; //Used by engine to block and wake up requesting thread.
  JNIEnv *jenv; //This belongs to the requesting thread.
  jobject jASE;  //this is local ref on requesting thread.
  ActiveScriptEngine &ASE; //The active script engine this threadrequest belongs to.

  //Script evaluation variables.
  LPOLESTR script;  //The script to execute.
  bool done;    //Set to true at start of engine validating script.  Set to false by engine when done with script.
  bool evaluate;  //true if script will be evaluated for a return value
  VARIANT evaluateReturn; //The return value. 
  EXCEPINFO scriptExcepInfo; //Exception recorded by invoking script.
  EXCEPINFO siteExcepInfo; //Exception recorded by the scripting site.
  LONG  linenumber;  //Line number. Seems to be off by 1
  LONG  linepos; //Position in the line.
  BSTR  errline; //Possible the line itself.
  HRESULT hr;  //Com return code.

  //Script's Java callback request parameters.
  jobject bean; //Target bean for the method.
  char* methodName; //Name of method to call.
  JavaClass::Method  *method;  //The method object to call if the method name is NULL.
  WORD      flags; //Get, Put, Method, Case sensitive.
  DISPPARAMS __RPC_FAR *pDispParams;  //Parmaters
  VARIANT __RPC_FAR *pVarResult;  //The result of the callback.
  EXCEPINFO __RPC_FAR *javaExcepInfo; //Error information
  jthrowable throwObj;
  UINT __RPC_FAR *puArgErr; 
  HRESULT hr_java;  //Com return code error.
  bool javaPending; //True indicates the engine has dispatched this thread back into Java.
  const int lineOffset;  //Add this to the actual reported error
  const int colOffset;   //Add this to the actual reported error.
  const std::string *scriptName;  //Add this to the error information

  JNIEnv *getJenv(){return jenv;} //The requesting threads JNIEnv

 public:
  bool isJavaPending(){ return javaPending;} //Thread is running in java for the script.
  ThreadRequest(JNIEnv *_jenv, jobject jase, ActiveScriptEngine &ase, std::string *_scriptName, int _lineOffset, int _colOffset, LPOLESTR s, bool _evaluate):
   jASE(jase), ASE(ase), jenv(_jenv), script(s), evaluate(_evaluate), done(false), hr(E_FAIL), javaPending(false),
   scriptName(_scriptName), lineOffset(_lineOffset), colOffset(_colOffset) 
   {
    Block=CreateEvent(NULL, FALSE, FALSE, NULL);
    ZeroIt(siteExcepInfo); 
    ZeroIt(scriptExcepInfo);
    ZeroIt(evaluateReturn);
    method= NULL;
    methodName= NULL;
    javaExcepInfo=NULL;
    linenumber= -1; //Means we don't know where error occured.
    errline= NULL;
    linepos=-1;
    throwObj= NULL;
   }
  /**This thread is asking to evaluate a script after the engine has been asked to terminate.
   */
  jobject tooLate(LPCOLESTR msg= L"Script engine is terminating")
   { //Inorder to expedite the any shutdown pending requests are also told they are too late.
    VARIANT DontCare;
    ZeroIt(DontCare);
    siteExcepInfo.scode = 1;
    siteExcepInfo.pfnDeferredFillIn = NULL;
    siteExcepInfo.bstrSource=SysAllocString(L"ActiveScriptEngine"); 
    siteExcepInfo.bstrDescription=SysAllocString(msg); 
    Done(E_FAIL, DontCare);
    return NULL;
   }
   
  ~ThreadRequest()
   {
    CloseHandle(Block);
   }
  /** Called by script engine when it thinks the thread is done.
   */
  void Done(HRESULT _hr, const VARIANT &rc)
   {
    evaluateReturn= rc;
    hr= _hr;
    done= true;
    SetEvent(Block);
   }
  /**Routine used by requesting thread to wait while script is executing.
   */
  jobject Wait(); 
   
  /**Called by the engine thread to dispatch to the Java thread a callback to a Java object 
   * with the given method name.  NOTE THIS COULD BE ELIMATED WHEN ARRAY SUPPORT IS PROVIDED.
   */
  void dispatchJavaMethod(jobject bean, char* methodName, WORD notused, DISPPARAMS __RPC_FAR *pDispParams, VARIANT __RPC_FAR *pVarResult,
			  EXCEPINFO __RPC_FAR *pExcepInfo, UINT __RPC_FAR *puArgErr);

  /**Called by the engine thread to dispatch to the Java thread a callback to a Java object 
   * with the given method object. 
   */
  void dispatchJavaMethod(jobject _bean, JavaClass::Method* m, WORD flags ,DISPPARAMS __RPC_FAR *_pDispParams,
			  VARIANT __RPC_FAR *_pVarResult, EXCEPINFO __RPC_FAR *_pExcepInfo, UINT __RPC_FAR *_puArgErr);
   
 }; //Endof:  class ThreadRequest

protected:
 bool terminateRequest;  //We have been asked to terminate.
 BSFException engineRuntimeError;
 DWORD  engineThreadId;
 JNIEnv *jenv; //The engine's JNIEnv variable.
 std::map<std::string,JavaClass*> javaClasses;  //Contains the classes that have so far been accessed.
 jobject javaActiveScriptEngine; //This is the java script engine object, As global ref.
 const char *g_langstr; //The script language being run. 
 bool _respectCase; //True if language expects case to be significant.
 CASHostSite *css; //Our COM host site.
 IActiveScript *pIas;  //
 IActiveScriptParse *pIasp;
  
 CRITICAL_SECTION EngineControl;  //Critical section for thread request queues.
 HANDLE engineWork;  //Semaphore to control engine thread when work needs to be done.
 HANDLE waitEngineTerminate;  //Semaphore to block thread requesting engine termination till engine has terminated. 

 std::deque<ThreadRequest*> requestList;  //These are threads with requests to the script engine to evaluate scripts. Index 0 is next.
 // std::deque<ThreadRequest*> javaPendingThreads; //These are threads the script engines has dispatched back to java to do work: Index 0 is last out.
 ThreadRequest *currentProxyThread;  //Thread which is running in the engine.
 int dispatchedMethods;  //The number of current call backs to Java from the script.

public:
 ActiveScriptEngine(JNIEnv *jenv, jobject thisobj, const char *lang, HANDLE &wait);
  
 /**Return the engine's JNIEnv variable */
 JNIEnv *getJenv(){return jenv;}
 /**Return language string */
 const char * getLanguage(){return g_langstr;}
  
 bool respectCase(){return _respectCase;}
 bool isPerlScript(){ return 0 == _stricmp(g_langstr, "PerlScript");}
 /** Used by COM's script site interface to record it received an error*/
 void throwSiteException(BSTR &source, BSTR &errorDescription, LONG code, LONG lineno=-1, LONG _linepos=-1, BSTR text = NULL)
 {
  assert(currentProxyThread);
  if(currentProxyThread)
  {
   if(0==currentProxyThread->siteExcepInfo.scode)
   {//Only record the first error.
    currentProxyThread->siteExcepInfo.scode= code;
    currentProxyThread->siteExcepInfo.bstrSource= SysAllocString(source? source : L"");
    currentProxyThread->siteExcepInfo.bstrDescription=SysAllocString(errorDescription ? errorDescription : L""); //make our own copy
    currentProxyThread->linenumber=lineno;
    currentProxyThread->linepos= _linepos;
    currentProxyThread->errline= SysAllocString(text ? text : L"");
   }
  }
 }
  
 /** Main loop that runs the engine thread*/
 void run();
  
 /** Called by thread requesting script to execute */
 jobject eval(ThreadRequest & );
  
 void wakeUpEngine(){SetEvent(engineWork);};
    
 template<class T>HRESULT dispatchJavaMethod(jobject bean, T method, WORD wFlags, DISPPARAMS __RPC_FAR *pDispParams, VARIANT __RPC_FAR *pVarResult,
					     EXCEPINFO __RPC_FAR *pExcepInfo, UINT __RPC_FAR *puArgErr)
  { //called by engine thread only
   assert(currentProxyThread);
   ThreadRequest *dispatchThread=currentProxyThread;
   currentProxyThread=NULL; //Officially not working for this thread anymore.
   dispatchThread->hr_java= E_FAIL; //expect failure
   ++dispatchedMethods;
   dispatchThread->dispatchJavaMethod(bean, method, wFlags, pDispParams,pVarResult,pExcepInfo,puArgErr);
   processPendingWork(dispatchThread); 
   --dispatchedMethods;
   //Were back from java to process this thread!
   currentProxyThread= dispatchThread; 
   return currentProxyThread->hr_java; 
  }
       
 HRESULT dispatchJavaMethod(char* methodName,DISPPARAMS __RPC_FAR *pDispParams, VARIANT __RPC_FAR *pVarResult,
			    EXCEPINFO __RPC_FAR *pExcepInfo, UINT __RPC_FAR *puArgErr)
  {
   return dispatchJavaMethod(javaActiveScriptEngine, methodName, 0, pDispParams, 
			     pVarResult, pExcepInfo, puArgErr);
  }

 HRESULT dispatchJavaMethodA(jobject bean, char* methodName,DISPPARAMS __RPC_FAR *pDispParams, VARIANT __RPC_FAR *pVarResult,
			     EXCEPINFO __RPC_FAR *pExcepInfo, UINT __RPC_FAR *puArgErr)
  {
   DISPPARAMS SingleSafeArray;
   ZeroIt(SingleSafeArray);
   VARIANT  SingleSafeArrayVariant;
   ZeroIt(SingleSafeArrayVariant);
   typedef struct tagSAFEARRAY_SINGLEDIM
   {
    SAFEARRAY sa;
    SAFEARRAYBOUND sab;
   } SAFEARRAY_SINGLEDIM;
   SAFEARRAY_SINGLEDIM sda;
   ZeroMemory(&sda, sizeof sda);
   SingleSafeArrayVariant.vt= VT_ARRAY;
   SingleSafeArrayVariant.parray= &sda.sa;
   sda.sa.cDims=1;
   sda.sa.fFeatures= FADF_VARIANT | FADF_AUTO;
   sda.sa.cbElements= sizeof VARIANT;
   sda.sa.rgsabound[0].cElements= pDispParams->cArgs; 
   sda.sa.rgsabound[0].lLbound=0;
   sda.sa.pvData= _alloca(sizeof VARIANT * pDispParams->cArgs); 
   for(int i=pDispParams->cArgs-1; i> -1; --i)
   {
    (reinterpret_cast<VARIANT*>(sda.sa.pvData))[pDispParams->cArgs-i-1]=pDispParams->rgvarg[i];
   }

   SingleSafeArray.cArgs=1;
   SingleSafeArray.rgvarg=&SingleSafeArrayVariant;

   return dispatchJavaMethod(bean, methodName, 0, &SingleSafeArray, pVarResult, pExcepInfo, puArgErr);
  }
 HRESULT dispatchJavaMethodA(char* methodName,DISPPARAMS __RPC_FAR *pDispParams, VARIANT __RPC_FAR *pVarResult,
			     EXCEPINFO __RPC_FAR *pExcepInfo, UINT __RPC_FAR *puArgErr)
  {
   return dispatchJavaMethodA(javaActiveScriptEngine, methodName,pDispParams,pVarResult, pExcepInfo, puArgErr);
  
  }
 void processPendingWork();
 void processPendingWork(ThreadRequest *pending);
 JavaClass * getClass(std::string className, const char *dispids= NULL, const char* factory= NULL)
  {
   JavaClass *javaClass= NULL;
   std::map<std::string,JavaClass*>::iterator pos;
   if(javaClasses.end() != (pos=javaClasses.find(className)))
   {
    javaClass= pos->second;
   }
   else
   {
    javaClass= new JavaClass(*this, className, dispids, factory);
    javaClasses.insert(std::make_pair(className, javaClass));
   }
   return javaClass;
  }

 JavaClass * getClass(JNIEnv *jenv, jobject _o)
  {
   JavaClass *javaClass=NULL;
   if(_o)
   {
    JNIEnv *ase_env= jenv? jenv : getJenv();
    jboolean isCopy= JNI_FALSE;
    jboolean isCopyDISPID= JNI_FALSE;
    jboolean isCopyFACTORY= JNI_FALSE;
#if 0
    jthrowable t;
    t= jenv->ExceptionOccurred(); 
    if(t != NULL)
    {
      jenv->ExceptionDescribe();
    }
#endif    
    
    jclass clazzPeerBean= ase_env->GetObjectClass(_o);
    jmethodID mid=ase_env->GetMethodID(clazzPeerBean,"getClass" , "()Ljava/lang/Class;");
    jobject beansClass= ase_env->CallObjectMethod(_o, mid);
   
    jclass clazzbeansClass= ase_env->GetObjectClass(beansClass);
    mid=ase_env->GetMethodID(clazzbeansClass,"getName" , "()Ljava/lang/String;");
   
    jstring jclassName= reinterpret_cast<jstring>(ase_env->CallObjectMethod(beansClass, mid));
    ase_env->DeleteLocalRef(beansClass);
   
    const char* cmsg= ase_env->GetStringUTFChars(jclassName, &isCopy);

    /*now find if there specific DISPIDS for methods */
    const char* cDISPID= NULL;
    jfieldID jfieldDISPID= ase_env->GetStaticFieldID(clazzPeerBean,"BSFSCRIPTMETHODSDISPIDS", "Ljava/lang/String;");
    jstring jstringDISPID;
    if(jfieldDISPID)
    {
      jstringDISPID= reinterpret_cast<jstring>(ase_env->GetStaticObjectField(clazzPeerBean, jfieldDISPID));
      cDISPID= ase_env->GetStringUTFChars(jstringDISPID, &isCopyDISPID);
    }
    else
    {
     jenv->ExceptionClear();
    }

    /*Find out if any special factory procedures are required for the C side creation. */
    const char* cFACTORY= NULL;
    jfieldID jfieldFACTORY= ase_env->GetStaticFieldID(clazzPeerBean,"BSFSCRIPTOBJECTFACTORY", "Ljava/lang/String;");
    jstring jstringFACTORY;
    if(jfieldFACTORY)
    {
      jstringFACTORY= reinterpret_cast<jstring>(ase_env->GetStaticObjectField(clazzPeerBean, jfieldFACTORY));
      cFACTORY= ase_env->GetStringUTFChars(jstringFACTORY, &isCopyFACTORY);
    }  
    else
    {
     jenv->ExceptionClear();
    }

    ase_env->DeleteLocalRef(clazzPeerBean);//Don't stop it from being grabage collected.

    
    if(cmsg)
    {
   
     javaClass= getClass(cmsg, cDISPID, cFACTORY);
     if(isCopy == JNI_TRUE) ase_env->ReleaseStringUTFChars(jclassName, cmsg);
    }
    if(isCopyDISPID== JNI_TRUE) ase_env->ReleaseStringUTFChars(jstringDISPID, cDISPID);
    if(isCopyFACTORY== JNI_TRUE) ase_env->ReleaseStringUTFChars(jstringFACTORY, cFACTORY);

   }
   return javaClass;
  }
}; //Endof class ActiveScriptEngine

#endif






















