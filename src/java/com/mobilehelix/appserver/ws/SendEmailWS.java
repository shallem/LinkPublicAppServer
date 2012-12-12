/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.ws;

import com.mobilehelix.appserver.connections.MHConnectException;
import com.mobilehelix.appserver.email.EmailOperations;
import com.mobilehelix.appserver.errorhandling.AppserverSystemException;
import com.mobilehelix.appserver.session.Session;
import com.mobilehelix.appserver.session.SessionManager;
import com.mobilehelix.appserver.wsobjects.SendEmailRequest;
import com.mobilehelix.wsclient.common.GenericBsonResponse;
import com.mobilehelix.wsclient.common.WSResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

/**
 *
 * @author shallem
 */
@Stateless
@Path("/sendemail")
@PermitAll
public class SendEmailWS {
    private static final Logger LOG = Logger.getLogger(SendEmailWS.class.getName());
    
    @Context
    private HttpServletRequest request;
    
    @EJB
    private SessionManager sessionMgr;
    
    @POST
    public byte[] sendEmail(byte [] b) {
        int statusCode = WSResponse.FAILURE;
        String msg = null;
     
        SendEmailRequest sendReq = null;
        try {
            sendReq = SendEmailRequest.fromBson(b);
            Session currentSession = sessionMgr.getSessionForRequest(request);
            if (currentSession != null) {
                EmailOperations.sendMessage(sendReq, currentSession, request);
            } else {
                msg = "Could not find session.";
            }
        } catch(IOException | MHConnectException | AppserverSystemException e) {
            LOG.log(Level.SEVERE, "Send email failed with exception.", e);
            msg = e.getLocalizedMessage();
            statusCode = WSResponse.FAILURE;
            if (msg == null) {
                msg = "Unknown failure.";
            }
        }
        
        GenericBsonResponse gbr = new GenericBsonResponse(statusCode, msg);
        try {
            return gbr.toBson();
        } catch (IOException ioe) {
            LOG.log(Level.SEVERE, "Failed to serialize send email response.", ioe);
        }
        return null;
    }
}
