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
 * 4. The names "BSF", "Apache", and "Apache Software Foundation"
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
#if !defined (Iace860b0557d11d3b2a282ca7e000000 )
#define Iace860b0557d11d3b2a282ca7e000000 

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
#include <jni.h>
#include "ActiveScriptEngine.h"


extern const GUID IID_IJavaPeer; 
class ActiveScriptEngine;
class JavaCOM;
class JavaClass
{ //Helper class for JavaCOM. This represents the java class that the JavaCOM object belongs to. Contains the
 // methods for objects.
public:
 HRESULT STDMETHODCALLTYPE addMethod(LPCOLESTR name,  DISPID *dispid);

  HRESULT addMethod(const std::string &name, DISPID *dispid= NULL) 
 {
    return addMethod(mbs2ws(name.c_str()), dispid);
 }

static JavaCOM * JavaClass::createObject(ActiveScriptEngine  &_ase, jobject peerBean, JNIEnv *o_jnienv= NULL);

 JavaCOM * instantiate(ActiveScriptEngine  &_ase, jobject peerBean, JNIEnv *o_jnienv );
 JavaClass(ActiveScriptEngine &ase, std::string &name, const char*assignedDISPIDS= NULL, const char *factoryString= NULL):ASE(ase), className(name), loadLibraryFactory(NULL), factory(NULL)
 {
  int dispidlen=0;
  bool defDISPIDFound=false;
  if(factoryString && 24 < (dispidlen=strlen(factoryString)) && !strncmp(factoryString,"BSFSCRIPTOBJECTFACTORY:",23))
  {
    const char *factoryProcedure;
    
    std::string parse(factoryString + 23);
    int sep= parse.find(':');
    if(sep== std::string.npos)
    {
      factoryProcedure= factoryString+23;  
    }
    else
    {
      const char *FactoryModuleName= parse.substr(0, sep).c_str();
      loadLibraryFactory=LoadLibrary( FactoryModuleName);
      factoryProcedure= parse.substr(sep+1).c_str();  
    }
//rrfoo Need error conditions
    factory= (JavaCOM * (__stdcall *)(ActiveScriptEngine  &, jobject , JNIEnv *))GetProcAddress((HINSTANCE)( loadLibraryFactory ? loadLibraryFactory : thisModule), factoryProcedure); 

  }
  if(assignedDISPIDS && 25 < (dispidlen=strlen(assignedDISPIDS)) && !strncmp(assignedDISPIDS,"BSFSCRIPTMETHODSDISPIDS:",24))
  {
    std::string parse(assignedDISPIDS+24);
    int sc= parse.find(';');
    for(int bc=0;bc != std::string.npos;   sc= parse.find(';',bc))
    {
      std::string x( parse.substr(bc, sc));
      int ep= x.find('=');
      if(ep != std::string.npos)
      {
        std::string mname( x.substr(0, ep));
	std::string mdispid( x.substr(ep+1));
	int dispid= atoi(mdispid.c_str());
	if(0== dispid)
	{
	  addMethod( mname); //This is the default method.
	  defDISPIDFound= true;
	}  
	else  
	{
	 Method *x= new Method(mname, dispid);
	 assignedMethods.push_back(x );
	}  
      }
      bc= sc != std::string.npos ? sc+1: sc;
    }
    
  }  
  if(!defDISPIDFound) // addMethod( NULL);  //dont use 0
     methods.push_back( NULL);
 }; //Don't assign position 0. Dispatch id 0 is special.



 operator const char*(){ return className.c_str();}

 bool operator == ( const JavaClass &d) { return className == d.className; }

 bool operator < (const JavaClass &d) { return className < d.className; }

 bool operator > (const JavaClass &d) { return className > d.className;}

 int numberOfMethods() { return methods.size()-1; }

   
 ~JavaClass()
 { 
   for(int i=0; i< methods.size(); delete methods[i++]);
   if(loadLibraryFactory){FreeLibrary((HINSTANCE)loadLibraryFactory);loadLibraryFactory= NULL;}
 }

