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

package org.apache.bsf.util.event;

import java.util.Hashtable;
import org.apache.bsf.util.event.generator.EventAdapterGenerator;

/**
 * The <em>EventAdapterRegistry</em> is the registry of event adapters.
 * If a desired adapter is not found, the adapter will be dynamically
 * generated when lookup is attempted. Set the <code>dynamic</code> property
 * to <code>false</code> to disable this feature.
 * <p>
 * This implementation first looks for an adapter in its lookup table
 * and if it doesn't find one looks for a standard implementation of
 * that adapter in the org.apache.bsf.util.event.adapters package with a
 * standard naming convention. The naming convention it assumes is the
 * following: for event listener type <tt>a.b.c.FooListener</tt>, 
 * it loads an adapter of type 
 * <tt>org.apache.bsf.util.event.adapters.a_b_c_FooAdapter</tt>.
 * If both the loading and the dynamic generation fail, then a
 * <code>null</code> is returned.
 * <p>
 * 
 * @author   Sanjiva Weerawarana
 * @author   Matthew J. Duftler
 * @see      EventAdapter
 */
public class EventAdapterRegistry {
  private static Hashtable reg = new Hashtable ();
  private static ClassLoader cl = null;
  private static String adapterPackage = "org.apache.bsf.util.event.adapters";
  private static String adapterSuffix = "Adapter";
  private static boolean dynamic = true;

  public static Class lookup (Class listenerType) {
	String key = listenerType.getName().replace ('.', '_');
	Class adapterClass = (Class) reg.get (key);
	
	if (adapterClass == null) {
	  String en = key.substring (0, key.lastIndexOf ("Listener"));
	  String cn = adapterPackage + "." + en + adapterSuffix;

	  try {
		// Try to resolve one.
		adapterClass = (cl != null) ? cl.loadClass (cn) : Class.forName (cn);
	  } catch (ClassNotFoundException e) {
		if (dynamic) {
		  // Unable to resolve one, try to generate one.
		  adapterClass =
			EventAdapterGenerator.makeEventAdapterClass (listenerType, false);
		}
	  }

	  if (adapterClass != null) {
		reg.put (key, adapterClass);
	  }
	}

	return adapterClass;
  }
  public static void register (Class listenerType, Class eventAdapterClass) {
	String key = listenerType.getName().replace('.', '_');
	reg.put (key, eventAdapterClass);
  }
  /**
   * Class loader to use to load event adapter classes.
   */
  public static void setClassLoader (ClassLoader cloader) {
	cl = cloader;
  }
  /**
   * Indicates whether or not to dynamically generate adapters; default is
   * <code>true</code>.
   * <p>
   * If the <code>dynamic</code> property is set to true, and the
   * <code>ClassLoader</code> is unable to resolve an adapter, one will be
   * dynamically generated.
   * 
   * @param dynamic whether or not to dynamically generate adapters.
   */
  public static void setDynamic (boolean dynamic) {
	EventAdapterRegistry.dynamic = dynamic;
  }
}
