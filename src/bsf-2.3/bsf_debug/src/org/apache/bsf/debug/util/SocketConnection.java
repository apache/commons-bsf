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

package org.apache.bsf.debug.util;

import java.util.*;
import java.net.*;
import java.io.*;

import org.apache.bsf.debug.*;

public abstract class SocketConnection {

    Vector m_rcells; // ResultCell
    Hashtable m_tcells; // ThreadCell
    IntHashtable m_tcellsById;

    private boolean keep_listening;
    int fCmdIdGenerator;
    
    IntHashtable m_skeletons;
    protected StubTable fStubs;

    protected InputStream fInputStream;
    protected OutputStream fOutputStream;

    protected DataInputStream fDataInputStream;
    protected DataOutputStream fDataOutputStream;

    protected SocketConnection() {
        m_skeletons = new IntHashtable();
        fStubs = null;
        m_rcells = new Vector();
        m_tcells = new Hashtable();
        m_tcellsById = new IntHashtable();

        if (ThreadCell.isServer) {
            fCmdIdGenerator = 10000;
        } else {
            fCmdIdGenerator = 90000;
        }
    }

    public void exportSkeleton(Skeleton skel) {
        skel.allocOid(this);
        m_skeletons.put(skel.getUid(), skel);
    }

    public Skeleton getSkeleton(int uid) {
        return (Skeleton) m_skeletons.get(uid);
    }

    public Stub getStub(int tid, int uid) {
        return (Stub) fStubs.swizzle(tid, uid);
    }

    public void listen() {
        ResultCell cell = null;
        int cmdId, thId, count;
        int classId;
        int methodId;
        byte bytes[];
        boolean errorOccured, isResult;
            
        setListening(true);
            
        while (keep_listening) {
            try {
                count = fDataInputStream.readInt(); 
                // total count
                thId = fDataInputStream.readInt(); 
                // distributed thread id
                errorOccured = fDataInputStream.readBoolean(); 
                // cmdId.
                cmdId = fDataInputStream.readInt(); 
                // cmdId.
                isResult = fDataInputStream.readBoolean(); 
                // if result.
                    
                if (count != 0) {
                    bytes = new byte[count];
                    if (count != fInputStream.read(bytes))
                        throw new Error("Wire Protocol Error");
                } 
                else bytes = new byte[0];
                    
                if (errorOccured) {
                    receivedException(thId,cmdId,bytes);    
                } 
                else {
                    if (isResult) {
                        // a result...
                        receivedResult(thId,cmdId,bytes);
                    } 
                    else {
                        // an invocation...
                        receivedInvocation(thId,cmdId,bytes);
                    }
                }
            }		
            catch (InterruptedIOException iioe) {
                // Continue on; this timeout is expected.
                continue;
            }
            catch (Exception ex) {
                wireExceptionNotify(ex);
            }
        }
    }
    
