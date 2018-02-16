/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.objects;

import com.mobilehelix.security.AES.AESUtils;
import com.mobilehelix.security.MHSecurityException;
import com.mobilehelix.services.interfaces.WSResponse;
import de.undercouch.bson4jackson.BsonFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.SecretKey;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 *
 * @author shallem
 */
public class ApplicationServerPushDump extends WSResponse {
    private List<ApplicationServerPushSession> pushSessions;
    private ConcurrentHashMap<String, ConcurrentHashMap<String, String> > BGPushData;
    
    public ApplicationServerPushDump() {
        this.pushSessions = new LinkedList<>();
    }

    public void addPushSession(ApplicationServerPushSession asps) {
        this.pushSessions.add(asps);
    }

    public List<ApplicationServerPushSession> getPushSessions() {
        return pushSessions;
    }
    
    public ConcurrentHashMap<String, ConcurrentHashMap<String, String>> getBGPushData() {
        return BGPushData;
    }

    public void setbGPushData(ConcurrentHashMap<String, ConcurrentHashMap<String, String>> bGPushData) {
        this.BGPushData = bGPushData;
    }
    
    public byte[] toBson(SecretKey dumpKey) throws IOException, MHSecurityException {
        //serialize data
        BsonFactory factory = new BsonFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
       
        JsonGenerator gen = factory.createJsonGenerator(baos);
        gen.writeStartObject();
        super.toBson(gen);
        if (dumpKey != null) {
            // Only reveal this data when we can safely encrypt it.
            gen.writeArrayFieldStart("sess");
            if (pushSessions != null && !pushSessions.isEmpty()) {
                for (ApplicationServerPushSession asps: pushSessions) {
                    asps.serializeObject(gen);
                }
            }
            gen.writeEndArray();
            if (this.BGPushData != null) {
                gen.writeArrayFieldStart("bg");
                for (Entry<String, ConcurrentHashMap<String, String>> e : this.BGPushData.entrySet()) {
                    gen.writeStartObject();
                    gen.writeStringField("key", e.getKey());
                    gen.writeArrayFieldStart("val");
                    for (Entry<String, String> subE : e.getValue().entrySet()) {
                        gen.writeStartObject();
                        gen.writeStringField("subKey", subE.getKey());
                        gen.writeStringField("subVal", subE.getValue());
                        gen.writeEndObject();
                    }
                    gen.writeEndArray();
                    gen.writeEndObject();
                }
                gen.writeEndArray();
            }
        }
        gen.writeEndObject();
        gen.close();
        return AESUtils.EncryptSensitiveData(baos.toByteArray(), dumpKey);
    }
    
    @Override
    protected void fromBson(byte[] b) throws IOException {
        JsonParser parser = WSResponse.InitFromBSON(b);
        JsonToken nxtToken = parser.nextToken();
        while (nxtToken != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            switch(fieldName) {
                case "sess":
                    parser.nextToken(); // Move past START_ARRAY
                    while(parser.nextToken() != JsonToken.END_ARRAY) {
                        this.addPushSession(ApplicationServerPushSession.fromBson(parser));
                    }
                    break;
                case "bg":
                    ConcurrentHashMap<String, ConcurrentHashMap<String, String>> bgData = new ConcurrentHashMap<>();
                    parser.nextToken(); // Move past START_ARRAY
                    while(parser.nextToken() != JsonToken.END_ARRAY) {
                        String entryKey = null;
                        ConcurrentHashMap<String, String> entryVal = new ConcurrentHashMap<>();
                        while (parser.nextToken() != JsonToken.END_OBJECT) {
                            String entryField = parser.getCurrentName();
                            parser.nextToken(); // Move past field name.
                            switch(entryField) {
                                case "key":
                                    entryKey = parser.getText();
                                    break;
                                case "subVal":
                                    parser.nextToken(); // Move past start_array
                                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                                        String subKey = null;
                                        String subVal = null;
                                        while (parser.nextToken() != JsonToken.END_OBJECT) {
                                            String subField = parser.getCurrentName();
                                            parser.nextToken(); // Move past field name
                                            switch(subField) {
                                                case "subKey":
                                                    subKey = parser.getText();
                                                    break;
                                                case "subVal":
                                                    subVal = parser.getText();
                                            }
                                        }
                                        if (subKey != null && subVal != null) {
                                            entryVal.put(subKey, subVal);
                                        }
                                    }
                                    break;
                            }
                            if (entryKey != null) {
                                bgData.put(entryKey, entryVal);
                            }
                        }
                    }
                    break;
                default:
                    // Advance past the value we are going to ignore (status/msg).
                    parser.nextToken();
                    break;
            }
            nxtToken = parser.nextToken();
        }
    }
    
    public static ApplicationServerPushDump createFromBson(byte[] encryptedData, SecretKey restoreKey) throws IOException, MHSecurityException {
        byte[] b = AESUtils.DecryptSensitiveData(encryptedData, restoreKey);
        ApplicationServerPushDump pdump = new ApplicationServerPushDump();
        pdump.fromBson(b);
        return pdump;
    }
    
    @Override
    public byte[] toBson() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}