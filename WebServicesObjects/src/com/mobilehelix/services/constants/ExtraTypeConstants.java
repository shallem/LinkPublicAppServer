/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.constants;

/**
 *
 * @author shallem
 */
public class ExtraTypeConstants {
    // Constants defining object types that extra categories are attached to.
    public static final int APPLICATION_EXTRAS = 1;
    public static final int USER_EXTRAS = 2;
    public static final int POLICY_EXTRAS = 3;
    
    // Constants defining the extra schema types.
    public static final int EXTRA_SCHEMA_ATTRIBUTE = 1;
    public static final int EXTRA_SCHEMA_POLICY = 2;
    
    // Constants defining data types for "extra" values, which are extensible
    // attribute lists attached to an object.
    
    // Integer extras.
    public static final int EXTRA_TYPE_INT = 0;
    
    // String extras.
    public static final int EXTRA_TYPE_STRING = 1;
    
    // Image extras.
    public static final int EXTRA_TYPE_IMAGE = 2;

    // Boolean extras.
    public static final int EXTRA_TYPE_BOOLEAN = 3;
    
    // URL extras.
    public static final int EXTRA_TYPE_URL = 4;
    
    // Picklist extras.
    public static final int EXTRA_TYPE_PICKLIST = 5;
    
    // PEM-encoded certificates.
    public static final int EXTRA_TYPE_PEM_CERT = 6;
 
    // Newline-delimited list of quoted strings.
    public static final int EXTRA_TYPE_STRING_LIST = 7;
    
    // Selection of multiple items from a list of choices.
    public static final int EXTRA_TYPE_MULTI_PICKLIST = 8;

    // String buffer for multi-line input.
    public static final int EXTRA_TYPE_STRING_BUFFER = 9;
    
    // IP address
    public static final int EXTRA_TYPE_IP_ADDRESS = 10;
}
