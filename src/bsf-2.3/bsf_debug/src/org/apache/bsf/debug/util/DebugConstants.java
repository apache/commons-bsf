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

package org.apache.bsf.debug.util;

public class DebugConstants {

    ////////////////////////////////////////////////

    public static final int BSF_DEBUG_SERVER_PORT = 4444;

    ////////////////////////////////////////////////

    public static final int BSF_DEBUG_PROTOCOL_MAJOR = 1;
    public static final int BSF_DEBUG_PROTOCOL_MINOR = 0;
    public static final int BSF_DEBUG_PROTOCOL_ACCEPT = 1;
    public static final int BSF_DEBUG_PROTOCOL_REJECT = 0;

    ////////////////////////////////////////////////
    public static final int WAIT_FOR_VOID 	= 50;
    public static final int WAIT_FOR_BOOLEAN= 51;
    public static final int WAIT_FOR_INT 	= 52;
    public static final int WAIT_FOR_LONG 	= 53;
    public static final int WAIT_FOR_FLOAT 	= 54;
    public static final int WAIT_FOR_DOUBLE = 55;
    public static final int WAIT_FOR_OBJECT = 56;
    public static final int WAIT_FOR_MIRROR = 57;


    ////////////////////////////////////////////////

    public static final int NULLTID = 1;
    public static final int NULLUID = 2;
	
    public static final int UNKNOWN_TID = 3;
    public static final int UNKNOWN_UID = 4;
	
    public static final int SPECIAL_TID = 5;	
    public static final int NOT_FOUND_UID = 6;	
    public static final int UNDEFINED_UID = 7;

    public static final int BSF_DEBUG_MANAGER_UID = 8;

    ////////////////////////////////////////////////
    public static final int FIRST_NON_RESERVED_UID = 9999;

    ////////////////////////////////////////////////

    public static final int NULL_OBJECT = 99;
    public static final int VALUE_OBJECT = 100;
    public static final int STUB_OBJECT = 101;
    public static final int SKEL_OBJECT = 102;

    ////////////////////////////////////////////////
    // Reserved Uid for special object going across...
    ////////////////////////////////////////////////


    ////////////////////////////////////////////////
    // Type Id of object going across...
    ////////////////////////////////////////////////
	
    public static final int BSF_DEBUG_MANAGER_TID = 104;
    public static final int JS_ENGINE_TID = 105;
    public static final int JS_CONTEXT_TID = 106;
    public static final int JS_OBJECT_TID = 107;

    public static final int BSF_DEBUGGER_TID = 108;
    public static final int JS_CALLBACKS_TID = 109;

    public static final int VALUE_OBJECT_TID = 110;

    public static final int JS_CODE_TID = 111;

    ////////////////////////////////////////////////
    // Special type Id indicating a command has
    // failed and that an execption object follows
    // in the stream.
    ////////////////////////////////////////////////

    public static final int ERROR_OCCURED = 150;
	
	
    //-----------------------------------------------------------
    // DebugManager Methods...
    public static final int DM_GET_LANG_FROM_FILENAME = 300;
    public static final int DM_IS_LANGUAGE_REGISTERED = 301;
    public static final int DM_PLACE_BREAKPOINT_AT_LINE = 302;
    public static final int DM_REMOVE_BREAKPOINT = 303;
    public static final int DM_SET_ENTRY_EXIT = 304;
    public static final int DM_REGISTER_DEBUGGER_FOR_LANG = 305;
    public static final int DM_UNREGISTER_DEBUGGER_FOR_LANG = 306;
    public static final int DM_QUIT_NOTIFY = 307;
	
    //-------------------------------------------------
    // JsEngine Methods...
    public static final int JE_GET_CONTEXT_AT = 401;
    public static final int JE_GET_CONTEXT_COUNT = 402;

    public static final int JE_RUN = 404;
    public static final int JE_STEP_IN = 405;
    public static final int JE_STEP_OUT = 406;
    public static final int JE_STEP_OVER = 407;

    public static final int JE_GET_GLOBAL_OBJECT = 409;
    public static final int JE_GET_UNDEFINED_VALUE = 410;
	
    public static final int JE_SET_DEBUGGER = 411;
    public static final int JE_GET_THREAD = 412;
    public static final int JE_GET_THREADGROUP = 413;

    //-------------------------------------------------
    // JsObject Methods...
    public static final int JO_DEFINE = 501;
    public static final int JO_DELETE_BY_INDEX = 502;
    public static final int JO_DELETE_BY_NAME = 503;
    public static final int JO_GET_BY_NAME = 504;
    public static final int JO_GET_BY_INDEX = 505;
    public static final int JO_GET_CLASSNAME = 506;
    public static final int JO_GET_DEFAULT_VALUE = 507;
    public static final int JO_GET_IDS = 508;
    public static final int JO_HAS_BY_INDEX = 509;
    public static final int JO_HAS_BY_NAME = 510;
    public static final int JO_GET_PROTOTYPE = 511;
    public static final int JO_GET_SCOPE = 512;
    public static final int JO_HAS_INSTANCE = 513;
    public static final int JO_PUT_BY_INDEX = 514;
    public static final int JO_PUT_BY_NAME = 515;
    public static final int JO_SET_PROTOTYPE = 516;
    public static final int JO_SET_SCOPE = 517;

