package org.helix.mobile.model;

/**
 *
 * @author Seth Hallem
 */
public interface JSONSerializerLockable {
    
    /**
     * Lock this object before serializing.
     */
    public void lock();
    
    /**
     * Unlock this object when serializing is done.
     */
    public void unlock();
}
