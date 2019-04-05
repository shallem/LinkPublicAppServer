/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.ws;

import com.mobilehelix.services.objects.GetLogRequest;
import com.mobilehelix.webutils.logutils.LogUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DeflaterInputStream;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.PUT;
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
@Path("/getlog")
@RolesAllowed({"ApplicationServerUser", "Superuser"})
public class GetLogWS {
    private static final Logger LOG = Logger.getLogger(GetLogWS.class.getName());
    
    // Glassfish Instance Root folder system variable
    private static String glassfishInstanceRootPropertyName = "com.sun.aas.instanceRoot";
    
    @PUT
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
 
            final File tmpConcat = LogUtils.concatGlassfishLogs(req.getnBytes(), 7, instanceRoot);
            final FileInputStream concatInputStream = new FileInputStream(tmpConcat);
            final DeflaterInputStream zippedServerLog = new DeflaterInputStream(concatInputStream);
            
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
                        concatInputStream.close();
                        tmpConcat.delete();
                    }
                }
            }).build();
        } catch(IOException ioe) {
            LOG.log(Level.SEVERE, "Failed to read the app server log.", ioe);
            return Response.serverError().build();
        }        
    }
}
