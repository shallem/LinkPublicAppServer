package org.helix.mobile.model;

import java.io.IOException;

/**
 *
 * @author frederic
 */
public class GlobalFilterField {
    
    private final String displayName;
    private final int[] intValues;
    private final String[] stringValues;
    private final String[] valueNames;

    public GlobalFilterField(String displayName,
            int[] intValues,
            String[] stringValues,
            String[] valueNames) {
        this.displayName = displayName;
        this.intValues = intValues;
        this.stringValues = stringValues;
        this.valueNames = valueNames;
    }

    public void serialize(JSONGenerator gen) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("display", displayName);

        gen.writeArrayFieldStart("values");
        if (this.intValues != null) {
            for (int i : intValues) {
                gen.writeString(Integer.toString(i));
            }
        } else if (this.stringValues != null) {
            for (String s : this.stringValues) {
                gen.writeString(s);
            }
        }
        gen.writeEndArray();

        if (this.valueNames != null) {
            gen.writeArrayFieldStart("valueNames");
            for (String s : this.valueNames) {
                gen.writeString(s);
            }
            gen.writeEndArray();
        }

        gen.writeEndObject();
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public int[] getIntValues() {
        return this.intValues;
    }

    public String[] getStringValues() {
        return this.stringValues;
    }

    public String[] getValueNames() {
        return this.valueNames;
    }
    
    
}
