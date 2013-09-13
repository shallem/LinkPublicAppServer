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
public class ApplicationServerAppDeleteRequest extends WSRequest {
    byte[] serverSessId;
    private Long appID;
    
    public ApplicationServerAppDeleteRequest(byte[] serverSessId,
            Long appID) {
        this.serverSessId = serverSessId;
        this.appID = appID;
    }

    public byte[] getServerSessId() {
        return serverSessId;
    }

    public Long getAppID() {
        return appID;
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
        gen.writeFieldName("appid");
        gen.writeNumber(this.appID);
        gen.writeEndObject();
        gen.close();
        return baos.toByteArray();
    }
    
    public static ApplicationServerAppDeleteRequest fromBson(byte[] b) throws IOException {
        Long appID = null;
        byte [] sessid = null;
        JsonParser parser = WSRequest.InitFromBSON(b);
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            // Move past the field name token.
            parser.nextToken();
            switch (fieldName) {
                case "appID":
                    appID = parser.getLongValue();
                    break;
                case "sessid":
                    sessid = (byte[])parser.getEmbeddedObject();
                    break;
            }
        }
        return new ApplicationServerAppDeleteRequest(sessid, appID);
    }
}
