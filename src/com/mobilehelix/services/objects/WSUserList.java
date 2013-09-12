/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.objects;

import java.util.List;

/**
 *
 * @author shallem
 */
public class WSUserList {
    private WSUser[] users;
    
    public WSUserList() {
        
    }
    
    public WSUserList(WSUser u) {
        this.users = new WSUser[1];
        this.users[0] = u;
    }
    
    public WSUserList(List<WSUser> userList) {
        users = new WSUser[userList.size()];
        users = userList.toArray(users);
    }

    public WSUser[] getUsers() {
        return users;
    }

    public void setUsers(WSUser[] users) {
        this.users = users;
    }
}
