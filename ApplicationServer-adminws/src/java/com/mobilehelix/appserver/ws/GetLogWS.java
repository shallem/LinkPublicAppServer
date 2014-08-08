/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.ws;

import com.mobilehelix.services.objects.GetLogRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DeflaterInputStream;
import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author shallem
 */
@Stateless
@Path("/getlog")
@PermitAll
public class GetLogWS {
    private static final Logger LOG = Logger.getLogger(GetLogWS.class.getName());
    
    // Glassfish Instance Root folder system variable
    private static String glassfishInstanceRootPropertyName = "com.sun.aas.instanceRoot";
 
    // "log" sub-folder name
    private static String glassfishDomainLogsFolderName = "logs";
    
    @POST
    @Produces("application/octet-stream")
    public Response getLog(byte[] input) {
        GetLogRequest req = null;
        try {
            req = GetLogRequest.fromBson(input);
            
            // Instance Root folder
            final String instanceRoot = System.getProperty( glassfishInstanceRootPropertyName );
            if (instanceRoot == null)
            {
                throw new IOException( "Cannot find Glassfish instanceRoot. Is the com.sun.aas.instanceRoot system property set?" );
            }
 
            // Instance Root + /logs folder
            File logsFolder = new File( instanceRoot + File.separator + glassfishDomainLogsFolderName );
            File serverLogFile = new File( logsFolder, "server.log" );
            
            if (serverLogFile.exists()) {
                FileInputStream serverLogStream = new FileInputStream(serverLogFile);
                if (serverLogFile.length() > req.getnBytes()) {
                    serverLogStream.skip(serverLogFile.length() - req.getnBytes());
                }
                final DeflaterInputStream zippedServerLog = new DeflaterInputStream(serverLogStream);
                return Response.ok(new StreamingOutput() {
                    @Override
                    public void write(OutputStream out) throws IOException, WebApplicationException {
                        try {
                            IOUtils.copy(zippedServerLog, out);
                        } catch(Exception e) {
                            LOG.log(Level.SEVERE, "Failed to return zipped server log file.", e);
                            throw new WebApplicationException(Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
                        } finally {
                            out.close();
                        }
                    }
                }).build();
            }
        } catch(IOException ioe) {
            LOG.log(Level.SEVERE, "Failed to read the app server log.", ioe);
            return Response.serverError().build();
        }
        
        return Response.noContent().build();
    }
}
