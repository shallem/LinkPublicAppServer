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
public class GetLogRequest extends WSRequest {
    private byte[] serverSessID;
    private int nBytes;
    
    public GetLogRequest() {
        
    }

    public byte[] getServerSessID() {
        return serverSessID;
    }

    public void setServerSessID(byte[] serverSessID) {
        this.serverSessID = serverSessID;
    }

    public int getnBytes() {
        return nBytes;
    }

    public void setnBytes(int nBytes) {
        this.nBytes = nBytes;
    }
    
    @Override
    public byte[] toBson() throws IOException {
        //serialize data
        BsonFactory factory = new BsonFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        JsonGenerator gen = factory.createJsonGenerator(baos);
        gen.writeStartObject();
        gen.writeFieldName("controllersessid");
        gen.writeBinary(serverSessID);
        gen.writeNumberField("max", nBytes);
        gen.writeEndObject();
        gen.close();
        return baos.toByteArray();
    }
    
    public static GetLogRequest fromBson(byte[] b) throws IOException {
        GetLogRequest ret = new GetLogRequest();
        JsonParser parser = WSRequest.InitFromBSON(b);
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            // Move past the field name token.
            parser.nextToken();
            switch (fieldName) {
                case "controllersessid":
                    ret.setServerSessID((byte[])parser.getEmbeddedObject());
                    break;
                case "max":
                    ret.setnBytes(parser.getIntValue());
                    break;
            }
        }
        return ret;
    }
}
