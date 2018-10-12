/*
 * Copyright 2014 Mobile Helix, Inc.
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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import static org.helix.mobile.model.JSONSerializer.SCHEMA_TYPE_FIELD_NAME;
import static org.helix.mobile.model.JSONSerializer.TYPE_FIELD_NAME;

/**
 * Represents a "delta" object, which includes a list of objects to add to the client-side
 * data cache, a list of keys referencing objects to remove from the cache, and
 * a list of objects to update.
 * 
 * @author shallem
 */
public abstract class DeltaObject implements JSONSerializable {
    /**
     * Return the list of adds.
     * @return 
     */
    @ClientData
    public abstract Object[] getAdds();
    
    /**
     * Return the list of keys to delete.
     * @return 
     */
    @ClientData
    public abstract String[] getDeletes();
    
    /**
     * Return the list of updates.
     * @return 
     */
    @ClientData
    public abstract Object[] getUpdates();

    /**
     * Return a query criteria to be used on the client to select a set of items to delete. Functions
     * as either an alternative or in addition to an explicit delete list.
     * @return 
     */
    @ClientData
    public abstract Criteria[] getDeleteSpec();
    
    /**
     * Update a single field.
     * @return 
     */
    @ClientData
    public abstract Update[] getFieldUpdates();
    
    @Override
    public void toJSON(JSONGenerator jg) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        Class<?> c = this.getClass();

        jg.writeStartObject();
        
        // Mark as a delta object for the client code
        jg.writeFieldName(TYPE_FIELD_NAME);
        jg.writeNumber(1001);

        Method m = c.getMethod("getAdds", (Class[]) null);
        Class<?> returnType = m.getReturnType();
        jg.writeFieldName(SCHEMA_TYPE_FIELD_NAME);
        String cName = returnType.getComponentType().getName();

        jg.writeString(cName);
        JSONSerializer.iterateOverObjectField(jg, this, m);

        m = c.getMethod("getDeletes", (Class[]) null);
        JSONSerializer.iterateOverObjectField(jg, this, m);

        m = c.getMethod("getUpdates", (Class[]) null);
        JSONSerializer.iterateOverObjectField(jg, this, m);

        Criteria[] deleteSpec = this.getDeleteSpec();
        if (deleteSpec != null) {
            jg.writeArrayFieldStart("deleteSpec");
            for (Criteria crit : deleteSpec) {
                if (crit != null) {
                    crit.toJSON(jg);
                }
            }
            jg.writeEndArray();
        }

        Update[] fieldUpdates = this.getFieldUpdates();
        jg.writeArrayFieldStart("fieldUpdates");
        if (fieldUpdates != null) {
            for (Update u : fieldUpdates) {
                if (u != null) {
                    u.toJSON(jg);
                }
            }
        }
        jg.writeEndArray();
        
        jg.writeEndObject();
    }
}
