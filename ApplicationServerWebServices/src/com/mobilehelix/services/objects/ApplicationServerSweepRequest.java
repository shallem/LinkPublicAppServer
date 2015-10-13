/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.objects;

import com.mobilehelix.services.interfaces.WSRequest;
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
public class ApplicationServerSweepRequest extends WSRequest {
    private final byte[] serverSessId;
    private final List<byte[]> sessionKeysToDelete;
    
    public ApplicationServerSweepRequest(byte[] serverSessId,
            List<byte[]> toDelete) {
        this.serverSessId = serverSessId;
        this.sessionKeysToDelete = toDelete;
    }

    public byte[] getServerSessId() {
        return serverSessId;
    }
    
    public List<byte[]> getSessionKeysToDelete() {
        return sessionKeysToDelete;
    }
    
    @Override
    public byte[] toBson() throws IOException {
        //serialize data
        BsonFactory factory = new BsonFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        JsonGenerator gen = factory.createJsonGenerator(baos);
        gen.writeStartObject();
        gen.writeFieldName("sessid");
        gen.writeBinary(this.serverSessId);
        gen.writeArrayFieldStart("ids");
        for (byte[] key : this.sessionKeysToDelete) {
            gen.writeBinary(key);
        }
        gen.writeEndArray();
        gen.writeEndObject();
        gen.close();
        return baos.toByteArray();
    }
    
    public static ApplicationServerSweepRequest fromBson(byte[] b) throws IOException {
        List<byte[]> sessids = null;
        byte [] sessid = null;
        JsonParser parser = WSRequest.InitFromBSON(b);
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            // Move past the field name token.
            parser.nextToken();
            switch (fieldName) {
                case "ids":
                    sessids = new LinkedList<>();
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        byte[] todelete = (byte[])parser.getEmbeddedObject();
                        sessids.add(todelete);
                    }
                    break;
                case "sessid":
                    sessid = (byte[])parser.getEmbeddedObject();
                    break;
            }
        }
        return new ApplicationServerSweepRequest(sessid, sessids);
    }
}
