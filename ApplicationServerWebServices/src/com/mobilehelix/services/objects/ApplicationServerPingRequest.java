/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.objects;

import com.mobilehelix.services.interfaces.WSRequest;
import de.undercouch.bson4jackson.BsonFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 *
 * @author shallem
 */
public class ApplicationServerPingRequest extends WSRequest {
    private byte[] serverSessId;
    
    public ApplicationServerPingRequest(byte[] sessid) {
        this.serverSessId = sessid;
    }

    public byte[] getServerSessId() {
        return serverSessId;
    }

    @Override
    public byte[] toBson() throws IOException {
        //serialize data
        BsonFactory factory = new BsonFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        JsonGenerator gen = factory.createJsonGenerator(baos);
        gen.writeStartObject();
        gen.writeFieldName("controllersessid");
        gen.writeBinary(this.serverSessId);
        gen.writeEndObject();
        gen.close();
        return baos.toByteArray();
    }
    
    public static ApplicationServerPingRequest fromBson(byte[] b) throws IOException {
        byte[] sessid = null;
        JsonParser parser = WSRequest.InitFromBSON(b);
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            // Move past the field name token.
            parser.nextToken();
            switch (fieldName) {
                case "controllersessid":
                    sessid = (byte[])parser.getEmbeddedObject();
                    break;
            }
        }
        return new ApplicationServerPingRequest(sessid);
    }
}
