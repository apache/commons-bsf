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
#ifndef __DBGUTLSH_INCLUDED__
#define __DBGUTLSH_INCLUDED__ 
#if !defined(NDEBUG)
#include <CRTDBG.H>
#define CRTDBGBRK {if(getenv("JNIDBGOK")){   _CrtDbgBreak();}}
#else
#define CRTDBGBRK    
#endif

#if defined(FAILED)
#include <cassert>
#if defined(NDEBUG)
#define ASSERT_FAILED FAILED
#else
#define ASSERT_FAILED(S) ( (true == FAILED((S))) ?((bool) (assert(0),true)) : false) 
#endif
#endif

#endif  //__DBGUTLSH_INCLUDED__ 
