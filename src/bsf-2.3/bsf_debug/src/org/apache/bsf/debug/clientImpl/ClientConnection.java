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

package org.apache.bsf.debug.clientImpl;

import java.util.*;
import java.net.*;
import java.io.*;

import org.apache.bsf.debug.*;
import org.apache.bsf.debug.util.*;
import org.apache.bsf.debug.meta.*;

public class ClientConnection extends SocketConnection implements Runnable {

    int CommandIdGenerator = 1;
    Socket fSocket;
    Thread fThread;
        
    private static final int SOCKET_TIMEOUT = 100;

    Dispatcher m_dispatchers[];
    DebugManagerStub m_debugManager;
	
    public ClientConnection(String host, int port) throws IOException {
        int proto_result = -1;

        fStubs = new ClientStubTable(this);
		
        m_dispatchers = new Dispatcher[2];
        m_dispatchers[0] = new DebuggerDispatcher(this);
        m_dispatchers[1] = new JsCallbacksDispatcher(this);
		
        fSocket = new Socket(host, port);
                
        fInputStream = fSocket.getInputStream();
        fDataInputStream = new DataInputStream(fInputStream);
        fOutputStream = fSocket.getOutputStream();
        fDataOutputStream = new DataOutputStream(fOutputStream);
            
        fDataOutputStream.writeInt(DebugConstants.BSF_DEBUG_PROTOCOL_MAJOR);
        fDataOutputStream.writeInt(DebugConstants.BSF_DEBUG_PROTOCOL_MINOR);
        proto_result = fDataInputStream.readInt();

        if (proto_result == DebugConstants.BSF_DEBUG_PROTOCOL_REJECT) {
            fSocket.close();
            fSocket = null;
            throw new ProtocolException("Protocol version mismatch.");
        }
        else {
            fThread = new Thread(this, "Socket Listener");
            fThread.start();
		
            m_debugManager = new DebugManagerStub(this);
        }
    }
	
    public DebugManagerStub getDebugManager() {
        return m_debugManager;
    }

    public void run() {
        listen();
    }

    protected void dispatchInvocation(ResultCell rcell) throws Exception {
        Exception ex;
        switch(rcell.classId) { 
        case DebugConstants.BSF_DEBUGGER_TID:
            m_dispatchers[0].dispatch(rcell);
            break;
        case DebugConstants.JS_CALLBACKS_TID:			
            m_dispatchers[1].dispatch(rcell);
            break;
        default:
            throw new Error("Can't parse the invocation!");
        }
    }

    public void breakConnection() {
        try {
            m_debugManager.sendQuitNotice();
        }
        catch (Exception ex) {
            // Ignore. Means we were already disconnected.
        }
        stopListening();
        try {
            // Try to close down the socket ASAP. Otherwise, CPU gets chewed
            // in a loop.
            fSocket.setSoLinger(false, 0);
            fSocket.shutdownInput();
            fSocket.close();
            fSocket = null;
        }
        catch (IOException ioe) {
            // Socket must already be closed, or is otherwise unreadable
        }
    }

    protected void wireExceptionNotify(Exception ex) {

        DebugLog.stdoutPrintln("Disconnected", DebugLog.BSF_LOG_L1);

        m_debugManager.disconnectNotify(ex);

        super.wireExceptionNotify(ex);
    }	
}
