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

package org.apache.bsf.util;

import java.util.*;

/**
 * The <em>ObjectRegistry</em> is used to do name-to-object reference lookups.
 * If an <em>ObjectRegistry</em> is passed as a constructor argument, then this
 * <em>ObjectRegistry</em> will be a cascading registry: when a lookup is
 * invoked, it will first look in its own table for a name, and if it's not
 * there, it will cascade to the parent <em>ObjectRegistry</em>.
 * All registration is always local. [??]
 * 
 * @author   Sanjiva Weerawarana
 * @author   Matthew J. Duftler
 */
public class ObjectRegistry {
  Hashtable      reg    = new Hashtable ();
  ObjectRegistry parent = null;

  public ObjectRegistry () {
  }
  public ObjectRegistry (ObjectRegistry parent) {
	this.parent = parent;
  }
  // lookup an object: cascade up if needed
  public Object lookup (String name) throws IllegalArgumentException {
	Object obj = reg.get (name);

	if (obj == null && parent != null) {
	  obj = parent.lookup (name);
	}

	if (obj == null) {
	  throw new IllegalArgumentException ("object '" + name + "' not in registry");
	}

	return obj;
  }
  // register an object
  public void register (String name, Object obj) {
	reg.put (name, obj);
  }
  // unregister an object (silent if unknown name)
  public void unregister (String name) {
	reg.remove (name);
  }
}
