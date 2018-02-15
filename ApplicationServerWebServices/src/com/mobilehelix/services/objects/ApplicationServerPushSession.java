/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.objects;

import com.mobilehelix.security.AES.AESUtils;
import com.mobilehelix.security.MHSecurityException;
import com.mobilehelix.services.interfaces.WSRequest;
import com.mobilehelix.services.interfaces.WSResponse;
import de.undercouch.bson4jackson.BsonFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.SecretKey;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 *
 * @author shallem
 */
public class ApplicationServerPushSession extends WSRequest {
    private String uniqueID;
    private String clientid;
    private String userid;
    private String password;
    private String deviceType;
    private Long appID;
    
    public ApplicationServerPushSession() {
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public String getClientid() {
        return clientid;
    }

    public void setClientid(String clientid) {
        this.clientid = clientid;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public Long getAppID() {
        return appID;
    }

    public void setAppID(Long appID) {
        this.appID = appID;
    }
    
    @Override
    public byte[] toBson() throws IOException {
        //serialize data
        BsonFactory factory = new BsonFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
       
        JsonGenerator gen = factory.createJsonGenerator(baos);
        this.serializeObject(gen);
        gen.close();
        return baos.toByteArray();        
    }
    
    public void serializeObject(JsonGenerator gen) throws IOException {
        gen.writeStartObject();
        // write out fields.
        gen.writeStringField("id", this.uniqueID);
        gen.writeStringField("client", this.clientid);
        gen.writeStringField("user", this.userid);
        gen.writeStringField("password", this.password);
        gen.writeStringField("device", this.deviceType);
        gen.writeNumberField("appid", appID);
        gen.writeEndObject();
    }
    
    public static ApplicationServerPushSession fromBson(byte[] data) throws IOException {
        JsonParser parser = WSResponse.InitFromBSON(data);
        return ApplicationServerPushSession.fromBson(parser);
    }

    public static ApplicationServerPushSession fromBson(JsonParser parser) throws IOException {
        ApplicationServerPushSession asps = new ApplicationServerPushSession();
        JsonToken nxtTok = parser.nextToken();
        while (nxtTok != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            // Move past field name token.
            nxtTok = parser.nextToken();
            switch (fieldName) {
                case "id":
                    asps.setUniqueID(parser.getText());
                    break;
                case "client":
                    asps.setClientid(parser.getText());
                    break;
                case "user":
                    asps.setUserid(parser.getText());
                    break;
                case "password":
                    asps.setPassword(parser.getText());
                    break;
                case "device":
                    asps.setDeviceType(parser.getText());
                    break;
                case "appid":
                    asps.setAppID(parser.getLongValue());
                    break;
            }
            nxtTok = parser.nextToken();
        }

        return asps;
    }
}
