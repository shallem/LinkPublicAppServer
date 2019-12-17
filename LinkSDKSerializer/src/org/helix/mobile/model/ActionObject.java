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
public abstract class ActionObject implements JSONSerializable {
    /**
     * Return the list of adds.
     * @return 
     */
    @ClientData
    public abstract int getActionType();
    
    /**
     * Return the list of keys to delete.
     * @return 
     */
    @ClientData
    public abstract String[] getDeletes();
    
    /**
     * Return the list of target objects.
     * @return 
     */
    @ClientData
    public abstract Object[] getTargetObjects();
    
    @Override
    public void toJSON(JSONGenerator jg) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        Class<?> c = this.getClass();

        jg.writeStartObject();
        
        // Mark as a delta object for the client code
        jg.writeFieldName(TYPE_FIELD_NAME);
        jg.writeNumber(2001);

        jg.writeNumberField("actionType", this.getActionType());
        
        Method m = c.getMethod("getTargetObjects", (Class[]) null);
        Class<?> returnType = m.getReturnType();
        jg.writeFieldName(SCHEMA_TYPE_FIELD_NAME);
        String cName = returnType.getComponentType().getName();
        jg.writeString(cName);
        
        JSONSerializer.iterateOverObjectField(jg, this, m);
        
        m = c.getMethod("getDeletes", (Class[]) null);
        JSONSerializer.iterateOverObjectField(jg, this, m);
        
        jg.writeEndObject();
    }
}
