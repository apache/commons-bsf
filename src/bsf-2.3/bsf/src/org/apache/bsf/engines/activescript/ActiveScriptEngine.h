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















