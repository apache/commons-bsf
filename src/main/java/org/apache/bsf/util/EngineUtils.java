/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.bsf.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.apache.bsf.BSFEngine;
import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;

/**
 * This class contains utilities that language integrators can use
 * when implementing the BSFEngine interface.
 */

 /*
    2012-01-15: Rony G. Flatscher
        - take into account that the context thread class loader is not set, hence
          raise an exception in loadClass(mgr,name) instead of returning null
        - corrected some indentations

    2007-09-21: Rony G. Flatscher, new class loading sequence:

        - Thread's context class loader
        - settable class loader stored with BSF manager
        - BSFManager's defining class loader
        - BSF-custom class loader (loads from temp dir)
 */
public class EngineUtils {
    // the BSF class loader that knows how to load from the a specific
    // temp directory
    static BSFClassLoader bsfCL;

    // rgf, 20070917: class loaders that we might need to load classes
    static ClassLoader bsfManagerDefinedCL=BSFManager.getDefinedClassLoader();

    // ---rgf, 2003-02-13, determine whether changing accessibility of Methods is possible
    static boolean bMethodHasSetAccessible=false;
    static {
        final Class mc=Method.class;            // get the "Method" class object
        final Class arg[]={boolean.class};      // define an array with the primitive "boolean" pseudo class object
        try {
            mc.getMethod("setAccessible", arg ); // is this method available?
            bMethodHasSetAccessible=true; // no exception, hence method exists
        }
        catch (final Exception e)
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
    public static void addEventListener (final Object bean, final String eventSetName,
                                         final String filter, final BSFEngine engine,
                                         final BSFManager manager, final String source,
                                         final int lineNo, final int columnNo,
                                         final Object script) throws BSFException {
        final BSFEventProcessor ep = new BSFEventProcessor (engine, manager, filter,
                                                      source, lineNo, columnNo,
                                                      script);

        try {
            ReflectionUtils.addEventListener (bean, eventSetName, ep);
        } catch (final Exception e) {
            e.printStackTrace ();
            throw new BSFException (BSFException.REASON_OTHER_ERROR,
                                    "[EngineUtils.addEventListener()] ouch while adding event listener: "
                                    + e, e);
        }
    }


    /**
     * Add a script as a listener to some event coming out of an object. The
     * first two args identify the src of the event and the event set
     * and the rest identify the script which should be run when the event
     * fires. The processing will use the engine's apply() method.
     *
     * @param bean         event source
     * @param eventSetName name of event set from event src to bind to
     * @param filter       filter for events
     * @param engine       BSFEngine which can run this script
     * @param manager      BSFManager of the above engine
     * @param source       (context info) the source of this expression (e.g., filename)
     * @param lineNo       (context info) the line number in source for expr
     * @param columnNo     (context info) the column number in source for expr
     * @param script       the script to execute when the event occurs
     * @param dataFromScriptingEngine
     *                     this contains any object supplied by the scripting engine and gets sent
     *                     back with the supplied script, if the event occurs.
     *                     This could be used e.g. for indicating to the scripting engine which
     *                     scripting engine object/routine/function/procedure
     *                     should be ultimately informed of the event occurrence.
     *
     * @exception BSFException if anything goes wrong while running the script
     */
    public static void addEventListenerReturningEventInfos ( final Object bean,
                               final String eventSetName,
                               final String filter,
                               final BSFEngine engine,
                               final BSFManager manager,
                               final String source,
                               final int lineNo,
                               final int columnNo,
                               final Object script,
                               final Object dataFromScriptingEngine
                               ) throws BSFException
    {
        final BSFEventProcessorReturningEventInfos ep =
        new BSFEventProcessorReturningEventInfos (engine,
                                                  manager,
                                                  filter,
                                                  source,
                                                  lineNo,
                                                  columnNo,
                                                  script,
                                                  dataFromScriptingEngine
                                                  );

        try {
            ReflectionUtils.addEventListener (bean, eventSetName, ep);
        } catch (final Exception e) {
            e.printStackTrace ();
            throw new BSFException (BSFException.REASON_OTHER_ERROR,
                                    "[EngineUtils.addEventListenerReturningEventInfos()] ouch while adding event listener: "
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
    public static Object callBeanMethod (final Object bean, final String methodName,
                                         final Object[] args) throws BSFException {
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
        final boolean isStaticOnly = false;
        final Class beanClass = (bean instanceof Class) ? (Class)bean :
                                                    bean.getClass ();

        // now try to call method with the right signature
  	try {
  	  Method m;
  	  try {
  	m = MethodUtils.getMethod (beanClass, methodName, argTypes,
  				       isStaticOnly);
  	  } catch (final NoSuchMethodException e) {
  	// ok, so that didn't work - now try converting any primitive
  	// wrapper types to their primitive counterparts
  	try {
  	  // if args is null the NullPointerException will get caught
  	  // below and the right thing'll happen .. ugly but works
  	  for (int i = 0; i < args.length; i++) {
             if (args[i] instanceof Number)
             {
                 if      (args[i] instanceof Byte) {
                    argTypes[i] = byte.class;
                } else if (args[i] instanceof Integer) {
                    argTypes[i] = int.class;
                } else if (args[i] instanceof Long) {
                    argTypes[i] = long.class;
                } else if (args[i] instanceof Float) {
                    argTypes[i] = float.class;
                } else if (args[i] instanceof Double ) {
                    argTypes[i] = double.class;
                } else if (args[i] instanceof Short  ) {
                    argTypes[i] = short.class;
                }
             }
             else if (args[i] instanceof Boolean) {
                argTypes[i] = boolean.class;
            } else if (args[i] instanceof Character) {
                argTypes[i] = char.class;
            }
  	  }

  	  m = MethodUtils.getMethod (beanClass, methodName, argTypes,
  					 isStaticOnly);
  	} catch (final Exception e2) {
  	  // throw the original
  	  throw e;
  	}
  	  }

  	  // call it, and return the result
        try {
            return m.invoke (bean, args);
        }
        catch (final Exception e)                   // 2003-02-23, --rgf, maybe an IllegalAccessException?
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

        } catch (final Exception e) {
            // something went wrong while invoking method
            final Throwable t = (e instanceof InvocationTargetException) ?
                          ((InvocationTargetException)e).getTargetException () :
                          null;
            throw new BSFException (BSFException.REASON_OTHER_ERROR,
                                    "[EngineUtils.callBeanMethod()] method invocation failed: " + e +
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
    public static Object createBean (final String className, final Object args[])
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
            } catch (final NoSuchMethodException me) {
                // ok, so that didn't work - now try converting any primitive
                // wrapper types to their primitive counterparts
                try {
                    // if args is null the NullPointerException will get caught
                    // below and the right thing'll happen .. ugly but works
                    for (int i = 0; i < args.length; i++) {
                        if (args[i] instanceof Number) {
                            argTypes[i] = byte.class;
                        } else if (args[i] instanceof Boolean) {
                            argTypes[i] = boolean.class;
                        } else if (args[i] instanceof Character) {
                            argTypes[i] = char.class;
                        }
                    }
                    obj = ReflectionUtils.createBean (null, className,
                                                      argTypes, args);
                    return obj.value;
                } catch (final Exception e) {
                    // throw the previous exception
                    throw me;
                }
            }
        } catch (final Exception e) {
            throw new BSFException (BSFException.REASON_OTHER_ERROR,
                                    "[EngineUtils.createBean()]" + e.getMessage (), e);
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
    public static String getTypeSignatureString (final Class cl) {
        if (cl.isPrimitive ()) {
            if (cl == boolean.class) {
                return "Z";
            } else if (cl == byte.class) {
                return "B";
            } else if (cl == char.class) {
                return "C";
            } else if (cl == short.class) {
                return "S";
            } else if (cl == int.class) {
                return "I";
            } else if (cl == long.class) {
                return "J";
            } else if (cl == float.class) {
                return "F";
            } else if (cl == double.class) {
                return "D";
            } else {
                return "V";
            }
        } else {
            final StringBuffer sb = new StringBuffer ("L");
            sb.append (cl.getName ());
            sb.append (";");
            return sb.toString().replace ('.', '/');
        }
    }

    /**
     * Loads a class using the following sequence of class loaders:
     * <ul>
     * <li>  Thread's context class loader,
     * <li>  settable class loader stored with BSFManager,
     * <li>  BSFManager's defining class loader,
     * <li>  BSF customized class loader (from the BSFManager's temporary directory).
     *
     * @param mgr  BSFManager who's classLoader and tempDir props are
     *        consulted
     * @param name name of the class to load
     *
     * @return the loaded class
     *
     * @exception BSFException if something goes wrong.
     */
    public static Class loadClass (final BSFManager mgr, final String name)
        throws BSFException {

        ClassLoader mgrCL = null;

        try {
            // TCCL may not be set, adapt logic!
            final ClassLoader cl=Thread.currentThread().getContextClassLoader();
            if (cl!=null)
            {
                try {   // try the Thread's context loader first
                        return Thread.currentThread().getContextClassLoader().loadClass(name);
                }
                catch (final ClassNotFoundException e01) {
                }
            }

            try {   // try the class loader of the supplied BSFManager ("mgr")
                mgrCL = mgr.getClassLoader ();
                if (mgrCL != null) {
                    return mgrCL.loadClass(name);
                }
            }
            catch (final ClassNotFoundException e02) {
                    // o.k., now try the defined class loader
            }

                // try the class loader stored with the BSF manager
            if (mgrCL != bsfManagerDefinedCL) {
                return bsfManagerDefinedCL.loadClass(name);
            }

        } catch (final ClassNotFoundException e) {
            // try to load it from the temp dir using my own class loader
            try {
                if (bsfCL == null) {
                    bsfCL = new BSFClassLoader ();
                }
                bsfCL.setTempDir (mgr.getTempDir ());
                return bsfCL.loadClass (name);
            } catch (final ClassNotFoundException e2) {
                throw new BSFException (BSFException.REASON_OTHER_ERROR,
                        "[EngineUtils.loadClass()] unable to load class '" + name + "':" + e, e);
            }
        }

        throw new BSFException (BSFException.REASON_OTHER_ERROR,
                "[EngineUtils.loadClass()] unable to load class '" + name + "'");
    }
}
