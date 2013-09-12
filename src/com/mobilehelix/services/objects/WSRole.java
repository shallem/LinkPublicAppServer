/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.objects;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

/**
 *
 * @author shallem
 */
public class WSRole {

    private Long uniqueID;
    private String roleName;
    private String roleDescription;
    private List<WSPolicy> rolePolicies;
    private List<String> roleCapabilities;

    public WSRole() {
    }

    public WSRole(String roleName,
            String roleDescription) {
        this.roleName = roleName;
        this.roleDescription = roleDescription;
    }

    public WSRole(Long uniqueID) {
        this.uniqueID = uniqueID;
    }

    public Long getUniqueID() {
        return this.uniqueID;
    }

    private void setUniqueID(Long id) {
        this.uniqueID = id;
    }

    public String getRoleDescription() {
        return roleDescription;
    }

    private void setRoleDescription(String d) {
        this.roleDescription = d;
    }

    public String getRoleName() {
        return roleName;
    }

    private void setRoleName(String n) {
        this.roleName = n;
    }

    public List<WSPolicy> getRolePolicies() {
        return rolePolicies;
    }

    public void setRolePolicies(List<WSPolicy> rolePolicies) {
        this.rolePolicies = rolePolicies;
    }

    public List<String> getRoleCapabilities() {
        return roleCapabilities;
    }

    public void setRoleCapabilities(List<String> roleCapabilities) {
        this.roleCapabilities = roleCapabilities;
    }

    public void toBson(JsonGenerator gen) throws IOException {
        gen.writeStartObject();
        if (this.uniqueID != null) {
            gen.writeFieldName("id");
            gen.writeNumber(this.uniqueID);
        }
        if (this.roleName != null) {
            gen.writeFieldName("name");
            gen.writeString(this.roleName);
        }
        if (this.roleDescription != null) {
            gen.writeFieldName("desc");
            gen.writeString(this.roleDescription);
        }
        if (this.rolePolicies != null) {
            gen.writeArrayFieldStart("policies");
            for (WSPolicy p : this.rolePolicies) {
                p.toBson(gen);
            }
            gen.writeEndArray();
        }
        if (this.roleCapabilities != null) {
            gen.writeArrayFieldStart("capabilities");
            for (String capability : roleCapabilities) {
                gen.writeString(capability);
            }
            gen.writeEndArray();
        }
        gen.writeEndObject();
    }

    public static WSRole fromBson(JsonParser parser) throws IOException {
        WSRole wsr = new WSRole();

        // When we start, parser is pointing to START_OBJECT token.
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = parser.getCurrentName();
            // Advance to the field value
            parser.nextToken();
            switch (fieldName) {
                case "name":
                    wsr.setRoleName(parser.getText());
                    break;
                case "desc":
                    wsr.setRoleDescription(parser.getText());
                    break;
                case "policies":
                    LinkedList<WSPolicy> attachedPolicies = new LinkedList<>();
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        WSPolicy p = WSPolicy.fromBson(parser);
                        attachedPolicies.add(p);
                    }
                    wsr.setRolePolicies(attachedPolicies);
                    break;
                case "capabilities":
                    wsr.setRoleCapabilities(new LinkedList<String>());
                    while (parser.nextToken() != JsonToken.END_ARRAY) {
                        wsr.getRoleCapabilities().add(parser.getText());
                    }
                    break;
                case "id":
                    wsr.setUniqueID(parser.getLongValue());
                    break;
            }
        }
        return wsr;
    }

    public void print() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        if (this.roleName != null) {
            sb.append("NAME='").append(this.roleName).append("'");
            first = false;
        }
        if (this.roleDescription != null) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append("DESCRIPTION='").append(this.roleDescription).append("'");
        }
        if (this.uniqueID != null) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append("ID='").append(this.uniqueID).append("'");
        }
        if (this.rolePolicies != null && !this.rolePolicies.isEmpty()) {
            if (!first) {
                sb.append(",");
            }
            sb.append("POLICIES='");
            boolean firstPolicy = true;
            for (WSPolicy p : this.rolePolicies) {
                if (!firstPolicy) {
                    sb.append(",");
                }
                sb.append(p.getPolicyName());
                firstPolicy = false;
            }
            sb.append("'");
        }
        System.out.println(sb.toString());
    }
}
