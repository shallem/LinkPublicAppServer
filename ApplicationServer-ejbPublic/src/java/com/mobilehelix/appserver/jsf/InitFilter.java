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
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private static final Logger LOG = Logger.getLogger(InitFilter.class.getName()); 

    private static final String NOINIT = "The application server has not been initialized. Please run the init-application-server ant target from the command line.";
    private static final String NOAUTH = "Authentication is denied.";
    private static final String ERROR = "The app server failed to respond to this request: {0}";
    private static final String META_VIEWPORT = "<meta name=\"viewport\" content=\"width =device-width,initial-scale=1,user-scalable=no\">";
    private InitialContext ictx;
    protected InitApplicationServer appServerSettings;
    protected SessionManager sessionManager;

    public boolean checkInit(HttpServletRequest req,
            HttpServletResponse resp,
            FilterChain chain,
            int apptype) throws IOException, ServletException {
        String dstUrl = req.getRequestURL().toString();

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
                    this.sendError(req, resp, NOINIT);
                    return false;
                }
            }

            if (!appServerSettings.isIsInitialized()) {
                // Need to initialize the app server.
                this.sendError(req, resp, NOINIT);
                return false;
            }

            // Next check to see if the user is authorized.
            Session currentSession = null;
            try {
                currentSession = sessionManager.getSessionForRequest(req);
            } catch (AppserverSystemException ex) {
                LOG.log(Level.SEVERE, "Session init failed with exception.", ex);
                this.sendError(req, resp, ERROR, ex.getLocalizedMessage());
                return false;
            }

            /* If access is authorized, currentSession should be non-null. */
            if (currentSession == null) {
                this.sendError(req, resp, NOAUTH);
                return false;
            }

            // Finally, ask the session to process this request.
            currentSession.processRequest(req, apptype);
        } catch (AppserverSystemException ex) {
            LOG.log(Level.SEVERE, "Fatal error in session init.", ex);
            this.sendError(req, resp, ERROR, ex.getLocalizedMessage());
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
            String msgTemplate,
            String errMsg) throws UnsupportedEncodingException, IOException {
        String fullMsg = MessageFormat.format(msgTemplate, new Object[]{ errMsg });
        this.sendError(req, res, fullMsg);
    }

    protected void sendError(HttpServletRequest req,
            HttpServletResponse resp,
            String msg) {
        try {
            resp.setContentType("text/html; charset=UTF-8");
            resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
            resp.getWriter()
                    .append("<html><head>").append(META_VIEWPORT).append("</head>")
                    .append("<body>")
                    .append(msg)
                    .append("</body></html>");
        } catch (IOException ex) {
            throw new FacesException(ex);
        }
    }
}
