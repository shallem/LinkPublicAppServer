/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.ws;

import com.mobilehelix.appserver.connections.MHConnectException;
import com.mobilehelix.appserver.constants.EmailConstants;
import com.mobilehelix.appserver.email.EmailOperations;
import com.mobilehelix.appserver.errorhandling.AppserverSystemException;
import com.mobilehelix.appserver.httputils.FormParsers;
import com.mobilehelix.appserver.session.Session;
import com.mobilehelix.appserver.session.SessionManager;
import com.mobilehelix.appserver.wsobjects.SendEmailRequest;
import com.mobilehelix.appserver.xmlobjects.GenericResponse;
import com.mobilehelix.wsclient.common.WSResponse;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;

/**
 *
 * @author shallem
 */
@Stateless
@Path("/email")
@PermitAll
public class EmailWS {
    private static final Logger LOG = Logger.getLogger(EmailWS.class.getName());
    
    @Context
    private HttpServletRequest request;
    
    @EJB
    private SessionManager sessionMgr;
    
    @POST
    @Path("/send")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("text/xml")
    public GenericResponse sendEmail(MultivaluedMap<String, String> form) {
        int statusCode = WSResponse.FAILURE;
        String msg = null;
     
        try {
            SendEmailRequest sendReq = new SendEmailRequest();
            sendReq.setToEmails(FormParsers.csvStringToList(form.getFirst("to")));
            sendReq.setToEmails(FormParsers.csvStringToList(form.getFirst("cc")));
            sendReq.setToEmails(FormParsers.csvStringToList(form.getFirst("bcc")));
            sendReq.setMsgSubject(form.getFirst("subject"));
            sendReq.setMsgBody(form.getFirst("body"));
            FormParsers.base64FilesToBytes(form.get("attachnames"), form.get("attach"), sendReq);
            sendReq.setComposeType(FormParsers.stringToInt(form.getFirst("composetype"), EmailConstants.COMPOSE_TYPE_NEW));
            
            switch (sendReq.getComposeType()) {
                case EmailConstants.COMPOSE_TYPE_NEW:
                    break;
                default:
                    sendReq.setReplyMsgId(form.getFirst("msgid"));
                    break;
            }
            
            Session currentSession = sessionMgr.getSessionForRequest(request);
            if (currentSession != null) {
                EmailOperations.sendMessage(sendReq, currentSession, request);
            } else {
                msg = "Could not find session.";
            }
        } catch(MHConnectException | AppserverSystemException e) {
            LOG.log(Level.SEVERE, "Send email failed with exception.", e);
            msg = e.getLocalizedMessage();
            statusCode = WSResponse.FAILURE;
            if (msg == null) {
                msg = "Unknown failure.";
            }
        }
        
        GenericResponse gr = new GenericResponse();
        gr.status = statusCode;
        gr.message = msg;
        return gr;
    }
}