    public static final int JO_FUNCTION = 600;
    public static final int JO_SCRIPT = 601;

    //-------------------------------------------------
    // JsCode Methods...
    public static final int JC_GET_LINE_NUMBERS = 610;
	
    //-------------------------------------------------
    // BSFDebugger Methods...
    public static final int BSFD_CREATED_ENGINE = 701;
    public static final int BSFD_DELETED_ENGINE = 702;
    public static final int BSFD_DISCONNECT = 703;
	
	
    //-------------------------------------------------
    // JsContext Methods...
    public static final int CX_BIND = 800;
    public static final int CX_GET_CODE = 801;
    public static final int CX_GET_DEPTH = 802;
    public static final int CX_GET_ENGINE = 803;
    public static final int CX_GET_LINE_NUMBER = 804;
    public static final int CX_GET_SCOPE = 805;
    public static final int CX_GET_SOURCE_NAME = 806;
    public static final int CX_GET_THIS = 807;
	
    //-------------------------------------------------
    // JsCallbacks Methods...

    public static final int CB_POLL = 900;
    public static final int CB_HANDLE_BREAKPOINT_HIT = 901;
    public static final int CB_HANDLE_ENGINE_STOPPED = 902;
    public static final int CB_HANDLE_EXCEPTION_THROWN = 903;
    public static final int CB_HANDLE_STEPPING_DONE = 904;

    ////////////////////////////////////////////////
    ////////////////////////////////////////////////
    ////////////////////////////////////////////////
    static IntHashtable m_constantNames;
		
    public static String getConstantName(int id) {
        String name = (String)m_constantNames.get(id);
        if (name==null) name = "Unknown name for "+Integer.toString(id);
        return name;
    }

