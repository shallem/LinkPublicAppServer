/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.objects;

import java.io.IOException;
import java.text.MessageFormat;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 *
 * @author shallem
 */
public class WSUserDataTraffic {
    private String userid;
    private long totalSent;
    private long totalRecv;
    
    public WSUserDataTraffic(String userid,
            long totalSent,
            long totalRecv) {
        this.userid = userid;
        this.totalSent = totalSent;
        this.totalRecv = totalRecv;
    }

    public long getTotalRecv() {
        return totalRecv;
    }

    public long getTotalSent() {
        return totalSent;
    }

    public String getUserid() {
        return userid;
    }
    
    public void toBson(JsonGenerator gen) throws IOException {
	gen.writeStartObject();
        gen.writeFieldName("userid");
        gen.writeString(this.userid);
        gen.writeFieldName("sent");
        gen.writeNumber(this.totalSent);
        gen.writeFieldName("recv");
        gen.writeNumber(this.totalRecv);
        gen.writeEndObject();
    }
    
    public static WSUserDataTraffic fromBson(JsonParser parser) throws IOException {
        String userid = null;
        long totalSent = 0;
        long totalRecv = 0;
        
        // Input should be pointing to START_OBJECT token.
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = parser.getCurrentName();
            parser.nextToken();
            
            if ("userid".equals(fieldname)) {
                userid = parser.getText();
            } else if ("sent".equals(fieldname)) {
                totalSent = parser.getLongValue();
            } else if ("recv".equals(fieldname)) {
                totalRecv = parser.getLongValue();
            }
        }
        
        return new WSUserDataTraffic(userid, totalSent, totalRecv);
    }
    
    public void print() {
        String fmt = "USERID=''{0}'',SENT=''{1}'',RECV=''{2}''";
        MessageFormat mf = new MessageFormat(fmt);
        System.out.println(mf.format(new Object[]{ this.userid, Long.toString(this.totalSent), Long.toString(this.totalRecv) }));
    }
}
