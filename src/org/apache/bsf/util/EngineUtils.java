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

package org.apache.bsf.util;

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.beans.*;

import org.apache.bsf.*;
import org.apache.bsf.util.*;

/**
 * This class contains utilities that language integrators can use
 * when implementing the BSFEngine interface.
 *
 * @author   Sanjiva Weerawarana
 * @author   Sam Ruby
 */
public class EngineUtils {
    // the BSF class loader that knows how to load from the a specific
    // temp directory
    static BSFClassLoader bsfCL;

    // ---rgf, 2003-02-13, determine whether changing accessibility of Methods is possible
    static boolean bMethodHasSetAccessible=false;
    static {
        Class mc=Method.class;            // get the "Method" class object
        Class arg[]={boolean.class};      // define an array with the primitive "boolean" pseudo class object
        try {
            Object o=mc.getMethod("setAccessible", arg ); // is this method available?
            bMethodHasSetAccessible=true; // no exception, hence method exists
        }
        catch (Exception e)
        {
            bMethodHasSetAccessible=false;// exception occurred, hence method does not exist
        }
    }


    /**
     * Add a script as a listener to some event coming out of an object. The
     * first two args identify the src of the event and the event set
     * and the rest identify the script which should be run when the event
     * fires.
     *
     * @param bean         event source
     * @param eventSetName name of event set from event src to bind to
     * @param filter       filter for events
     * @param engine       BSFEngine which can run this script
     * @param manager      BSFManager of the above engine
     * @param source       (context info) the source of this expression
     *                                    (e.g., filename)
     * @param lineNo       (context info) the line number in source for expr
     * @param columnNo     (context info) the column number in source for expr
     * @param script       the script to execute when the event occurs
     *
     * @exception BSFException if anything goes wrong while running the script
     */
    public static void addEventListener (Object bean, String eventSetName,
                                         String filter, BSFEngine engine,
                                         BSFManager manager, String source,
                                         int lineNo, int columnNo,
                                         Object script) throws BSFException {
        BSFEventProcessor ep = new BSFEventProcessor (engine, manager, filter,
                                                      source, lineNo, columnNo,
                                                      script);

        try {
            ReflectionUtils.addEventListener (bean, eventSetName, ep);
        } catch (Exception e) {
            e.printStackTrace ();
            throw new BSFException (BSFException.REASON_OTHER_ERROR,
                                    "ouch while adding event listener: "
                                    + e, e);
        }
    }

    /**
     * Finds and invokes a method with the given signature on the given
     * bean. The signature of the method that's invoked is first taken
     * as the types of the args, but if that fails, this tries to convert
     * any primitive wrapper type args to their primitive counterparts
     * to see whether a method exists that way. If it does, done.
     *
     * @param bean       the object on which to invoke the method
     * @param methodName name of the method
     * @param args       arguments to be given to the method
     *
     * @return the result of invoking the method, if any
     *
     * @exception BSFException if something goes wrong
     */
    public static Object callBeanMethod (Object bean, String methodName,
                                         Object[] args) throws BSFException {
        Class[] argTypes = null;
        // determine arg types. note that a null argtype
        // matches any object type

        if (args != null) {
            argTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                argTypes[i] = (args[i] == null) ? null : args[i].getClass ();
            }
        }

        // we want to allow a static call to occur on an object, similar
        // to what Java allows. So isStaticOnly is set to false.
        boolean isStaticOnly = false;
        Class beanClass = (bean instanceof Class) ? (Class)bean :
                                                    bean.getClass ();

