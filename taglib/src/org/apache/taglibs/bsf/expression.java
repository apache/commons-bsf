/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.taglibs.bsf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTag;

import javax.servlet.jsp.*;
import java.security.*;

import org.apache.bsf.*;

public class expression extends bsftag implements BodyTag
{
    public int doEndTag() throws JspException {
        try {
            final BodyContent bodyOut = getBodyContent();

            JspWriter out = pageContext.getOut();
            if (bodyOut == null) return EVAL_PAGE;

            if (mgr == null) mgr = new BSFManager();
            final BSFEngine engine = mgr.loadScriptingEngine(language);

            try {
                register( mgr, "request"    , pageContext.getRequest());
                register( mgr, "response"   , pageContext.getResponse());
                register( mgr, "pageContext", pageContext);
                register( mgr, "application", pageContext.getServletContext());
                register( mgr, "out"        , out);
                register( mgr, "config"     , pageContext.getServletConfig());
                register( mgr, "page"       , pageContext.getPage());
                register( mgr, "exception"  , pageContext.getException());
                register( mgr, "session"    , pageContext.getSession());
            } 
            catch( Exception e ) {
                out.println(e.toString());
                e.printStackTrace();
            }

            Object result = null;
            try {
                result =
                    AccessController.doPrivileged(new PrivilegedExceptionAction() {
                                                      public Object run() throws Exception {
                                                          HttpServletRequest pnamer = 
                        (HttpServletRequest) 
                        pageContext.getRequest();
                    String pthinfo = 
                        (pnamer.getPathInfo() != null) ? 
                        pnamer.getPathInfo() : "";
                    return engine.eval (pnamer.getContextPath() +
                                        pnamer.getServletPath() +
                                        pthinfo,
                                        lineNo++, 0, bodyOut.getString());
                                                      }
                                                  });
            }
            catch (PrivilegedActionException prive) {
                throw (BSFException) prive.getException();
            }

            if (result != null) out.println(result.toString());
        } 
        catch( Exception e ) {
            System.out.println(e.toString());
            e.printStackTrace();
        }

        return EVAL_PAGE;
    }
}
