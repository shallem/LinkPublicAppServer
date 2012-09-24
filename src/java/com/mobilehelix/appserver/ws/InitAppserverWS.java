/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.ws;

import com.mobilehelix.wsclient.common.WSResponse;
import java.io.IOException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

/**
 *
 * @author shallem
 */
@Stateless
@Path("/initas")
@PermitAll
public class InitAppserverWS {
    private static final Logger LOGGER = Logger
        .getLogger(InitAppserverWS.class.getName());
        
    @POST
    public byte[] DownloadCert(byte [] b) {
        int statusCode = WSResponse.FAILURE;
        String msg = null;
        byte[] jksb = null;
        
        try {
            if (client == null) {
                throw new WSException("InvalidClientError", new String[]{ "null" });
            }
        
            PersistenceManagerFactory pmf = factoryProvider.getEntityManagerFactoryForMaster();
            if (pmf == null) {
                throw new WSException("PersistenceErrorOccurred", new String[]{ "Persistence manager factory is null." });
            }
          
            try {
                GlobalSettings gs = settingsFacade.findSettings(pmf);
                if (gs == null) {
                    throw new WSException("GlobalSettingsNotFoundError");
                }

                // Get the private key and decrypt it.
                byte[] encryptedPrivKey = gs.getPrivkey();
                byte[] decryptedPrivKey = cKeyManager.DecryptSensitiveData(encryptedPrivKey);
                
                // Signing cert/key are the glassfish cert & key used by the server.
                X509Certificate signingCert = CertificateManager.decodeX509Certificate(gs.getCertificate());
                PrivateKey signingKey = KeyManager.decodePrivateKey(decryptedPrivKey);
        
                // Extract the issuerDn from the signing cert.
                String issuerDn = signingCert.getIssuerX500Principal().getName();
        
                if (serverType.equals("gateway")) {
                    // Find the gateway with the given IP address.
                    Gateway g = gatewayFacade.findGatewayByName(serverName, pmf);
                    if (g == null) {
                        throw new WSException("GatewayIPNotFoundError", new String[]{ serverName });
                    }
                    
                    jksb = gatewayFacade.createAndPackageCerts(g, signingKey, issuerDn, client, serverName, signingCert, storepass, pmf);
                } else if (serverType.equals("appserver")) {
                    // Find the application server with the given IP address.
                    ApplicationServer as = asFacade.findAppServerByName(serverName, pmf);
                    if (as == null) {
                        throw new WSException("AppServerIPNotFoundError", new String[]{ serverName });
                    }
                    jksb = asFacade.createAndPackageCerts(as, signingKey, issuerDn, client, serverName, signingCert, storepass, pmf);
                } else {
                    throw new WSException("InvalidServerType", new String[]{ serverType });
                }
                
                statusCode = WSResponse.SUCCESS;
                msg = "Success";
            } catch(com.mobilehelix.security.MHSecurityException se) {
                msg = se.getLocalizedMessage();
                statusCode = WSResponse.FAILURE;
            } catch (FacadeException fe) {
                Throwable t = fe.getCause();
                if (t instanceof JDOException) {
                    WSException.JDOToWSException((JDOException)t);
                } else {
                    msg = fe.getMessage();
                    statusCode = WSResponse.FAILURE;
                }
            }
        } catch(WSException wse) {
            msg = wse.getLocalizedMessage();
            statusCode = WSResponse.FAILURE;
        }
        
        GatewayCertificateResponse gcr = new GatewayCertificateResponse(statusCode, msg, jksb);
        try {
            return gcr.toBson();
        } catch (IOException ioe) {
            LOGGER.log(Level.SEVERE, "Failed to serialize gateway cert response.", ioe);
        }
        return null;
    }
}
