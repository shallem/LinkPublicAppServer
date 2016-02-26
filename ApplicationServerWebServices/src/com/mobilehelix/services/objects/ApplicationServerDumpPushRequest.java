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
public class ApplicationServerDumpPushRequest extends WSRequest {
    private byte[] serverSessId;
    
    public ApplicationServerDumpPushRequest() {
        
    }

    public byte[] getServerSessId() {
        return serverSessId;
    }

    public void setServerSessId(byte[] serverSessId) {
        this.serverSessId = serverSessId;
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
        gen.writeEndObject();
        gen.close();
        
        return baos.toByteArray();
    }
    
    public static ApplicationServerDumpPushRequest fromBson(byte[] b) throws IOException {
        ApplicationServerDumpPushRequest ret = new ApplicationServerDumpPushRequest();
        JsonParser parser = WSRequest.InitFromBSON(b);
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            // Move past the field name token.
            parser.nextToken();
            switch (fieldName) {
                case "sessid":
                    ret.setServerSessId((byte[])parser.getEmbeddedObject());
                    break;
            }
        }
        
        return ret;
    }
    
}
