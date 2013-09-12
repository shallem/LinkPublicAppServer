/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.jsf;

import com.mobilehelix.appserver.errorhandling.AppserverSystemException;
import com.mobilehelix.appserver.session.Session;
import com.mobilehelix.appserver.session.SessionManager;
import com.mobilehelix.appserver.system.InitApplicationServer;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import javax.faces.FacesException;
import javax.faces.application.ResourceHandler;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author shallem
 */
public class InitFilter {

    private static final String NOINIT_URL = "/Dispatcher/faces/noinit.xhtml";
    private static final String NOAUTH_URL = "/Dispatcher/faces/denied.xhtml";
    private static final String NOAPP_URL = "/Dispatcher/faces/noconfig.xhtml";
    private static final String ERROR_URL = "/Dispatcher/faces/error.xhtml";
    private InitialContext ictx;
    protected InitApplicationServer appServerSettings;
    protected SessionManager sessionManager;

    public boolean checkInit(HttpServletRequest req,
            HttpServletResponse resp,
            FilterChain chain,
            int apptype) throws IOException, ServletException {
        String dstUrl = req.getRequestURL().toString();

        // See if we are already redirecting to an error page.
        if (dstUrl.startsWith("/Dispatcher")) {
            chain.doFilter(req, resp);
            return false;
        }

        // If this is a resource request, return.
        String rsrcPath = ResourceHandler.RESOURCE_IDENTIFIER;
        if (dstUrl.startsWith(req.getContextPath() + "/faces" + rsrcPath)
                || dstUrl.startsWith(req.getContextPath() + "/faces" + "/resources")) {
            chain.doFilter(req, resp);
            return false;
        }

        try {

            // Get a reference to the global settings object. If we get back null, then the
            // app server has not been initialized. Note that we only need
            // read-only access to this object.
            if (appServerSettings == null) {
                try {
                    ictx = new InitialContext();
                    java.lang.Object asObj =
                            ictx.lookup("java:global/InitApplicationServer");
                    appServerSettings =
                            (InitApplicationServer) asObj;

                    java.lang.Object smgrObj =
                            ictx.lookup("java:global/SessionManager");
                    sessionManager =
                            (SessionManager) smgrObj;
                } catch (NamingException | ClassCastException ne) {
                    throw new AppserverSystemException(ne,
                            "JNDI lookup for the app server settings failed.",
                            "InternalError");
                }

                if (appServerSettings == null || sessionManager == null) {
                    this.sendError(req, resp, NOINIT_URL);
                    return false;
                }
            }

            if (!appServerSettings.isIsInitialized()) {
                // Need to initialize the app server.
                this.sendError(req, resp, NOINIT_URL);
                return false;
            }

            // Next check to see if the user is authorized.
            Session currentSession = null;
            try {
                currentSession = sessionManager.getSessionForRequest(req);
            } catch (AppserverSystemException ex) {
                this.sendError(req, resp, ERROR_URL, ex.getLocalizedMessage());
                return false;
            }

            /* If access is authorized, currentSession should be non-null. */
            if (currentSession == null) {
                this.sendError(req, resp, NOAUTH_URL);
                return false;
            }

            // Next, make sure the application exists.
            if (!currentSession.findApplication(req, apptype)) {
                this.sendError(req, resp, NOAPP_URL);
                return false;
            }

            // Finally, ask the session to process this request.
            currentSession.processRequest(req);
        } catch (AppserverSystemException ex) {
            this.sendError(req, resp, ERROR_URL, ex.getLocalizedMessage());
            return false;
        }
        return true;
    }

    protected String getBaseUrl(HttpServletRequest req) {
        String baseUrl = MessageFormat.format("{0}://{1}:{2}",
                new Object[]{req.getScheme(), req.getServerName(), Integer.toString(req.getServerPort())});
        return baseUrl;
    }

    protected void sendError(HttpServletRequest req,
            HttpServletResponse res,
            String errorBaseURL,
            String errMsg) throws UnsupportedEncodingException, IOException {
        String newUrl = this.getBaseUrl(req) + errorBaseURL
                + "?faces-redirect=true&error="
                + URLEncoder.encode(errMsg, "UTF-8");
        res.sendRedirect(newUrl);
    }

    protected void sendError(HttpServletRequest req,
            HttpServletResponse resp,
            String errorURL) {
        try {
            String newURL = this.getBaseUrl(req) + errorURL + "?faces-redirect=true";
            resp.sendRedirect(newURL); // calls responseComplete() according to JavaDocs
        } catch (IOException ex) {
            throw new FacesException(ex);
        }
    }
}
