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

import org.apache.bsf.debug.util.DebugLog;

public class ThreadCell implements Runnable {

	static private int ThreadIdGenerator = 0;
	static boolean isServer;

	static {
		ThreadIdGenerator = (int)System.currentTimeMillis();
		isServer = Boolean.getBoolean("org.apache.bsf.isServer");
	}

	Thread m_thread;
	Object m_streamLock;
	Object m_waitLock;
	ResultCell m_stack;
	int m_thid;
	boolean m_loopback;
	SocketConnection m_con;
	boolean m_started;

	/**
	 * This creates a distributed thread upon an outgoing
	 * invocation, whose first local thread is the one 
	 * passed as parameter.
	 * Notice that the thId is generated either on the 
	 * client or the server and distinguised through 
	 * negative or positive numbers.
	 * IMPORTANT:
	 * This is a very simple scheme that allows only for
	 * one server and one client to dialog.
	 */
	ThreadCell(SocketConnection con, Thread thread) {

		m_streamLock = new Object();
		m_waitLock = new Object();

		m_con = con;
		if (isServer)
			m_thid = ++ThreadIdGenerator;
		else
			m_thid = --ThreadIdGenerator;

		m_thread = thread;
	}

	/**
	 * This is to create a proxy of an existing distributed 
	 * thread... it is called when an incoming invocation 
	 * arrives under an unknown thid... 
	 * A thread will be created later and provided to the
	 * ThreadCell::start method. This will be the thread
	 * carrying on the incoming call.
	 * See below.
	 */

	ThreadCell(SocketConnection con, int thId) {
		m_streamLock = new Object();
		m_waitLock = new Object();
		m_thread = new Thread(this, "BSF Thread " + thId);

		m_started = false;
		m_thread.start();
		synchronized (m_waitLock) {
			while (!m_started) {
				try {
					m_waitLock.wait(1);
				} catch (InterruptedException ex) {
				}
			}
		}		
		m_con = con;
		m_thid = thId;
		m_stack = null;
	}

	int getThId() {
		return m_thid;
	}

	Thread getThread() {
		return m_thread;
	}

	/**
	 * Simply allows to switch to this thread...
	 * it automatically executes the top invocation
	 * on the stack.
	 */
	public synchronized void run() {
            boolean loop = true;

            m_started = true;
            while (loop) {
                try {
                    this.wait();
                } catch (InterruptedException ex) {
                    loop = false;
                    continue;
                }
                if (m_loopback) {
                    // this thread has been looping back..
                    // the ResultCell of the loopback invocation has already 
                    // been pushed on the stack, see loopbackNotify(ResultCell)
                    m_loopback = false;
                    execTopStack();
                    
                    try {
                        popInvocation(null,true);
                    } catch (Exception ex) {
                        // just ignore top-level exceptions...
                    }
                    continue;
                }
            }
	}

	/**
	 * Utility method for executing the top invocation 
	 * on the stack. The top frame is not popped, it
	 * is left on the stack once invoked.
	 * 
	 * Exception Management:
	 * If any exception occurs during the invoke it is
	 * caught and put in the result cell. It will therefore
	 * be sent back to the loopback invoker.
	 * 
	 */
	private synchronized void execTopStack() {
		try {
			m_con.dispatchInvocation(m_stack);
		} catch (Exception ex) {
			DebugLog.stderrPrintln("\nException Raised in loopback...", DebugLog.BSF_LOG_L3);
			m_stack.setException(ex);
		}
		// send the result if any, 
		// or the exception if any occured.
		m_stack.sendResult();
	}

	/**
	 * Wait for completion of an outgoing invocation.
	 * While waiting, it is possible to have a loopback
	 * condition, in which case this thread is reused to 
	 * carry on the incoming invocation.
	 */
    public synchronized void waitOnCompletion(ResultCell rcell) 
        throws Exception {

		if (rcell != m_stack)
			throw new Error("About to wait for completion, but not on the top of the stack...");

                // send the invocation for the top of the stack.
                rcell.sendInvocation();
                
                // then suspend until done...
                // see in the while, the loopback case...
                while (!rcell.done) {
                    try {
                        this.wait();
                    } catch (InterruptedException ex) {
                    }
                    // we have been awaken for one of two reasons:
                    //		- loopback
                    //		- the invocation we waited on completed
                    
                    if (m_loopback) {
                        // this thread has been looping back..
                        // the ResultCell of the loopback invocation has 
                        // already been pushed on the stack, see 
                        // loopbackNotify(ResultCell)
                        m_loopback = false;
                        
                        // execute the loopback invocation, 
                        // if any exception occurs, it is caught and sent back.
                        // it never percolates back up here.
                        execTopStack();
                        
                        // pop the loopback that just completed.
                        // ignore any exception because we are in a loopback,
                        // it has been sent over the wire already.
                        popInvocation(m_stack, true);
                        continue;
                    }
                    if (m_stack.done) break;
		}
		popInvocation(rcell, false);
	}

	public synchronized void completionNotify(ResultCell rcell) {
            rcell.done = true;
            if (rcell.disconnected) m_thread.interrupt();
            this.notify();
	}

	public synchronized void pushLoopback(ResultCell rcell) {
            // push the incoming invocation on the 
            // top of the stack.
            pushInvocation(rcell);
            m_loopback = true;
            this.notify();
	}

	public synchronized void pushInvocation(ResultCell rcell) {
		// push the incoming invocation on the 
		// top of the stack.
		rcell.thread = this;
		rcell.parent = m_stack;
		m_stack = rcell;
	}

    private synchronized ResultCell popInvocation (ResultCell rcell,
                                                   boolean loopback) 
            throws Exception {
		// push the incoming invocation on the 
		// top of the stack.
		Exception ex;
		if (m_stack==null) 
			throw new Error("***** Unpaired Push/Pop.");
		if (rcell==null) {
			if (m_stack.parent!=null) 
				throw new Error("***** Not popping last frame...");
			rcell = m_stack;
			m_stack = null;
		} else {
			if (m_stack!=rcell) 
				throw new Error("***** Popping but not top of stack.");
			
			m_stack = rcell.parent;
		}

		if (!loopback) {
			ex = rcell.getException();
			if (ex!=null) 
				throw ex;
		}
		return rcell;
	}
}