        // now try to call method with the right signature
  	try {
  	  Method m;
  	  try {
  	m = MethodUtils.getMethod (beanClass, methodName, argTypes,
  				       isStaticOnly);
  	  } catch (NoSuchMethodException e) {
  	// ok, so that didn't work - now try converting any primitive
  	// wrapper types to their primitive counterparts
  	try {
  	  // if args is null the NullPointerException will get caught
  	  // below and the right thing'll happen .. ugly but works
  	  for (int i = 0; i < args.length; i++) {
             if (args[i] instanceof Number)
             {
                 if      (args[i] instanceof Byte)    argTypes[i] = byte.class;
                 else if (args[i] instanceof Integer) argTypes[i] = int.class;
                 else if (args[i] instanceof Long)    argTypes[i] = long.class;
                 else if (args[i] instanceof Float)   argTypes[i] = float.class;
                 else if (args[i] instanceof Double ) argTypes[i] = double.class;
                 else if (args[i] instanceof Short  ) argTypes[i] = short.class;
             }
             else if (args[i] instanceof Boolean)   argTypes[i] = boolean.class;
             else if (args[i] instanceof Character) argTypes[i] = char.class;
  	  }

  	  m = MethodUtils.getMethod (beanClass, methodName, argTypes,
  					 isStaticOnly);
  	} catch (Exception e2) {
  	  // throw the original
  	  throw e;
  	}
  	  }

  	  // call it, and return the result
        try {
            return m.invoke (bean, args);
        }
        catch (Exception e)                   // 2003-02-23, --rgf, maybe an IllegalAccessException?
        {
            if (e instanceof IllegalAccessException &&
                bMethodHasSetAccessible &&
                Modifier.isPublic(m.getModifiers())   )   // if a public method allow access to it
            {
                m.setAccessible(true);        // allow unconditional access to method
  	        return m.invoke (bean, args);
            }
  	  // re-throw the exception
  	  throw e;
        }

        } catch (Exception e) {
            // something went wrong while invoking method
            Throwable t = (e instanceof InvocationTargetException) ?
                          ((InvocationTargetException)e).getTargetException () :
                          null;
            throw new BSFException (BSFException.REASON_OTHER_ERROR,
                                    "method invocation failed: " + e +
                                    ((t==null) ? "" :
                                     (" target exception: " + t)), t);
        }
    }

    /**
     * Creates a new bean. The signature of the constructor that's invoked
     * is first taken as the types of the args, but if that fails, this tries
     * to convert any primitive wrapper type args to their primitive
     * counterparts to see whether a method exists that way. If it does, done.
     *
     * @param className fully qualified name of class to instantiate
     * @param args      array of constructor args (or null if none)
     *
     * @return the created bean
     *
     * @exception BSFException if something goes wrong (@see
     *            org.apache.cs.util.MethodUtils for the real
     *            exceptions that can occur).
     */
    public static Object createBean (String className, Object args[])
        throws BSFException {
        Bean obj;
        Class[] argTypes = null;

        if (args != null) {
            argTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                argTypes[i] = (args[i] != null) ? args[i].getClass () : null;
            }
        }

        try {
            try {
                obj = ReflectionUtils.createBean (null, className,
                                                  argTypes, args);
                return obj.value;
            } catch (NoSuchMethodException me) {
                // ok, so that didn't work - now try converting any primitive
                // wrapper types to their primitive counterparts
                try {
                    // if args is null the NullPointerException will get caught
                    // below and the right thing'll happen .. ugly but works
                    for (int i = 0; i < args.length; i++) {
                        if (args[i] instanceof Number)
                            argTypes[i] = byte.class;
                        else if (args[i] instanceof Boolean)
                            argTypes[i] = boolean.class;
                        else if (args[i] instanceof Character)
                            argTypes[i] = char.class;
                    }
                    obj = ReflectionUtils.createBean (null, className,
                                                      argTypes, args);
                    return obj.value;
                } catch (Exception e) {
                    // throw the previous exception
                    throw me;
                }
            }
        } catch (Exception e) {
            throw new BSFException (BSFException.REASON_OTHER_ERROR,
                                    e.getMessage (), e);
        }
    }

    /**
     * Given a class return the type signature string fragment for it.
     * That is, return "I" for int, "J" for long, ... etc..
     *
     * @param cl class object for whom the signature fragment is needed.
     *
     * @return the string representing the type signature
     */
    public static String getTypeSignatureString (Class cl) {
        if (cl.isPrimitive ()) {
            if (cl == boolean.class)
                return "Z";
            else if (cl == byte.class)
                return "B";
            else if (cl == char.class)
                return "C";
            else if (cl == short.class)
                return "S";
            else if (cl == int.class)
                return "I";
            else if (cl == long.class)
                return "J";
            else if (cl == float.class)
                return "F";
            else if (cl == double.class)
                return "D";
            else
                return "V";
        } else {
            StringBuffer sb = new StringBuffer ("L");
            sb.append (cl.getName ());
            sb.append (";");
            return sb.toString().replace ('.', '/');
        }
    }

    /**
     * Load a class using the class loader of given manager. If that fails
     * try using a class loader that loads from the tempdir of the manager.
     *
     * @param mgr  BSFManager who's classLoader and tempDir props are
     *        consulted
     * @param name name of the class to load
     *
     * @return the loaded class
     *
     * @exception BSFException if something goes wrong.
     */
    public static Class loadClass (BSFManager mgr, String name)
        throws BSFException {
        ClassLoader classLoader = mgr.getClassLoader ();

        try {
            return (classLoader == null) ? Class.forName (name)
                : classLoader.loadClass (name);
        } catch (ClassNotFoundException e) {
            // try to load it from the temp dir using my own class loader
            try {
                if (bsfCL == null)
                    bsfCL = new BSFClassLoader ();
                bsfCL.setTempDir (mgr.getTempDir ());
                return bsfCL.loadClass (name);
            } catch (ClassNotFoundException e2) {
                throw new BSFException (BSFException.REASON_OTHER_ERROR,
                        "unable to load class '" + name + "':" + e, e);
            }
        }
    }
}
