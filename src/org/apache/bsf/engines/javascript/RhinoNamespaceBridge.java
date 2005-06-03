package org.apache.bsf.engines.javascript;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import javax.script.Namespace;
import javax.script.ScriptContext;

import org.apache.bsf.util.AbstractNamespace;
import org.apache.bsf.util.SimpleEntrySet;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;

/**
 * ScriptableNamespace implements Scriptable interface which the native Rhino
 * Engine can access directly. It porvides various mechanisms to locate, store,
 * wrap, unwrap Java objects when needed.
 * 
 * @author Nandika Jayawardana <nandika@opensource.lk>
 * @author Sanka Samaranayake  <ssanka@gmail.com>
 *
 */

public class RhinoNamespaceBridge extends AbstractNamespace implements Scriptable, Namespace {
	/** */
	protected Scriptable scope = null;
	/** */
	protected RhinoScriptEngine  engine = null;
	/** */
	protected Namespace engineNamespace = null;
	/** */
	protected Namespace globalNamespace = null;
	
	public RhinoNamespaceBridge(RhinoScriptEngine engine, ScriptableObject scope) {
		this.engine = engine;
		this.scope = scope;
		this.engineNamespace = null;
	}
	
	public RhinoNamespaceBridge(Namespace engineNamespace, ScriptableObject scope) {
		this.engineNamespace = engineNamespace;
		this.scope = scope;
		this.engine = null;
	}
	
	public RhinoNamespaceBridge(RhinoScriptEngine engine) {
		ImporterTopLevel importer = new ImporterTopLevel();
		Context cx = Context.getCurrentContext();
		
		/* CHECK ME */
		if (cx == null) {
			try {
				cx = Context.enter();
				importer.initStandardObjects(cx, false); 
				Context.exit();
			} catch (Exception ex) {
				System.err.println("Error : " + ex.getMessage());
			}
		} else {
			importer.initStandardObjects(cx, false);
		}
		this.scope = importer;
		this.engine = engine;
		this.engineNamespace = null;	
	}
	
	public RhinoNamespaceBridge(Namespace engineNamespace) {
		ImporterTopLevel importer = new ImporterTopLevel();
		Context cx = Context.getCurrentContext();
		
		/* CHECK ME */
		if (cx == null) {
			try {
				cx = Context.enter();
				importer.initStandardObjects(cx, false); 
				Context.exit();			
			} catch (Exception ex) {
				System.err.println("Error : " + ex.getMessage());
			}
		} else {
			importer.initStandardObjects(cx, false);
		}
		
		this.scope = importer;
		this.engineNamespace = engineNamespace;
		this.engine = null;	
	}
	
	public void put(String key, Object value) {
		getLocalNamespace().put(key, value);
	}
	
	public Object get(String key) {
		
		Object retValue = getLocalNamespace().get(key);
		
		if (retValue != null && !(retValue instanceof Undefined)) {
			return retValue;
		} else if (getGlobalNamespace() != null && 
				getGlobalNamespace().get(key) != null) {
			
			return getGlobalNamespace().get(key);
		}
		retValue = scope.get(key, scope);
		
		if (retValue instanceof Undefined || retValue == NOT_FOUND) {
			return null;
		} else if (retValue instanceof NativeJavaObject) {
			return ((NativeJavaObject) retValue).unwrap();
		} else {
			return retValue; // CHECK ME
		}
	}
	
	public void remove(String key) {
		getLocalNamespace().remove(key);
		scope.delete(key);
	}
		
	public String[] getNames() {
		ArrayList myList = new ArrayList();
		
		Iterator iter = getLocalNamespace().keySet().iterator();
		while (iter.hasNext()) {
			myList.add(iter.next());
		}
		
		Object[] ids = scope.getIds();
		for (int i = 0; i < ids.length; i++) {
			if (!(myList.contains(ids[i]))) {
				myList.add(ids[i]);
			}
		}
		
		String[] myArray = new String[myList.size()];
		iter = myList.iterator();
		
		int i = 0;
		while (iter.hasNext()) {
			myArray[i] = (String) iter.next();
			i++;
		}
		return (String[]) myList.toArray();
	}

