/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.objects;

import com.mobilehelix.services.interfaces.WSRequest;
import com.mobilehelix.services.interfaces.WSResponse;
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
public class ApplicationServerAppExtensionRequest extends WSRequest {
    private String extensionType;
    private String client;
    private Long appID;
    private String userID;
    private byte[] extensionKey;
    private byte[] serverSessID;

    public ApplicationServerAppExtensionRequest() {
        
    }
    
    public ApplicationServerAppExtensionRequest(String extensionType,
            String client,
            Long appID,
            String userID,
            byte[] extensionKey,
            byte[] sessID) {
        this.extensionType = extensionType;
        this.client = client;
        this.appID = appID;
        this.userID = userID;
        this.extensionKey = extensionKey;
        this.serverSessID = sessID;
    }

    public String getExtensionType() {
        return extensionType;
    }

    public void setExtensionType(String extensionType) {
        this.extensionType = extensionType;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public Long getAppID() {
        return appID;
    }

    public void setAppID(Long appID) {
        this.appID = appID;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public byte[] getExtensionKey() {
        return extensionKey;
    }

    public void setExtensionKey(byte[] extensionKey) {
        this.extensionKey = extensionKey;
    }

    public byte[] getServerSessID() {
        return serverSessID;
    }

    public void setServerSessID(byte[] serverSessID) {
        this.serverSessID = serverSessID;
    }
    
    @Override
    public byte[] toBson() throws IOException {
        BsonFactory factory = new BsonFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (JsonGenerator gen = factory.createJsonGenerator(baos)) {
            gen.writeStartObject();
            gen.writeStringField("client", this.client);
            gen.writeStringField("user", this.userID);
            gen.writeBinaryField("key", this.extensionKey);
            gen.writeNumberField("app", this.appID);
            gen.writeStringField("type", this.extensionType);
            gen.writeBinaryField("sessid", this.serverSessID);
            gen.writeEndObject();
        }
        return baos.toByteArray();
    }
    
    public static ApplicationServerAppExtensionRequest fromBson(byte[] data) throws IOException {
        ApplicationServerAppExtensionRequest ret = new ApplicationServerAppExtensionRequest();
        JsonParser parser = WSResponse.InitFromBSON(data);
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            // Move past field name token.
            parser.nextToken();
            switch (fieldName) {
                case "type":
                   ret.setExtensionType(parser.getText());
                   break;
                case "client":
                    ret.setClient(parser.getText());
                    break;
                case "user":
                    ret.setUserID(parser.getText());
                    break;
                case "app":
                    ret.setAppID(parser.getLongValue());
                    break;
                case "key":
                    ret.setExtensionKey((byte[])parser.getEmbeddedObject());
                    break;
                case "sessid":
                    ret.setServerSessID((byte[])parser.getEmbeddedObject());
                    break;
            }
        }
        return ret;
    }
}
