
package javax.script;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * See Javadoc of <a href="http://java.sun.com/javase/6/docs/api/javax/script/package-summary.html">Java Scripting API</a>
 */
public class SimpleBindings implements Bindings {
	
	/**
     * In which the key-value pairs are stored.
	 */
    protected Map map;
	
    /**
     * Constructs a SimpleNamespace.
     */
	public SimpleBindings(){
        map = new HashMap();
	}
    
    /**
     * Constructs a SimpleNamespace and initializes it using a 
     * specified map. 
     * 
     * @param map a map which is used to initialize the 
     *            SimpleNamespace
     */
	public SimpleBindings(Map map){
		this.map = map;
	}

    /**
     * Associates the specified value with the specified key in a 
     * java.util.Map. If the map previously contained a mapping for 
     * this key, the old value is replaced.
     * 
     * @param key the String value which uniquely identifies the 
     *            object
     * @param value the object to be stored.
     * @throws IllegalArgumentException if the key is null or is not
     *         java.lang.String type
     */
    public Object put(Object key, Object value) 
            throws IllegalArgumentException{
      
    	if ((key == null) || !(key instanceof java.lang.String) ) {
            throw new IllegalArgumentException("key is null or not a String");
        }       
		
    	return map.put(key,value);
	}
	
    /**
     * Copies all of the mappings from the specified map to this map.
     * These mappings will replace any mappings that this map had for
     * any of the keys currently in the specified map.
     * 
     * @param toMerge mappings to be stored in the map.
     * @throws IllegalArgumentException if a key is null or is not 
     *         java.lang.String type in the specified map
     */
	public void putAll(Map toMerge) throws IllegalArgumentException {
        
        Set keySet= toMerge.keySet();
		Iterator keys= keySet.iterator();
		
        while (keys.hasNext()) {
			if (keys.next() instanceof String) {
				throw new IllegalArgumentException("a key is not a String");
            }
        }
            
		map.putAll(toMerge);	
	}
    
    public int size() {
        return map.size();
    }
    
    public void clear() {
        map.clear();    
    }
    
    public boolean isEmpty() {
        return map.isEmpty();
    }
    
	public boolean containsKey(Object key) {
		return map.containsKey(key);
	}
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}
    
    public Collection values() {
        return map.values();
    }
    
	public Set entrySet() {
		return map.entrySet();
	}
	
    public Object get(Object key) {
		return map.get(key);
	}

	public Set keySet() {
		return map.keySet();
	}

    public Object remove(Object key) {
		return map.remove(key);
	}

}
