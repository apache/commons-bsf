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

package org.apache.bsf.dbline;

import java.io.*;
import java.util.*;
import java.net.ProtocolException;
import java.net.ConnectException;
import java.rmi.RemoteException;

import org.apache.bsf.debug.*;
import org.apache.bsf.debug.jsdi.*;

public class JsDb 
    implements Runnable {
    
    private JsObject global;
    private Callbacks callbacks;
    private Thread m_cmdThread;
    private Object m_lock;
    private static boolean m_inCallback;
    private static int exitStatus = 0;
    private static boolean running = false;

    Vector m_buffers;
    int m_currentDepth, m_stackDepth;
    Context m_stack[];
    JsObject undefined;
    JsEngine m_jse;
    Hashtable m_proxies, m_rot;
    
    static JsDb self;
    static BSFDebugManager gBsfManager;
    static final int JSDB_CMD_LEN = 256;
    static final String usage[] =
    {
        "Supported commands:",
        "load <FILENAME> <URI>",
        "br shortname:16",
        "rm <breakpoint id>",
        "set_entry_exit shortname [true, false]",
        "exec shortname:12,28 (start line, end line to be interpreted)",
        "step [in, over, out]",
        "run",
        "thrinfo",
        "list frames (display the stack of contexts)",
        "list (display source around current position)",
        "list shortname[:lineno] (display source starting at the given line)",
        "up / down (move up or down the current context on the stack)",
        "show frame (display the current context on the stack)",
        "show object:id[,all]  (display an object with the given id, ",
        "                       the option 'all' precise to enum properties",
        "                       with a DONTENUM attribute.)",
        "show scope:id[,all]  (display the scope chain of the object with ",
        "                      the given id,the option all precise to enum ",
        "                      properties with a DONTENUM attribute.)",
        "show prototype:id[,all]  (display the prototype chain of the object ",
        "                          with the given id, the option all precise ",
        "                          to enum properties with a DONTENUM attribute.)",
        "put <id>:propname=value (where the value is either a string,",
        "                         a float, a boolean, or an object)",
        "               Examples: ",
        " 		put <2>:foo=That's it folks",
        "		put <4>:bar=4.0",
        " 		put <45>:trueOrFalse=false",
        "		put <31>:foo=<4>",
        "That's it...",
        "Notations:",
        "   URI: full URI, sans hostname and protocol specification",
        "   shortname: last component of the URI" 
    };

    public JsDb(String args[]) 
        throws RemoteException {
        
        self = this;
        m_buffers = new Vector();
        m_proxies = new Hashtable();
        m_rot = new Hashtable();
        m_lock = new Object();
        m_inCallback = false;
        
        callbacks = new Callbacks(this);
        gBsfManager.registerDebugger("javascript", callbacks);
        
        m_cmdThread = new Thread(this, "User Input Thread");
        m_cmdThread.start();
    }
    
    /**
     * This returns a proxy object for a remote JsObject.
     * The debugger maintains a hashtable of its proxies
     * keyed on JsObject.
     */
    JsObjectProxy proxyObject(JsObject obj) {
        
        if (obj == null) return null;

        JsObjectProxy proxy = (JsObjectProxy) m_proxies.get(obj);
        if (proxy == null) {
            Integer oid;
            proxy = new JsObjectProxy(obj);
            m_proxies.put(obj, proxy);
            oid = new Integer(proxy.getOid());
            m_rot.put(oid, proxy);
        }
        return proxy;
    }
    
    /**
     * Display the properties of an Object.
     * The "all" parameters indicates if one wants to see
     * all the properties, including the ones with DONTENUM
     * attribute.
     * IMPORTANT Note:
     * This shows only the properties on the object itself,
     * the prototype chain is not considered.
     */
    private void displayProperties(JsObjectProxy proxy, boolean all)
        throws RemoteException {
        
        Object ids[], id, value;
        String name;
        JsObject object = proxy.getObject();
        JsObject scope = object.getScope(), prot = object.getPrototype();
        JsObjectProxy scopeProxy, protProxy;
        int n, index;

        System.out.println("Object <" + proxy.getOid() + ">");

        if (scope != null) {
            scopeProxy = proxyObject(scope);
            System.out.println("    Scope <" + scopeProxy.getOid() + ">");
        } 
        else System.out.println("    No scope.");

        if (prot != null) {
            protProxy = proxyObject(prot);
            System.out.println("    Prototype <" + protProxy.getOid() + ">");
        } 
        else System.out.println("    No prototype.");
        
        ids = object.getIds(all);
        if (ids != null) {
            for (n = 0; n < ids.length; n++) {
                id = ids[n];
                if (id instanceof String) {
                    name = (String) id;
                    value = object.get(name);
                    displayProperty(name, value);
                } 
                else {
                    index = ((Integer) id).intValue();
                    value = object.get(index);
                    displayProperty(String.valueOf(index), value);
                }
            }
        } 
        else System.out.println("    No properties.");
    }
    
    /**
     * Display the Scope of an Object.
     * The "all" parameters indicates if one wants to see
     * all the properties, including the ones with DONTENUM
     * attribute.
     * IMPORTANT Note:
     * This shows only the properties on the object itself,
     * the prototype chain is not considered.
     */
    private void displayScope(JsObjectProxy proxy, boolean all)
        throws RemoteException {
        
        Object ids[], id, value;
        String name;
        JsObject object = proxy.getObject();
        JsObject scope = object.getScope();
        JsObjectProxy scopeProxy;
        int n, index;
        
        if (scope == null) {
            System.out.println("No scope");
            return;
        }
        System.out.println("Scope chain for Object <" + proxy.getOid() + ">");
        
        while (scope != null) {
            scopeProxy = proxyObject(scope);
            System.out.println("** Scope <" + scopeProxy.getOid() + ">");
            
            ids = scope.getIds(all);
            for (n = 0; n < ids.length; n++) {
                id = ids[n];
                if (id instanceof String) {
                    name = (String) id;
                    value = object.get(name);
                    displayProperty(name, value);
                }
                else {
                    index = ((Integer) id).intValue();
                    value = object.get(index);
                    displayProperty(String.valueOf(index), value);
                }
            }
            scope = scope.getScope();
        }
    }
    
    /**
     * Display the prototype chain...
     * The "all" parameters indicates if one wants to see
     * all the properties, including the ones with DONTENUM
     * attribute.
     * IMPORTANT Note:
     * This shows only the properties on the object itself,
     * the prototype chain is not considered.
     */
    private void displayPrototype(JsObjectProxy proxy, boolean all)
        throws RemoteException {
        
        Object ids[], id, value;
        String name;
        JsObject object = proxy.getObject();
        JsObject prot = object.getPrototype();
        JsObjectProxy protProxy;
        int n, index;
        
        if (prot == null) {
            System.out.println("Empty prototype chain.");
            return;
        } 
        else System.out.println("Prototype Chain for Object <" 
                                + proxy.getOid() + ">");
        
        while (prot != null) {
            protProxy = proxyObject(prot);
            System.out.println("** Prototype <" + protProxy.getOid() + ">");
            
            ids = prot.getIds(all);
            for (n = 0; n < ids.length; n++) {
                id = ids[n];
                if (id instanceof String) {
                    name = (String) id;
                    value = object.get(name);
                    displayProperty(name, value);
                } 
                else {
                    index = ((Integer) id).intValue();
                    value = object.get(index);
                    displayProperty(String.valueOf(index), value);
                }
            }
            prot = prot.getPrototype();
        }
    }
    
    /**
     * Display a property. A property value can only be of the
     * following types:
     * <UL>
     * <LI>java.lang.Boolean objects</LI>
     * <LI>java.lang.String objects</LI>
     * <LI>java.lang.Number objects</LI>
     * <LI>org.apache.bsf.debug.jsdi.JsObject objects</LI>
     * <LI>null</LI>
     * <LI>The object returned by JsContext.getUndefinedValue()</LI>
     */
    private void displayProperty(String name, Object value) {
        
        System.out.print("  ");
        
        if (value instanceof Number) {
            System.out.println(name + "=" + ((Number) value).floatValue());
        }
        else if (value instanceof String) {
            System.out.println(name + "=" + value);
        } 
        else if (value instanceof Boolean) {
            System.out.println(name + "=" + ((Boolean) value).booleanValue());
        } 
        else if (value instanceof JsObject) {
            if (value == undefined) {
                System.out.println(name + "= undefined");
            }
            else {
                JsObjectProxy proxy = proxyObject((JsObject) value);
                int oid = proxy.getOid();

                System.out.println(name + "= Object <" + oid + ">");
            }
        } 
        else System.out.println(name + value);
    }

    /**
     * Search for a buffer with the given name as
     * a short name (no path).
     * A prefix search is done, that is, any buffer
     * that has a matching prefix will be returned.
     */
    private Buffer findBufferWithShortName(String name) {

        Enumeration e = m_buffers.elements();
        Buffer buffer;
        String bufferName;

        while (e.hasMoreElements()) {
            buffer = (Buffer) e.nextElement();
            bufferName = buffer.getName();
            if (bufferName.startsWith(name)) return buffer;
        }
        return null;
    }

    /**
     * Search for a buffer with the given URI (full path).
     * A prefix search is done, that is, any buffer
     * that has a matching prefix will be returned.
     */
    private Buffer findBufferWithURI(String name) {

        Enumeration e = m_buffers.elements();
        Buffer buffer;

        while (e.hasMoreElements()) {
            buffer = (Buffer) e.nextElement();
            if (name.equals(buffer.getURI())) return buffer;
        }
        return null;
    }

    public void run() {
        
        String cmd;
        boolean resume = false;
        
        running = true;
        
        while (running) {
            cmd = readCmd();
            try {
                if (cmd != null) resume = parseCmd(cmd);
                if (resume) inCallback(false);
            } catch (Throwable t) {
                System.err.println("\nError while parsing/executing command.");
                t.printStackTrace();
            }
        }
        System.exit(exitStatus);
    }

    /** 
     * Suspend a callback until the user enters a "resume"
     * kind of commands...
     */
    private static void inCallback(boolean inCallback) {
        
        m_inCallback = inCallback;
        if (m_inCallback) System.out.print("> ");
    }

    public String readCmd() {
        
        int count = 0;
        char c, chars[] = new char[JSDB_CMD_LEN];
        
        if (m_inCallback) System.out.print("> ");
        else System.out.print("< ");

        while (count < JSDB_CMD_LEN) {
            try {
                c = (char) System.in.read();
                chars[count++] = c;
                if (c == '\n')
                    break;
            } catch (IOException ex) {
                return null;
            }
        }
        if (count == JSDB_CMD_LEN) {
            System.out.println("\nLine too long.\n");
            return null;
        }
        return new String(chars, 0, count);
    }
    
    /**
     * Parses a command line.
     * Returns true if input needs to be suspended,
     * that is, if the command was to resume execution
     * in the debugged engine.
     */
    public boolean parseCmd(String line) 
        throws RemoteException {

        boolean resume = cmdParser(line);
        
        if (m_inCallback) {
            try {
                m_jse.poll();
            } catch (RemoteException ex) {
                resume = true;
            }
        } 
        
        return resume;
    }

    private boolean parseLoad(StringTokenizer cmdTokenizer)
        throws RemoteException {
	
	String filename, uri;
 
        filename = cmdTokenizer.nextToken();
        filename = filename.trim();
        uri = cmdTokenizer.nextToken();
        uri = uri.trim();
        addBuffer(filename, uri);
        return false;
    }
	
    private void showUsage() {
        
        for (int l = 0; l < usage.length; l++) {
            System.out.println(usage[l]);
        }
    }

    private boolean parseStep(StringTokenizer cmdTokenizer)
        throws RemoteException {
	
	String cmd, args;
        
        if (cmdTokenizer.hasMoreTokens()) {
            cmd = cmdTokenizer.nextToken();
            cmd = cmd.trim();
            if (cmd.equals("in")) {
                m_jse.stepIn();
                return true;
            } 
            else if (cmd.equals("out")) {
                if (!m_inCallback) {
                    System.out.println("Not in a callback...");
                    return false;
                }
                m_jse.stepOut();
                return true;
            } 
            else if (cmd.equals("over")) {
                if (!m_inCallback) {
                    System.out.println("Not in a callback...");
                    return false;
                }
                m_jse.stepOver();
                return true;
            }
        } 
        else System.out.println("Incorrect syntax...");
        
        return false;
    }

    private boolean parseSetEntryExit(StringTokenizer cmdTokenizer)
        throws RemoteException {
        String buffername;
        boolean onval;
        Buffer buffer;

        if (cmdTokenizer.countTokens() == 2) {
            buffername = cmdTokenizer.nextToken();
            onval = (Boolean.valueOf((cmdTokenizer.nextToken()).trim())).booleanValue();
            buffer = findBufferWithShortName(buffername);

            if (buffer != null) {
                gBsfManager.setEntryExit(buffer.getURI(), onval);
                System.out.println("Setting entry/exit status for " +
                                   buffer.getURI() + " to " + onval);
            }
        }
        else {
            System.out.println("Incorrect syntax...");
        }

        return false;
    }

    private boolean parseRemoveBreakpoint(StringTokenizer cmdTokenizer)
        throws RemoteException {
        
        String strid = cmdTokenizer.nextToken(), URI;
        int bpid = Integer.valueOf(strid.trim()).intValue();
        Enumeration e = m_buffers.elements();
        BreakPoint bp;
        Buffer buffer;
        
        while (e.hasMoreElements()) {
            buffer = (Buffer) e.nextElement();
            bp = buffer.removeBreakpoint(bpid);
            if (bp != null) {
                URI = buffer.getURI();
                gBsfManager.removeBreakpoint(URI, bpid);
                System.out.println("Removed breakpoint " + bpid + " in " +
                                   URI);
                break;
            }
        }
        return false;
    }
    
    private boolean parseBreakpoint(StringTokenizer cmdTokenizer)
        throws RemoteException {
        
        String cmd, args, buffername, strno;
        Integer lineno;
        StringTokenizer wordTokenizer;
        
        args = cmdTokenizer.nextToken();
        wordTokenizer = new StringTokenizer(args, ":,", false);
        buffername = wordTokenizer.nextToken();
        buffername = buffername.trim();
        strno = wordTokenizer.nextToken();
        lineno = Integer.valueOf(strno.trim());
        addBreakpoint(buffername, (lineno.intValue() - 1));
        return false;
    }

    private boolean parseExec(StringTokenizer cmdTokenizer)
        throws RemoteException {
        
        String cmd, args, buffername, strno, scriptText;
        Integer start, end;
        StringTokenizer wordTokenizer;
        Buffer buffer;
        StringBuffer buf;

        args = cmdTokenizer.nextToken();
        wordTokenizer = new StringTokenizer(args, ":,", false);
        buffername = wordTokenizer.nextToken();
        buffername = buffername.trim();
        strno = wordTokenizer.nextToken();
        start = Integer.valueOf(strno.trim());
        start = new Integer(start.intValue() - 1);
        strno = wordTokenizer.nextToken();
        end = Integer.valueOf(strno.trim());
        end = new Integer(end.intValue() - 1);
        buffer = findBufferWithShortName(buffername);
        buf = buffer.buildFnOrScript(start.intValue(), end.intValue());
        scriptText = buf.toString();

        System.out.println("\nExecuting...");
        try {
            m_jse.eval(buffer.getName(), scriptText, start.intValue());
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
        System.out.println("\nExecution done.");

        return false;
    }

    private boolean parseShow(StringTokenizer cmdTokenizer)
        throws RemoteException {
        
        String cmd, args, what, strno;
        StringTokenizer wordTokenizer;
        Integer oid;
        Boolean bool;

        args = cmdTokenizer.nextToken();
        wordTokenizer = new StringTokenizer(args, " :,", false);
        what = wordTokenizer.nextToken();
        what = what.trim();
        
        if (what.equals("frame")) {
            showCurrentFrame();
        } 
        else if (what.equals("scope")) {
            bool = new Boolean(false);
            strno = wordTokenizer.nextToken();
            oid = Integer.valueOf(strno.trim());
            if (wordTokenizer.hasMoreTokens()) {
                strno = wordTokenizer.nextToken();
                strno = strno.trim();
                if (strno.equals("all")) bool = new Boolean(true);
            } 
            else bool = new Boolean(false);
            showScope(oid, bool.booleanValue());
            
        } 
        else if (what.equals("prototype")) {
            bool = new Boolean(false);
            strno = wordTokenizer.nextToken();
            oid = Integer.valueOf(strno.trim());
            if (wordTokenizer.hasMoreTokens()) {
                strno = wordTokenizer.nextToken();
                strno = strno.trim();
                if (strno.equals("all")) bool = new Boolean(true);
            } 
            else bool = new Boolean(false);
            showPrototype(oid, bool.booleanValue());
        } 
        else if (what.equals("object")) {
            bool = new Boolean(false);
            strno = wordTokenizer.nextToken();
            oid = Integer.valueOf(strno.trim());
            if (wordTokenizer.hasMoreTokens()) {
                strno = wordTokenizer.nextToken();
                strno = strno.trim();
                if (strno.equals("all")) bool = new Boolean(true);
            } else bool = new Boolean(false);
            showObject(oid, bool.booleanValue());
        }
        return false;
    }
    
    private boolean parsePut(String args) 
        throws RemoteException {

        String token, strval, propname;
        StringTokenizer wordTokenizer, fieldTokenizer;
        Float fval;
        Integer oid;
        Boolean bool;
        JsObjectProxy proxy;
        JsObject target, oval;

        wordTokenizer = new StringTokenizer(args, ":=", false);
        token = wordTokenizer.nextToken();
        token = token.trim();
        token = token.substring(1, token.length() - 1);
        oid = Integer.valueOf(token);
        proxy = (JsObjectProxy) m_rot.get(oid);

        if (proxy == null) return false;
        target = proxy.getObject();
        token = wordTokenizer.nextToken();
        propname = token.trim();
        token = wordTokenizer.nextToken();
        token = token.trim();

        try {
            fval = Float.valueOf(token);
            target.put(propname, fval);
        } catch (NumberFormatException ex) {
            if (token.equals("false")) {
                bool = new Boolean(false);
                target.put(propname, bool);
            } 
            else if (token.equals("true")) {
                bool = new Boolean(true);
                target.put(propname, bool);
            } 
            else if (token.startsWith("<")) {
                token = token.substring(1, token.length() - 1);
                oid = Integer.valueOf(token);
                proxy = (JsObjectProxy) m_rot.get(oid);
                if (proxy == null) return false;
                oval = proxy.getObject();
                target.put(propname, oval);
            } 
            else target.put(propname, token);
        }
        return false;
    }

    private boolean parseList(StringTokenizer cmdTokenizer)
        throws RemoteException {
        
        String cmd, args, what, strno;
        StringTokenizer wordTokenizer;
        
        if (!cmdTokenizer.hasMoreTokens()) {
            listBuffer();
            return false;
        }
        
        args = cmdTokenizer.nextToken();
        wordTokenizer = new StringTokenizer(args, " :", false);
        what = wordTokenizer.nextToken();
        what = what.trim();
        
        if (what.equals("buffers")) {
            listBuffers();
        } 
        else if (what.equals("breakpoints")) {
            listBreakpoints();
        } 
        else if (what.equals("frames")) {
            listFrames();
        } 
        else {
            Integer lineno = new Integer(-1);
            if (wordTokenizer.hasMoreTokens()) {
                strno = wordTokenizer.nextToken();
                lineno = Integer.valueOf(strno.trim());
                lineno = new Integer(lineno.intValue() - 1);
            }
            listBuffer(what, lineno.intValue());
        }
        return false;
    }

    public boolean cmdParser(String line)
        throws RemoteException {
        
        String cmd, args;
        StringTokenizer cmdTokenizer = new StringTokenizer(line, " ", false);
        
        if (cmdTokenizer.hasMoreTokens()) {
            cmd = cmdTokenizer.nextToken();
            cmd = cmd.trim();
            cmd = cmd.toLowerCase();
            if (cmd.equals("load")) {
                return parseLoad(cmdTokenizer);
            }
            else if (cmd.equals("put")) {
                if (m_inCallback) {
                    args = line.substring(line.indexOf(" "));
                    parsePut(args);
                }
                else System.out.println("Not in a callback...");
            }
            else if (cmd.equals("usage") || cmd.equals("help")) {
                showUsage();
            }
            else if (cmd.equals("step")) {
                if (m_inCallback) return parseStep(cmdTokenizer);
                else System.out.println("Not in a callback...");
            }
            else if (cmd.equals("br")) {
                return parseBreakpoint(cmdTokenizer);
            }
            else if (cmd.equals("rm")) {
                return parseRemoveBreakpoint(cmdTokenizer);
            }
            else if (cmd.equals("set_entry_exit")) {
                return parseSetEntryExit(cmdTokenizer);
            }
            else if (cmd.equals("exec")) {
                if (m_inCallback) return parseExec(cmdTokenizer);
                else System.out.println("Not in a callback...");
            }
            else if (cmd.equals("up")) {
                if (m_inCallback) up();
                else System.out.println("Not in a callback...");
            }
            else if (cmd.equals("down")) {
                if (m_inCallback) down();
                else System.out.println("Not in a callback...");
            }
            else if (cmd.equals("run")) {
                if (m_inCallback) {
                    m_jse.run();
                    return true;
                }
                else System.out.println("Not in a callback...");
            } 
            else if (cmd.equals("show")) {
                return parseShow(cmdTokenizer);
            } 
            else if (cmd.equals("list")) {
                return parseList(cmdTokenizer);
            }
            else if (cmd.equals("thrinfo")) {
                if (m_inCallback) showThrInfo();
                else System.out.println("Not in a callback...");
            }
            else if (cmd.equals("quit")) {
                exitDebugger(0);
            }
            else if (cmd.equals("")) {
                // Ignore bogus user input.
            }
            else System.out.println("Unrecognized command: " + cmd +
                                    "\nTo see valid commands, type usage");
        }
        return false;
    }
    
    public void addBreakpoint(String buffername, int lineno)
        throws RemoteException {
	
	Buffer buffer = findBufferWithShortName(buffername);
        
        if (buffer != null) {
            BreakPoint bp = new BreakPoint();
            bp.m_lineno = lineno;
            bp.m_buffer = buffer;
            buffer.addBreakpoint(bp);
            gBsfManager.placeBreakpointAtLine(bp.m_id, buffer.getURI(), 
                                              lineno);
            System.out.println("Breakpoint " + bp.m_id + " at " 
                               + (bp.m_lineno + 1));
        }
    }

    public Buffer addBuffer(String filename, String uri) {
        
        Buffer buffer = findBufferWithURI(uri);
        
        if (buffer == null) buffer = Buffer.factory(filename, uri);
        if (buffer != null) {
            m_buffers.addElement(buffer);
            System.out.println("Loaded buffer: " + uri);
        }
        return buffer;
    }

    public void createdEngine(JsEngine engine) 
        throws RemoteException {

        m_jse = engine;
        m_jse.setDebugger(callbacks);
        undefined = m_jse.getUndefinedValue();
    }

    public void deletedEngine(JsEngine engine) {
        
        if (m_jse == engine || m_jse.equals(engine)) m_jse = null;    
    }

    public void down() {
        
        m_currentDepth--;
        if (m_currentDepth < 0) m_currentDepth = 0;
    }

    public void showThrInfo() {
        try {
            System.out.println("Thread: " + m_jse.getThread());
            System.out.println("ThreadGroup: " + m_jse.getThreadGroup());
        } catch (RemoteException ex) {
            ex.printStackTrace();
            return;
        }
    }

    public static void exitDebugger(int status) {
        inCallback(false);
        BSFConnect.disconnect();
        exitStatus = status;
        running = false;
    }

    /**
     * initialize the engine. put the manager into the context -> manager
     * map hashtable too.
     */
    public void handleBreakpointHit(JsContext top)
        throws RemoteException {
	
	int d, lineno;
        JsContext cx;
        String name;
        Buffer buffer;
        JsEngine jse = top.getEngine();
        
        if (m_jse != jse) throw new Error();
  
	m_stackDepth = m_jse.getContextCount();
        System.out.println("\n    stack depth="+m_stackDepth);
        if (m_stackDepth <= 0) 
            throw new RemoteException("Error: Stack cannot be empty.");

        m_stack = new Context[m_stackDepth];
        for (d = 0; d < m_stackDepth; d++) {
            cx = m_jse.getContext(d);
            name = cx.getSourceName();
            buffer = findBufferWithURI(name);
            m_stack[d] = new Context(cx, buffer);
        }
        m_currentDepth = 0;
        lineno = m_stack[0].getCurrentLine();
        name = m_stack[0].getBufferName();
        System.out.println("    in JSP " + name + " at line " + (lineno + 1));
        
        inCallback(true);
    }

    public void handleEngineStopped(JsContext cx) {
    }

    public void handleExceptionThrown(JsContext top, Object exception)
        throws RemoteException {
        
        int d, lineno;
        String name;
        JsContext cx;
        Buffer buffer;
        JsEngine jse = top.getEngine();
        
        if (m_jse != jse) throw new Error();
        
        m_stackDepth = m_jse.getContextCount();
        m_stack = new Context[m_stackDepth];
        for (d = 0; d < m_stackDepth; d++) {
            cx = m_jse.getContext(d);
            name = cx.getSourceName();
            buffer = findBufferWithURI(name);
            m_stack[d] = new Context(cx, buffer);
        }
        m_currentDepth = 0;
        lineno = m_stack[0].getCurrentLine();
        name = m_stack[0].getBufferName();
        System.out.println("Exception thrown at line " + name 
                           + ":" + (lineno + 1) + " reached.");
        inCallback(true);
    }

    /**
     * initialize the engine. put the manager into the context -> manager
     * map hashtable too.
     */
    public void handleSteppingDone(JsContext top)
        throws RemoteException {
	
	int d, lineno;
        JsContext cx;
        String name;
        Buffer buffer;
        JsEngine jse = top.getEngine();
	
	if (m_jse != jse) throw new Error();

        m_stackDepth = m_jse.getContextCount();
        System.out.println("\n    stack depth="+m_stackDepth);
        if (m_stackDepth <= 0)
            throw new RemoteException("Error: Stack cannot be empty.");

        m_stack = new Context[m_stackDepth];
        for (d = 0; d < m_stackDepth; d++) {
            cx = m_jse.getContext(d);
            name = cx.getSourceName();
            buffer = findBufferWithURI(name);
            m_stack[d] = new Context(cx, buffer);
        }
        m_currentDepth = 0;
        lineno = m_stack[0].getCurrentLine();
        name = m_stack[0].getBufferName();
        System.out.println("Stepped to line " + name 
                           + ":" + (lineno + 1) + " reached.");
        inCallback(true);
    }
    
    public void listBreakpoints() {
        
        Enumeration e = m_buffers.elements(), ebp;
        BreakPoint bp;
        Buffer buffer;

        while (e.hasMoreElements()) {
            buffer = (Buffer) e.nextElement();
            System.out.println("Buffer " + buffer.getName());
            ebp = buffer.getBreakpoints();
            while (ebp.hasMoreElements()) {
                bp = (BreakPoint) ebp.nextElement();
                System.out.println("Breakpoint " + bp.m_id 
                                   + " at " + (bp.m_lineno + 1));
            }
        }
    }
    
    public void listBuffer() {
        
        String line, name;
        int d, start, end;
        Context cx;
        Buffer buffer;
        
        if (m_stack == null) return;
        
        cx = m_stack[m_currentDepth];
        name = cx.getBufferName();
        
        buffer = cx.getBuffer();
        start = cx.getCurrentLine();
        
        listBufferLines(buffer, start - 5, 10, cx);
    }
    
    public void listBuffer(String buffername, int start) {

        Buffer buffer = findBufferWithShortName(buffername);
        Context cx = null;
        int d;

        if (buffer == null) return;

        for (d = 0; d < m_stackDepth; d++) {
            cx = m_stack[d];
            if (buffer == cx.getBuffer()) break;
            cx = null;
        }
        listBufferLines(buffer, start, 10, cx);
    }

    public void listBufferLines(Buffer buffer, int start,
                                int count, Context cx) {
        
        String line;
        int d, l;
        
        if (start < 0) start = buffer.getCurrentLine();
        
        for (l = start; l < start + count; l++) {
            line = buffer.getLine(l);
            if (line == null)
                break;
            if (cx != null && l == cx.getCurrentLine())
                System.out.print("-> " + (l+1) + ":");
            else
                System.out.print("   " + (l+1) + ":");
            System.out.println(line);
        }
        buffer.setCurrentLine(l);
    }

    public void listBuffers() {
        
        Enumeration e = m_buffers.elements();
        Buffer buffer;

        while (e.hasMoreElements()) {
            buffer = (Buffer) e.nextElement();
            System.out.println(buffer.getName());
        }
    }

    public void listFrames() {

        int d;
        Context cx;

        for (d = m_currentDepth; d < m_stackDepth; d++) {
            cx = m_stack[d];
            System.out.print(cx.getBufferName());
            System.out.println(":" + (cx.getCurrentLine() + 1));
        }
    }

    public static void main(String args[]) {

        try {
            Object lock = new Object();
            String host =
                System.getProperty("org.apache.bsf.dbline.hostName");

            while (gBsfManager == null && exitStatus == 0) {
                try {
                    gBsfManager =
                        BSFConnect.connect(host, -1);
                } catch (ProtocolException pe) {
                    System.out.println(pe.getMessage());
                    exitStatus = 1;
                    continue;
                } catch (ConnectException ce) {
                    System.out.println(ce.getMessage());
                    exitStatus = 2;
                    continue;
                } catch (Throwable re) {
                    gBsfManager = null;
                }
                
                if (gBsfManager != null) {
                    new JsDb(args);
                }
                else {
                    System.out.println("Manager not there yet, sleeping.");
                    synchronized (lock) {
                        try {
                            lock.wait(10000);
                        } catch (InterruptedException ie) {
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (exitStatus > 0) System.exit(exitStatus);
    }

    public void showCurrentFrame()
        throws RemoteException {

        int d, n, index;
        Context cx = m_stack[m_currentDepth];
        JsContext jscx = cx.getJsContext();
        JsObject scope = jscx.getScope(), args;
        boolean notAfunction = false;
        Object ids[], value;
        String name;
        JsObjectProxy proxy;
        
        System.out.print("\nCurrent frame: " + cx.getBufferName());
        System.out.println(":" + cx.getCurrentLine());
        System.out.println("\nProperties in scope: ");
        while (scope != null) {
            proxy = proxyObject(scope);
            displayProperties(proxy, false);
            scope = scope.getScope();
        }
    }
    
    public void showObject(Integer oid, boolean all)
        throws RemoteException {
        
        JsObjectProxy proxy = (JsObjectProxy) m_rot.get(oid);
        
        if (proxy == null) System.out.println("Unknown object <" 
                                              + oid + "> !");
        else displayProperties(proxy, all);
    }
    
    public void showScope(Integer oid, boolean all)
        throws RemoteException {
	
	JsObjectProxy proxy = (JsObjectProxy) m_rot.get(oid);
        
        if (proxy == null) System.out.println("Unknown object <" 
                                              + oid + "> !");
	else displayScope(proxy, all);
    }
    
    public void showPrototype(Integer oid, boolean all)
        throws RemoteException {
	
	JsObjectProxy proxy = (JsObjectProxy) m_rot.get(oid);
        
        if (proxy == null) System.out.println("Unknown object <" 
                                              + oid + "> !");
	else displayPrototype(proxy, all);
    }
	
    public void up() {

        m_currentDepth++;
        if (m_currentDepth >= m_stackDepth) m_currentDepth = m_stackDepth - 1;
    }
}
