package org.apache.bsf.util;

import java.util.Set;

import javax.script.Namespace;

/**
 * @author Sanka Samaranayake <ssanka@gmail.com>
 *
 */
public abstract class NamespaceImpl extends AbstractNamespace {
	
	protected Namespace globalspace = null;
	protected Namespace namespace = null;
	
	public NamespaceImpl() {
	}
	
	public void setLocalNamespace(Namespace namespace) {
		this.namespace = namespace;
	}
	
	public Namespace getLocalNamespace() {
		return namespace;
	}
	
	public void setGlobalNamespace(Namespace globalspace) {
		this.globalspace = globalspace;
	}
	
	public Namespace getGlobalNamespace() {
		return globalspace;
	}
	
	public Set entrySet() {
		return new SimpleEntrySet(this);
	}
	
	public Object put(Object key, Object value) {
		if (key == null || !(key instanceof String)) {
			throw new IllegalArgumentException("key is null or is not a String");
		}
		put((String) key, value);
		return value;
	}
	
	public Object get(Object key) {
		
		if (key == null || !(key instanceof String)) {
			throw new IllegalArgumentException("key is null or is not a String");
		}
		
		return ((String)key);
	}
		
	public Object remove(Object key) {
		if (!(key instanceof String)) {
			throw new IllegalArgumentException("key cannot be null");
		}
		Object value = get(key);
		remove((String) key);
		return value;
	}
}
