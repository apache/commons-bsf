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

package org.apache.bsf.debug.serverImpl;

import java.io.*;
import java.net.*;
import java.security.*;
import org.apache.bsf.debug.util.*;

public class GatedListener implements Runnable {
    private ServerSocket fServerSocket;
    private Socket fClientSocket;
    private Thread m_thread;
    private int m_port;
    private ObjectServer m_oserver;
    private boolean m_accept = true;
    private Object m_acceptLock = new Object();

    public GatedListener(ObjectServer oserver, int port) {
        m_oserver = oserver;
        m_port = port;
        m_thread = new Thread(this, "JSDI Connection Thread");

        m_thread.start();
    }

    public void run() {
        int num_retries = 3;

        while (m_accept) {
            try {
                accept();
            } catch (Exception e) {
                if (e instanceof SecurityException) {
                    DebugLog.stdoutPrintln("Security violation during " +
                                           "socket operation.",
                                           DebugLog.BSF_LOG_L0);
                    e.printStackTrace();
                }
                else if (e instanceof ProtocolException) {
                    DebugLog.stdoutPrintln("Client attempted to connect " +
                                           "using unsupported protocol " +
                                           "version",
                                           DebugLog.BSF_LOG_L0);
                }
                else if (e instanceof IOException) {
                    DebugLog.stdoutPrintln("Reason: I/O error opening socket.",
                                           DebugLog.BSF_LOG_L0);
                }
                else {
                    if (num_retries != 0) {
                        // Possible temporary problem
                        // Retry num_retries times...
                        DebugLog.stdoutPrintln("**** Error in accept() - " +
                                               num_retries +
                                               " retry attempts remaining.",
                                               DebugLog.BSF_LOG_L0);
                        num_retries--;
                    }
                    else {
                        DebugLog.stdoutPrintln("**** accept() failure. " +
                                               "Please correct error " +
                                               "and restart server.",
                                               DebugLog.BSF_LOG_L0);
                        DebugLog.stdoutPrintln(e.getMessage(), 
                                               DebugLog.BSF_LOG_L0);
                        m_accept = false;
                    }
                }
            }
        }
    }

    private void accept() throws Exception {
        int major = -1, minor = -1;
        InputStream istream;
        OutputStream ostream;
        DataInputStream distream;
        DataOutputStream dostream;
        
        synchronized(m_acceptLock) {
            try {
                fServerSocket = new ServerSocket(m_port);
            } catch (Exception e) {
                DebugLog.stdoutPrintln("**** Could not listen on port: " 
                                       + m_port, 
                                       DebugLog.BSF_LOG_L0);
                m_accept = false;
                throw e;
            }
            
            try {
                DebugLog.stdoutPrintln("Listener accepting on port: " 
                                       + m_port, 
                                       DebugLog.BSF_LOG_L1);
                fClientSocket = 
                    (Socket) 
                    AccessController.doPrivileged(new PrivilegedExceptionAction() {
                            public Object run() throws IOException {
                                return fServerSocket.accept();
                            }
                        }
                                                  );
            } catch (PrivilegedActionException e) {
                DebugLog.stdoutPrintln("Accept failed on port: " + m_port, 
                                       DebugLog.BSF_LOG_L0);
                throw e.getException();
            }
            
            DebugLog.stdoutPrintln("Accepted a connection on port: " + m_port,
                                   DebugLog.BSF_LOG_L1);
            
            fServerSocket.close();
            fServerSocket = null;
            
            istream = fClientSocket.getInputStream();
            ostream = fClientSocket.getOutputStream();
            dostream = new DataOutputStream(ostream);
            distream = new DataInputStream(istream);
            
            major = distream.readInt();
            minor = distream.readInt();
            if (major != DebugConstants.BSF_DEBUG_PROTOCOL_MAJOR ||
                minor != DebugConstants.BSF_DEBUG_PROTOCOL_MINOR) {
                dostream.writeInt(DebugConstants.BSF_DEBUG_PROTOCOL_REJECT);
                fClientSocket.close();
                fClientSocket = null;
                throw new ProtocolException("Protocol version mismatch!");
            }
            else {
                DebugLog.stdoutPrintln("Debug client attached on port: " + 
                                       m_port,
                                       DebugLog.BSF_LOG_L1);
                m_oserver.setIOStreams(istream, ostream, distream, dostream);  
                m_oserver.awake();
                dostream.writeInt(DebugConstants.BSF_DEBUG_PROTOCOL_ACCEPT);
                m_acceptLock.wait();
                fClientSocket.close();
                fClientSocket = null;
            }
        }
    }

    protected void awake() {
        synchronized(m_acceptLock) {
            m_acceptLock.notify();
        }
    }
}
