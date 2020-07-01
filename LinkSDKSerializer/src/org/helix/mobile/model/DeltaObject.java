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
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.UUID;
import static org.helix.mobile.model.JSONSerializer.CHANGEID_FIELD_NAME;
import static org.helix.mobile.model.JSONSerializer.SCHEMA_TYPE_FIELD_NAME;
import static org.helix.mobile.model.JSONSerializer.TYPE_FIELD_NAME;

/**
 * Represents a "delta" object, which includes a list of objects to add to the client-side
 * data cache, a list of keys referencing objects to remove from the cache, and
 * a list of objects to update.
 * 
 * @author shallem
 */
public abstract class DeltaObject<BaseType> implements JSONSerializable {
    
    private final String changeID = UUID.randomUUID().toString();
    
    protected final LinkedList<BaseType> newItems = new LinkedList<>();
    protected final LinkedList<String> deleteIDs = new LinkedList<>();
    protected final TreeSet<String> deletedIDsSet = new TreeSet<>();
    protected final LinkedList<BaseType> updateItems = new LinkedList<>();
    protected final LinkedList<Update> fieldUpdates = new LinkedList<>();
    protected final LinkedList<Criteria> deleteSpec = new LinkedList<>();
    protected boolean isReverseOrder = false;
    
    public DeltaObject() {
        
    }
    
    /**
     * Return the list of adds.
     * @return 
     */
    @ClientData
    public abstract BaseType[] getAdds();
    
    public synchronized void addNew(BaseType c) {
        newItems.add(c);
    }
    
    /**
     * Return the list of keys to delete.
     * @return 
     */
    @ClientData
    public synchronized String[] getDeletes() {
        return this.deleteIDs.toArray(new String[this.deleteIDs.size()]);
    }
    
    public synchronized void addDelete(String k) {
        if (!this.deletedIDsSet.contains(k)) {
            this.deletedIDsSet.add(k);
            this.deleteIDs.add(k);
        }
    }
    
    /**
     * Return the list of updates.
     * @return 
     */
    @ClientData
    public abstract BaseType[] getUpdates();
    
    public synchronized void addUpdate(BaseType c) {
        updateItems.add(c);
    }

    /**
     * Return a query criteria to be used on the client to select a set of items to delete. Functions
     * as either an alternative or in addition to an explicit delete list.
     * @return 
     */
    @ClientData
    public synchronized Criteria[] getDeleteSpec() {
        return this.deleteSpec.toArray(new Criteria[this.deleteSpec.size()]);
    }
    
    public synchronized void addDeleteSpec(Criteria c) {
        this.deleteSpec.add(c);
    }
    
    /**
     * Update a single field.
     * @return 
     */
    @ClientData
    public synchronized Update[] getFieldUpdates() {
        return this.fieldUpdates.toArray(new Update[this.fieldUpdates.size()]);
    }
    
    public synchronized void addFieldUpdate(Update u) {
        this.fieldUpdates.add(u);
    }
    
    @ClientData
    public String getChangeID() {
        return changeID;
    }
    
    public synchronized boolean isEmpty() {
        return this.newItems.isEmpty() && 
                this.deleteIDs.isEmpty() && 
                this.updateItems.isEmpty() && 
                this.deleteSpec.isEmpty() && 
                this.fieldUpdates.isEmpty();
    }
    
    public void setReverseOrder() {
        this.isReverseOrder = true;
    }
    
    @Override
    public void toJSON(JSONGenerator jg) throws IOException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException {
        Class<?> c = this.getClass();

        jg.writeStartObject();
        
        // Mark as a delta object for the client code
        jg.writeFieldName(TYPE_FIELD_NAME);
        jg.writeNumber(1001);
        
        jg.writeStringField(CHANGEID_FIELD_NAME, this.changeID);
        
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
    
    @Override
    public String toString() {
        StringBuilder summary = new StringBuilder(1024);
        summary.append("[adds: ").append(this.newItems.size())
                .append(", mods: ").append(this.updateItems.size())
                .append(", deleteIDs: ").append(this.deleteIDs.size())
                .append(", field updates: ").append(this.fieldUpdates.size())
                .append("]");
        return summary.toString();
    }
}
