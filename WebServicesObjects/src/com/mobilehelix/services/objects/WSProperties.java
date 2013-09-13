/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.objects;

import java.io.IOException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.Map.Entry;
import java.util.Properties;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 *
 * @author shallem
 */
public class WSProperties {

    private Properties props;

    public WSProperties() {
        this.props = new Properties();
    }

    public WSProperties(Properties p) {
        this.props = p;
    }

    public void addProperty(String key, String value) {
        this.props.setProperty(key, value);
    }
    
    public String getProperty(String key) {
        return this.props.getProperty(key);
    }

    public void propertyToBSON(JsonGenerator gen, 
            Object key, Object value) throws IOException {
        gen.writeFieldName("key");
        gen.writeString(key.toString());
        gen.writeFieldName("value");
        gen.writeString(value.toString());
    }
    
    public void toBson(JsonGenerator gen) throws IOException {
        gen.writeStartObject();
        gen.writeArrayFieldStart("properties");
        for (Entry<Object, Object> e : this.props.entrySet()) {
            gen.writeStartObject();
            this.propertyToBSON(gen, e.getKey(), e.getValue());
            gen.writeEndObject();
        }
        gen.writeEndArray();
        gen.writeEndObject();
    }
    
    public static WSProperties fromBson(JsonParser parser) throws IOException, ParseException {
        WSProperties newProps = new WSProperties();

        // When we start, parser is pointing to START_OBJECT token.
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            // Advance to the field value
            parser.nextToken();
            if (fieldName.equals("properties")) {
                // We are now pointing to a START_ARRAY token.
                
                String key = null;
                String value = null;
                // Skip to the START_OBJECT token.
                while(parser.nextToken() != JsonToken.END_ARRAY) {
                    // Skip the START_OBJECT token.
                    while (parser.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = parser.getCurrentName();
                        if (fieldName.equals("key")) {
                            key = parser.getText();
                        } else if (fieldName.equals("value")) {
                            value = parser.getText();
                            if (key == null) {
                                // Malformed key/value pair list. We got a value with no key!
                                throw new IOException("Malformed key/value property list in WSproperties BSON object.");
                            }
                        
                            newProps.addProperty(key, value);
                        }
                    }
                }
            }
        }

        return newProps;
    }

    public void print() {
        String fmt = "NAME=''{0}'',VALUE=''{1}''";
        MessageFormat mf = new MessageFormat(fmt);
        for (Entry<Object, Object> e : this.props.entrySet()) {
            System.out.print(mf.format(new Object[]{ e.getKey(), e.getValue() }));
        }
    }
}
