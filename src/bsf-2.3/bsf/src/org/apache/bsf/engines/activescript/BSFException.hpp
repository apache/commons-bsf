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
