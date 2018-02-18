/*
 * Copyright 2015 Mobile Helix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.helix.mobile.model;

import java.io.IOException;

/**
 * Represents a client side update of a single field.
 * 
 * @author Seth
 */
public class Update implements JSONSerializable {
    
    private final String key;
    private final String field;
    private final String valueString;
    private final Boolean valueBoolean;
    
    /**
     * Construct an update
     * 
     * @param field
     * @param op
     * @param value 
     */
    public Update(String key, String field, String value) {
        this.key = key;
        this.field = field;
        
        this.valueString = value;
        this.valueBoolean = null;
    }

    public Update(String key, String field, Boolean value) {
        this.key = key;
        this.field = field;
        
        this.valueBoolean = value;
        this.valueString = null;
    }
    
    // Explicit serialization of fields
    // MUST be updated when new client visible fields are added !!!
    @Override
    public void toJSON(JSONGenerator jg) throws IOException {
        jg.writeStartObject();
        jg.writeStringField("key", this.key);
        jg.writeStringField("field", this.field);
        if (this.valueString != null) {
            jg.writeStringField("value", this.valueString);
            jg.writeStringField("type", "TEXT");
        } else if (this.valueBoolean != null) {
            jg.writeBooleanField("value", this.valueBoolean);
            jg.writeStringField("type", "BOOL");
        }
        jg.writeEndObject();
    } 
}
