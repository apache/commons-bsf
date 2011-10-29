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

import org.apache.bsf.BSFManager;   // rgf, 20070917

import java.beans.BeanInfo;
import java.beans.Beans;
import java.beans.EventSetDescriptor;
import java.beans.FeatureDescriptor;
import java.beans.IndexedPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.util.TreeSet;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.bsf.util.event.EventAdapter;
import org.apache.bsf.util.event.EventAdapterRegistry;
import org.apache.bsf.util.event.EventProcessor;
import org.apache.bsf.util.type.TypeConvertor;
import org.apache.bsf.util.type.TypeConvertorRegistry;

/**
 * This file is a collection of reflection utilities. There are utilities
 * for creating beans, getting bean infos, setting/getting properties,
 * and binding events.
 *
 * @author   Sanjiva Weerawarana
 * @author   Joseph Kesselman
 */
 /*  2007-09-21: Rony G. Flatscher, new class loading sequence:

        - supplied class loader (given as an argument)
        - Thread's context class loader
        - BSFManager's defining class loader

     2011-10-29: Rony G. Flatscher, in case an event is not found, create a
          user-friendly error message that lists all available event names

     2011-10-29: Rony G. Flatscher, make sure that the context class loader
          is used only, if not null
 */
public class ReflectionUtils {
    // rgf, 20070921: class loaders that we might need to load classes
    static ClassLoader bsfManagerDefinedCL=BSFManager.getDefinedClassLoader();


  //////////////////////////////////////////////////////////////////////////

  /**
   * Add an event processor as a listener to some event coming out of an
   * object.
   *
   * @param source       event source
   * @param eventSetName name of event set from event src to bind to
   * @param processor    event processor the event should be delegated to
   *                     when it occurs; either via processEvent or
   *                     processExceptionableEvent.
   *
   * @exception IntrospectionException if unable to introspect
   * @exception IllegalArgumentException if event set is unknown
   * @exception IllegalAccessException if the event adapter class or
   *            initializer is not accessible.
   * @exception InstantiationException if event adapter instantiation fails
   * @exception InvocationTargetException if something goes wrong while
   *            running add event listener method
   */
  public static void addEventListener (Object source, String eventSetName,
                       EventProcessor processor)
       throws IntrospectionException, IllegalArgumentException,
              IllegalAccessException, InstantiationException,
              InvocationTargetException {
    // find the event set descriptor for this event
    BeanInfo bi = Introspector.getBeanInfo (source.getClass ());

    EventSetDescriptor arrESD[]=bi.getEventSetDescriptors ();
    EventSetDescriptor esd=(EventSetDescriptor) findFeatureByName ("event", eventSetName, arrESD);

    if (esd == null)        // no events found, maybe a proxy from OpenOffice.org?
        {
          String errMsg="event set '" + eventSetName +"' unknown for source type '" + source.getClass () + "': ";
          if (arrESD.length==0)     // no event sets found in class!
          {
              errMsg=errMsg+"class does not implement any event methods following Java's event pattern!";
          }
          else
          {
              // errMsg=errMsg+"class defines the following event set(s): {";
              errMsg=errMsg+"class defines the following event set(s): ";

              // sort ESD by Name
              TreeSet ts=new TreeSet(new Comparator () {
                          public int    compare(Object o1, Object o2) {return ((EventSetDescriptor)o1).getName().compareToIgnoreCase(((EventSetDescriptor)o2).getName());}
                          public boolean equals(Object o1, Object o2) {return ((EventSetDescriptor)o1).getName().equalsIgnoreCase   (((EventSetDescriptor)o2).getName());}
                         });

              for (int i=0;i<arrESD.length;i++)
              {
                  ts.add(arrESD[i]);
              }
              Iterator it=ts.iterator();    // get iterator

              int i=0;
              while (it.hasNext())          // iterate in sorted order
              {
                  EventSetDescriptor tmpESD=(EventSetDescriptor) it.next();

                  if (i>0)
                  {
                      errMsg=errMsg+", ";
                  }
                  errMsg=errMsg+"\n\t"+'\''+tmpESD.getName()+"'={";  // event set name


                    // iterate over listener methods and display their names in sorted order
                  Method m[]=tmpESD.getListenerMethods();
                  TreeSet tsM=new TreeSet(new Comparator () {
                          public int    compare(Object o1, Object o2) {return ((Method)o1).getName().compareToIgnoreCase(((Method)o2).getName());}
                          public boolean equals(Object o1, Object o2) {return ((Method)o1).getName().equalsIgnoreCase   (((Method)o2).getName());}
                         });

                  for (int j=0;j<m.length;j++)
                  {
                      tsM.add(m[j]);
                  }
                  Iterator itM=tsM.iterator();

                  int j=0;
                  while (itM.hasNext())
                  {
                      if (j>0)
                      {
                          errMsg=errMsg+',';
                      }
                      errMsg=errMsg+'\''+((Method) itM.next()).getName()+'\'';
                      j++;
                  }
                  errMsg=errMsg+'}';    // close event method set
                  i++;
              }

              errMsg=errMsg+".";       // close set of event sets
          }
          throw new IllegalArgumentException (errMsg);
    }

    // get the class object for the event
    Class listenerType=esd.getListenerType(); // get ListenerType class object from EventSetDescriptor

    // find an event adapter class of the right type
    Class adapterClass = EventAdapterRegistry.lookup (listenerType);
    if (adapterClass == null) {
      throw new IllegalArgumentException ("event adapter for listener type " +
                          "'" + listenerType + "' (eventset " +
                          "'" + eventSetName + "') unknown");
    }

    // create the event adapter and give it the event processor
    EventAdapter adapter = (EventAdapter) adapterClass.newInstance ();
    adapter.setEventProcessor (processor);

    // bind the adapter to the source bean
    Method addListenerMethod;
    Object[] args;
    if (eventSetName.equals ("propertyChange") ||
        eventSetName.equals ("vetoableChange")) {
      // In Java 1.2, beans may have direct listener adding methods
      // for property and vetoable change events which take the
      // property name as a filter to be applied at the event source.
      // The filter property of the event processor should be used
      // in this case to support the source-side filtering.
      //
      // ** TBD **: the following two lines need to change appropriately
          addListenerMethod = esd.getAddListenerMethod ();
      args = new Object[] {adapter};
    }
        else
        {
          addListenerMethod = esd.getAddListenerMethod ();
      args = new Object[] {adapter};
    }
    addListenerMethod.invoke (source, args);
  }
  //////////////////////////////////////////////////////////////////////////



