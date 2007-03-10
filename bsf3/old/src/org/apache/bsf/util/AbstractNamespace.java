package org.apache.bsf.util;

import java.util.AbstractMap;
import java.util.Set;

import javax.script.Namespace;

/**
 * @author Sanka Samaranayake <ssanka@gmail.com>
 *
 */
public abstract class AbstractNamespace extends AbstractMap implements Namespace {

	public AbstractNamespace() {
	}
	
    public abstract void put(String key, Object value);
 
    public abstract Object get(String key);
    
    public abstract void remove(String key);

    public abstract String[] getNames();

    public abstract Set entrySet();

    public abstract Object put(Object key, Object value);

    public abstract Object get(Object key);

    public abstract Object remove(Object key);
}
