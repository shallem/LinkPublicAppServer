/*
 * Copyright 2013 Mobile Helix, Inc.
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
import static org.helix.mobile.model.JSONSerializer.TYPE_FIELD_NAME;
import static org.helix.mobile.model.JSONSerializer.serializeObjectFields;

/**
 * Intended to send both an object to synchronize and a second object to be passed to be handled 
 * manually by the client.
 * 
 * @author Seth
 * @param <T>
 */
public abstract class ParamObject<T, P> implements JSONSerializable {
    protected final P paramObject;
    protected final T syncObject;
    
    public ParamObject(T syncObject, P paramObject) {
        this.syncObject = syncObject;
        this.paramObject = paramObject;
    }
    
    @ClientData
    public Object getParamObject() {
        return paramObject;
    }

    @ClientData
    public Object getSyncObject() {
        return syncObject;
    }
    
    public void toJSON(JSONGenerator jg) throws IOException,
                IllegalAccessException,
                IllegalArgumentException,
                InvocationTargetException,
                NoSuchMethodException {
        jg.writeStartObject();

        /* Mark as a param object for the client code. */
        jg.writeNumberField(TYPE_FIELD_NAME, 1004);

        /* Write the param. */
        if (this.getParamObject() != null) {
            JSONSerializer.serializeObjectFields(jg, this.getParamObject(), "param");
        }
        if (this.getSyncObject() != null) {
            JSONSerializer.serializeObjectFields(jg, this.getSyncObject(), "sync");
        }

        jg.writeEndObject();
    }
}
