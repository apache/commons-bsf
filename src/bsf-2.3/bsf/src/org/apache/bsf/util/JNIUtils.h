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

#include <jni.h>
#if defined(__cplusplus)
extern "C" {
#endif

/* throw a BSFException with the given message  */
extern void bsf_exception (JNIEnv *jenv, int code, char *msg);

/* cvt a pointer to a Long object whose value is the pointer value */
extern jobject bsf_pointer2longobj (JNIEnv *jenv, void *ptr);

/* cvt a Long object whose value is the pointer value to the pointer */
extern void *bsf_longobj2pointer (JNIEnv *jenv, jobject lobj);

/* convert an object to a string obj */
jstring bsf_obj2jstring (JNIEnv *jenv, jobject obj);

/* cvt an object to a c-string .. wastes memory, but useful for debug */
const char *bsf_obj2cstring (JNIEnv *jenv, jobject obj);

/* create an instance of the named class with the given args */
extern jobject bsf_createbean (JNIEnv *jenv, char *methodname, 
                               jobjectArray args);

/* call the named method with the given args on the given bean */
extern jobject bsf_callmethod (JNIEnv *jenv, jobject target,
			       char *methodname, jobjectArray args);

/* return the named bean from the given mgr's bean registry */
extern jobject bsf_lookupbean (JNIEnv *jenv, jobject mgr, char *beanname);

/* return the type signature string component for the given type:
   I for ints, J for long, ... */
extern char *bsf_getTypeSignatureString (JNIEnv *jenv, jclass objclass);

/* make objects from primitives */
extern jobject bsf_makeBoolean (JNIEnv *jenv, int val);
extern jobject bsf_makeByte (JNIEnv *jenv, int val);
extern jobject bsf_makeShort (JNIEnv *jenv, int val);
extern jobject bsf_makeInteger (JNIEnv *jenv, int val);
extern jobject bsf_makeLong (JNIEnv *jenv, long val);
extern jobject bsf_makeFloat (JNIEnv *jenv, float val);
extern jobject bsf_makeDouble (JNIEnv *jenv, double val);
#if defined(__cplusplus)
}
#endif
