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

package org.apache.bsf.debug.util;

import java.util.Enumeration;
import java.util.NoSuchElementException;
/**
 * Hashtable associates keys with values. Keys and values cannot be null.
 * The size of the Hashtable is the number of key/value pairs it contains.
 * The capacity is the number of key/value pairs the Hashtable can hold.
 * The load factor is a float value which determines how full the Hashtable
 * gets before expanding the capacity. If the load factor of the Hashtable
 * is exceeded, the capacity is doubled.
 *
 * @author		OTI
 * @version		initial
 *
 * @see			Enumeration
 * @see			java.io.Serializable
 * @see			java.lang.Object#equals
 * @see			java.lang.Object#hashCode
 */

public class IntHashtable implements Cloneable {
  
  int elementCount;
  IntHashMapEntry[] elementData;
  private int loadFactor;
  private int threshold;

  private static final class HashEnumerator implements Enumeration {
	IntHashMapEntry array[];
	int start, end;
	IntHashMapEntry entry;
	HashEnumerator (IntHashMapEntry[] entries) {
	  array = entries;
	  start = 0;
	  end = array.length;
	}
	public boolean hasMoreElements () {
	  if (entry != null) return true;
	  while (start < end) {
		if (array[start] == null) start++;
		else return true;
	  }
	  return false;
	}
	public Object nextElement () {
	  if (hasMoreElements()) {
		if (entry == null) entry = array[start++];
		Object result = entry.value;
		entry = entry.next;
		return result;
	  }
	  throw new NoSuchElementException();
	}
  }

