/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2004 The Apache Software Foundation.  All rights
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

package org.apache.bsf.util.cf;

import java.io.*;
import org.apache.bsf.util.DebugLog;

/**
 * This is an example of how a <code>CodeFormatter</code> bean can be used.
 * <p>
 * The CFDriver is a stand-alone tool that will instantiate a
 * <code>CodeFormatter</code> bean, configure it according to your
 * command-line arguments, and invoke the formatting. Since the
 * default source of input is <code>stdin</code>, and the default
 * target for output is <code>stdout</code>, a <code>CFDriver</code>
 * can also be used as a filter.
 *
 * @see CodeFormatter
 *
 * @version 1.0
 * @author Matthew J. Duftler
 */
public class CFDriver
{
	/**
	 * Not used.
	 */
	public CFDriver()
  {
  }
  /**
	* A driver for <code>CodeFormatter</code>.
	*<p>
	* Usage:
	*<code><pre>
	*  java org.apache.cf.CFDriver [args]
	*<p>
	*    args:
	*<p>
	*      [-in      fileName]   default: &lt;STDIN&gt;
	*      [-out     fileName]   default: &lt;STDOUT&gt;
	*      [-maxLine   length]   default: 74
	*      [-step        size]   default: 2
	*      [-delim      group]   default: (+
	*      [-sdelim     group]   default: ,
	*</pre></code>
	*/
	public static void main(String[] argv)
  {
	if (argv.length % 2 == 0)
	{
	  String        inFile  = null,
					outFile = null,
					maxLine = null,
					indStep = null,
					delim   = null,
					sDelim  = null;
	  Reader        in      = null;
		Writer        out     = null;
	  CodeFormatter cf      = new CodeFormatter();

	  for (int i = 0; i < argv.length; i += 2)
	  {
		if (argv[i].startsWith("-i"))
		  inFile = argv[i + 1];
		else if (argv[i].startsWith("-o"))
		  outFile = argv[i + 1];
	  	else if (argv[i].startsWith("-m"))
	  		maxLine = argv[i + 1];
		else if (argv[i].startsWith("-st"))
		  indStep = argv[i + 1];
	  	else if (argv[i].startsWith("-d"))
	  		delim = argv[i + 1];
		else if (argv[i].startsWith("-sd"))
		  sDelim = argv[i + 1];
	  }

	  if (inFile != null)
	  {
		try
		{
		  in = new FileReader(inFile);
		}
		catch (FileNotFoundException e)
		{
		  printError("Cannot open input file: " + inFile);
			
		  return;
		}
	  }
	  else
	  {
		in = new InputStreamReader(System.in);
	  }

	  if (outFile != null)
	  {
		try
		{
		  out = new FileWriter(outFile);
		}
		catch (IOException e)
		{
		  printError("Cannot open output file: " + outFile);
		  
		  return;
		}
	  }
	  else
	  {
		out = new OutputStreamWriter(System.out);
	  }

		if (maxLine != null)
	  {
	  	try
		{
			cf.setMaxLineLength(Integer.parseInt(maxLine));
		}
	  	catch (NumberFormatException nfe)
		{
			printError("Not a valid integer: " + maxLine);
			
			return;
		}
	  }

	  if (indStep != null)
	  {
		try
		{
		  cf.setIndentationStep(Integer.parseInt(indStep));
		}
		catch (NumberFormatException nfe)
		{
		  printError("Not a valid integer: " + indStep);
		  
		  return;
		}
	  }
		
		if (delim != null)
		  cf.setDelimiters(delim);
		
		if (sDelim != null)
		cf.setStickyDelimiters(sDelim);
			
		cf.formatCode(in, out);
	}
	else
	  printHelp();
  }
	private static void printError(String errMsg)
  {
  	DebugLog.stderrPrintln("ERROR: " + errMsg, DebugLog.BSF_LOG_L2);
  }
	private static void printHelp()
  {
	System.out.println("Usage:");
	System.out.println();
	System.out.println("  java " + CFDriver.class.getName() + " [args]");
	System.out.println();
	System.out.println("    args:");
	System.out.println();
	System.out.println("      [-in      fileName]   default: <STDIN>");
	System.out.println("      [-out     fileName]   default: <STDOUT>");
	System.out.println("      [-maxLine   length]   default: " +
					   CodeFormatter.DEFAULT_MAX);
	System.out.println("      [-step        size]   default: " +
					   CodeFormatter.DEFAULT_STEP);
	System.out.println("      [-delim      group]   default: " +
					   CodeFormatter.DEFAULT_DELIM);
	System.out.println("      [-sdelim     group]   default: " +
					   CodeFormatter.DEFAULT_S_DELIM);
  }
}
