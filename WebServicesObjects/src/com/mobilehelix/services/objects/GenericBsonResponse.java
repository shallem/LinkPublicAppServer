package com.mobilehelix.services.objects;

import com.mobilehelix.services.interfaces.WSResponse;
import de.undercouch.bson4jackson.BsonFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 *
 * @author shallem
 */
public class GenericBsonResponse extends WSResponse {
    public GenericBsonResponse(int statusCode, String msg) {
        super(statusCode, msg);
    }

    public GenericBsonResponse(byte[] b) throws IOException {
        if (b != null) {
            this.fromBson(b);
        } else {
            throw new IOException("Cannot create GenericBsonResponse from null data. Web service request failed.");
        }
    }
    
    @Override
    public byte[] toBson() throws IOException {
        //serialize data
        BsonFactory factory = new BsonFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        JsonGenerator gen = factory.createJsonGenerator(baos);
        gen.writeStartObject();
        super.toBson(gen);
        gen.close();
        return baos.toByteArray();
    }

    @Override
    final protected void fromBson(byte[] data) throws IOException {
	BsonFactory factory = new BsonFactory();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        JsonParser parser = factory.createJsonParser(bais);
        
        parser.nextToken();
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = parser.getCurrentName();
            parser.nextToken();
            switch (fieldname) {
                case WSResponse.STATUS_FIELD_NAME:
                    this.statusCode = parser.getIntValue();
                    break;
                case WSResponse.MSG_FIELD_NAME:
                    this.msg = parser.getText();
                    break;
            }
        }
    }
}
