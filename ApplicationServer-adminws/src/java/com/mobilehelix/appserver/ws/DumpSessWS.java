package com.mobilehelix.appserver.ws;

import com.mobilehelix.appserver.errorhandling.AppserverSystemException;
import com.mobilehelix.appserver.session.Session;
import com.mobilehelix.appserver.session.SessionManager;
import com.mobilehelix.appserver.system.InitApplicationServer;
import com.mobilehelix.security.MHSecurityException;
import com.mobilehelix.services.interfaces.WSResponse;
import com.mobilehelix.services.objects.ApplicationServerSessDump;
import com.mobilehelix.services.objects.ApplicationServerSession;
import com.mobilehelix.services.objects.CreateSessionRequest;
import com.mobilehelix.services.objects.GenericBsonResponse;
import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.RolesAllowed;
import javax.crypto.SecretKey;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Dumps all active push sessions. We validate the session ID prior to accepting this call.
 * 
 * @author shallem
 */
@Path("/dumpsess")
@RolesAllowed({"ApplicationServerUser", "Superuser"})
public class DumpSessWS {
    private static final Logger LOG = Logger.getLogger(DumpSessWS.class.getName());
    
    @Inject
    private SessionManager sessMgr;
    
    @Inject
    private InitApplicationServer initEJB;
    
    @GET
    @Produces("application/octet-stream")
    @Path("/get")
    public byte[] getSessions() {
        int statusCode;
        String msg;
        ApplicationServerSessDump ret = new ApplicationServerSessDump();
        SecretKey dumpKey = null;
        
        if (!initEJB.isIsInitialized()) {
            statusCode = WSResponse.FAILURE;
            msg = "Cannot dump sessions on the app server because it is not initialized.";
        } else {
            try {
                dumpKey = initEJB.getSessDumpKey();
                if (dumpKey == null) {
                    statusCode = WSResponse.FAILURE;
                    msg = "Cannot dump sessions on the app server because there is no dump key.";
                } else {
                    statusCode = WSResponse.SUCCESS;
                    msg = "Success";
                    
                    for (Session s : this.sessMgr.allSessions()) {
                        ret.addSession(s.generateDumpObject());
                    }
                }
            } catch (IOException ex) {
               LOG.log(Level.SEVERE, "Failed to get the sess dump key", ex);  
               statusCode = WSResponse.FAILURE;
               msg = "Failed to get the sess dump key. Please check the server logs.";
            } catch (ParseException ex) {
               LOG.log(Level.SEVERE, "Failed to get the sess dump key", ex);  
               statusCode = WSResponse.FAILURE;
               msg = "Failed to get the sess dump key. Please check the server logs.";
            } catch (MHSecurityException ex) {
               LOG.log(Level.SEVERE, "Failed to get the sess dump key: " + ex.getLocalizedMessage(), ex);  
               statusCode = WSResponse.FAILURE;
               msg = "Failed to get the sess dump key. Please check the server logs.";
            }
        }

        ret.setStatusCode(statusCode);
        ret.setMsg(msg);
        try {
            return ret.toBson(dumpKey);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Failed to serialize sess dump response.", ex);
        } catch (MHSecurityException mhs) {
            LOG.log(Level.SEVERE, "Failed to encrypt sess dump response.", mhs);
        }
        return null;
    }
    
    @POST
    @Consumes("application/octet-stream")
    @Produces("application/octet-stream")
    @Path("/restore")
    public byte[] restoreSessions(byte[] input) {
        int statusCode;
        String msg;
        GenericBsonResponse ret = null;
        
        try {
            SecretKey restoreKey = initEJB.getSessDumpKey();
            ApplicationServerSessDump req = ApplicationServerSessDump.createFromBson(input, restoreKey);
            String prevClientName = initEJB.getPrevClientName();
            
            // WE do not validate with the session ID here. Generally it is not necessary - b/c
            // (a) the client is already authenticated with a certificate, and (b) if the provided data
            // is full of bogus credentials then it really doesn't create any leak of sensitive information.
            // Creating such push sessions will fail when the attempting to authenticate with A-D.
            if (!initEJB.isIsInitialized()) {
                statusCode = WSResponse.NOINIT;
                msg = "Cannot restore push sessions on the app server because it is not initialized.";
            } else {
                statusCode = WSResponse.SUCCESS;
                msg = "Success";
                
                int nRestored = 0;
                for (ApplicationServerSession aps : req.getSessions()) {
                    try {
                        CreateSessionRequest creq = new CreateSessionRequest();
                        creq.setClient(aps.getClientid());
                        creq.setUserID(aps.getUserid());
                        creq.setUserEmail(aps.getUserEmail());
                        creq.setLegacyUserID(aps.getLegacyUserID());
                        creq.setDeviceID(aps.getDeviceID());
                        creq.setDeviceType(aps.getDeviceType());
                        creq.setPassword(aps.getPassword());
                        creq.setSessionKey(aps.getSessID());
                        creq.setAttributeMap(aps.getAttributeMap());
                        this.sessMgr.addSession(creq);
                        ++nRestored;
                    } catch (AppserverSystemException ex) {
                        LOG.log(Level.SEVERE, "Unable to restore session for {0}:{1}", new Object[] {
                            aps.getClientid(), aps.getUserid()
                        });
                        LOG.log(Level.SEVERE, "Unable to restore session.", ex);
                    }
                }
                LOG.log(Level.INFO, "Restored {0} sessions", nRestored);
            }
            initEJB.updateAppServerDumpKey();
        } catch(IOException ioe) {
            statusCode = WSResponse.FAILURE;
            msg = ioe.getMessage();
        } catch(MHSecurityException secEx) {
            LOG.log(Level.SEVERE, "Decryption error when restoring session data.", secEx);
            statusCode = WSResponse.FAILURE;
            msg = secEx.getLocalizedMessage();
        } catch (ParseException ex) {
            LOG.log(Level.SEVERE, "Failed to read or update session dump key", ex);
            statusCode = WSResponse.FAILURE;
            msg = ex.getMessage();
        }
        
        ret = new GenericBsonResponse(statusCode, msg);
        try {
            return ret.toBson();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Failed to serialize session restore response.", ex);
        }
        return null;
    }
}