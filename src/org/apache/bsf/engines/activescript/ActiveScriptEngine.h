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

#if !defined (I60B6BF92809E11d3B2E4BA59F8000000 )
#define I60B6BF92809E11d3B2E4BA59F8000000
#include <deque>
#include <malloc.h>
#include <exception>
#include <new>
#include <new.h>
#include <windows.h>
#include <jni.h>
#include <dbgutils.h>

extern char ModuleName[MAX_PATH *2 ];
extern HANDLE thisModule;

class DLLInit
{
  private:
  char *memory;
  DLLInit()
  {
    memory= ::new(std::nothrow) char[8*1024];
  }
  public:
  ~DLLInit()
  {
    if(memory) delete[] memory;
    memory= NULL;
  }
  inline void releaseMemory(){char *x= memory; memory= NULL; if(x)delete[]x;}
  static DLLInit dllInit;
};

static int my_new_handler(size_t)
{
 DLLInit::dllInit.releaseMemory();
 throw std::bad_alloc();
 return 0;
}

inline wchar_t *mbs2wsConvert(const char *s, void *d)
{
 size_t size= strlen(s);
 if(d && !size) *(wchar_t *)d= L'\0';
 if(d && size)
 {
 ((wchar_t *)d)[MultiByteToWideChar(
  CP_ACP,         // code page
  MB_COMPOSITE,         // character-type options
  s, // address of string to map
  size,       // number of bytes in string
  (wchar_t *)d,  // address of wide-character buffer
  ((size<<1)+1)        // size of buffer
  )
  ]= L'\0';
 }
 return (wchar_t *) d;
}

#define mbs2ws(s) (mbs2wsConvert((s),  _alloca( (2+strlen((const char *)(s)))<<1)))

inline LPOLESTR mbs2LPOLESTR(const char *s)
{
 wchar_t *ws= mbs2ws(s);
 if(ws)
 {
  return _wcsdup(ws);
 }
 return NULL;
}

inline LPOLESTR mbs2LPOLESTR(const std::string &s)
{
 return mbs2LPOLESTR( s.c_str());
}

inline char *ws2mbsConvert(const wchar_t *s, void *d)
{
 size_t size= wcslen(s); //It maybe the same size as the widechar string
 if(!size && d) *((char*)d)= '\0';
 if(d && size){((char *)d)[WideCharToMultiByte
 (
  CP_ACP,         // code page
  WC_COMPOSITECHECK,         // character-type options
  s, // address of string to map
  size,       // number of chars in string
  (char *)d,  // address of mbs-character buffer
  size<<1,        // size of buffer in bytes ,        //Use system default character.
  NULL,
  NULL 
  )
  ]= L'\0';
 }
 return (char *) d;
}

#define ws2mbs(s) (ws2mbsConvert((s),  _alloca( ((2+(wcslen((const wchar_t *)(s))<<1))))))

template<class T> inline void ZeroIt(T &t){  memset(&t, 0, sizeof t);};


#endif /* I60B6BF92809E11d3B2E4BA59F8000000 */















