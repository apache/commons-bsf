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

package org.apache.bsf.debug.clientImpl;


public class CircularBuffer {

	Object m_elements[];
	int    m_pos,m_end;

	public CircularBuffer() {
		m_elements = new Object[256];
	}
	
	/**
	 * Grow but be carefull that we might be already wrapped
	 * around where the end is before the current position
	 *  (m_end<m_pos)
	 * 
	 * 	|....E.... P....|
	 * 
	 * in which case we have to grow the array and then 
	 * copy the two parts:
	 * 		- 0 to E==m_end
	 * 		- P==m_pos to the end  
	 * the free space is created in the middle (tmp.length 
	 * free slots) and therefore m_pos needs to be updated.
	 */
	private void grow() {
		int p,q;
  		Object tmp[] = m_elements;
  		m_elements = new Object[2*m_elements.length];
	  	if (m_pos<=m_end) {
	  		for (p=m_pos;p<m_end;p++) 
	  			m_elements[p] = tmp[p];
	  	} else {
	  		for (p=0;p<m_end;p++) 
	  			m_elements[p] = tmp[p];

	  		for (p=m_pos;p<tmp.length;p++) 
	  			m_elements[p+tmp.length] = tmp[p];
	  		
	  		m_pos += tmp.length;
	  	}
	}

	public synchronized void push(Object elem) {
		
	  if (m_end==m_elements.length) {
	  	// we are full but not warped,
	  	// can we warp around?
	  	if (m_pos==0) {
 			// no... therefore just grow.
  			grow();
	  	} else {
	  		// yes, warp;
	  		m_end = 0;
	  	}
	  	m_elements[m_end++] = elem;
	  	return;
	  }	  			
	  if (m_pos<=m_end) {
	  	// we are not wrapped and we
	  	// are not full (above test)...
	    // just add the event.
	  	m_elements[m_end++] = elem;
	  	return;
	  }
	  // we are wrapped...
	  if (m_end==m_pos-1) {
  		// we are warpped and full, grow.
  		grow();
	  }
	  m_elements[m_end++] = elem;			
	}
	//////////////////////////////////
    public boolean isEmpty() {
	  return (m_pos==m_end);    	
    }
    //////////////////////////////////
	public Object pop() {
	  Object elem;
	  
	  if (m_pos==m_end) return null;
	  elem = m_elements[m_pos++];
	  if (m_pos==m_elements.length &&
	  	  m_pos!=m_end) 
	  	  m_pos=0;
	  return elem;
	}	  	  
	
}

