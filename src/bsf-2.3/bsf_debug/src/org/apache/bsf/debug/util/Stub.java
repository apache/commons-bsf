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
import org.apache.bsf.debug.*;
import java.net.*;
import java.io.*;
import java.rmi.RemoteException;
import org.apache.bsf.debug.util.DebugConstants;

public class Stub implements RemoteService {

	public static Stub UNDEFINED;
	public static Stub NOT_FOUND;

	public static void Init(SocketConnection con) {
		UNDEFINED =
			new Stub(con, DebugConstants.SPECIAL_TID, DebugConstants.UNDEFINED_UID);
		NOT_FOUND =
			new Stub(con, DebugConstants.SPECIAL_TID, DebugConstants.NOT_FOUND_UID);
	}

        protected int m_tid, m_uid;
	protected boolean m_revoked;
	protected SocketConnection m_con;	
	
	public Stub(SocketConnection con, int tid, int uid) {
		m_con = con;
		m_tid = tid;
		m_uid = uid;
	}

	public boolean equals(Object o) {
		if (o instanceof Stub)
			return m_uid == ((Stub) o).m_uid;
            return false;
	}
	
	public int getTid() {
		return m_tid;
	}
	public int getUid() {
		return m_uid;
	}
	
	public void revoked() {
		Enumeration e;
		RemoteServiceListener l;

		DebugLog.stdoutPrintln("Revoking a stub " + this, DebugLog.BSF_LOG_L3);
		m_revoked = true;

		if (m_listeners == null)
			return;
		
		e = m_listeners.elements();
		while (e.hasMoreElements()) {
			l = (RemoteServiceListener) e.nextElement();
			l.revokedNotify(this);
		}		

		FutureCell cell = null;
		Exception ex = new RemoteException("Lost connection... stub revoked");
		
		DebugLog.stdoutPrintln("	revoking futures...", DebugLog.BSF_LOG_L3);
		synchronized (m_futureLock) {
			e = m_futureCells.elements();
			while (e.hasMoreElements()) {
				cell = (FutureCell) e.nextElement();
				DebugLog.stdoutPrintln(
					"		revoking for requester " + cell.requester,
					DebugLog.BSF_LOG_L3);
				cell.resume = true;
				cell.ex = ex;
				m_futureLock.notifyAll();
			}
			DebugLog.stdoutPrintln("	Done with futures.", DebugLog.BSF_LOG_L3);
			m_futureCells = new Vector();
		}
	}

	public SocketConnection getConnection() {
		return m_con;
	}

	public Stub swizzle(int tid, int uid) {
		return m_con.swizzle(tid, uid);
	}	

	private   Vector m_listeners;
	
	public void addListener(RemoteServiceListener l) {
		if (m_listeners == null)
			m_listeners = new Vector();
		m_listeners.addElement(l);
	}

	public void removeListener(RemoteServiceListener l) {
		if (m_listeners != null) {
			m_listeners.removeElement(l);
		}
	}

	private static Object m_futureLock = new Object();
	private Vector m_futureCells = new Vector(); // FutureCell 
	class FutureCell {
		Object requester;
		Thread thread;
		boolean resume;
		Exception ex;
	}
	