  /**
   * Create a bean using given class loader and using the appropriate
   * constructor for the given args of the given arg types.

   * @param cld       the class loader to use. If null, Class.forName is used.
   * @param className name of class to instantiate
   * @param argTypes  array of argument types
   * @param args      array of arguments
   *
   * @return the newly created bean
   *
   * @exception ClassNotFoundException    if class is not loaded
   * @exception NoSuchMethodException     if constructor can't be found
   * @exception InstantiationException    if class can't be instantiated
   * @exception IllegalAccessException    if class is not accessible
   * @exception IllegalArgumentException  if argument problem
   * @exception InvocationTargetException if constructor excepted
   * @exception IOException               if I/O error in beans.instantiate
   */
  public static Bean createBean (ClassLoader cld, String className,
                 Class[] argTypes, Object[] args)
       throws ClassNotFoundException, NoSuchMethodException,
              InstantiationException, IllegalAccessException,
              IllegalArgumentException, InvocationTargetException,
              IOException {
    if (argTypes != null) {

            // if class loader given, use that one, else try
            // the Thread's context class loader (if set) and then
            // the BSFMananger defining class loader
          Class cl=null;
          ClassNotFoundException exCTX=null;

// -----------------------------
          if (cld != null) {    // class loader supplied as argument
              try {     // CL passed as argument
                  cl=cld.loadClass(className);
              }
              catch (ClassNotFoundException e02) {
                  exCTX=e02;
              }
          }

          if (cl==null) {
              // load context class loader, only use it, if not null
              ClassLoader tccl=Thread.currentThread().getContextClassLoader();
              if (tccl!=null) {
                  try {         // CTXCL
                          cl=tccl.loadClass(className);
                      }
                  catch (ClassNotFoundException e01) {}
              }
          }

          if (cl==null) {   // class not loaded yet
                    // defined CL
              if (cld != bsfManagerDefinedCL) {   // if not used already, attempt to load
                  cl=bsfManagerDefinedCL.loadClass(className);
              }
              else {    // classloader was already used, hence re-throw exception
                  throw exCTX;      // re-throw very first exception
              }
          }
// -----------------------------

      Constructor c = MethodUtils.getConstructor (cl, argTypes);
      return new Bean (cl, c.newInstance (args));
    } else {
      // create the bean with no args constructor
      Object obj = Beans.instantiate (cld, className);
      return new Bean (obj.getClass (), obj);
    }
  }
  //////////////////////////////////////////////////////////////////////////