 class Method //used to save method information.
 {
  friend class JavaClass;
 private:
  Method(){assert(false);}
  Method(const Method &j){assert(false);}
  LPCOLESTR name;
  DISPID dispid;
  int putJavaIndex;
  int getJavaIndex;
  int methodJavaIndex;
     
  void Method_init(LPCOLESTR _name, DISPID dispid) 
   {
    name= _wcsdup(_name);
    this->dispid=dispid;
    putJavaIndex= getJavaIndex=  methodJavaIndex=0;
       
   };
  Method(LPCOLESTR _name, DISPID dispid){ Method_init(_name, dispid); }; 
  Method(std::string &name, DISPID dispid){ Method_init(mbs2ws( name.c_str()), dispid); };

  ~Method() { assert(name);   free((void*)name); };
 public:
  DISPID getDISPID(){ return dispid;}

  jobject  invoke(JNIEnv *jenv, jobject jASE, jobject bean, WORD wFlags, jobjectArray args )
   {
    jclass clazzASE= jenv->GetObjectClass(jASE);
    int *invokeIndex;
       
    if(wFlags & (DISPATCH_PROPERTYPUT | DISPATCH_PROPERTYPUTREF)) invokeIndex= &putJavaIndex; 
    else if(wFlags &DISPATCH_PROPERTYGET ) invokeIndex= &getJavaIndex; 
    else if(wFlags & DISPATCH_METHOD) invokeIndex= &methodJavaIndex; 
    else assert(0  /* bad dispatch type */);
       
    if(!*invokeIndex)
    {// need to try and bind first 
     jstring jMethodName= jenv->NewStringUTF(ws2mbs(name)); 
     jmethodID mid=jenv->GetMethodID(clazzASE,"bindMember" , "(Ljava/lang/Object;Ljava/lang/String;S)I");
     *invokeIndex= jenv->CallIntMethod(jASE,mid,bean ,jMethodName, wFlags);
     jenv->DeleteLocalRef(jMethodName);
     
    }
       
    if(jenv->ExceptionOccurred())
    {
     jenv->DeleteLocalRef(clazzASE);
     return NULL;
    }
       
    jmethodID mid=jenv->GetMethodID(clazzASE,"callMethod" , "(Ljava/lang/Object;I[Ljava/lang/Object;)Ljava/lang/Object;");
    jenv->DeleteLocalRef(clazzASE);
    return jenv->CallObjectMethod(jASE,mid,bean ,*invokeIndex,args);
       
   }
  LPCOLESTR getName() const{ return name;}
 }; //endof class Method.

 Method *getMethodById(int id) const
 { 
   Method *found=NULL;
   if(id>=0 && id < methods.size()) found= methods[id];
   else
   {
     for(int j=0; !found && j< assignedMethods.size(); ++j)
     {
       if(id == assignedMethods[j]->getDISPID()) found= assignedMethods[j];
     }
   }
 
   return found;
 } 

 LPCOLESTR methodNameById(DISPID id)
 { assert( validDispid(id));
  return getMethodById(id)->name;
 }
 bool validDispid(const DISPID id)
 {
   return NULL != getMethodById(id);
 }
protected:
 std::string className;
 std::vector<Method*> methods;
 std::vector<Method*> assignedMethods; //These are methods that have been specifically in java class specified a particular DISPID
 ActiveScriptEngine &ASE;
 HANDLE loadLibraryFactory;
 JavaCOM* (__stdcall *factory)(ActiveScriptEngine  &, jobject , JNIEnv *);
}; //Endof class Method

interface IJavaPeer : public IUnknown
{
 virtual HRESULT STDMETHODCALLTYPE getPeerBean(jobject*)= 0;
};

