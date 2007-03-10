package org.apache.bsf.util;

import java.util.AbstractSet;
import java.util.Iterator;

/**
 * @author Sanka Samaranayake <ssanka@gmail.com>
 *
 */
public class SimpleEntrySet extends AbstractSet {
	private AbstractNamespace ns;
	private String[] keys;
	
	public SimpleEntrySet(AbstractNamespace ns) {
		this.ns = ns;
		keys  = ns.getNames();
	}
	
	public int size() {
		return keys.length;
	}
	
	public Iterator iterator() {
		return new SimpleIterator();
	}
	
	class SimpleIterator implements Iterator {
		
		public boolean hasNext() {
			return pos < keys.length - 1;
		}
		
		public Object next() {
			flag = false;			
			return new SimpleEntry(keys[pos++]);
		}
			
		public void remove() {
			if (flag || pos == 0) {
				throw new IllegalStateException();
			} else {
				flag = true;
				ns.remove(keys[pos - 1]);
			}
		}
	
		private int pos = 0;
		private boolean flag = false;
	}
	
	class SimpleEntry implements java.util.Map.Entry {
		private String key = null;
		
		public SimpleEntry(String key) {
			this.key = key;			
		}
		
		public Object getKey() {
			return key;
		}
		
		public Object getValue() {
			return ns.get(key);
		}
		public Object setValue(Object value) {
			throw new UnsupportedOperationException();
		}
	}
}
	
	
