/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.push;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author shallem
 */
public class PushCompletion {
    
    private ConcurrentHashMap<String, ConcurrentLinkedQueue<PushReceiver> > userPushMap;
    private ConcurrentHashMap<String, PushReceiver> idMap;
    private String uniqueID;
    private String combinedUser;
    private PushReceiver newReceiver;
    
    public PushCompletion(ConcurrentHashMap<String, ConcurrentLinkedQueue<PushReceiver> > userPushMap,
            ConcurrentHashMap<String, PushReceiver> idMap,
            String uniqueID,
            String combinedUser,
            PushReceiver newReceiver) {
        this.userPushMap = userPushMap;
        this.idMap = idMap;
        this.uniqueID = uniqueID;
        this.combinedUser = combinedUser;
        this.newReceiver = newReceiver;
    }
    
    public void execute() {
        idMap.put(uniqueID, newReceiver);
        ConcurrentLinkedQueue<PushReceiver> receivers = this.userPushMap.get(combinedUser);
        if (receivers == null) {
            receivers = new ConcurrentLinkedQueue<>();
            this.userPushMap.put(combinedUser, receivers);
        } 
        receivers.add(newReceiver);
    }
}
