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

package org.apache.bsf.engines.javascript;

import org.apache.bsf.*;

/**
 * Materializes a breakpoint.
 * A breakpoint can be defined at a line number or an
 * offset character in its document. For JavaScript,
 * we only support lineno for the underlying Rhino
 * engine does support only the line numbers.
 * Note: this is unfortunate for this prevents
 * setting a breakpoint on different statements on the 
 * same line...
 * 
 * A breakpoint is remembered at either the document level
 * (DocumentCell) or the function/script level (FnOrScript).
 * It is remembered at the document level when a FnOrScript
 * is not yet known for the breakpoint line number.
 * When a matching FnOrScrip will become known (compiled in Rhino),
 * the breakpoint will be remembered at the FnOrScript level.
 * 
 * Additionally, a breakpoint can be correlated or not
 * to a compilation unit. When not correlated (m_unit==null),
 * it means a compilation unit for the breakpoint
 * line has not been seen yet. It may be because the
 * breakpoint is on an invalid line but it may be
 * also that the breakpoint is in a script or function
 * that has not been compiled yet.
 * Indeed, there is a delay between knowing about a FnOrScript
 * and knowing about all its compilation unit. When a FnOrScript
 * is about to be compiled in Rhino, we create the FnOrScript,
 * with no known compilation unit. As a side effect of compiling,
 * the Rhino engine calls us back with compilation units such 
 * as independent functions or scripts. This is when those units,
 * represented by our class CompilationUnit are added to the 
 * corresponding FnOrScript.
 * 
 * @author: Olivier Gruber
 */
public class BreakPoint {

	protected int m_brkptId;
	protected int m_lineno;
	protected int m_offset;
	protected boolean m_lineDefined;

	protected DocumentCell m_cell;
	protected FnOrScript m_fnOrScript;

 	protected CompilationUnit m_unit;

	public BreakPoint(BreakPoint bp) {
		m_fnOrScript = bp.m_fnOrScript;
		m_cell = bp.m_cell;
		m_brkptId = bp.m_brkptId;
		m_lineno = bp.m_lineno;
		m_offset = bp.m_offset;
		m_lineDefined = bp.m_lineDefined;
		m_unit = bp.m_unit;	
	}
	public BreakPoint(DocumentCell cell, int brkptid) {
		super();
		m_fnOrScript = null;
		m_cell = cell;
		m_brkptId = brkptid;
		m_lineno = -1;
		m_lineDefined = true;
		m_unit=null;
	}
	public void setUnit(CompilationUnit unit) {
		m_unit = unit;
	}
	
	/**
	 * Propagating the breakpoint to its corresponding
	 * compilation unit, the side effect of that is to 
	 * set the breakpoint in the Rhino engine.
	 */
	public void propagate() {
		if (m_unit != null) {
			m_unit.propagate(m_lineno);
		}
	}
	/**
	 * Unpropagating the breakpoint to its corresponding
	 * compilation unit, the side effect of that is to 
	 * unset the breakpoint in the Rhino engine.
	 */
	public void unpropagate() {
		if (m_unit != null) {
			m_unit.unpropagate(m_lineno);
		}
	}
	
	/**
	 * attaches this breakpoint to the specified FnOrScript.
	 */
	public void attachToFnOrScript(FnOrScript fnOrScript) {
		m_fnOrScript = fnOrScript;
	}
	/**
	 * @return the identifier of the breakpoint.
	 */
	public int getId() {
		return m_brkptId;
	}
	/**
	 * @return the line number of that breakpoint.
	 * This method will succeed only if the breakpoint as been
	 * defined at a line number.
	 * There is no automated translation between line number and 
	 * offsets...
	 */	
	public int getLineNo() throws BSFException {
		if (!m_lineDefined)
			throw new BSFException(
				BSFException.REASON_INVALID_ARGUMENT,
				"Breakpoint is offset defined, can't provide its line number.");
		return m_lineno;
	}
	/**
	 * @return the character offset of that breakpoint.
	 * This method will succeed only if the breakpoint as been
	 * defined at an offset.
	 * There is no automated translation between line number and 
	 * offsets...
	 */	
	public int getOffset() throws BSFException {
		if (m_lineDefined)
			throw new BSFException(
				BSFException.REASON_INVALID_ARGUMENT,
				"Breakpoint is line defined, can't provide its offset.");
		return m_offset;
	}
	
	public void setLineNo(int lineno) {
		m_lineno = lineno;
	}
	public void setOffset(int offset) {
		m_offset = offset;
	}
}
