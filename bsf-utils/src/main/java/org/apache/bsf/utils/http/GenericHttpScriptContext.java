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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.script.SimpleScriptContext;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GenericHttpScriptContext extends SimpleScriptContext implements     HttpScriptContext {

    public static final String[] defaultMethods = {"GET", "POST"};
    protected boolean disableScript = false;
    protected boolean displayResults = false;
    protected String scriptDir;
    protected String[] languages;
    protected String[] methods;
    protected HttpServletRequest request;
    protected HttpServletResponse response;
    protected Servlet servlet;
    protected boolean useSession = true;

    public GenericHttpScriptContext() {
        super();
    }

    public boolean disableScript() {
        return disableScript;

    }

    public boolean displayResults() {
        return displayResults;

    }

    public String[] getAllowedLanguages() {
        return languages;
    }

    public Object getAttribute(String key, Object value, int scope){

        switch (scope) {
            case HttpScriptContext.ENGINE_SCOPE:
                return request.getAttribute(key);
            case HttpScriptContext.SESSION_SCOPE:
                if (useSession()) {
                    return request.getSession().getAttribute(key);
                } else {
                    return null;
                }
            case HttpScriptContext.APPLICATION_SCOPE:
                return servlet.getServletConfig().getServletContext().getAttribute(key);
            default:
                return null;
        }
    }


    public void setAttribute(String key, Object value, int scope)
            throws IllegalArgumentException {

        switch (scope) {
            case HttpScriptContext.REQUEST_SCOPE:
                request.setAttribute(key, value);
                break;
            case HttpScriptContext.SESSION_SCOPE:
                if (useSession()) {
                    request.getSession().setAttribute(key, value);
                } else {
                    throw new IllegalArgumentException("Session is disabled");
                }
                break;
            case HttpScriptContext.APPLICATION_SCOPE:
                servlet.getServletConfig().getServletContext().setAttribute(
                    key, value);
                break;
            default:
                throw new IllegalArgumentException("Invalid scope");
        }
    }

    public void forward(String relativePath) throws ServletException, IOException {

        ServletContext context =  servlet.getServletConfig().getServletContext();

        String baseURI;
        String requestURI = request.getRequestURI();

        if(relativePath.startsWith("/")){
            baseURI = requestURI.substring(0, request.getContextPath().length());

        }else{
            baseURI = requestURI.substring(0, requestURI.lastIndexOf("/"));
        }

        context.getRequestDispatcher(baseURI+relativePath).forward(request, response);
    }


    public String[] getMethods() {
        return methods;
    }

    public HttpServletRequest getRequest() {
        return new HttpScriptRequest(this, request);        
    }

    public HttpServletResponse getResponse() {
        return new HttpScriptResponse(this, response);
    }

    public Reader getScriptSource() {

        String requestURI = request.getRequestURI();
        String resourcePath =
            requestURI.substring(request.getContextPath().length());

        if(scriptDir == null){
        // should I construct a reader by combing contextRoot+ resourcePath
        }else{
            String fullPath;
            if(scriptDir.endsWith("/") || resourcePath.startsWith("/")){
                fullPath = scriptDir + resourcePath;
            }else{
                fullPath = scriptDir+"/"+resourcePath;
            }
            try{
                return new FileReader(fullPath);                
            }catch(IOException ioe){
            }
        }

        return null;
    }

    public Servlet getServlet() {
        return servlet;
    }

    public void include(String relativePath) throws ServletException, IOException {
        ServletContext context =  servlet.getServletConfig().getServletContext();

        String baseURI;
        String requestURI = request.getRequestURI();

        if(relativePath.startsWith("/")){
            baseURI = requestURI.substring(0, request.getContextPath().length());

        }else{
            baseURI = requestURI.substring(0, requestURI.lastIndexOf("/"));
        }

        context.getRequestDispatcher(baseURI+relativePath).include(request, response);       
    }

    public void initialize(Servlet servlet, HttpServletRequest request,
            HttpServletResponse response) throws ServletException {

        this.request = request;
        this.response = response;
        this.servlet = servlet;

        ServletContext context = servlet.getServletConfig().getServletContext();

        scriptDir = context.getInitParameter("script-directory");

        if(scriptDir == null || !(new File(scriptDir).isDirectory())){
            throw new ServletException("Specifed script directory either does " +
                    "not exist or not a directory");
        }

        String disable = context.getInitParameter("script-disable");
        if(disable != null && disable.equals("true")){
            disableScript = true;
            return;
        }

        String session = context.getInitParameter("script-use-session");
        if(session != null && session.equals("false")){
            useSession = false;
        }

        String display = context.getInitParameter("script-display-results");
        if(display != null && display.equals("true")){
            displayResults = true;
        }

        String methodNames = context.getInitParameter("script-methods");
        if(methodNames != null){
            methods = methodNames.split(",");
        }else{
            methods = defaultMethods;
        }

        String languageNames = context.getInitParameter("allowed-languages");
        if(languageNames != null){
            languages = languageNames.split(",");
        }       
    }

    public void release() {
        disableScript = false;
        displayResults = false;
        useSession = true;
        servlet = null;
        request = null;
        response = null;
    }


    public boolean useSession() {
        return useSession;
    }

    public Writer getWriter() {
        try{
            return response.getWriter();
       }catch(IOException ioe){
               return null;
       }
    }
}
