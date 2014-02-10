/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.objects;

import com.mobilehelix.services.interfaces.WSResponse;
import de.undercouch.bson4jackson.BsonFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import org.codehaus.jackson.JsonGenerator;

/**
 * Returned by the app server in answer to a ping request received from a gateway.
 * 
 * @author shallem
 */
public class ApplicationServerPingResponse extends WSResponse {
   
    public class ServerResponse {
        
        /**
         * Unique ID of this server.
         */
        private Long serverID;

        /**
         * Number of active sessions on this app server.
         */
        private int sessionCt;        
    
        public ServerResponse(Long serverID, int sessCt) {
            this.serverID = serverID;
            this.sessionCt = sessCt;
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
    }
    
    List<ServerResponse> serverResponses;
    
    public ApplicationServerPingResponse(int status, String msg) {
        super(status, msg);
        this.serverResponses = new LinkedList<>();
    }
    
    public void addServer(Long serverID, int sessCt) {
        this.serverResponses.add(new ServerResponse(serverID, sessCt));
    }
    
    @Override
    public byte[] toBson() throws IOException {
        //serialize data
        BsonFactory factory = new BsonFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        JsonGenerator gen = factory.createJsonGenerator(baos);
        gen.writeStartObject();
        super.toBson(gen);
        
        gen.writeArrayFieldStart("resps");
        for (ServerResponse resp : this.serverResponses) {
            gen.writeStartObject();
            gen.writeNumberField("status", 0);
            gen.writeNumberField("id", resp.getServerID());
            gen.writeNumberField("ct", resp.getSessionCt());
            gen.writeEndObject();
        }
        gen.writeEndArray();
        
        gen.writeEndObject();
        gen.close();
        return baos.toByteArray();
    }

    @Override
    protected void fromBson(byte[] b) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
