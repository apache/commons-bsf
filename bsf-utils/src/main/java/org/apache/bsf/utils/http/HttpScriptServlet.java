/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */

package org.apache.bsf.utils.http;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * HttpScriptServlet contains method which allows execution of 
 * scripts written in one or more languages. It uses a ScriptEngine 
 * supplied by calling its getEngine method to execute a script in a
 * HttpScriptContext supplied by calling its getContext method.
 *   
 * @author Nandika Jayawardana <nandika@opensource.lk>
 * @author Sanka Samaranayake  <sanka@opensource.lk>
 */
public class HttpScriptServlet extends GenericServlet {
	private static final long serialVersionUID = 1L;

	public HttpScriptServlet(){
    }
   
	/**
     * Retrieves a HttpScriptContext initialized using specified 
     * HttpServletRequest, HttpServletResponse and a reference to 
     * this HttpScriptServlet.
     * 
	 * @param req the supplied HttpServletRequest instance
	 * @param res the supplied HttpServletResponse instance
	 * @return an initialized HttpScriptContext
	 * @throws ServletException if an error occurs
	 */
    public HttpScriptContext getContext(HttpServletRequest req,
			HttpServletResponse res) throws ServletException {
    	
    	return null; // TODO
    }
	
    /**
     * Returns a ScriptEngine instance which is used by the 
     * HttpScriptServlet to executes a single request.
     * 
     * @param req the current request
     * @return an instance of a ScriptEngine which is used by the 
     *         HttpScriptServlet to executes a single request
     */
	public ScriptEngine getEngine(HttpServletRequest req) {
       return null; // TODO		
	}
	
    /**
     * Called to indicate that the ScriptEngine returned by call to
     * getEngine() is no longer in use.
     * 
     * @param eng the ScriptEngine which is no longer in use
     */
    public void releaseEngine(ScriptEngine eng) {
    	
    }
    
    /**
     * Executes a script using the HttpScriptContext obtained by a 
     * call to getContext() and the ScriptEngine obtained by a call
     * to getEngine().
     * 
     * @param req the current request
     * @param res the current response
     * @throws IllegalArgumentException if the req is not an 
     *         instance of HttpServletRequest or if the res is not an
     *         instance of HttpServletResponse
     * @throws IOException if an input, output error occurs
     * @throws ServletException if error occurs in processing the 
     *         request
     */
	public void service(ServletRequest req,ServletResponse res)
			  throws IllegalArgumentException, IOException, ServletException {
        
        HttpServletRequest request;
        HttpServletResponse response;
        HttpScriptContext context;
        ScriptEngine engine;
        Object retValue;
        
        if( !(req instanceof HttpServletRequest) || 
                    !(res instanceof HttpServletResponse)){
            throw new IllegalArgumentException(
                    "Method arguments should be HttpServletRequest and " +
                    "HttpServletResponse type");
        }
        
        request = (HttpServletRequest)req;
        response =(HttpServletResponse)res;
        
        context = getContext(request, response);
        if (context.disableScript()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        String requestMethod = request.getMethod();
        String[] allowedMethods = context.getMethods();
        int score;
        
        for (score=0; score<allowedMethods.length; score++ ) {
            if (requestMethod.equalsIgnoreCase(allowedMethods[score])) {
                break;                
            }
        }
        
        if (score == allowedMethods.length) {
            response.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        
        engine = getEngine(request);
        
        String[] languages = context.getAllowedLanguages();
        List names     = engine.getFactory().getNames();
        
        if (languages != null) {
            
            boolean found = false;
            
            for (int i=0; i<languages.length; i++) {
                for (int j=0; j<names.size(); j++) {
                    if (languages[i].equals(names.get(i))) {
                        found = true;
                    }
                }
            }
            
            if (!found) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;                
            }
        }
      
        try {
        	           
        	Reader reader = context.getScriptSource();
            if (reader == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            engine.put("javax.script.filename", request.getRequestURI());
            engine.put("Request", context.getRequest());
            engine.put("Response",context.getResponse());
            engine.put("Servlet", context.getServlet());
            engine.put("Context", context);
            
            retValue = engine.eval(reader, context);
            
            response.setContentType("text/html");
            if ((retValue != null) && (context.displayResults())) {
                response.getWriter().write(retValue.toString());
            }
            
            response.getWriter().close();
          } catch (ScriptException se) {
  
          	response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            throw new ServletException(se);
            
        } finally {
   
            releaseEngine(engine);
            context.release();
        }
	}
}
