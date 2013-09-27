/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.objects;

import com.mobilehelix.services.constants.ExtraTypeConstants;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 *
 * @author shallem
 */
public class WSExtraGroup {
    private TreeSet<WSExtra> policyExtras;

    public WSExtraGroup() {
        this.policyExtras = new TreeSet<>();
    }
    
    public Collection<WSExtra> getPolicyExtras() {
        return policyExtras;
    }

    public void setPolicyExtras(List<WSExtra> policyExtras) {
        this.policyExtras.addAll(policyExtras);
    }
    
    public void addPolicyExtra(WSExtra e) {
        this.policyExtras.add(e);
    }

    public void toBson(JsonGenerator gen) throws IOException {
        gen.writeStartObject();
        gen.writeArrayFieldStart("extras");
        for (WSExtra e : this.getPolicyExtras()) {
            e.toBson(gen);
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }
    
    public static WSExtraGroup fromBson(JsonParser parser) throws IOException {
        WSExtraGroup ret = new WSExtraGroup();
        
        // Input should be pype.ointing to START_OBJECT token.
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldname = parser.getCurrentName();
            parser.nextToken();
            switch (fieldname) {
                case "extras":
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        WSExtra e = WSExtra.fromBson(parser);
                        ret.addPolicyExtra(e);
                    }
                    break;
            }
        }
        
        return ret;
    }
}
