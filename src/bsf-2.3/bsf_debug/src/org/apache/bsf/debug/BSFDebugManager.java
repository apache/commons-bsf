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

package org.apache.bsf.debug;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Debug Manager.
 * 
 * This is a service for remote debuggers to gain access 
 * to remote debugging services on language engines.
 * 
 * Currently, there is only one debugger supported per 
 * engine. However, multiple debuggers may register for 
 * different engines. There may be more than one engine 
 * per language given than more than one BSFManager can 
 * be instanciated within a Java virtual machine.
 * 
 * Each debugger (instance of BSFDebugger) should first 
 * register itself to a debug manager running in the Java
 * virtual machine in which debugging is intended.
 * A debugger registers for a specific language, such as 
 * JavaScript. 
 * As a consequence of registration, the debugger will be 
 * notified of already existing engines as well as any 
 * future creation or termination of engines for the 
 * relevant language.
 * Upon this notification, the debugger can ask the engine
 * for its language-specific debugging interface and then
 * register the debugger callbacks for debugging events.
 * See org.apache.bsf.debug.jsdi.Callbacks for the JavaScript
 * example.
 * 
 * The debugging framework works on the concept of documents
 * and breakpoints. A document is basically a container for 
 * scripts (be them functions, expressions, or actual scripts).
 * These scripts are subsets of the document. Documents are
 * declared to the BSFEngine when evaluating or executing some 
 * scripts. Scripts are defined as a range, either line or 
 * character range in their document. Correspondingly, 
 * breakpoints can be set at lines or offsets in a document.
 * The line numbers and offsets are global numbers with respect
 * to the entire document. 
 * 
 * So for instance, in a JSP with JavaScript, the document is
 * the JSP file. The scripts are the tags containing JavaScript
 * code. The Jasper compiler extracts the scripts from the JSP
 * and produces a Servlet that will provide these scripts to 
 * the BSF JavaScript engine at execution time. 
 * Each of these scripts start at a given line, offsets are 
 * not supported. Breakpoints can therefore be set at lines
 * belonging to these JavaScript scripts, considering line 
 * numbers at the document level, that is, the entire JSP file.
 * 
 */
public interface BSFDebugManager extends Remote {

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
	 * @exception BSFException if file's extension is unknown.
	 */
	public String getLangFromFilename(String fileName) throws RemoteException;
	
	/**
	 * Determine whether a language is registered.
	 *
	 * @param lang string identifying a language
	 *
	 * @return true iff it is
	 */
	public boolean isLanguageRegistered(String lang) throws RemoteException;
	
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
		throws RemoteException;
	public void placeBreakpointAtOffset(int bpid, String docname, int offset)
		throws RemoteException;

	/**
	* Allows to remove a breakpoint.
	*/
	public void removeBreakpoint(String docname, int brkptid)
		throws RemoteException;

        /**
        * Allows setting entry/exit mode
        */
        public void setEntryExit(String docname, boolean on)
                throws RemoteException;

	/**
	 * Allows a debugger to ask if the engine for a given language
	 * will support either line or offset breakpoints.
	 * Note: this will most likely provoke the loading of the engine.
	 */
	public boolean supportBreakpointAtOffset(String lang)
		throws RemoteException;

	public boolean supportBreakpointAtLine(String lang)
		throws RemoteException;
	
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
		throws RemoteException;
	
	public void unregisterDebugger(String lang) 
		throws RemoteException;
}
