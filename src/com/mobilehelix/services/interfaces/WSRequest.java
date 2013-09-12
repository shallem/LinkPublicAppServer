package com.mobilehelix.services.interfaces;

import de.undercouch.bson4jackson.BsonFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.codehaus.jackson.JsonParser;

/**
 * WSRequest
 *
 * Implements generic interface for all web services requests.
 *
 * @author Seth Hallem
 */
public abstract class WSRequest {

    /**
     * toBson
     *
     * Convert this WS request into a stream of binary data.
     */
    public abstract byte[] toBson() throws IOException;        
    
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
