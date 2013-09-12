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
public class WSApplicationDataTraffic {
    private String appname;
    private long totalSent;
    private long totalRecv;
    
    public WSApplicationDataTraffic(String appname,
            long totalSent,
            long totalRecv) {
        this.appname = appname;
        this.totalSent = totalSent;
        this.totalRecv = totalRecv;
    }

    public long getTotalRecv() {
        return totalRecv;
    }

    public long getTotalSent() {
        return totalSent;
    }

    public String getAppname() {
        return appname;
    }
    
    public void toBson(JsonGenerator gen) throws IOException {
	gen.writeStartObject();
        gen.writeFieldName("appname");
        gen.writeString(this.appname);
        gen.writeFieldName("sent");
        gen.writeNumber(this.totalSent);
        gen.writeFieldName("recv");
        gen.writeNumber(this.totalRecv);
        gen.writeEndObject();
    }
    
    public static WSApplicationDataTraffic fromBson(JsonParser parser) throws IOException {
        String appname = null;
        long totalSent = 0;
        long totalRecv = 0;
        
        // Input should be pointing to START_OBJECT token.
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = parser.getCurrentName();
            parser.nextToken();
            
            if ("appname".equals(fieldname)) {
                appname = parser.getText();
            } else if ("sent".equals(fieldname)) {
                totalSent = parser.getLongValue();
            } else if ("recv".equals(fieldname)) {
                totalRecv = parser.getLongValue();
            }
        }
        
        return new WSApplicationDataTraffic(appname, totalSent, totalRecv);
    }
    
    public void print() {
        String fmt = "APPNAME=''{0}'',SENT=''{1}'',RECV=''{2}''";
        MessageFormat mf = new MessageFormat(fmt);
        System.out.println(mf.format(new Object[]{ this.appname, Long.toString(this.totalSent), Long.toString(this.totalRecv) }));
    }
}