  /**
   * Create a bean using given class loader and using the appropriate
   * constructor for the given args. Figures out the arg types and
   * calls above.

   * @param cld       the class loader to use. If null, Class.forName is used.
   * @param className name of class to instantiate
   * @param args      array of arguments
   *
   * @return the newly created bean
   *
   * @exception ClassNotFoundException    if class is not loaded
   * @exception NoSuchMethodException     if constructor can't be found
   * @exception InstantiationException    if class can't be instantiated
   * @exception IllegalAccessException    if class is not accessible
   * @exception IllegalArgumentException  if argument problem
   * @exception InvocationTargetException if constructor excepted
   * @exception IOException               if I/O error in beans.instantiate
   */
  public static Bean createBean (ClassLoader cld, String className, Object[] args)
       throws ClassNotFoundException, NoSuchMethodException,
              InstantiationException, IllegalAccessException,
              IllegalArgumentException, InvocationTargetException,
              IOException {
    Class[] argTypes = null;
    if (args != null) {
      argTypes = new Class[args.length];
      for (int i = 0; i < args.length; i++) {
        argTypes[i] = (args[i] != null) ? args[i].getClass () : null;
      }
    }
    return createBean (cld, className, argTypes, args);
  }
  //////////////////////////////////////////////////////////////////////////

  /**
   * locate the item in the fds array whose name is as given. returns
   * null if not found.
   */
  private static
  FeatureDescriptor findFeatureByName (String featureType, String name,
                       FeatureDescriptor[] fds) {
    for (int i = 0; i < fds.length; i++) {
      if (name.equals (fds[i].getName())) {
        return fds[i];
      }
    }
    return null;
  }


  public static Bean getField (Object target, String fieldName)
      throws IllegalArgumentException, IllegalAccessException {
    // This is to handle how we do static fields.
    Class targetClass = (target instanceof Class)
                        ? (Class) target
                        : target.getClass ();

    try {
      Field f = targetClass.getField (fieldName);
      Class fieldType = f.getType ();

      // Get the value and return it.
      Object value = f.get (target);
      return new Bean (fieldType, value);
    } catch (NoSuchFieldException e) {
      throw new IllegalArgumentException ("field '" + fieldName + "' is " +
                          "unknown for '" + target + "'");
    }
  }
  //////////////////////////////////////////////////////////////////////////

  /**
   * Get a property of a bean.
   *
   * @param target    the object whose prop is to be gotten
   * @param propName  name of the property to set
   * @param index     index to get (if property is indexed)
   *
   * @exception IntrospectionException if unable to introspect
   * @exception IllegalArgumentException if problems with args: if the
   *            property is unknown, or if the property is given an index
   *            when its not, or if the property is not writeable, or if
   *            the given value cannot be assigned to the it (type mismatch).
   * @exception IllegalAccessException if read method is not accessible
   * @exception InvocationTargetException if read method excepts
   */
  public static Bean getProperty (Object target, String propName,
                  Integer index)
       throws IntrospectionException, IllegalArgumentException,
              IllegalAccessException, InvocationTargetException {
    // find the property descriptor
    BeanInfo bi = Introspector.getBeanInfo (target.getClass ());
    PropertyDescriptor pd = (PropertyDescriptor)
      findFeatureByName ("property", propName, bi.getPropertyDescriptors ());
    if (pd == null) {
      throw new IllegalArgumentException ("property '" + propName + "' is " +
                          "unknown for '" + target + "'");
    }

    // get read method and type of property
    Method rm;
    Class propType;
    if (index != null) {
      // if index != null, then property is indexed - pd better be so too
      if (!(pd instanceof IndexedPropertyDescriptor)) {
        throw new IllegalArgumentException ("attempt to get non-indexed " +
                            "property '" + propName +
                            "' as being indexed");
      }
      IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) pd;
      rm = ipd.getIndexedReadMethod ();
      propType = ipd.getIndexedPropertyType ();
    } else {
      rm = pd.getReadMethod ();
      propType = pd.getPropertyType ();
    }

    if (rm == null) {
      throw new IllegalArgumentException ("property '" + propName +
                          "' is not readable");
    }