    public void stopListening() {
        Enumeration e;
        ResultCell cell;

        e = m_rcells.elements();
        while (e.hasMoreElements()) {
            cell = (ResultCell) e.nextElement();
            try {
                cell.disconnected = true;
                cell.completionNotify();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        fStubs.disconnectNotify();
        setListening(false);
    }

    private synchronized void setListening(boolean listen) {
        keep_listening = listen;
    }

    private void receivedException(int thId, int cmdId, byte bytes[]) 
        throws IOException {
        Exception ex;
        ResultCell cell;		

        if (ThreadCell.isServer) 
            DebugLog.stdoutPrintln("Received error from CLIENT...",
                                   DebugLog.BSF_LOG_L2);
        else 
            DebugLog.stdoutPrintln("Received error from SERVER...",
                                   DebugLog.BSF_LOG_L2);

        DebugLog.stdoutPrintln("	**** ERROR: thId=" + thId +
                               ", cmdId=" + cmdId, 
                               DebugLog.BSF_LOG_L2);
        cell = searchCell(cmdId);
        cell.setPacketBytes(bytes);
        cell.readException();
        cell.print();

        // waking up invoker...
        // no more reading from the socket is allowed.
        cell.thread.completionNotify(cell);

    }

    private void receivedResult(int thId, int cmdId, byte bytes[]) {
        ResultCell cell;
        DebugLog.stdoutPrintln("	<<<< RESULT: thId=" + 
                               thId + ", cmdId=" + cmdId,
                               DebugLog.BSF_LOG_L3);
        if (bytes!=null)
            DebugLog.stdoutPrintln("		byte count=" + 
                                   bytes.length, DebugLog.BSF_LOG_L3);
        else 
            DebugLog.stdoutPrintln("		no bytes", 
                                   DebugLog.BSF_LOG_L3);
                
        cell = searchCell(cmdId);
        cell.setPacketBytes(bytes);
        cell.parseResult();
        // waking up invoker...
        cell.completionNotify();
    }

    private synchronized void receivedInvocation(int thId, int cmdId, 
                                                 byte bytes[]) 
        throws Exception {
		
        DebugLog.stdoutPrintln("	>>>> INVOCATION: thId=" + 
                               thId + ", cmdId=" + cmdId,
                               DebugLog.BSF_LOG_L3);
        if (bytes!=null)
            DebugLog.stdoutPrintln("		byte count=" +
                                   bytes.length,
                                   DebugLog.BSF_LOG_L3);
        else 
            DebugLog.stdoutPrintln("		no bytes", 
                                   DebugLog.BSF_LOG_L3);
            
        ResultCell rcell = new ResultCell(this);
        m_rcells.addElement(rcell);

        rcell.incomingInvocation(cmdId, bytes);
            
        invoke(rcell, thId);
    }

    /**
     * Called from the completion of an incoming remote 
     * method from the ThreadCell class. 
     * The ResultCell encodes the result to send the same
     * way a stub encodes it expected result.
     */
    void completionNotify(ResultCell rcell) {
        try {
            rcell.sendResult();
        } catch (Exception ex) {
            DebugLog.stdoutPrintln("Exception Raised while sending result.", 
                                   DebugLog.BSF_LOG_L0);
            ex.printStackTrace();
        }
    }

    /**
     * Switch to the right thread to carry on the incoming
     * invocation. 
     * If the invocation is part of a loopback, reuse
     * the waiting thread...
     * If not, create a new thread to carry on the call.
     * 
     * IMPORTANT: the socket listener thread remains only
     * a listener, it does not carry any other job than 
     * reading packets and dispatching.
     */
    private void invoke(ResultCell rcell, int thId) {

        ThreadCell tcell = (ThreadCell) m_tcellsById.get(thId);
        if (tcell == null) {
            // Not a known distributed thread...
            // so create a local thread for supporting the 
            // distributed execution and switch to that thread
            // to carry on the call...
            Thread thread;
            tcell = new ThreadCell(this, thId);
            thread = tcell.getThread();
            m_tcells.put(thread, tcell);
            m_tcellsById.put(thId, tcell);
        }
        tcell.pushLoopback(rcell);
    }

    /**
     * First call made by a stub. 
     * It will allocate the ResultCell and the output buffer for 
     * the outgoing packet.
     * It will also check if this out-going remote invocation
     * is part of a global execution already or not.
     * If not, a global execution (distributed thread) is set,
     * other the current one is reused.
     */
    public synchronized ResultCell prepareOutgoingInvoke(Stub self, 
                                                         int classId, 
                                                         int methodId)
        throws IOException {

        ThreadCell tcell;
        ResultCell rcell;

        Thread thread;
        thread = Thread.currentThread();
        tcell = (ThreadCell) m_tcells.get(thread);
        if (tcell == null) {
            tcell = new ThreadCell(this, thread);
            m_tcells.put(thread, tcell);
            m_tcellsById.put(tcell.getThId(), tcell);
        }

        rcell = new ResultCell(this);

        int cmdId;
        if (ThreadCell.isServer)
            cmdId = ++fCmdIdGenerator;
        else
            cmdId = --fCmdIdGenerator;
		
        rcell.outgoingInvocation(cmdId, classId, methodId, self);
        m_rcells.addElement(rcell);
        tcell.pushInvocation(rcell);

        return rcell;
    }

    private boolean m_outStreamLocked = false;
    private Object m_outStreamLock = new Object();

    void lockOutStream() {
        synchronized (m_outStreamLock) {
            while (m_outStreamLocked) {
                try {
                    m_outStreamLock.wait();
                } catch (InterruptedException ex) {

                }
            }
            m_outStreamLocked = true;
        }
    }

    void releaseOutStream() {
        synchronized (m_outStreamLock) {
            m_outStreamLocked = false;
            m_outStreamLock.notifyAll();
        }
    }

    void sendPacket(int thId, int cmdId, boolean isResult, 
                    byte bytes[], boolean errorOccured) {
        try {
            synchronized (fDataOutputStream) {
                fDataOutputStream.writeInt(bytes.length);
                fDataOutputStream.writeInt(thId);
                fDataOutputStream.writeBoolean(errorOccured);
                fDataOutputStream.writeInt(cmdId);
                fDataOutputStream.writeBoolean(isResult);
                if (bytes.length != 0) {
                    fOutputStream.write(bytes);
                }
            }
        } catch (Exception ex) {
            DebugLog.stdoutPrintln("Exception during sending result...", 
                                   DebugLog.BSF_LOG_L0);
            ex.printStackTrace();
            this.wireExceptionNotify(ex);
        }
    }

    public synchronized ResultCell searchCell(int cmdId) {
        ResultCell cell = null;
        Enumeration e;
        e = m_rcells.elements();
        while (e.hasMoreElements()) {
            cell = (ResultCell) e.nextElement();
            if (cell.cmdId == cmdId) 
                break;
            cell = null;
        }
        if (cell == null)
            throw new Error("Error in Wire Protocol, can't find CmdId=" + 
                            cmdId);
        return cell;
    }

    public Stub swizzle(int tid, int uid) {
        return fStubs.swizzle(tid, uid);
    }

    protected abstract void dispatchInvocation(ResultCell rcell) 
        throws Exception;

    /**
     * A Wire-related exception occurred.
     * We will consider that we have lost the connection.
     * All stubs will be revoked... allowing higher-level
     * listener to pick up that some remote objects have 
     * been revoked through the StubListener mechanism.
     *
     * Log at lower priority than a standard exception,
     * since this is the client quit mechanism too.
     */
    protected void wireExceptionNotify(Exception ex) {
        DebugLog.stdoutPrintln("A WIRE exception occurred.", 
                               DebugLog.BSF_LOG_L2);
        DebugLog.stdoutPrintln(ex.toString(),
			       DebugLog.BSF_LOG_L2);
        disconnectNotify(ex);
        fStubs.disconnectNotify();
        setListening(false);
    }

    /**
     * Raise the exception in all waiting threads...
     */
    private synchronized void disconnectNotify(Exception ex) {
        Enumeration e;
        ResultCell cell;
        DebugLog.stdoutPrintln("Raise the exception in all waiting threads...",
                               DebugLog.BSF_LOG_L2);
        e = m_rcells.elements();
        while (e.hasMoreElements()) {
            cell = (ResultCell) e.nextElement();
            try {
                cell.setException(ex);
                cell.disconnected = true;
                cell.completionNotify();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        m_rcells = new Vector();
        m_tcells = new Hashtable();
        DebugLog.stdoutPrintln("Done with raising exceptions...", 
                               DebugLog.BSF_LOG_L2);
    }
}
