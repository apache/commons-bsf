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

package org.apache.bsf.util;

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.beans.*;

import org.apache.bsf.*;
import org.apache.bsf.debug.*;
import org.apache.bsf.debug.serverImpl.*;
import org.apache.bsf.debug.util.DebugLog;

import java.rmi.RemoteException;

import org.apache.bsf.debug.jsdi.*;
import org.apache.bsf.debug.util.*;

public class BSFDebugManagerImpl
    extends Skeleton
    implements BSFDebugManager, RemoteServiceListener {
		
    static BSFDebugManagerImpl self;

    private long m_fnOrScriptIdGenerator = 0x1;
    private long m_documentIdGenerator = 0x100000000l;

    private Hashtable m_langcells;
    private Hashtable m_documents;

    private ObjectServer m_server;
		
    public BSFDebugManagerImpl() throws RemoteException {

        super(DebugConstants.BSF_DEBUG_MANAGER_TID,
              DebugConstants.BSF_DEBUG_MANAGER_UID);
        self = this;		
        m_langcells = new Hashtable();
        m_documents = new Hashtable();

        Integer port = Integer.getInteger("org.apache.bsf.serverPort", -1);
        m_server = new ObjectServer(this, port.intValue());
    }

    /**
     * Callback from the socket/stub layer. 
     * A stub for a remote debugger is being revoked due 
     * to a lost connection. 
     * Since we support only one debuggers from only one 
     * JVM, there is only one connection and therefore we 
     * can drop all debuggers...
     * and then drop all breakpoints.
     * 
     * ATTENTION: 
     * Breakpoints shall not be dropped if we extend this 
     * implementation to multiple debuggers through multiple
     * socket connections.
     */	
    public void revokedNotify(RemoteService service) {
        Enumeration e;
        LangCell cell;
		
        e = m_langcells.elements();
        while (e.hasMoreElements()) {
            cell = (LangCell)e.nextElement();
            DebugLog.stderrPrintln("Disconnecting the debugger",
                                   DebugLog.BSF_LOG_L1);
            cell.disconnectDebugger();
            DebugLog.stderrPrintln("Debugger disconnected.",
                                   DebugLog.BSF_LOG_L1);
        }
        DebugLog.stderrPrintln("Dropping all breakpoints...", 
                               DebugLog.BSF_LOG_L1);
        removeAllBreakpoints();
        DebugLog.stderrPrintln("All breakpoints dropped.", 
                               DebugLog.BSF_LOG_L1);
    }
    ////////////////////////////////////////////////////////////////////////
    public void finalize() {
        terminate();
    }
    long generateDocumentId() {
        long docid = m_documentIdGenerator;
        m_documentIdGenerator += 0x100000000l;
        return docid;
    }
    ////////////////////////////////////////////////////////////////////////

    /**
     * Determine the language of a script file by looking at the file
     * extension. 
     *
     * @param filename the name of the file
     *
     * @return the scripting language the file is in if the file extension
     *         is known to me (must have been registered via 
     *         registerScriptingEngine).
     *
     * @exception RemoteException if file's extension is unknown.
     */
    public String getLangFromFilename(String fileName) throws RemoteException {
        try {
            return BSFManager.getLangFromFilename(fileName);
        } catch (BSFException ex) {
            return null;
        }
    }
    ////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////

    /**
     * Determine whether a language is registered.
     *
     * @param lang string identifying a language
     *
     * @return true iff it is
     */
    public boolean isLanguageRegistered(String lang) {
        return BSFManager.isLanguageRegistered(lang);
    }
    /**
     * Allows a debugger to ask if the engine for a given language
     * will support either line or offset breakpoints.
     * Note: this will most likely provoke the loading of the engine.
     */
    public boolean supportBreakpointAtOffset(String lang)
        throws RemoteException {
        return false;		
    }

    public boolean supportBreakpointAtLine(String lang)
        throws RemoteException {
        return true;
    }	
		
    ////////////////////////////////////////////////////////////////////////
    public synchronized void placeBreakpointAtLine(
                                                   int brkptid,
                                                   String docname,
                                                   int lineno)
        throws RemoteException {

        DocumentCell cell;
        DebugLog.stdoutPrintln("Placing breakpoint in "+docname+" at "+lineno, DebugLog.BSF_LOG_L1);
        cell = getDocumentCell(docname);
        cell.addBreakpointAtLine(brkptid, lineno);
    }
    public synchronized void placeBreakpointAtOffset(
                                                     int brkptid,
                                                     String docname,
                                                     int offset)
        throws RemoteException {

        DocumentCell cell;
        cell = getDocumentCell(docname);
        cell.addBreakpointAtOffset(brkptid, offset);
    }

    public synchronized void removeBreakpoint(String docname, int brkptid)
        throws RemoteException {

        DocumentCell cell;
        cell = (DocumentCell) m_documents.get(docname);
        if (cell != null)
            cell.removeBreakpoint(brkptid);
    }

    public synchronized void setEntryExit(String docname, boolean on)
        throws RemoteException {

        DocumentCell cell;
        cell = getDocumentCell(docname);
        if (cell != null)
            cell.setEntryExit(on);
    }

    public synchronized void removeAllBreakpoints() {
        String docname; 
        int bpid;
        DocumentCell cell;
        Enumeration e;
        e = m_documents.elements();
        while (e.hasMoreElements()) {
            cell = (DocumentCell) e.nextElement();
            cell.removeAllBreakpoints();
        }
    }
    ////////////////////////////////////////////////////////////////////////

    /**
     * Register a debugger for a scripting engine.
     *
     * @param lang string identifying language
     * @exception RemoteException if the language is unknown (i.e., if it
     *            has not been registered) with a reason of 
     *            REASON_UNKNOWN_LANGUAGE. If the language is known but
     *            if the interface can't be created for some reason, then
     *            the reason is set to REASON_OTHER_ERROR and the actual
     *            exception is passed on as well.
     */
	
    public synchronized void registerDebugger(String lang, BSFDebugger debugger)
        throws RemoteException {

        BSFDebugger dbg;
        DebugLog.stdoutPrintln("Registering debugger for "+lang, 
                               DebugLog.BSF_LOG_L1);
        try {
            LangCell cell = (LangCell)m_langcells.get(lang);
            if (cell == null) {
                cell = new LangCell(lang);
                m_langcells.put(lang,cell);
            } else {
                cell.disconnectDebugger();
            }
            // Now add the debugger...
            // This will notify the new registered debugger about 
            // the existing engines for that language.
            cell.setDebugger(debugger);
            debugger.addListener(this); 
			
        } catch (Exception ex) {
            DebugLog.stdoutPrintln("Error:", DebugLog.BSF_LOG_L0);
            DebugLog.stdoutPrintln(ex.getMessage(), DebugLog.BSF_LOG_L0);
            ex.printStackTrace(DebugLog.getDebugStream());
        }	
    }
	
    ////////////////////////////////////////////////////////////////////////
    // Called upon creation of a BSFManager.

    public synchronized DocumentCell loadDocumentNotify(BSFEngine eng,
                                                        String name) {
        DocumentCell cell;
        cell = (DocumentCell) m_documents.get(name);
        if (cell == null) {
            cell = new DocumentCell(this, name);
            m_documents.put(name, cell);
        }
        cell.loadNotify(eng);
        return cell;
    }
    ////////////////////////////////////////////////////////////////////////
    // Get the document cell for the provided name or create it if
    // it does not exist.

    synchronized DocumentCell getDocumentCell(String name) {
        DocumentCell cell;
        cell = (DocumentCell) m_documents.get(name);
        if (cell == null) {
            cell = new DocumentCell(this, name);
            m_documents.put(name, cell);
        }
        return cell;
    }
    ////////////////////////////////////////////////////////////////////////
    // Called upon the loading of an engine for the given BSFManager and 
    // language.

    public synchronized void registerEngine(
                                            BSFManager mger,
                                            BSFEngine eng,
                                            String lang) {

        LangCell cell = (LangCell)m_langcells.get(lang);
        if (cell == null) {
            cell = new LangCell(lang);
            m_langcells.put(lang,cell);
        }
        // Add the new engine, this will notify
        // the debugger for that language of the creation.
        cell.addEngine(eng);		
    }
    ////////////////////////////////////////////////////////////////////////
    // Called upon creation of a BSFManager.

    public synchronized void registerManager(BSFManager mger) {
    }
    ////////////////////////////////////////////////////////////////////////
    // Called upon termination of a BSFManager.

    public synchronized void terminateManagerNotify(BSFManager mger) {
    }
	
    public void terminate() {
        Enumeration e;
        LangCell cell;
        e = m_langcells.elements();
        while (e.hasMoreElements()) {
            cell = (LangCell) e.nextElement();
            cell.terminateNotify();
        }
        m_langcells = new Hashtable();
    }
    public synchronized void unregisterDebugger(String lang)
        throws RemoteException {

        LangCell cell;
        cell = (LangCell)m_langcells.get(lang);
        if (cell==null) 
            return; // silently ignore the error...
			
        cell.disconnectDebugger();
    }
    ////////////////////////////////////////////////////////////////////////
    // Called upon termination of an engine for the given manager.

    public synchronized void terminateEngineNotify(
                                                   BSFManager mger,
                                                   BSFEngine eng,
                                                   String lang) {

        LangCell cell;
        cell = (LangCell)m_langcells.get(lang);
        if (cell==null) 
            return; // silently ignore the error...
			
        cell.removeEngine(eng);

        // Remove the engine from all document cell for 
        // an unregistered engine 
        Enumeration e;
        DocumentCell doccell;
        e = m_documents.elements();
        while (e.hasMoreElements()) {
            doccell = (DocumentCell) e.nextElement();
            doccell.terminateEngineNotify(eng);
        }
    }
}
