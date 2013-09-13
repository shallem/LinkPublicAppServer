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

/**
 *
 * @author shallem
 */
public class ApplicationServerLoginRequest extends WSRequest {
    private String client;
    private String authID;
    private String authPassword;
    private Long delegateAppID;
    private byte[] sessId;
    
    public ApplicationServerLoginRequest(byte[] sessId,
            String client,
            String authID,
            String authPassword,
            Long delegateAppID) {
        this.sessId = sessId;
        this.client = client;
        this.authID = authID;
        this.authPassword = authPassword;
        this.delegateAppID = delegateAppID;
    }
    
    @Override
    public byte[] toBson() throws IOException {
        //serialize data
        BsonFactory factory = new BsonFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        JsonGenerator gen = factory.createJsonGenerator(baos);
        gen.writeStartObject();
        gen.writeFieldName("controllersessid");
        gen.writeBinary(this.sessId);
        gen.writeFieldName("client");
        gen.writeString(this.client);
        gen.writeFieldName("authid");
        gen.writeString(this.authID);
        gen.writeFieldName("password");
        gen.writeString(this.authPassword);
        gen.writeFieldName("appid");
        gen.writeNumber(this.delegateAppID);
        gen.writeEndObject();
        gen.close();
        return baos.toByteArray();
    }
}
