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

package org.apache.bsf.engines.activescript;

import java.util.Vector;
import java.lang.reflect.*;
import java.beans.*;

import org.apache.bsf.*;
import org.apache.bsf.util.*;

public final class JavaBean {


  private static Vector lsMembers = new Vector(512, 512);
  static
  {
	lsMembers.addElement(""); //don't use entry 0.  It is used to indicate an error.
  }
   
  private static final short DISPATCH_METHOD=         0x01;
  private static final short DISPATCH_PROPERTYGET =   0x02;
  private static final short DISPATCH_PROPERTYPUT=    0x04;
  private static final short DISPATCH_PROPERTYPUTREF= 0x08;
  private static final short DISPATCH_CASESENSITIVE=  0x40;
  private static final short DISPATCH_THROWEXCEPTION= 0x80;
  

  /**
   * Bind the member to a unique index.  Use positive indexes for methods,
   * negative indexes for properties.  Matches are case insensitive
   * Convenience fuction for those engines not knowing dispatch type, case insensitive, and not wanting exception
   */
  
   public final static int bindMember(Class jclass, String name) throws Exception {
	  return bindMember( jclass, name, (short)( DISPATCH_PROPERTYGET | DISPATCH_PROPERTYPUT | DISPATCH_METHOD )); 
   }
  /**
   * Bind the member to a unique index.  Use positive indexes for methods,
   * negative indexes for properties.  Matches are case insensitive.
   */