    // now get the value
    Object propVal = null;
    if (index != null) {
      propVal = rm.invoke (target, new Object[] {index});
    } else {
      propVal = rm.invoke (target, null);
    }
    return new Bean (propType, propVal);
  }
  public static void setField (Object target, String fieldName, Bean value,
                   TypeConvertorRegistry tcr)
      throws IllegalArgumentException, IllegalAccessException {
    // This is to handle how we do static fields.
    Class targetClass = (target instanceof Class)
                        ? (Class) target
                        : target.getClass ();

    try {
      Field f = targetClass.getField (fieldName);
      Class fieldType = f.getType ();

      // type convert the value if necessary
      Object fieldVal = null;
      boolean okeydokey = true;
      if (fieldType.isAssignableFrom (value.type)) {
        fieldVal = value.value;
      } else if (tcr != null) {
        TypeConvertor cvtor = tcr.lookup (value.type, fieldType);
        if (cvtor != null) {
          fieldVal = cvtor.convert (value.type, fieldType, value.value);
        } else {
          okeydokey = false;
        }
      } else {
        okeydokey = false;
      }
      if (!okeydokey) {
        throw new IllegalArgumentException ("unable to assign '" + value.value +
                            "' to field '" + fieldName + "'");
      }

      // now set the value
      f.set (target, fieldVal);
    } catch (NoSuchFieldException e) {
      throw new IllegalArgumentException ("field '" + fieldName + "' is " +
                          "unknown for '" + target + "'");
    }
  }
  //////////////////////////////////////////////////////////////////////////

  /**
   * Set a property of a bean to a given value.
   *
   * @param target    the object whose prop is to be set
   * @param propName  name of the property to set
   * @param index     index to set (if property is indexed)
   * @param value     the property value
   * @param valueType the type of the above (needed when its null)
   * @param tcr       type convertor registry to use to convert value type to
   *                  property type if necessary
   *
   * @exception IntrospectionException if unable to introspect
   * @exception IllegalArgumentException if problems with args: if the
   *            property is unknown, or if the property is given an index
   *            when its not, or if the property is not writeable, or if
   *            the given value cannot be assigned to the it (type mismatch).
   * @exception IllegalAccessException if write method is not accessible
   * @exception InvocationTargetException if write method excepts
   */
  public static void setProperty (Object target, String propName,
                  Integer index, Object value,
                  Class valueType, TypeConvertorRegistry tcr)
       throws IntrospectionException, IllegalArgumentException,
              IllegalAccessException, InvocationTargetException {
    // find the property descriptor
    BeanInfo bi = Introspector.getBeanInfo (target.getClass ());
    PropertyDescriptor pd = (PropertyDescriptor)
      findFeatureByName ("property", propName, bi.getPropertyDescriptors ());
    if (pd == null) {
      throw new IllegalArgumentException ("property '" + propName + "' is " +
                          "unknown for '" + target + "'");
    }

    // get write method and type of property
    Method wm;
    Class propType;
    if (index != null) {
      // if index != null, then property is indexed - pd better be so too
      if (!(pd instanceof IndexedPropertyDescriptor)) {
        throw new IllegalArgumentException ("attempt to set non-indexed " +
                            "property '" + propName +
                                            "' as being indexed");
      }
      IndexedPropertyDescriptor ipd = (IndexedPropertyDescriptor) pd;
      wm = ipd.getIndexedWriteMethod ();
      propType = ipd.getIndexedPropertyType ();
    } else {
      wm = pd.getWriteMethod ();
      propType = pd.getPropertyType ();
    }

    if (wm == null) {
      throw new IllegalArgumentException ("property '" + propName +
                          "' is not writeable");
    }

    // type convert the value if necessary
    Object propVal = null;
    boolean okeydokey = true;
    if (propType.isAssignableFrom (valueType)) {
      propVal = value;
    } else if (tcr != null) {
      TypeConvertor cvtor = tcr.lookup (valueType, propType);
      if (cvtor != null) {
        propVal = cvtor.convert (valueType, propType, value);
      } else {
        okeydokey = false;
      }
    } else {
      okeydokey = false;
    }
    if (!okeydokey) {
      throw new IllegalArgumentException ("unable to assign '" + value +
                          "' to property '" + propName + "'");
    }

    // now set the value
    if (index != null) {
      wm.invoke (target, new Object[] {index, propVal});
    } else {
      wm.invoke (target, new Object[] {propVal});
    }
  }
}


