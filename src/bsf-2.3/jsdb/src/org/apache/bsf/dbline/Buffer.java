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

package org.apache.bsf.dbline;

import java.io.*;
import java.util.*;
import org.apache.bsf.*;

/**
 * A buffer represents a document such as a JSP.
 * Note that lines start at zero, so the first line
 * has a line number of zero.
 * 
 * A document is described by its URI as known in 
 * the servlet engine.
 * The URI includes the Web application prefix.
 * 		For instance: /examples/jsp/cal/cal1.jsp
 * 		with /examples being the application prefix.
 * 
 * Note: the name is the shortname, that is, the last 
 *       name of the URI. This allows easy manipulation
 *	     from the command line in the debugger.
 * 
 * @author: Olivier Gruber
 */

public class Buffer {

  String m_uri;  // Document URI in the servlet engine.
  String m_name; // Short name for this document.
  String m_filename;
  
  String m_lines[]; 	// actual text of the document once loaded.
  Vector m_breakpoints; // known breakpoints in this document.
  
  int m_currentLine; 	// current line when listing this buffer out on 
  						// the console.

//  Vector  m_fnOrScripts;

  	public Buffer(String uri,String filename,File file) {
		int index;
		m_filename = filename;
		m_lines = loadAndParse2(file);
		index = m_filename.lastIndexOf(File.separatorChar);
		m_name = m_filename.substring(index+1);
		m_uri= uri;

// 		m_fnOrScripts = new Vector();
		m_breakpoints = new Vector();
	}
	
	public void addBreakpoint(BreakPoint bp) {
		m_breakpoints.addElement(bp);
	}
	
	///////////////////////////////////////////////
 	StringBuffer buildFnOrScript(int start,int end) {

		StringBuffer buf = new StringBuffer();
  		String line;
  
  		if (start<0) start = 0;
  		if (end>m_lines.length) end = m_lines.length;
  		
		for (int l = start; l < end ; l++) {
			line = m_lines[l];
		  	buf.append(line+"\n");
		}
		return buf;
	}
	///////////////////////////////////////////////
	/*
	public FnOrScript exec(int start, int end) {
		FnOrScript fnOrScript;
  		try {
			fnOrScript = new FnOrScript(this,start,end);
	  		m_fnOrScripts.addElement(fnOrScript);
			return fnOrScript;
  		} catch (Throwable t) {
			t.printStackTrace();
			return null;
  		}
	}
	*/
	///////////////////////////////////////////////
	public static Buffer factory(String filename, String uri) {
		int index;
		Buffer buffer=null;
		char sep = File.separatorChar;

		filename = filename.replace('/',sep);
		filename = filename.trim();

  		File file = new File(filename);
  		if (file.exists()) {
			buffer = new Buffer(uri,filename,file);
  		} else {
	  		System.out.println("File "+filename+" does not exist.");
  		}
		return buffer;
	}
	///////////////////////////////////////////////
	public BreakPoint getBreakpoint(int id) {
		BreakPoint bp;
		Enumeration e;
		e = m_breakpoints.elements();
		while (e.hasMoreElements()) {
			bp = (BreakPoint)e.nextElement();
			if (bp.m_id==id) return bp;
		}
		return null;
	}
	///////////////////////////////////////////////
	public BreakPoint removeBreakpoint(int id) {
		BreakPoint bp = getBreakpoint(id);
		m_breakpoints.removeElement(bp);
		return bp;
	}
	///////////////////////////////////////////////
	public Enumeration getBreakpoints() { 
		return m_breakpoints.elements(); 
	}
	///////////////////////////////////////////////
  	public int getCurrentLine() {
	  return m_currentLine;
  	}
  	public void setCurrentLine(int lineno) {
		m_currentLine = lineno;
  	}
	///////////////////////////////////////////////
  	public String getFileName() { return m_filename; }
	///////////////////////////////////////////////
	public int getLineCount() {
		return m_lines.length;
	}
	///////////////////////////////////////////////
	public String getLine(int lineno) {
		try {
			return m_lines[lineno];
		} catch (Throwable t) {
			return null;
		}
	}
	///////////////////////////////////////////////
	public String getName() { return m_name; }
	///////////////////////////////////////////////
	public String getURI() { return m_uri; }


	///////////////////////////////////////////////
	private String[] loadAndParse2(File file) {
  		int b, size, bytes_read = 0;
  		int count;
  		FileInputStream in;
  		byte bytes[];
  		String buf, line;
  		Vector lines;
  		StringTokenizer lineTokenizer;

		lines = new Vector();
  	
  		try {
			// read in the bytes...
			size = (int) file.length();
			in = new FileInputStream(file);
			bytes = new byte[size];
			count = 0;
			while (bytes_read < size) {
				b = in.read();
				bytes[count++] = (byte)b;
	  			bytes_read++;
	  			if (b==(int)'\n') {
	  				line = new String(bytes,0,count-1);
					lines.addElement(line);
	  				count=0;	
	  			}
			}
		} catch (Throwable t) {
	  		t.printStackTrace();
			return null;
  		}
		String tmp[] = new String[lines.size()];
		System.arraycopy(lines.toArray(),0,tmp,0,tmp.length);
		return tmp; 
	}

}
