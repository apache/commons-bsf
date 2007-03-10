
package javax.script;

/**
 * See Javadoc of <a href="http://java.sun.com/javase/6/docs/api/javax/script/package-summary.html">Java Scripting API</a>
 */
public interface Invocable {
	
	/**
     * Invokes a scripting procedure with the given name using the 
     * array of objects as its arguments set.
     * 
	 * @param name name of the scripting procedure
	 * @param args       arguments set for the scripting procedure
	 * @return resultant object after the execution of the procedure
     * @throws ScriptException if the invocation of the scripting procedure
     *         fails
	 */
    public Object invokeFunction(String name, Object[] args) 
            throws ScriptException;
    
    /**
     * Invokes a procedure on an object which already defined in the
     * script using the array of objects as its arguments set.
     * 
	 * @param name name of the procedure to be invoked
	 * @param thiz       object on which the procedure is called
	 * @param args       arguments set for the procedure
	 * @return           resultant object after the execution of the 
     *                   procedure
	 * @throws ScriptException if the invocation of the procedure 
     *         fails
	 */
	public Object invokeMethod(Object thiz, String name, Object[] args) throws 
            ScriptException;
	
    /**
     * Retrieves an instance of java class whose methods are 
     * impelemented using procedures in script which are in the 
     * intermediate code repository in the underlying interpreter.
     * 
     * @param clasz an interface which the returned class must 
     *              implement
     * @return an instance of the class which implement the specified
     *         interface
     */
	public Object getInterface(Class clasz);
    
	public Object getInterface(Object thiz, Class clasz);
}
