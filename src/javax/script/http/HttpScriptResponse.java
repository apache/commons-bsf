
package javax.script.http;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * HttpScriptResponse is a Wrapper class which is used to wrap the a  
 * HttpServletResponse  in HttpScriptContext.getReponse() method.
 * 
 * @author Nandika Jayawarda  <nandika@opensource.lk>
 * @author Sanka Samaranayake <sanka@opensource.lk>
 */
public class HttpScriptResponse extends HttpServletResponseWrapper {
    
    public HttpScriptResponse(HttpScriptContext context, HttpServletResponse res){
        super(res);        
    }
    
}
