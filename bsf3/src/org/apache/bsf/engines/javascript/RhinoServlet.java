
package org.apache.bsf.engines.javascript;

import javax.script.ScriptEngine;
import javax.script.http.GenericHttpScriptContext;
import javax.script.http.HttpScriptContext;
import javax.script.http.HttpScriptServlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.bsf.util.ScriptEnginePool;

public class RhinoServlet extends HttpScriptServlet {

    private ServletConfig config;
    private ScriptEnginePool pool;
       
    public HttpScriptContext getContext(HttpServletRequest req,
            HttpServletResponse res) throws ServletException {
        
        GenericHttpScriptContext context = new GenericHttpScriptContext();
        context.initialize(this, req, res);
        return context;
    }

    public ScriptEngine getEngine(HttpServletRequest req) {
        return pool.get();
    }
    
    public ServletConfig getServletConfig() {
        return config;
    }
    
    public String getServletInfo() {
        return "Rhino Script Servlet";                
    }
    
    public void init(ServletConfig config) throws ServletException {
        this.config = config;
        pool = new ScriptEnginePool(new RhinoScriptEngineFactory());        
    }

    public void releaseEngine(ScriptEngine eng) {
        pool.free(eng);
    }

}
