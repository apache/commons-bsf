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

package org.apache.bsf.debug.jsdi;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This is interface that all objects in JavaScript must implement.
 * The interface provides for the management of properties and for
 * performing conversions.
 * <p>
 * Host system implementors may find it easier to extend the ScriptableObject
 * class rather than implementing Scriptable when writing host objects.
 * <p>
 * There are many static methods defined in ScriptableObject that perform
 * the multiple calls to the Scriptable interface needed in order to  
 * manipulate properties in prototype chains.
 * <p>
 * 
 * @author: Olivier Gruber
 */

public interface JsObject extends Remote {

	/**
	 * Property attribute indicating property cannot be deleted.
	 *
	 * @see org.mozilla.javascript.ScriptableObject#delete
	 * @see org.mozilla.javascript.ScriptableObject#getAttributes
	 * @see org.mozilla.javascript.ScriptableObject#setAttributes
	 */
	public static final int DONTDELETE = 0x04;
	/**
	* Property attribute indicating property is not enumerated.
	*
	* Only enumerated properties will be returned by getIds().
	*
	* @see org.mozilla.javascript.ScriptableObject#getIds
	* @see org.mozilla.javascript.ScriptableObject#getAttributes
	* @see org.mozilla.javascript.ScriptableObject#setAttributes
	*/
	public static final int DONTENUM = 0x02;
	/**
	* The empty property attribute.
	*
	* Used by getAttributes() and setAttributes().
	*
	* @see org.mozilla.javascript.ScriptableObject#getAttributes
	* @see org.mozilla.javascript.ScriptableObject#setAttributes
	*/
	public static final int EMPTY = 0x00;
	/**
	* Property attribute indicating property cannot be deleted.
	*
	* @see org.mozilla.javascript.ScriptableObject#delete
	* @see org.mozilla.javascript.ScriptableObject#getAttributes
	* @see org.mozilla.javascript.ScriptableObject#setAttributes
	*/
	public static final int INTERNAL = 0x05;
	/**
	* Property attribute indicating assignment to this property is ignored.
	*
	* @see org.mozilla.javascript.ScriptableObject#put
	* @see org.mozilla.javascript.ScriptableObject#getAttributes
	* @see org.mozilla.javascript.ScriptableObject#setAttributes
	*/
	public static final int READONLY = 0x01;

	/**
	* The value can be any of the following type:
	*
	* 		java.lang.Boolean
	* 		java.lang.Number
	* 		java.lang.String
	*			org.apache.bsf.debug.jsdi.JsObject
	*/
	public void define(String propertyName, Object value, int attributes)
		throws RemoteException;
	/**
	 * Removes a property from this object.
	 * The prototype chain is not walked to find the
	 * property.
	*
	 * This operation corresponds to the ECMA [[Delete]] except that
	 * the no result is returned. The runtime will guarantee that this
	 * method is called only if the property exists. After this method
	 * is called, the runtime will call Scriptable.has to see if the
	 * property has been removed in order to determine the boolean
	 * result of the delete operator as defined by ECMA 11.4.1.
	 * <p>
	 * A property can be made permanent by ignoring calls to remove
	 * it.	 
	 *
	 * The property is specified by an integral index
	 * as defined for <code>get</code>.
	 * <p>
	 * To delete properties defined in a prototype chain,
	 * first find the owner object of the property and then
	 * call deleteProperty on that owner object.
	 *
	 * Identical to <code>delete(String)</code> except that
	 * an integral index is used to select the property.
	 *
	 * @param index the numeric index for the property
	 * @see org.mozilla.javascript.Scriptable#get
	 * @see org.mozilla.javascript.ScriptableObject#deleteProperty
	 */
	public void delete(int index) throws RemoteException;
	/**
	 * Removes a property from this object.
	 * This operation corresponds to the ECMA [[Delete]] except that
	 * the no result is returned. The runtime will guarantee that this
	 * method is called only if the property exists. After this method
	 * is called, the runtime will call Scriptable.has to see if the
	 * property has been removed in order to determine the boolean
	 * result of the delete operator as defined by ECMA 11.4.1.
	 * <p>
	 * A property can be made permanent by ignoring calls to remove
	 * it.<p>
	 * The property is specified by a String name
	 * as defined for <code>get</code>.
	 * <p>
	 * To delete properties defined in a prototype chain,
	 * first find the owner object of the property and then
	 * call deleteProperty on that owner object.
	 *
	 * @param name the identifier for the property
	 * @see org.mozilla.javascript.Scriptable#get
	 * @see org.mozilla.javascript.ScriptableObject#deleteProperty
	 */
	public void delete(String name) throws RemoteException;

