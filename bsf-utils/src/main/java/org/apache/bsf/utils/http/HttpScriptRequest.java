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

    private final HttpScriptContext context;

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
