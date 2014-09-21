/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.objects;

import com.mobilehelix.services.interfaces.WSResponse;
import de.undercouch.bson4jackson.BsonFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 *
 * @author shallem
 */
public class ApplicationServerPushDump extends WSResponse {
    private List<ApplicationServerPushSession> pushSessions;
    
    public ApplicationServerPushDump() {
        this.pushSessions = new LinkedList<>();
    }

    public void addPushSession(ApplicationServerPushSession asps) {
        this.pushSessions.add(asps);
    }

    public List<ApplicationServerPushSession> getPushSessions() {
        return pushSessions;
    }
    
    @Override
    public byte[] toBson() throws IOException {
        //serialize data
        BsonFactory factory = new BsonFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
       
        JsonGenerator gen = factory.createJsonGenerator(baos);
        gen.writeStartArray();
        if (pushSessions != null && !pushSessions.isEmpty()) {
            for (ApplicationServerPushSession asps: pushSessions) {
                asps.serializeObject(gen);
            }
        }
        gen.writeEndArray();
        gen.close();
        return baos.toByteArray();
    }
    
    @Override
    protected void fromBson(byte[] b) throws IOException {
        JsonParser parser = WSResponse.InitFromBSON(b);
        JsonToken nxtToken = parser.nextToken();
        while (nxtToken != JsonToken.END_OBJECT) {
            parser.nextToken();
            this.addPushSession(ApplicationServerPushSession.fromBson(parser));
            nxtToken = parser.nextToken();
        }
    }
    
    public static ApplicationServerPushDump createFromBson(byte[] b) throws IOException {
        ApplicationServerPushDump pdump = new ApplicationServerPushDump();
        pdump.fromBson(b);
        return pdump;
    }
}