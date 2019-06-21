/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.objects;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.mobilehelix.security.AES.AESUtils;
import com.mobilehelix.security.MHSecurityException;
import com.mobilehelix.services.interfaces.WSResponse;
import de.undercouch.bson4jackson.BsonFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;

/**
 *
 * @author shallem
 */
public class ApplicationServerSessDump extends WSResponse {

    private static final Logger LOG = Logger.getLogger(ApplicationServerSessDump.class.getName());
    
    private List<ApplicationServerSession> allSessions;
    
    public ApplicationServerSessDump() {
        this.allSessions = new LinkedList<>();
    }

    public void addSession(ApplicationServerSession asps) {
        this.allSessions.add(asps);
    }

    public List<ApplicationServerSession> getSessions() {
        return allSessions;
    }
    
    public byte[] toBson(SecretKey dumpKey) throws IOException, MHSecurityException {
        //serialize data
        BsonFactory factory = new BsonFactory();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
       
        try (JsonGenerator gen = factory.createJsonGenerator(baos)) {
            gen.writeStartObject();
            super.toBson(gen);
            if (dumpKey != null) {
                // Only reveal this data when we can safely encrypt it.
                gen.writeArrayFieldStart("sess");
                if (allSessions != null && !allSessions.isEmpty()) {
                    for (ApplicationServerSession asps: allSessions) {
                        asps.serializeObject(gen);
                    }
                }
                gen.writeEndArray();
            }
            gen.writeEndObject();
        }
        return AESUtils.EncryptSensitiveData(baos.toByteArray(), dumpKey);
        //return baos.toByteArray();
    }
    
    @Override
    protected void fromBson(byte[] b) throws IOException {
        long nIters = 0;
        long nSess = 0;
        
        try (JsonParser parser = WSResponse.InitFromBSON(b)) {
            // Pointing at START_OBJECT after InitFromBSON. Move forward to either kEY_NAME or END_OBJECT
            try {
                while (parser.nextToken() != JsonToken.END_OBJECT) {
                    ++nIters;
                    // Pointing at field name.
                    String fieldName = parser.getCurrentName();
                    // Move past field name.
                    LOG.log(Level.FINE, "1: {0}", parser.getCurrentToken().toString());
                    parser.nextToken();
                    switch(fieldName) {
                        case "sess":
                            // Pointing at START_ARRAY. Next is either (a) END_ARRAY (empty), or (b) START_OBJECT
                            LOG.log(Level.FINE, "2: {0}", parser.getCurrentToken().toString());
                            while(parser.nextToken() != JsonToken.END_ARRAY) {
                                LOG.log(Level.FINE, "3: {0}", parser.getCurrentToken().toString());
                                // Parse starting at START_OBJECT
                                this.addSession(ApplicationServerSession.fromBson(parser));
                                // Pointint at END_OBJECT
                                LOG.log(Level.FINE, "4: {0}", parser.getCurrentToken().toString());
                                ++nSess;
                            }
                            // Pointing at END_ARRAY
                            break;
                        default:
                            // Advance past the value we are going to ignore (status/msg).
                            break;
                    }
                    // Pointing at last token in the value of the outer field. Move forward to next KEY_NAME or END_OBJECT
                    LOG.log(Level.FINE, "11: {0}", parser.getCurrentToken().toString());
                }
            } finally {
                LOG.log(Level.INFO, "Unpacked session dump with nIters: {0}, nSess: {1}", new Object[] {
                    nIters,
                    nSess
                });
            }
        }
    }
    
    public static ApplicationServerSessDump createFromBson(byte[] encryptedData, SecretKey restoreKey) throws IOException, MHSecurityException {
        byte[] b = AESUtils.DecryptSensitiveData(encryptedData, restoreKey);
        //byte[] b = encryptedData;
        ApplicationServerSessDump pdump = new ApplicationServerSessDump();
        pdump.fromBson(b);
        return pdump;
    }
    
    @Override
    public byte[] toBson() throws IOException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}