	/**
	 * Get a named property from the object.
	 * Does not walk the prototype chain.
		 *
		 * Looks property up in this object and returns the associated value
		 * if found. Returns NOT_FOUND if not found.
		 * Note that this method is not expected to traverse the prototype
		 * chain. This is different from the ECMA [[Get]] operation.
		 *
		 * Depending on the property selector, the runtime will call
		 * this method or the form of <code>get</code> that takes an
		 * integer:
		 * <table>
		 * <tr><th>JavaScript code</th><th>Java code</th></tr>
		 * <tr><td>a.b      </td><td>a.get("b", a)</td></tr>
		 * <tr><td>a["foo"] </td><td>a.get("foo", a)</td></tr>
		 * <tr><td>a[3]     </td><td>a.get(3, a)</td></tr>
		 * <tr><td>a["3"]   </td><td>a.get(3, a)</td></tr>
		 * <tr><td>a[3.0]   </td><td>a.get(3, a)</td></tr>
		 * <tr><td>a["3.0"] </td><td>a.get("3.0", a)</td></tr>
		 * <tr><td>a[1.1]   </td><td>a.get("1.1", a)</td></tr>
		 * <tr><td>a[-4]    </td><td>a.get(-4, a)</td></tr>
		 * </table>
		 * <p>
		 * The values that may be returned are limited to the following:
		 * <UL>
		 * <LI>java.lang.Boolean objects</LI>
		 * <LI>java.lang.String objects</LI>
		 * <LI>java.lang.Number objects</LI>
		 * <LI>org.apache.bsf.debug.jsdi.JsObject objects</LI>
		 * <LI>null</LI>
		 * <LI>The value returned by Context.getUndefinedValue()</LI>
		 * <LI>NOT_FOUND</LI>
		 * </UL>
		 * @param name the name of the property
		 * @return the value of the property (may be null), or NOT_FOUND
		 */
	public Object get(String name) throws RemoteException;
	public Object get(int index) throws RemoteException;

	/**
	 * Get the name of the set of objects implemented by this Java class.
	 * This corresponds to the [[Class]] operation in ECMA and is used
	 * by Object.prototype.toString() in ECMA.<p>
	 * See ECMA 8.6.2 and 15.2.4.2.
	 */
	public String getClassName() throws RemoteException;

	/**
	 * Get the default value of the object with a given hint.
	 * The hints are String.class for type String, Number.class for type
	 * Number, Scriptable.class for type Object, and Boolean.class for
	 * type Boolean. <p>
	 *
	 * A <code>hint</code> of null means "no hint".
	 *
	 * See ECMA 8.6.2.6.
	 *
	 * @param hint the type hint
	 * @return the default value
	 */
	public Object getDefaultValue(Class hint) throws RemoteException;

	/**
	* Returns an array of property ids defined on this object.
	* The prototype chain is not walked.
	* However, modified properties that were prototype properties
	* will be seen.
	* The parameter indicates to enumerate the "DONTENUM" properties
	* or not.
	* <p>
	* @return an array of all ids from all object in the prototype chain.
	*         If a given id occurs multiple times in the prototype chain,
	*         it will occur only once in this list.
	* The elements are limited to:
	*	- java.lang.String for the property name if it has one.
	*	- java.lang.Integer for properties without a name, it is their index.
	* @since 1.5R2
	*/
	public Object[] getIds(boolean all) throws RemoteException;

	/**
	* Get the prototype of the object.
	* @return the prototype
	*/
	public JsObject getPrototype() throws RemoteException;