	public String getClassName() {
		return scope.getClassName();
	}
	
	public Object get(String name, Scriptable start) {
		Object retValue;
		
		if (start == this) {
		
			if (name != "Function") {
				retValue = getLocalNamespace().get(name);
			
				if (((retValue == null) || (retValue instanceof Undefined)) 
						&& (getGlobalNamespace() != null)) {
					retValue = getGlobalNamespace().get(name);
				}
				
				if (retValue != null) {
					Context cx = Context.getCurrentContext();
					
					if (cx == null) {
						try {
							cx = Context.enter();
							/* must wrap a native Java object before presenting
							 * it to native Rhino Interpreter                   
							 */
							retValue = cx.getWrapFactory().wrap(
							        cx, scope, retValue, null);
						} finally {
							Context.exit();
						}
					}
					
					return retValue;
				}
			}
			return scope.get(name, scope);
		}
		return scope.get(name, start);
	}

	public Object get(int index, Scriptable start) {
		return scope.get(index, start);
	}
	
	public boolean has(String name, Scriptable start) {
		return ((getLocalNamespace().get(name) !=  null) 
				|| scope.has(name, start));
	}

	public boolean has(int index, Scriptable start) {
		return scope.has(index, start);
	}
	
	public void put(String name, Scriptable start, Object value) {
		
	       if (this == start) {
            value = (value instanceof NativeJavaObject)
                    ?((NativeJavaObject) value).unwrap() 
                    : value;
            put(name, value);            
        } else {
            scope.put(name, start, value);
        }
	}

	public void put(int index, Scriptable start, Object value) {
		scope.put(index, start, value);
	}

	public void delete(String name) {
		remove(name);
	}

	public void delete(int index) {
		scope.delete(index);
	}

	public Scriptable getPrototype() {
		return scope.getPrototype();
	}

	public void setPrototype(Scriptable prototype) {
		scope.setPrototype(prototype);
	}
	
	public Scriptable getParentScope() {
		return scope.getParentScope();
	}

	public void setParentScope(Scriptable parent) {
		scope.setParentScope(parent);
	}
	
	public Object[] getIds() {
		String[] names = getNames();
		Object[] ids = new Object[names.length];
		System.arraycopy(names, 0, ids, 0, ids.length);
		return ids;
	}

	public Object getDefaultValue(Class hint) {
		return scope.getDefaultValue(hint);
	}

	public boolean hasInstance(Scriptable instance) {
		return scope.hasInstance(instance);
	}
	
	// followings should be taken to NamespaceIml and extends from it instead
	// of AbstractNamespace
	
	public Set entrySet() {
		return new SimpleEntrySet(this);
	}
	
	public Object get(Object key) {
		
		if (key == null || !(key instanceof String)) {
			throw new IllegalArgumentException("key is null or is not a String");
		}
		
		return get((String)key);
	}
	
	// public void setLocalNamespace(Namespace namespace) {
	// }
	
	public Namespace getGlobalNamespace() {
		return globalNamespace;
	}
	
	public Namespace getLocalNamespace() {
		if (engine != null) {
			return engine.getNamespace(ScriptContext.ENGINE_SCOPE);
		}
		if (engineNamespace != null) {
			return engineNamespace;
		}
		throw new IllegalStateException("Engine Namespace is not set");
	}
	
	public Object put(Object key, Object value) {
		if (key == null || !(key instanceof String)) {
			throw new IllegalArgumentException("key is null or is not a String");
		}
		put((String) key, value);
		return value;
	}
	public Object remove(Object key) {
		if (key == null || !(key instanceof String)) {
			throw new IllegalArgumentException("key is null or is not a String");
		}
		Object value = get(key);
		remove((String) key);
		return value;
	}	
	
	public void setGlobalNamespace(Namespace globalNamespace) {
		this.globalNamespace = globalNamespace;		
	}
}
