package com.mobilehelix.appserver.httputils;

import com.mobilehelix.appserver.wsobjects.SendEmailRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.ws.rs.core.MultivaluedMap;
import sun.misc.BASE64Decoder;

/**
 *
 * @author shallem
 */
public class FormParsers {
    
    public static List<String> csvStringToList(String s) {
        if (s == null ||
                s.isEmpty()) {
            return new LinkedList<>();
        }
        
        String[] emailList = s.split("[,]");
        List<String> ret = new ArrayList<>(emailList.length);
        int i = 0;
        for (i = 0; i < emailList.length; ++i) {
            String a = emailList[i].trim();
            if (!a.isEmpty()) {
                ret.add(a);
            }
        }
        
        return ret;
    }
    
    public static Integer stringToInt(String s, int defaultValue) {
        if (s == null ||
                s.isEmpty()) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(s);
        } catch(NumberFormatException nfe) {
            return defaultValue;
        }
    }
    
    public static void base64FilesToBytes(List<String> attachNames,
            List<String> attachMimeTypes,
            List<String> b64Attachments,
            SendEmailRequest req) {
        if (b64Attachments == null ||
                b64Attachments.isEmpty()) {
            return;
        }
        
        BASE64Decoder b64Decode = new BASE64Decoder();
        List<SendEmailRequest.Attachment> ret = new LinkedList<>();
        int idx = 0;
        for (String b64File : b64Attachments) {
            try {
                byte[] f = b64Decode.decodeBuffer(b64File);
                String name;
                if (attachNames != null && idx < attachNames.size()) {
                    name = attachNames.get(idx);
                } else {
                    name = "Attachment " + Integer.toString(idx);
                }
                String mimeType = null;
                if (attachMimeTypes != null && idx < attachMimeTypes.size()) {
                    mimeType = attachMimeTypes.get(idx);
                }
                
                if (mimeType != null) {
                    req.addAttachment(name, f, mimeType);
                } else {
                    req.addAttachment(name, f);
                }
            } catch (IOException ex) {
                // Skip this attachment. Eventually need to signal this to the client ...
            }
            ++idx;
        } 
    }
    
    /**
     * Converts a multi-valued map representing a posted form into a standard 1-1 map. Only
     * retains the first value for each key.
     * 
     * @param formMap
     * @return 
     */
    public static void convertFormToMap(MultivaluedMap<String, String> formMap,
             Map<String, String> fieldsMap,
             Map<String, String> typesMap) {
        for (MultivaluedMap.Entry<String, List<String> > entry : formMap.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                String key = entry.getKey();
                String type = null;
                int dotPos = key.indexOf(".");
                
                if (dotPos > 0) {
                    type = key.substring(dotPos + 1);
                    key = key.substring(0, dotPos);
                }
                fieldsMap.put(key, entry.getValue().get(0));
                if (type != null) {
                    typesMap.put(key, type);
                }
            }
        }
    }
}