class JavaCOM : public IDispatch, public IJavaPeer 
{
private:
 LONG refCounter;
 // JavaCOM():ASE(NULL){assert(false);};                   /*Don't let the compiler call these by default!!!*/
 // JavaCOM(const JavaCOM &j):ASE(NULL){assert(false);};
protected:

 jobject peerBean;  //The java object counter part.  Should be a global ref.
 ActiveScriptEngine &ASE; //A reference back to the C++ Scripting Engine Assoc. with this bean
 JavaClass *javaClass;  //A pointer to its class object.  As a design choice class does not go away until engine goes away
 // There are no pointer from JavaClass back to JavaCOM
 JNIEnv *getJenv();   

public:
// JavaCOM(ActiveScriptEngine  &_ase, jobject o );
 JavaCOM(ActiveScriptEngine  &_ase, jobject o, JNIEnv * o_jnienv = NULL );
 JavaCOM(ActiveScriptEngine  &_ase, const char * className );
 int numberOfMethods() { assert(javaClass); return javaClass->numberOfMethods();}
 JavaClass::Method *getMethodById(int i) const { assert(javaClass); return javaClass->getMethodById(i);} 
 LPCOLESTR  methodNameById(int i)
 { assert(javaClass->validDispid(i) ); return javaClass->methodNameById(i);}
   
 virtual ~JavaCOM()
  {
#if 0 // !defined(NDEBUG)
   std::cerr << "JavaCOM dying:" << this << ", RefCounter:" << refCounter << "Class:" << (const char *) *javaClass << std::endl <<std::flush;
#endif	
   if(peerBean) { getJenv()->DeleteGlobalRef(peerBean); peerBean= NULL;}
  }
   
 /**Add the method with the given name and return the associate dispatch id */
 HRESULT STDMETHODCALLTYPE addMethod(LPCOLESTR name, DISPID *dispid)
  {
   return javaClass->addMethod(name,  dispid);
  }
 inline HRESULT addMethod(const std::string &name, DISPID *dispid= NULL) 
  {
   return addMethod(mbs2ws(name.c_str()), dispid);
  }

 /////////////////////////////////////////////////////////////////////// 
 // IUnknown
 /////////////////////////////////////////////////////////////////////// 
 HRESULT STDMETHODCALLTYPE QueryInterface( REFIID riid, void **ppv);

 ULONG   STDMETHODCALLTYPE AddRef(void);

 ULONG   STDMETHODCALLTYPE Release(void);
 /////////////////////////////////////////////////////////////////////// 
 // IDispatch
 /////////////////////////////////////////////////////////////////////// 
 HRESULT STDMETHODCALLTYPE GetTypeInfoCount( UINT __RPC_FAR *pctinfo);
       
 HRESULT STDMETHODCALLTYPE GetTypeInfo( UINT iTInfo, LCID lcid,
					ITypeInfo __RPC_FAR *__RPC_FAR *ppTInfo);
       
 HRESULT STDMETHODCALLTYPE GetIDsOfNames( REFIID riid, LPOLESTR __RPC_FAR *rgszNames,
					  UINT cNames, LCID lcid, DISPID __RPC_FAR *rgDispId);
       
 HRESULT STDMETHODCALLTYPE Invoke( DISPID dispIdMember, REFIID riid, LCID lcid, WORD wFlags,
				   DISPPARAMS __RPC_FAR *pDispParams, VARIANT __RPC_FAR *pVarResult,
				   EXCEPINFO __RPC_FAR *pExcepInfo, UINT __RPC_FAR *puArgErr);

 /////////////////////////////////////////////////////////////////////// 
 // IJavaPeer
 /////////////////////////////////////////////////////////////////////// 

 HRESULT STDMETHODCALLTYPE getPeerBean(jobject* pJobj);

 // void*   operator new (size_t s){return };
 // void      operator delete[] (void*);
};//endof JavaCOM

#endif // #if !defined (Iace860b0557d11d3b2a282ca7e000000 )
