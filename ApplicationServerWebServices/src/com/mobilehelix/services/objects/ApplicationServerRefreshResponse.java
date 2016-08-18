/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.objects;

import com.mobilehelix.services.interfaces.WSResponse;
import de.undercouch.bson4jackson.BsonFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 *
 * @author v3devel
 */
public class ApplicationServerRefreshResponse extends WSResponse {
    private byte[] payload;
    
    public ApplicationServerRefreshResponse(int statusCode,
            String msg,
            byte[] payload) {
        this.statusCode = statusCode;
        this.msg = msg;
        this.payload = payload;
    }
    
    public ApplicationServerRefreshResponse(int statusCode,
            String msg) {
        this.statusCode = statusCode;
        this.msg = msg;
        this.payload = null;
    }

    @Override
    public byte[] toBson() throws IOException {
        //serialize data
        BsonFactory factory = new BsonFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
       
        try (JsonGenerator gen = factory.createJsonGenerator(baos)) {
            super.toBson(gen);
            if (this.payload != null) {
                gen.writeBinaryField("payload", this.payload);
            }
        }
        return baos.toByteArray();
    }

    @Override
    protected void fromBson(byte[] b) throws IOException {
        JsonParser parser = WSResponse.InitFromBSON(b);
        JsonToken nxtToken = parser.nextToken();
        while (nxtToken != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            // Move past the field name.
            parser.nextToken();
            switch(fieldName) {
                case "payload":
                    this.payload = (byte[])parser.getEmbeddedObject();
                    break;
                case WSResponse.STATUS_FIELD_NAME:
                    this.statusCode = parser.getIntValue();
                    break;
                case WSResponse.MSG_FIELD_NAME:
                    this.msg = parser.getText();
                    break;
            }
            // Move past the field value.
            nxtToken = parser.nextToken();
        }
    }
}