	/**
	 * The scope is for supporting two things.
	 * 
	 * If the object is a function, the scope is the "execution" scope
	 * of the function. It will be used when calling the function to
	 * initialize the Context scope.
	 *
	 * If the object is not a function, the scope is used for the scope
	 * chain of execution context.
	 */
	public JsObject getScope() throws RemoteException;

	/**
	 * Indicates whether or not an indexed  property is defined in an object.
	 * Does not traverse the prototype chain.<p>
	 *
	 * The property is specified by an integral index
	 * as defined for the <code>get</code> method.<p>
	 *
	 * @param index the numeric index for the property
	 * @return true if and only if the indexed property is found in the object
	 * @see org.mozilla.javascript.Scriptable#get
	 * @see org.mozilla.javascript.ScriptableObject#getProperty
	 */
	public boolean has(int index) throws RemoteException;

	/**
	* Indicates whether or not a named property is defined in an object.
	* Does not traverse the prototype chain.<p>
	*
	* The property is specified by a String name
	* as defined for the <code>get</code> method.<p>
	*
	* @param name the name of the property
	* @return true if and only if the named property is found in the object
	* @see org.mozilla.javascript.Scriptable#get
	* @see org.mozilla.javascript.ScriptableObject#getProperty
	*/
	public boolean has(String name) throws RemoteException;

	/**
		 * The instanceof operator.
		 *
		 * <p>
		 * The JavaScript code "lhs instanceof rhs" causes rhs.hasInstance(lhs) to
		 * be called.
		 *
		 * <p>
		 * The return value is implementation dependent so that embedded host objects can
		 * return an appropriate value.  See the JS 1.3 language documentation for more
		 * detail.
		 *
		 * <p>This operator corresponds to the proposed EMCA [[HasInstance]] operator.
		 *
		 * @param instance The value that appeared on the LHS of the instanceof
		 *              operator
		 *
		 * @return an implementation dependent value
		 */
	public boolean hasInstance(JsObject instance) throws RemoteException;
	public boolean isFunction() throws RemoteException;
	public boolean isScript() throws RemoteException;

	/**
		 * Sets an indexed property in this object.
		 * <p>
		 * The property is specified by an integral index
		 * as defined for <code>get</code>.<p>
		 *
		 * @param index the numeric index for the property
		 * @param value value to set the property to
		 */
	public void put(int index, Object value) throws RemoteException;

	/**
	* Sets a named property in this object.
	* <p>
	* The property is specified by a string name 
	* as defined for <code>get</code>.
	* <p>
	* Note that if a property <i>a</i> is defined in the prototype <i>p</i>
	* of an object <i>o</i>, then evaluating <code>o.a = 23</code> will cause
	* <code>set</code> to be called on the prototype <i>p</i> with
	* <i>o</i> as the  <i>start</i> parameter.
	* To preserve JavaScript semantics, it is the Scriptable
	* object's responsibility to modify <i>o</i>. <p>
	* This design allows properties to be defined in prototypes and implemented
	* in terms of getters and setters of Java values without consuming slots
	* in each instance.<p>
	* <p>
	* The values that may be set are limited to the following:
	* <UL>
	* <LI>java.lang.Boolean objects</LI>
	* <LI>java.lang.String objects</LI>
	* <LI>java.lang.Number objects</LI>
	* <LI>org.apache.bsf.debug.jsdi.JsObject</LI>
	* <LI>null</LI>
	* <LI>The value returned by Context.getUndefinedValue()</LI> 
	* </UL><p>
	*
	* IMPORTANT: JAVA OBJECTS.
	* The wrapping is not yet supported.
	*/
	public void put(String name, Object value) throws RemoteException;

	/**
	* Set the prototype of the object.
	* @param prototype the prototype to set
	*/
	public void setPrototype(JsObject prototype) throws RemoteException;
	
	
	/**
	* Set the prototype of the object.
	* @param prototype the prototype to set
	*/
	public void setScope(JsObject scope) throws RemoteException;

	/* For Native Objects, allow to unwrap them.
	public Object unwrap() throws RemoteException;
	public boolean wrapsJavaObject() throws RemoteException;
	public boolean isWrapper() throws RemoteException;
	*/
}
