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

#ifndef I26527320a82311d3ad5c00609456dbb1
#define I26527320a82311d3ad5c00609456dbb1
#include<string>
#include <sstream>
#include "JNIUtils.h"

class BSFException //JNI exceptions only set a state so throw this.
{
 std::string file;
 int line;
 std::ostringstream  msgbuf;
 jthrowable jthrown;
public:
 typedef enum { THROWIT} THROWITENUM;
 inline BSFException(): file(""), line(-1) { jthrown= NULL;}
 inline BSFException(jthrowable j): file(""), line(-1), jthrown(j) {}
 inline BSFException(const char * _file, int _line) : file(_file), line(_line)
  {msgbuf<< file << line <<"|";jthrown= NULL;};
 static class BSFException  newBSFException( const char * _file, int _line);
 template<class T> BSFException&  operator <<( const T &t) {  msgbuf <<t; return *this;};
 BSFException&  operator <<(jthrowable &j) {  jthrown= j; return *this;};
 inline BSFException&   operator <<( THROWITENUM t) { throw *this ;  return *this; };
 inline  operator const std::string () {std::string msg(msgbuf.str()); return msg;}
 inline  operator bool (){ return line != -1;}
 inline  BSFException& operator = ( const BSFException &l){ file=l.file; line=l.line; msgbuf << l.msgbuf.str(); return *this;}
 inline const char * what() {return ((std::string)*this).c_str();}
 inline void setJNIException(JNIEnv *jenv)
 {
   std::string msg(msgbuf.str());
   const char *msgChar= msg.c_str();
   if(NULL== msgChar) msgChar= "";
   jstring jmsgChar= jenv->NewStringUTF(msgChar); 
   jclass jcASE= jenv->FindClass("com/ibm/bsf/engines/activescript/ActiveScriptEngine");
   jmethodID mid=jenv->GetStaticMethodID(jcASE,"createBSFException","(ILjava/lang/String;Ljava/lang/Throwable;)Ljava/lang/Throwable;");
   //public static final Throwable  createBSFException( int reason, String msg, Throwable t)
   jthrowable jt= (jthrowable) jenv->CallStaticObjectMethod(jcASE,mid, 500, jmsgChar, jthrown);
   jenv->Throw(jt);  
   if(jthrown)
   {
     jenv->DeleteGlobalRef(jthrown);//Don't stop it from being grabage collected.
     jthrown= NULL;
   }
   jenv->DeleteLocalRef(jcASE);
   if(jmsgChar) jenv->DeleteLocalRef(jmsgChar);
   // bsf_exception2(e,msg.length(), const_cast<char*>(msg.c_str()));
 }
 virtual ~BSFException(){}
 inline BSFException( const BSFException & n)
  {
   file= n.file;
   line= n.line;
   msgbuf<< n.msgbuf.str();
   jthrown= n.jthrown;

  }
};

inline class BSFException  BSFException::newBSFException( const char * _file, int _line)
    {return *(new BSFException( _file,  _line));};

#define throwBSFException (BSFException::newBSFException(__FILE__,__LINE__))

inline void setBSFExceptionState(JNIEnv *env, const char*file, int lineno, const char *msg)
{
 char *buffer;
 msg= msg ? msg : "no message";
 file= file? file : "No file information";
 if(buffer= reinterpret_cast<char*>(_alloca(128+ strlen(file)+ strlen(msg))))
  sprintf(buffer,"%s.,%d:%s",file, lineno, msg);
 else
  buffer= "Low on stack memory";  
 bsf_exception(env,strlen(buffer), buffer);
}

#endif //#define I26527320a82311d3ad5c00609456dbb1
