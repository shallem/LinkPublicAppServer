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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 *
 * @author shallem
 */
public class ApplicationServerPushDump extends WSResponse {

    private static final Logger LOG = Logger.getLogger(ApplicationServerPushDump.class.getName());
    
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
        //return baos.toByteArray();
    }
    
    @Override
    protected void fromBson(byte[] b) throws IOException {
        JsonParser parser = WSResponse.InitFromBSON(b);
        // Pointing at START_OBJECT after InitFromBSON. Move forward to either kEY_NAME or END_OBJECT
        JsonToken nxtToken = parser.nextToken();
        while (nxtToken != JsonToken.END_OBJECT) {
            // Pointing at field name.
            String fieldName = parser.getCurrentName();
            // Move past field name.
            LOG.log(Level.INFO, "1: {0}", parser.getCurrentToken().toString());
            parser.nextToken();
            switch(fieldName) {
                case "sess":
                    // Pointing at START_ARRAY. Next is either (a) END_ARRAY (empty), or (b) START_OBJECT
                    LOG.log(Level.INFO, "2: {0}", parser.getCurrentToken().toString());
                    while(parser.nextToken() != JsonToken.END_ARRAY) {
                        LOG.log(Level.INFO, "3: {0}", parser.getCurrentToken().toString());
                        // Parse starting at START_OBJECT
                        this.addPushSession(ApplicationServerPushSession.fromBson(parser));
                        // Pointint at END_OBJECT
                        LOG.log(Level.INFO, "4: {0}", parser.getCurrentToken().toString());
                    }
                    // Pointing at END_ARRAY
                    break;
                case "bg":
                    ConcurrentHashMap<String, ConcurrentHashMap<String, String>> bgData = new ConcurrentHashMap<>();
                    try {
                        // Pointing at START_ARRAY. Next is either (a) END_ARRAY (empty), or (b) START_OBJECT
                        LOG.log(Level.INFO, "5: {0}", parser.getCurrentToken().toString());
                        while(parser.nextToken() != JsonToken.END_ARRAY) {
                            LOG.log(Level.INFO, "6: {0}", parser.getCurrentToken().toString());
                            String entryKey = null;
                            ConcurrentHashMap<String, String> entryVal = new ConcurrentHashMap<>();
                            // Pointing at START_OBJECT or last value (on iterations >1). Next is either a field name or END_OBJECT
                            while (parser.nextToken() != JsonToken.END_OBJECT) {
                                LOG.log(Level.INFO, "7: {0}", parser.getCurrentToken().toString());
                                String entryField = parser.getCurrentName();
                                parser.nextToken(); // Move past field name.
                                switch(entryField) {
                                    case "key":
                                        // Pointing at VALUE
                                        entryKey = parser.getText();
                                        break;
                                    case "val":
                                        // Pointing at START_ARRAY. Next is either START_OBJECT or END_ARRAY.
                                        LOG.log(Level.INFO, "8: {0}", parser.getCurrentToken().toString());
                                        while (parser.nextToken() != JsonToken.END_ARRAY) {
                                            LOG.log(Level.INFO, "9: {0}", parser.getCurrentToken().toString());
                                            String subKey = null;
                                            String subVal = null;
                                            // Pointing at START_OBJECT/last value (on iterations >1). Next is either KEY_NAME or END_OBJECT
                                            while (parser.nextToken() != JsonToken.END_OBJECT) {
                                                String subField = parser.getCurrentName();
                                                parser.nextToken(); // Move past field name
                                                switch(subField) {
                                                    case "subKey":
                                                        subKey = parser.getText();
                                                        break;
                                                    case "subVal":
                                                        subVal = parser.getText();
                                                        break;
                                                }
                                            }
                                            // Pointing at END_OBJECT
                                            if (subKey != null && subVal != null) {
                                                entryVal.put(subKey, subVal);
                                            }
                                            LOG.log(Level.INFO, "10: {0}", parser.getCurrentToken().toString());
                                        }
                                        // Pointing at END_ARRAY
                                        break;
                                }
                            }
                            // Pointing at END_OBJECT
                            if (entryKey != null) {
                                bgData.put(entryKey, entryVal);
                            }
                        }
                        // Pointing at END_ARRAY
                        this.setbGPushData(bgData);
                    } catch(Exception ex) {
                        LOG.log(Level.INFO, "Failed to restore push session BG refresh data. Push sessions will be restored, but background refresh will not work.", ex);
                    }
                    break;
                default:
                    // Advance past the value we are going to ignore (status/msg).
                    parser.nextToken();
                    break;
            }
            // Pointing at last token in the value of the outer field. Move forward to next KEY_NAME or END_OBJECT
            LOG.log(Level.INFO, "11: {0}", parser.getCurrentToken().toString());
            nxtToken = parser.nextToken();
            LOG.log(Level.INFO, "12: {0}", parser.getCurrentToken().toString());
        }
    }
    
    public static ApplicationServerPushDump createFromBson(byte[] encryptedData, SecretKey restoreKey) throws IOException, MHSecurityException {
        byte[] b = AESUtils.DecryptSensitiveData(encryptedData, restoreKey);
        //byte[] b = encryptedData;
        ApplicationServerPushDump pdump = new ApplicationServerPushDump();
        pdump.fromBson(b);
        return pdump;
    }
    
    @Override
    public byte[] toBson() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}