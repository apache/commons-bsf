
package javax.script.http;

import java.io.IOException;
import java.io.Reader;

import javax.script.ScriptContext;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HttpScriptContext is a subinteface of ScriptContext which is used 
 * to connect a ScriptEngine and implicit objects form a servlet 
 * container to for a single request.
 * 
 * @author Nandika Jayawardana <nandika@opensource.lk>
 * @author Sanka Samaranayake  <sanka@opensource.lk>
 */
public interface HttpScriptContext extends ScriptContext {
	
    /** Integer value for the level of SCRIPT_SCOPE */
	public static final int REQUEST_SCOPE = 0;
    
	/** Integer value for the level of SESSION_SCOPE */   
	public static final int SESSION_SCOPE = 100;
    
	/** Integer value for the level of APPLICATION_SCOPE */
	public static final int APPLICATION_SCOPE = 200;
    
    /**
     * Retrieves a boolean value which indicates whether the script 
     * execution has been disabled in the Web Application.
     *  
     * @return a booean indicating whether the script execution is 
     *         allowed
     */
    public boolean disableScript();
    
    /**
     * Retrieves a boolean value which indicates whether the
     * HttpScriptServlet executing in this context should display the
     * results of script evaluations.
     * 
     * @return a boolean indicating whether the results of script 
     *         eveluations should be displayed
     */
    public boolean displayResults();
    
    /**
     * Forwards the request to the resource identified by the 
     * specified relative path.
     * 
     * @param relativePath the URI to process the request
     * @throws IOException if an input or output error occurs while 
     *         processing the HTTP request
     * @throws ServletException if the HTTP cannot be handled 
     */
    public void forward(String relativePath) 
            throws ServletException, IOException;
    
    /**
     * Retrieves an array of Strings describing the languages that may
     * be used by scripts which is running in the associated 
     * HttpScriptContext. Returns null if no restrictions apply.
     * 
     * @return a String array of permitted languages 
     */
    public String[] getAllowedLanguages();
    
    /**
     * Retrieves the value for the specified key in the lowest scope 
     * in which the key is defined. Returns null if the specified key
     * is not defiend in any scope.
     * 
     * @param name  the name of the value to be retrieved
     * @return the value associated with the specified key
     */
    public Object getAttribute(String name);
    
    /**
     * Retrieves the value associated with the specified key in 
     * specified level of scope. 
     * 
     * @param scope the integer value of level of scope
     * @return the value associated with specified key in the 
     *         specified level of scope
     * @throws IllegalArgumentException if name is null or the scope 
     *         is invalid 
     */
    public Object getAttribute(String name,int scope) throws 
            IllegalArgumentException;
    
    /**
     * Retrieves an array of string describing HTTP request methods 
     * which are handled by servlets executing in current context.
     *  
     * @return a String array of HTTP request methods handled by 
     *         servelts in the current context
     */
    public String[] getMethods();
    
    /**
     * Retrieves a HttpScriptRequest for the current request. If the
     * session state is disabled, an adapter whose getSession() 
     * method returns null should be used.
     * 
     * @return the current request
     */
    public HttpServletRequest getRequest();
    
    /**
     * Retrieves a HttpScriptResponse for the current request.
     * 
     * @return the current response
     */
    public HttpServletResponse getResponse();
    
    /**
     * Retrieves a reader form which the executing script can be 
     * read.
     * 
     * @return a reader from which the script can be read.
     */
    public Reader getScriptSource();
    
    /**
     * Retrieves the associated HttpScriptServlet.
     * 
     * @return a reader form which the script source can be read
     */
    public Servlet getServlet();
    
    /**
     * Includes the resource in the sepcified relative path.
     *  
     * @param relativePath the URI of the request to be processed
     * @throws IOException if an input or output error occurs while 
     *         processing the HTTP request
     * @throws ServletException if the servlet cannot handled the 
     *         HTTP request 
     */
    public void include(String relativePath) throws IOException, 
            ServletException;
    
    /**
     * Initialize the current HttpScriptContext for processing of 
     * single request. Implementation must initialize request, 
     * session and application scopes. Further it should store 
     * servlet, request and response references for use.
     * 
     * @param servlet the HttpServlet which execute the request
     * @param req     the current request
     * @param res     the current response
     * @throws ServletException if the servlet cannot handle the HTTP
     *         request
     */
	public void initialize(Servlet servlet,
			HttpServletRequest req,HttpServletResponse res) throws 
            ServletException;

	/**
     * Clears any state stored in the current HttpScriptContext such 
     * that it can be reused to serve another request.
     */
    public void release();
       
    /**
     * Sets the value for the specified key in the specified level of
     * scope.
     * 
     * @param name  the key associated with the specified value
     * @param value the value associated with the specified key
     * @param scope the level of scope
     * @throws IllegalArgumentException if the level of scope is 
     *         invalid
     * @throws IllegalStateException if the session is either invalid
     *         or diabled
     */
    public void setAttribute(String name,Object value,int scope);
    
    /**
     * Retrieves a boolean value which indicates whether the 
     * HttpSession associated with the current request is exposed in 
     * SESSION_SCOPE attribute and in the HttpScriptRequest.
     *  
     * @return a boolean value which indicates whether the session is
     *         vaild 
     */
	public boolean useSession();
}