   public final static int bindMember(Class jclass, String name, short bindType) throws Exception {
	/* See if there is a unique match on method names, ignoring case.
	 * There are four cases to consider:
	 *   1) no methods match:  Match remains null.
	 *   2) only one method matches: Match is the unique method
	 *   3) multiple matches, with all names matching exactly:
	 *      Match is of type String with a value of the name
	 *   4) multiple matches, differing in case:
	 *      return from method with a result of zero, indicating no match
	 */
	Object match = null;

	boolean throwException =  0 != (bindType & DISPATCH_THROWEXCEPTION);
	boolean respectCase =  0 != (bindType & DISPATCH_CASESENSITIVE);
	boolean dispatchMethod= (0!=(bindType & DISPATCH_METHOD));
	boolean dispatchPropertyGet= (0!=(bindType & DISPATCH_PROPERTYGET)); 
	boolean dispatchPropertyPut= (0!=(bindType & DISPATCH_PROPERTYPUT)) ||(0!=(bindType & DISPATCH_PROPERTYPUTREF)); 
   
	if(!( dispatchMethod || dispatchPropertyGet || dispatchPropertyPut )) dispatchMethod= dispatchPropertyGet= dispatchPropertyPut= true;


	Method jmethods[] = jclass.getMethods(); 

	for (int i=0; i<jmethods.length; i++) {

	  if (respectCase ?jmethods[i].getName().equals(name):jmethods[i].getName().equalsIgnoreCase(name) ) {

		if (match == null) {
		  // first match
		  match = jmethods[i];

		} else if (match instanceof Method) {

		  if (jmethods[i].getName().equals(((Method)match).getName())) {
			// second match, but name matches exactly
			match = jmethods[i].getName(); 
			if(respectCase) i= jmethods.length; //in this case we're done just remember the name.
		  } else {
			if(!respectCase)
			{
			  // difference in case of names, bail
				     if(throwException)
			   throw new BSFException (BSFException.REASON_OTHER_ERROR,
						    "Method:" + name + " in " + jclass + "differs between two methods in case only, can't distiguish");
			 return 0;
			}
		  } 

		} else if (!jmethods[i].getName().equals((String)match)) {
		 if(!respectCase)
		 {
		   // difference in case of names, bail
				     if(throwException)
			   throw new BSFException (BSFException.REASON_OTHER_ERROR,
						    "Method:" + name + " in " + jclass + "differs between two methods in case only, can't distiguish");
		   return 0;
		  }
		}

	  }

	}

	// if there was a match, return the method information
	if (match != null) {
	  lsMembers.addElement(match);
	  return lsMembers.size()-1;
	}

	if(!(dispatchPropertyPut || dispatchPropertyGet))
	{
	  if(throwException)
		throw new BSFException (BSFException.REASON_OTHER_ERROR,
	  		    "Method:" + name + " in " + jclass + "not found.");

	  return 0; //done/
	}
	  

	
	BeanInfo beanInfo=null ; 
	if(dispatchPropertyPut || dispatchPropertyGet)
	{
	  // look for bean property information, again looking for a unique match
	  // this case is simpler: there is no overloading, so if two properties
	  // match, they must differ in case.
	  Field jfields[] = jclass.getFields();
	  for (int i=0; i<jfields.length; i++) {
		if (respectCase ? jfields[i].getName().equals( name) : jfields[i].getName().equalsIgnoreCase( name) ) {
		  if (match != null)
		  	{
			if(throwException)
			 throw new BSFException (BSFException.REASON_OTHER_ERROR,
			  			    "Field:" + name + " in " + jclass + "differs between two fields in case only, can't distinguish");
			  	 return 0;
			  	}
		  match = jfields[i];
	  if(respectCase) i= jfields.length; //done there should be no more.  I hope!
		}
	  }

	  // if there was a unique match, return the property information
	  if (match != null) {
		lsMembers.addElement(match);
		return -(lsMembers.size()-1);
	  }

	  // look for bean property information, again looking for a unique match
	  beanInfo = Introspector.getBeanInfo(jclass);
	  PropertyDescriptor properties[] = beanInfo.getPropertyDescriptors();
	  for (int i=0; i<properties.length; i++) {
		if (respectCase? properties[i].getName().equals(name) : properties[i].getName().equalsIgnoreCase(name)) {
		  if (match != null)
			  	{
			if(throwException)
			 throw new BSFException (BSFException.REASON_OTHER_ERROR,
			  			    "Field:" + name + " in " + jclass + "differs between two fields in case only, can't distinguish");
			  	  return 0;
			  	}  
		  match = properties[i];
	  if(respectCase) i= properties.length; //done there should be no more.  I hope!
		}
	  }

	  // if there was a unique match, return the property information
	  if (match != null) {
		lsMembers.addElement(match);
		return -(lsMembers.size()-1);
	  }
	}//Endif(dispatchPropertyPut || dispatchPropertyGet)

	if(dispatchPropertyPut)
	{
	  // look for bean event information, again looking for a unique match
	  if (name.length() > 2 && (respectCase ?  name.substring(0,2).equals("on"):name.substring(0,2).equalsIgnoreCase("on"))) {
		String eventName = name.substring(2);
		EventSetDescriptor events[] = beanInfo.getEventSetDescriptors();
		for (int i=0; i<events.length; i++) {
		  if (respectCase ? events[i].getName().equals(eventName): events[i].getName().equalsIgnoreCase(eventName)) {
			if (match != null)
			{  
			  if(throwException)
					   throw new BSFException (BSFException.REASON_OTHER_ERROR,
		  	     			    "Event:" + name + " in " + jclass + "differs between two fields in case only, can't distinguish");
		  				    
			   return 0;
			}
			match = events[i];
	    if(respectCase) i= events.length;
		  }
		}
	 
		// if there was a unique match, return the property information
		if (match != null) {
		lsMembers.addElement(match);
		return -(lsMembers.size()-1);
		}
	  }
	}


   if(throwException)
	  throw new BSFException (BSFException.REASON_OTHER_ERROR,
   			    "No method, property, or event matches " + name + " in " + jclass + ".");

	return 0;
  }
  /**
   * Call a method, property getter, or property setter.  
   * If index > 0 then call simple method.
   * Else if argc = 0 then call getter
   * Else call setter
   */
  public final static Object callMethod(
	JavaBeanAddEventListener engine,
	Object bean, 
	int methodID, 
	Object[] args) throws Exception
  {
	Object result = null;

	// call the method
	if (methodID > 0) {
	  Object member = lsMembers.elementAt(methodID);
	  if (member instanceof Method) {

		try
		{
		 result=((Method)member).invoke(bean, args);
	}
	catch(java.lang.reflect.InvocationTargetException e)
	{
	  java.lang.Throwable original=e.getTargetException();
	   
	   java.io.StringWriter sw= new java.io.StringWriter();
	   java.io.PrintWriter pw= new java.io.PrintWriter(sw);
	   original.printStackTrace(pw);
		   throw new BSFException (BSFException.REASON_OTHER_ERROR,
			  			    "Target method exception(" + original + ") message is: " +original.getMessage() + "stack trace" + sw.toString(), original );
	}
	  } else {
		result=EngineUtils.callBeanMethod(bean, (String)member, args);
	  }
	} else {
	  Object member = lsMembers.elementAt(-methodID);
	  if (member instanceof PropertyDescriptor) {
		PropertyDescriptor property = (PropertyDescriptor)member;
		if (args.length>0) 
	{
	  Method method= property.getWriteMethod();
	  
		  if(null== method) throw new BSFException (BSFException.REASON_OTHER_ERROR,
   			    "Property " + property.getName() + " in " + bean.getClass() + " is read only.");
		  result = method.invoke(bean, args);
	}		    
		else
	{
	  Method method= property.getReadMethod();

		  if(null== method) throw new BSFException (BSFException.REASON_OTHER_ERROR,
   			    "Property " + property.getName() + " in " + bean.getClass() + " is write only.");

		  result = method.invoke(bean, args);
	}  
	  } else if (member instanceof Field) {
		Field field = (Field)member;
		if (args.length>0) 
		  field.set(bean, args[0]);
		else
		  result = field.get(bean);
	  } else {
		if (args.length>0) {
		  engine.addEventListener(bean,
			((EventSetDescriptor)member).getName(),  null,
			args[0].toString());
		}
	  }
	}

	return result;
  }
}