	public void createFuture(Object requester) throws RemoteException {
		Enumeration e;
		FutureCell cell;

		DebugLog.stdoutPrintln(
			"Creating future for requester " + requester + " on " + this,
			DebugLog.BSF_LOG_L3);
		try {
			synchronized (m_futureLock) {
			e = m_futureCells.elements();
			while (e.hasMoreElements()) {
					cell = (FutureCell) e.nextElement();
					if (cell.requester == requester)
					throw new Exception("Can't create multiple future on same requester.");
			}
			cell = new FutureCell();
			cell.requester = requester;
			cell.thread = Thread.currentThread();
			cell.resume = false;
			m_futureCells.addElement(cell);
			}
		} catch (Exception ex) {
			throw new RemoteException("Error in future management", ex);
		}
	}
	public void suspendFuture(Object requester) throws RemoteException {
			
		Enumeration e;
		FutureCell cell = null;
		
		DebugLog.stdoutPrintln(
			"Suspending on future for requester " + requester + " on " + this,
			DebugLog.BSF_LOG_L3);
		try {
			synchronized (m_futureLock) {
				cell = findFuture(requester);
				DebugLog.stdoutPrintln(
					"Suspending future for "
						+ cell.requester
						+ " on thread "
						+ cell.thread
						+ " on "
						+ this,
                                               DebugLog.BSF_LOG_L3);
			
				if (!cell.resume & !this.m_revoked) {
					// only do the loop if we need to suspend
					// it may be the case that the debugger called back
					// a "run" or "step" command as part of the callback,
					// before it returned.
			while (true) {
				try {
					m_futureLock.wait(1000);
							if (cell.resume)
								break;
							if (this.m_revoked)
								break;
				} catch (InterruptedException ex) {
				}
			}
				}
				// remove the future...
				m_futureCells.removeElement(cell);
				// Treat the future state and act accordingly.
			if (cell.ex != null) {
					DebugLog.stdoutPrintln(
						"Future for "
							+ cell.requester
							+ " on thread "
							+ cell.thread
							+ " throwing Exception "
							+ cell.ex,
                                                       DebugLog.BSF_LOG_L3);
				throw cell.ex;
			} else {
					DebugLog.stdoutPrintln(
						"Future for " + cell.requester + " on thread " + cell.thread + " resuming...",
                                                       DebugLog.BSF_LOG_L3);
			}			
		}
		} catch (Exception ex) {
			throw new RemoteException("Error in future management", ex);
		}
	}
	
	public void completeFuture(Object requester) throws RemoteException {

		Enumeration e;
		FutureCell cell = null;

		DebugLog.stdoutPrintln(
			"Completing future for requester " + requester + " on " + this,
                                       DebugLog.BSF_LOG_L3);
		try {
			synchronized (m_futureLock) {
				cell = findFuture(requester);
				DebugLog.stdoutPrintln(
					"Waking up future for requester " + requester + " on " + this,
					DebugLog.BSF_LOG_L3);
				// OG m_futureCells.removeElement(cell);
			cell.resume = true;
			m_futureLock.notifyAll();
			}
		} catch (Exception ex) {
			throw new RemoteException("Error in future management", ex);
		}
	}
	
	public void revokeFuture(Object requester, Exception ex) throws Exception {

		Enumeration e;
		FutureCell cell = null;

		DebugLog.stdoutPrintln(
			"revoking future for requester " + requester + " on " + this,
			DebugLog.BSF_LOG_L3);

		try {
			synchronized (m_futureLock) {
				cell = findFuture(requester);
				DebugLog.stdoutPrintln(
					"Waking up future for requester " + requester + " on " + this,
					DebugLog.BSF_LOG_L3);
				//  OG m_futureCells.removeElement(cell);
			cell.resume = true;
			cell.ex = ex;
			m_futureLock.notifyAll();
		}
		} catch (Exception ex2) {
			throw new RemoteException("Error in future management", ex2);
		}
	}
	private FutureCell findFuture(Object requester) throws Exception {

		Enumeration e;
		FutureCell cell = null;

		DebugLog.stdoutPrintln(
			"finding future for requester " + requester + " on " + this,
			DebugLog.BSF_LOG_L3);
		e = m_futureCells.elements();
		while (e.hasMoreElements()) {
			cell = (FutureCell) e.nextElement();
			if (cell.requester == requester)
				return cell;
		}
		DebugLog.stdoutPrintln(
			"Non-existant future for requester " + requester,
			DebugLog.BSF_LOG_L2);
		throw new Exception("Non-existant future for requester " + requester);
	}	

}

