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

package org.apache.bsf.debug.meta;
import org.apache.bsf.debug.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.rmi.RemoteException;

import org.apache.bsf.*;
import org.apache.bsf.debug.util.*;

public class DebugManagerStub extends Stub implements BSFDebugManager {
    protected Vector fEngines;
	
    public DebugManagerStub(SocketConnection con) throws IOException {
        super(con, DebugConstants.BSF_DEBUG_MANAGER_TID, 
              DebugConstants.BSF_DEBUG_MANAGER_UID);
        fEngines = new Vector();	
    }

    /**
     * A communication error occured, simply disconnect 
     * and therefore clean everything up.
     */
    public void disconnectNotify(Exception ex) {
        fEngines = new Vector();
    }

    public void sendQuitNotice() throws RemoteException {
        ResultCell cell = null;
        try {
            cell = m_con.prepareOutgoingInvoke(this,DebugConstants.BSF_DEBUG_MANAGER_TID,DebugConstants.DM_QUIT_NOTIFY);
            cell.writeInt(0);
            cell.waitForCompletion();
        }
        catch (Exception ex) {
            throw new RemoteException("Error sending quit notice.", ex);
        }
        
    }

    void engineCreateNotify(JsEngineStub eng) {
        fEngines.addElement(eng);
    }

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
    public String getLangFromFilename(String fileName) 
        throws RemoteException {
        ResultCell cell;		
        try {
            cell = m_con.prepareOutgoingInvoke(this, DebugConstants.BSF_DEBUG_MANAGER_TID, DebugConstants.DM_GET_LANG_FROM_FILENAME);
            cell.writeObject(fileName);
            return (String) cell.waitForValueObject();
        } 
        catch (IOException ex) {
            throw new RemoteException("Marshalling error", ex);
        } 
        catch (Exception ex) {
            throw new RemoteException("Error at server", ex);
        }
    }

    /**
     * Determine whether a language is registered.
     *
     * @param lang string identifying a language
     *
     * @return true iff it is
     */
    public boolean isLanguageRegistered(String lang) 
        throws RemoteException {
        ResultCell cell=null;
        try {
            cell = m_con.prepareOutgoingInvoke(this,DebugConstants.BSF_DEBUG_MANAGER_TID,DebugConstants.DM_IS_LANGUAGE_REGISTERED);
            cell.writeObject(lang);

            return cell.waitForBooleanValue();
        } 
        catch (IOException ex) {
            throw new RemoteException("Marshalling error", ex);
        } 
        catch (Exception ex) {
            throw new RemoteException("Error at server", ex);
        }
    }

    /**
     * Breakpoints are placed within documents either at a specific line
     * or offset. While breakpoints can be set at lines and offsets in 
     * the same document, there is no conversions between lines and offsets.
     * Some engines may support only offsets or only lines and therefore 
     * some breakpoints may be ignored.
     * 
     * Placing a breakpoint is local to a debugger connection.
     * In other words, breakpoints set by other debuggers are not visible
     * to a given debugger.
     * 
     * Breakpoints are given identifiers so to make easier for debuggers
     * to manipulate breakpoints. Identifiers are allocated by the debugger;
     * they must be unique for the entire session between that debugger
     * and the debug manager.
     * 
     */
    public void placeBreakpointAtLine(int bpid, String docname, int lineno)
        throws RemoteException {

        ResultCell cell=null;
        try {
            cell = m_con.prepareOutgoingInvoke(this,DebugConstants.BSF_DEBUG_MANAGER_TID,DebugConstants.DM_PLACE_BREAKPOINT_AT_LINE);
            cell.writeInt(bpid);
            cell.writeObject(docname);
            cell.writeInt(lineno);
            cell.waitForCompletion();
        } 
        catch (IOException ex) {
            throw new RemoteException("Marshalling error", ex);
        } 
        catch (Exception ex) {
            throw new RemoteException("Error at server", ex);
        }

    }

    public void placeBreakpointAtOffset(int bpid, String docname, int offset)
        throws RemoteException {
        throw new Error("FYI");
    }

    /**
     * Allows to remove a breakpoint.
     */
    public void removeBreakpoint(String docname, int bpid) 
        throws RemoteException {
        ResultCell cell;
        try {
            cell = m_con.prepareOutgoingInvoke(this,DebugConstants.BSF_DEBUG_MANAGER_TID, DebugConstants.DM_REMOVE_BREAKPOINT);
            cell.writeObject(docname);
            cell.writeInt(bpid);
            cell.waitForCompletion();
        } 
        catch (IOException ex) {
            throw new RemoteException("Marshalling error", ex);
        } 
        catch (Exception ex) {
            throw new RemoteException("Error at server", ex);
        }
    }

    /**
     * Allows setting entry/exit mode
     */
    public void setEntryExit(String docname, boolean on)
        throws RemoteException {
        ResultCell cell;
        int int_on = (on) ? 1 : 0;

        try {
            cell = m_con.prepareOutgoingInvoke(this,DebugConstants.BSF_DEBUG_MANAGER_TID, DebugConstants.DM_SET_ENTRY_EXIT);
            cell.writeObject(docname);
            cell.writeInt(int_on);
            cell.waitForCompletion();
        }
        catch (IOException ex) {
            throw new RemoteException("Marshalling error", ex);
        }
        catch (Exception ex) {
            throw new RemoteException("Error at server", ex);
        }
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
    public void registerDebugger(String lang, BSFDebugger debugger)
        throws RemoteException {
        ResultCell cell;
        try {
            cell = m_con.prepareOutgoingInvoke(this,DebugConstants.BSF_DEBUG_MANAGER_TID,DebugConstants.DM_REGISTER_DEBUGGER_FOR_LANG);
            cell.writeObject(lang);
            cell.writeObject(debugger);
            cell.waitForCompletion();
        } 
        catch (IOException ex) {
            throw new RemoteException("Marshalling error", ex);
        } 
        catch (Exception ex) {
            throw new RemoteException("Error at server", ex);
        }
    }

    public void unregisterDebugger(String lang) throws RemoteException {
        ResultCell cell;
        try {
            cell = m_con.prepareOutgoingInvoke(this, DebugConstants.BSF_DEBUG_MANAGER_TID,DebugConstants.DM_UNREGISTER_DEBUGGER_FOR_LANG);
            cell.writeObject(lang);
            cell.waitForCompletion();
        } 
        catch (IOException ex) {
            throw new RemoteException("Marshalling error", ex);
        } 
        catch (Exception ex) {
            throw new RemoteException("Error at server", ex);
        }
    }
}
