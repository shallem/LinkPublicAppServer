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

/**
 * Intended to send both an object to synchronize and a second object to be passed to be handled 
 * manually by the client.
 * 
 * @author Seth
 * @param <T>
 */
public abstract class ParamObject<T> {
    private Object paramObject;
    
    @ClientData
    public Object getParamObject() {
        return paramObject;
    }

    public void setParamObject(Object paramObject) {
        this.paramObject = paramObject;
    }

    @ClientData
    public abstract T getSyncObject();

    public abstract void setSyncObject(T syncObject);
}
