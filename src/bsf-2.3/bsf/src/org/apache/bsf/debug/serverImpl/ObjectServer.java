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

package org.apache.bsf.debug.serverImpl;

import java.io.*;
import org.apache.bsf.util.BSFDebugManagerImpl;
import org.apache.bsf.debug.*;
import org.apache.bsf.debug.util.*;
import org.apache.bsf.debug.jsdi.*;
import org.apache.bsf.debug.meta.*;

public class ObjectServer extends SocketConnection implements Runnable {
    public Dispatcher fDispatchers[];

    private BSFDebugManagerImpl fDebugManager;
    private Thread m_thread;
    private boolean m_outStreamLocked = false, m_ready = false;
    private Object  m_outStreamLock = new Object(), m_readyLock = new Object();
    private static GatedListener m_listener;

    public ObjectServer(BSFDebugManagerImpl debugManager, int port) {
        fDebugManager = debugManager;
        exportSkeleton(debugManager);

        if (port <= 0) port = DebugConstants.BSF_DEBUG_SERVER_PORT;

        m_thread = new Thread(this, "JSDI Server Thread");
        m_thread.start();

        if (m_listener != null) {
            DebugLog.stdoutPrintln("BSF Debug Listener instantiated already.", 
                                   DebugLog.BSF_LOG_L1);
        }
        else m_listener = new GatedListener (this, port);
    }

    public BSFDebugManagerImpl getDebugManager() {
        return fDebugManager;
    }

    public void run() {
        fDispatchers = new Dispatcher[4];
        fDispatchers[0] = new DebugManagerDispatcher(this);
        fDispatchers[1] = new JsEngineDispatcher(this);
        fDispatchers[2] = new JsContextDispatcher(this);
        fDispatchers[3] = new JsObjectDispatcher(this);

        fStubs = new ServerStubTable(this);

        // automatically export the debug manager 
        // as a skeleton.
        this.exportSkeleton(fDebugManager);
				
        while (true) {
            synchronized(m_readyLock) {
                try {
                    if (!m_ready) m_readyLock.wait();
                }
                catch (Exception ex) {
                    // Hmm. Someone interrupted us, likely.
                    // We are probably not ready to listen.
                    // Go back to sleep.
                    continue;
                }
                m_ready = false;
            }
            listen();
            m_listener.awake();
        }
    }

    protected void setIOStreams(InputStream istream, OutputStream ostream, 
                                DataInputStream distream, 
                                DataOutputStream dostream) {
        fInputStream = istream;
        fOutputStream = ostream;
        fDataInputStream = distream;
        fDataOutputStream = dostream;
    }

    protected void awake() {
        synchronized(m_readyLock) {
            m_ready = true;
            m_readyLock.notify();
        }
    }

    protected void dispatchInvocation(ResultCell rcell)
	throws Exception {
        int selfTid,selfUid;
        Skeleton self;

        switch(rcell.classId) {
        case DebugConstants.BSF_DEBUG_MANAGER_TID:
            fDispatchers[0].dispatch(rcell);
            break;
        case DebugConstants.JS_ENGINE_TID:
            fDispatchers[1].dispatch(rcell);
            break;
        case DebugConstants.JS_CONTEXT_TID:
            fDispatchers[2].dispatch(rcell);
            break;
        case DebugConstants.JS_OBJECT_TID:
            fDispatchers[3].dispatch(rcell);
            break;
        default:
            throw new Error("Wire Protocol Format Error");			
        }
    }
}
