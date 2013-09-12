/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.objects;

import com.mobilehelix.services.interfaces.WSResponse;
import de.undercouch.bson4jackson.BsonFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;

/**
 * Returned by the app server in answer to a ping request received from a gateway.
 * 
 * @author shallem
 */
public class ApplicationServerPingResponse extends WSResponse {
   
    /**
     * Unique ID of this server.
     */
    private Long serverID;
    
    /**
     * Number of active sessions on this app server.
     */
    private int sessionCt;
    
    public ApplicationServerPingResponse(int status, String msg) {
        super(status, msg);
        serverID = (long)-1;
        sessionCt = 0;
    }

    public Long getServerID() {
        return serverID;
    }

    public void setServerID(Long serverID) {
        this.serverID = serverID;
    }

    public int getSessionCt() {
        return sessionCt;
    }

    public void setSessionCt(int sessionCt) {
        this.sessionCt = sessionCt;
    }
    
    @Override
    public byte[] toBson() throws IOException {
        //serialize data
        BsonFactory factory = new BsonFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        JsonGenerator gen = factory.createJsonGenerator(baos);
        gen.writeStartObject();
        super.toBson(gen);
        gen.writeFieldName("id");
        gen.writeNumber(serverID);
        gen.writeFieldName("ct");
        gen.writeNumber(sessionCt);
        gen.close();
        return baos.toByteArray();
    }

    @Override
    protected void fromBson(byte[] b) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
