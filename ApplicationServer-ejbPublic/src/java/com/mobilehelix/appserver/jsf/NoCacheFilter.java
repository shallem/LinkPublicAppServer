/*
 * Copyright 2013 Mobile Helix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mobilehelix.appserver.jsf;

import com.mobilehelix.appserver.errorhandling.AuthenticationException;
import java.io.IOException;
import javax.faces.application.ResourceHandler;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Default filter for setting cache-control headers. Should be installed in
 * web.xml for each application individually.
 *
 * @author shallem
 */
public class NoCacheFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        
        String reqURI = req.getRequestURI();
        String reqContextPath = req.getContextPath();
        String rsrcPath = ResourceHandler.RESOURCE_IDENTIFIER;
        
        if (reqURI.contains("javax.faces.resource")) {
            res.setHeader("Cache-Control", "public");
        } else if (req.getMethod().equals("GET")) {
            // Main app page and all AJAX. We set these to public so that we cache aggressively.
            // The device clients receive a URL from the Controller with a version number embedded.
            // Hence, we always want the main page to be cached.
            res.setHeader("Cache-Control", "public");
        } else {
            // All of our AJAX load commands are POST, not GET, so they are all uncacheable
            res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        }
        
        try {
            chain.doFilter(request, response);
        } catch (AuthenticationException ae) {
            // Force the user to login again.
            res.setStatus(407);
        }
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public void destroy() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