  /**
  * Constructs a new Hashtable using the default capacity
  * and load factor.
  *
  * @author		OTI
  * @version		initial
  */
  public IntHashtable() {
  this (101);
  }  
  /**
  * Constructs a new IntHashtable using the specified capacity
  * and the default load factor.
  *
  * @author		OTI
  * @version		initial
  *
  * @param		capacity	the initial capacity
  */
  public IntHashtable(int capacity) {
  if (capacity < 0) throw new IllegalArgumentException();
  elementCount = 0;
  elementData = new IntHashMapEntry[capacity == 0 ? 1 : capacity];
  loadFactor = 7500;	// Default load factor of 0.75
  computeMaxSize();
  }  
  /**
  * Constructs a new IntHashtable using the specified capacity
  * and load factor.
  *
  * @author		OTI
  * @version		initial
  *
  * @param		capacity	the initial capacity
  * @param		loadFactor	the initial load factor
  */
  public IntHashtable(int capacity, float loadFactor) {
  if (capacity < 0 || loadFactor <= 0)
  throw new IllegalArgumentException();
  elementCount = 0;
  elementData = new IntHashMapEntry[capacity];
  this.loadFactor = (int)(loadFactor * 10000);
  computeMaxSize();
  }  
  /**
   * Removes all key/value pairs from this IntHashtable, leaving the size zero
   * and the capacity unchanged.
   *
   * @author		OTI
   * @version		initial
   *
   * @see			#isEmpty
   * @see			#size
   */
  public synchronized void clear() {
	elementCount = 0;
	for (int i = elementData.length; --i >= 0;) {
	  elementData[i] = null;
	}
  }  
  /**
   * Answers a new IntHashtable with the same key/value pairs, capacity
   * and load factor.
   *
   * @author		OTI
   * @version		initial
   *
   * @return		a shallow copy of this IntHashtable
   *
   * @see			java.lang.Cloneable
   */
  public synchronized Object clone() {
	try {
	  IntHashtable hashtable = (IntHashtable) super.clone ();
	  hashtable.elementData = (IntHashMapEntry[])elementData.clone();
	  IntHashMapEntry entry;
	  for (int i=elementData.length; --i >= 0;) {
		if ((entry = elementData[i]) != null)
		  hashtable.elementData[i] = (IntHashMapEntry)entry.clone();
	  }
	  return hashtable;
	} catch (CloneNotSupportedException e) {
		return null;
	}
  }  
  private void computeMaxSize() {
	threshold = (int)((long)elementData.length * loadFactor / 10000);
  }  
  /**
   * Answers if this Hashtable contains the specified object as the value
   * of at least one of the key/value pairs.
   *
   * @author		OTI
   * @version		initial
   *
   * @param		value	the object to look for as a value in this Hashtable
   * @return		true if object is a value in this Hashtable, false otherwise
   *
   * @see			#containsKey
   * @see			java.lang.Object#equals
   */
  public synchronized boolean contains(Object value) {
	for (int i=elementData.length; --i >= 0;) {
	  IntHashMapEntry entry = elementData[i];
	  while (entry != null) {
		if (entry.value == value || entry.value.equals(value))
		  return true;
		entry = entry.next;
	  }
	}
	return false;
  }  
  /**
   * Answers if this Hashtable contains the specified object as a key
   * of one of the key/value pairs.
   *
   * @author		OTI
   * @version		initial
   *
   * @param		key	the object to look for as a key in this Hashtable
   * @return		true if object is a key in this Hashtable, false otherwise
   *
   * @see			#contains
   * @see			java.lang.Object#equals
   */
  public synchronized boolean containsKey(int key) {
	return getEntry(key) != null;
  }  
  /**
   * Answers an Enumeration on the values of this Hashtable. The
   * results of the Enumeration may be affected if the contents
   * of this Hashtable are modified.
   *
   * @author		OTI
   * @version		initial
   *
   * @return		an Enumeration of the values of this Hashtable
   *
   * @see			#keys
   * @see			#size
   * @see			Enumeration
   */
  public synchronized Enumeration elements() {
	return new HashEnumerator (elementData);
  }  
  /**
   * Answers the value associated with the specified key in
   * this Hashtable.
   *
   * @author		OTI
   * @version		initial
   *
   * @param		key	the key of the value returned
   * @return		the value associated with the specified key, null if the specified key
   *				does not exist
   *
   * @see			#put
   */
  public synchronized Object get(int key) {
	int index = (key & 0x7FFFFFFF) % elementData.length;
	IntHashMapEntry entry = elementData[index];
	while (entry != null) {
	  if (entry.key == key) return entry.value;
	  entry = entry.next;
	}
	return null;
  }  
  private IntHashMapEntry getEntry(int key) {
	int index = (key & 0x7FFFFFFF) % elementData.length;
	IntHashMapEntry entry = elementData[index];
	while (entry != null) {
	  if (entry.key == key) return entry;
	  entry = entry.next;
	}
	return null;
  }  
  /**
   * Answers if this Hashtable has no key/value pairs, a size of zero.
   *
   * @author		OTI
   * @version		initial
   *
   * @return		true if this Hashtable has no key/value pairs, false otherwise
   *
   * @see			#size
   */
  public boolean isEmpty() {
	return elementCount == 0;
  }  
  /**
   * Associate the specified value with the specified key in this Hashtable.
   * If the key already exists, the old value is replaced. The key and value
   * cannot be null.
   *
   * @author		OTI
   * @version		initial
   *
   * @param		key	the key to add
   * @param		value	the value to add
   * @return		the old value associated with the specified key, null if the key did
   *				not exist
   *
   * @see			#elements
   * @see			#get
   * @see			#keys
   * @see			java.lang.Object#equals
   */
  public synchronized Object put(int key, Object value) {
	if (value == null) throw new NullPointerException ();
	int index = (key & 0x7FFFFFFF) % elementData.length;
	IntHashMapEntry entry = elementData[index];
	while (entry != null) {
	  if (entry.key == key) break;
	  entry = entry.next;
	}
	if (entry == null) {
	  if (++elementCount > threshold) {
		rehash();
		index = (key & 0x7FFFFFFF) % elementData.length;
	  }
	  entry = new IntHashMapEntry(key, value);
	  entry.next = elementData[index];
	  elementData[index] = entry;
	  return null;
	}
	Object result = entry.value;
	entry.value = value;
	return result;
  }  
  /**
   * Increases the capacity of this Hashtable. This method is sent when
   * the size of this Hashtable exceeds the load factor.
   *
   * @author		OTI
   * @version		initial
   */
  protected void rehash() {
	int length = elementData.length<<1;
	if (length == 0) length = 1;
	IntHashMapEntry[] newData = new IntHashMapEntry[length];
	for (int i=elementData.length; --i >= 0;) {
	  IntHashMapEntry entry = elementData[i];
	  while (entry != null) {
		int index = (entry.key & 0x7FFFFFFF) % length;
		IntHashMapEntry next = entry.next;
		entry.next = newData[index];
		newData[index] = entry;
		entry = next;
	  }
	}
	elementData = newData;
	computeMaxSize();
  }  
  /**
   * Remove the key/value pair with the specified key from this Hashtable.
   *
   * @author		OTI
   * @version		initial
   *
   * @param		key	the key to remove
   * @return		the value associated with the specified key, null if the specified key
   *				did not exist
   *
   * @see			#get
   * @see			#put
   */
  public synchronized Object remove(int key) {
	IntHashMapEntry last = null;
	int index = (key & 0x7FFFFFFF) % elementData.length;
	IntHashMapEntry entry = elementData[index];
	while (entry != null) {
	  if (entry.key == key) break;
	  last = entry;
	  entry = entry.next;
	}
	if (entry != null) {
	  if (last == null) elementData[index] = entry.next;
	  else last.next = entry.next;
	  elementCount--;
	  return entry.value;
	}
	return null;
  }  
  /**
   * Answers the number of key/value pairs in this Hashtable.
   *
   * @author		OTI
   * @version		initial
   *
   * @return		the number of key/value pairs in this Hashtable
   *
   * @see			#elements
   * @see			#keys
   */
  public int size() {
	return elementCount;
  }  
  /**
   * Answers the string representation of this Hashtable.
   *
   * @author		OTI
   * @version		initial
   *
   * @return		the string representation of this Hashtable
   */
  public synchronized String toString() {
	Object key;
	int count = 0;
	StringBuffer buffer = new StringBuffer ();
	buffer.append ('{');
	for (int i=elementData.length; --i >= 0;) {
	  IntHashMapEntry entry = elementData[i];
	  while (entry != null) {
		buffer.append(entry.key);
		buffer.append('=');
		buffer.append(entry.value);
		buffer.append(',');
		entry = entry.next;
	  }
	}
	// Remove the last ','
	if (elementCount > 0) buffer.setLength(buffer.length() - 1);
	buffer.append ('}');
	return buffer.toString ();
  }  
}
