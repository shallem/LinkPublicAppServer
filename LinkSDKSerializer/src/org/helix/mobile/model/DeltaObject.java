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

/**
 * Represents a "delta" object, which includes a list of objects to add to the client-side
 * data cache, a list of keys referencing objects to remove from the cache, and
 * a list of objects to update.
 * 
 * @author shallem
 */
public interface DeltaObject {
    /**
     * Return the list of adds.
     */
    @ClientData
    public Object[] getAdds();
    
    /**
     * Return the list of keys to delete.
     */
    @ClientData
    public String[] getDeletes();
    
    /**
     * Return the list of updates.
     */
    @ClientData
    public Object[] getUpdates();

    /**
     * Retrun a query criteria to be used on the client to select a set of items to delete. Functions
     * as either an alternative or in addition to an explicit delete list.
     */
    @ClientData
    public Criteria getDeleteSpec();
}
