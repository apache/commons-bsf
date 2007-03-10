package javax.script;

import java.util.Map;

/**
 * Namespace is an interface which exposes a collectoin of key-value 
 * pairs.
 * 
 * @author Nandika Jayawardana <nandika@opensource.lk>
 * @author Sanka Samaranayake  <ssanka@gmail.com>
 */
public interface Namespace extends Map{

	/**
     * Associates the specified value with the specified key in a 
     * java.util.Map. If the map previously contained a mapping for 
     * this key, the old value is replaced.
     * 
	 * @param key the String value which uniquely identifies the 
     *            object
	 * @param value the object to be stored.
	 * @throws IllegalArgumentException if the key is null not an 
     *         instance of java.lang.String
     * */
	public Object put(Object key,Object value)throws IllegalArgumentException;

	
    /**
     * Copies all of the mappings from the specified map to this map.
     * These mappings will replace any mappings that this map had for
     * any of the keys currently in the specified map.
     * 
     * @param toMerge mappings to be stored in the map.
     * @throws IllegalArgumentException if a key is null or is not an
     *         instance of java.lang.String
     */
    public void putAll(Map toMerge) throws IllegalArgumentException;
}
