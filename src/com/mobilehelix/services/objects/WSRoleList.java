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
public class WSRoleList {
    private WSRole[] roles;
    
    public WSRoleList() {
        
    }

    public WSRoleList(List<WSRole> roleList) {
        this.roles = new WSRole[roleList.size()];
        this.roles = roleList.toArray(this.roles);
    }
    
    public WSRole[] getRoles() {
        return roles;
    }

    public void setRoles(WSRole[] roles) {
        this.roles = roles;
    }
}
