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
public class WSPolicyList {
    private WSPolicy[] policies;
    
    public WSPolicyList() {
        
    }
    
    public WSPolicyList(List<WSPolicy> policyList) {
        policies = new WSPolicy[policyList.size()];
        policies = policyList.toArray(policies);
    }

    public WSPolicy[] getPolicies() {
        return policies;
    }

    public void setPolicies(WSPolicy[] policies) {
        this.policies = policies;
    }
}
