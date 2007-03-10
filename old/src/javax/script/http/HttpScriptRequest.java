package javax.script.http;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;

/**
 * HttpScriptRequest class is a wrapper used to wrap
 * HttpServletRequest in HttpScriptContext.getResponse() method.
 * 
 * @author Nandika Jayawardana <nandika@opensource.lk>
 * @author Sanka Samaranayake <sanka@opensource.lk>
 */
public class HttpScriptRequest extends HttpServletRequestWrapper {
    
	private HttpScriptContext context;
    
    public HttpScriptRequest(HttpScriptContext context, HttpServletRequest req){
        super(req);
        this.context = context;
    }
    
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }
    
    public HttpSession getSession() {
        return (context.useSession()) ? super.getSession() : null;
    }
}
