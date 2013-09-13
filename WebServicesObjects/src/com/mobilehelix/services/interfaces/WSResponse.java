package com.mobilehelix.services.interfaces;

import de.undercouch.bson4jackson.BsonFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;

/**
 * Implements generic interface for all web services responses. This object and its
 * descendants can be serialized into BSON form. The field-value pairs in this object
 * are:
 * 
 * <table>
 * <thead><tr><th>Field Name</th><th>Field Type</th><th>Comments</th></thead>
 * <tr><td>status</td><td>Integer</td><td>Standard values of 0 for success, -1 for failure.</td></tr>
 * <tr><td>msg</td><td>String</td><td>Descriptive message.</td></tr>
 * </table>
 *
 * @author Seth Hallem
 */
public abstract class WSResponse {
    
    /**
     * Status code indicating that the web service call ended in success. The value
     * of this status code is 0.
     */
    public static final int SUCCESS = 0;
    
    /**
     * Status code indicating that the web service call ended in failure. The value of
     * this status code is -1.
     */
    public static final int FAILURE = -1;
    
    /**
     * Field name in a bson response inherited from this class used for a status code
     * indicating the result of the service request.
     */
    public static final String STATUS_FIELD_NAME = "status";
    
    /**
     * Field name in a bson response inherited from this class used for a message to
     * indicate the results of the service. Depending on the service, this message
     * may be localized. Consult service-specific documentation.
     */
    public static final String MSG_FIELD_NAME = "msg";
    
    protected int statusCode;
    protected String msg;
    
    protected WSResponse() {
    }
    
    public WSResponse(int statusCode, String msg) {
        this.statusCode = statusCode;
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    /**
     * toBson
     *
     * Convert this WS request into a stream of binary data.
     */
    public abstract byte[] toBson() throws IOException;        

    /**
     * toBson(gen)
     * 
     * @param gen JsonGenerator that the child class is using to serialize.
     */
    protected void toBson(JsonGenerator gen) throws IOException {
        gen.writeFieldName("status");
        gen.writeNumber(this.statusCode);
        gen.writeFieldName("msg");
        gen.writeString(this.msg);
    }
    
    /**
     * fromBson(byte[] b)
     * 
     * Convert a byte array into an object.
     */
    protected abstract void fromBson(byte[] b) throws IOException;
    
    /**
     * InitFromBSON
     * 
     * Helper to prepare to deserialize
     */
    public static JsonParser InitFromBSON(byte[] data) throws IOException {
        BsonFactory factory = new BsonFactory();
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        JsonParser parser = factory.createJsonParser(bais);
        parser.nextToken();
        return parser;
    }
}
