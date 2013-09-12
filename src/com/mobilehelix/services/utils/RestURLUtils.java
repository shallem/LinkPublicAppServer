/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.services.utils;

import java.util.Properties;

/**
 *
 * @author shallem
 */
public class RestURLUtils {
    /* Constants for common REST URL parameters. */
    public static final String CLIENT_QUERY_PARAM = "client";
    public static final String USER_QUERY_PARAM = "user";
    public static final String APP_NAME_QUERY_PARAM = "appname";
    
    public static String encodeURLParameter(String input) {
        StringBuilder resultStr = new StringBuilder();
        for (char ch : input.toCharArray()) {
            if (isUnsafe(ch)) {
                resultStr.append('%');
                resultStr.append(toHex(ch / 16));
                resultStr.append(toHex(ch % 16));
            } else {
                resultStr.append(ch);
            }
        }
        return resultStr.toString();
    }

    private static char toHex(int ch) {
        return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
    }

    private static boolean isUnsafe(char ch) {
        if (ch > 128 || ch < 0) {
            return true;
        }
        return " %$&+,/:;=?@<>#%".indexOf(ch) >= 0;
    }
    
    public static String GenQueryParams(Properties params) {
        StringBuilder sb = null;
        for (String s : params.stringPropertyNames()) {
            if (sb == null) {
                sb = new StringBuilder();
            } else {
                sb.append("&");
            }
            
            sb.append(RestURLUtils.encodeURLParameter(s));
            sb.append("=");
            sb.append(RestURLUtils.encodeURLParameter(params.getProperty(s)));
        }
        
        return sb.toString();
    }
}