    static {
        m_constantNames = new IntHashtable();
						
        m_constantNames.put(NULLTID,"NULLTID");
        m_constantNames.put(NULLUID,"NULLUID");
        m_constantNames.put(UNKNOWN_TID,"UNKNOWN_TID");
        m_constantNames.put(UNKNOWN_UID,"UNKNOWN_UID");
        m_constantNames.put(SPECIAL_TID,"SPECIAL_TID");
        m_constantNames.put(NOT_FOUND_UID,"NOT_FOUND_UID");
        m_constantNames.put(UNDEFINED_UID,"UNDEFINED_UID");
        m_constantNames.put(NULL_OBJECT,"NULL_OBJECT");
        m_constantNames.put(VALUE_OBJECT,"VALUE_OBJECT");
        m_constantNames.put(STUB_OBJECT,"STUB_OBJECT");
        m_constantNames.put(SKEL_OBJECT,"SKEL_OBJECT");
        m_constantNames.put(BSF_DEBUG_MANAGER_UID,"BSF_DEBUG_MANAGER_UID");
        m_constantNames.put(BSF_DEBUG_MANAGER_TID,"BSF_DEBUG_MANAGER_TID");
        m_constantNames.put(JS_ENGINE_TID,"JS_ENGINE_TID");
        m_constantNames.put(JS_CALLBACKS_TID,"JS_CALLBACKS_TID");
        m_constantNames.put(JS_CONTEXT_TID,"JS_CONTEXT_TID");
        m_constantNames.put(JS_OBJECT_TID,"JS_OBJECT_TID");
        m_constantNames.put(JS_CODE_TID,"JS_CODE_TID");
        m_constantNames.put(VALUE_OBJECT_TID,"VALUE_OBJECT_TID");
        m_constantNames.put(BSF_DEBUGGER_TID,"BSF_DEBUGGER_TID");
        m_constantNames.put(ERROR_OCCURED,"ERROR_OCCURED");
        m_constantNames.put(DM_GET_LANG_FROM_FILENAME,"DM_GET_LANG_FROM_FILENAME");
        m_constantNames.put(DM_IS_LANGUAGE_REGISTERED,"DM_IS_LANGUAGE_REGISTERED");
        m_constantNames.put(DM_PLACE_BREAKPOINT_AT_LINE,"DM_PLACE_BREAKPOINT_AT_LINE");
        m_constantNames.put(DM_REMOVE_BREAKPOINT,"DM_REMOVE_BREAKPOINT");
        m_constantNames.put(DM_REGISTER_DEBUGGER_FOR_LANG,"DM_REGISTER_DEBUGGER_FOR_LANG");
        m_constantNames.put(DM_UNREGISTER_DEBUGGER_FOR_LANG,"DM_UNREGISTER_DEBUGGER_FOR_LANG");
        m_constantNames.put(DM_SET_ENTRY_EXIT,"DM_SET_ENTRY_EXIT");
        m_constantNames.put(DM_QUIT_NOTIFY,"DM_QUIT_NOTIFY");
        m_constantNames.put(JE_GET_CONTEXT_AT,"JE_GET_CONTEXT_AT");
        m_constantNames.put(JE_GET_CONTEXT_COUNT,"JE_GET_CONTEXT_COUNT");
        m_constantNames.put(JE_RUN,"JE_RUN");
        m_constantNames.put(JE_STEP_IN,"JE_STEP_IN");
        m_constantNames.put(JE_STEP_OUT,"JE_STEP_OUT");
        m_constantNames.put(JE_STEP_OVER,"JE_STEP_OVER");
        m_constantNames.put(JE_GET_GLOBAL_OBJECT,"JE_GET_GLOBAL_OBJECT");
        m_constantNames.put(JE_GET_UNDEFINED_VALUE,"JE_GET_UNDEFINED_VALUE");
        m_constantNames.put(JE_SET_DEBUGGER,"JE_SET_DEBUGGER");
        m_constantNames.put(JE_GET_THREAD,"JE_GET_THREAD");
        m_constantNames.put(JE_GET_THREADGROUP,"JE_GET_THREADGROUP");
        m_constantNames.put(JO_DEFINE,"JO_DEFINE");
        m_constantNames.put(JO_DELETE_BY_INDEX,"JO_DELETE_BY_INDEX");
        m_constantNames.put(JO_DELETE_BY_NAME,"JO_DELETE_BY_NAME");
        m_constantNames.put(JO_GET_BY_NAME,"JO_GET_BY_NAME");
        m_constantNames.put(JO_GET_BY_INDEX,"JO_GET_BY_INDEX");
        m_constantNames.put(JO_GET_CLASSNAME,"JO_GET_CLASSNAME");
        m_constantNames.put(JO_GET_DEFAULT_VALUE,"JO_GET_DEFAULT_VALUE");
        m_constantNames.put(JO_GET_IDS,"JO_GET_IDS");
        m_constantNames.put(JO_HAS_BY_INDEX,"JO_HAS_BY_INDEX");
        m_constantNames.put(JO_HAS_BY_NAME,"JO_HAS_BY_NAME");
        m_constantNames.put(JO_GET_PROTOTYPE,"JO_GET_PROTOTYPE");
        m_constantNames.put(JO_GET_SCOPE,"JO_GET_SCOPE");
        m_constantNames.put(JO_HAS_INSTANCE,"JO_HAS_INSTANCE");
        m_constantNames.put(JO_PUT_BY_INDEX,"JO_PUT_BY_INDEX");
        m_constantNames.put(JO_PUT_BY_NAME,"JO_PUT_BY_NAME");
        m_constantNames.put(JO_SET_PROTOTYPE,"JO_SET_PROTOTYPE");
        m_constantNames.put(JO_SET_SCOPE,"JO_SET_SCOPE");
        m_constantNames.put(JO_FUNCTION,"JO_FUNCTION");
        m_constantNames.put(JO_SCRIPT,"JO_SCRIPT");
        m_constantNames.put(JC_GET_LINE_NUMBERS,"JC_GET_LINE_NUMBERS");
        m_constantNames.put(BSFD_CREATED_ENGINE,"BSFD_CREATED_ENGINE");
        m_constantNames.put(BSFD_DELETED_ENGINE,"BSFD_DELETED_ENGINE");
        m_constantNames.put(BSFD_DISCONNECT,"BSFD_DISCONNECT");
        m_constantNames.put(CX_BIND,"CX_BIND");
        m_constantNames.put(CX_GET_CODE,"CX_GET_CODE");
        m_constantNames.put(CX_GET_DEPTH,"CX_GET_DEPTH");
        m_constantNames.put(CX_GET_ENGINE,"CX_GET_ENGINE");
        m_constantNames.put(CB_POLL,"CB_POLL");
        m_constantNames.put(CX_GET_SCOPE,"CX_GET_SCOPE");
        m_constantNames.put(CX_GET_LINE_NUMBER,"CX_GET_LINE_NUMBER");
        m_constantNames.put(CX_GET_SOURCE_NAME,"CX_GET_SOURCE_NAME");
        m_constantNames.put(CX_GET_THIS,"CX_GET_THIS");
        m_constantNames.put(CB_POLL,"CB_POLL");
        m_constantNames.put(CB_HANDLE_BREAKPOINT_HIT,"CB_HANDLE_BREAKPOINT_HIT");
        m_constantNames.put(CB_HANDLE_ENGINE_STOPPED,"CB_HANDLE_ENGINE_STOPPED");
        m_constantNames.put(CB_HANDLE_STEPPING_DONE,"CB_HANDLE_STEPPING_DONE");
        m_constantNames.put(CB_HANDLE_EXCEPTION_THROWN,"CB_HANDLE_EXCEPTION_THROWN");

    }

}

