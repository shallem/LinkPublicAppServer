package com.mobilehelix.appserver.permissions;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author frederic
 */
public final class FilePermissions {
    private static final Logger LOG = Logger.getLogger(FilePermissions.class.getName());
   
    public static final int CAN_IMPORT = 1;
    public static final int CAN_EDIT = 2; // or 6 to force save ?
    public static final int CAN_SAVE = 4;
    public static final int CAN_DELETE = 8;
    public static final int CAN_CHECKIN = 16;  
    public static final int CAN_LINK = 32;
    public static final int CAN_COPY_FROM = 64;
//    public static final int CAN_SENDANDFILE = 64;
    
    
    public static boolean canImport(int permission) {
        return (permission & CAN_IMPORT) != 0;
    }
    
    public static boolean canSave(int permission) {
        return (permission & CAN_SAVE) != 0;
    }
    
    public static boolean canEdit(int permission) {
        return (permission & CAN_EDIT) != 0;
    }
    
    public static boolean canDelete(int permission) {
        return (permission & CAN_DELETE) != 0;
    }
    
    public static boolean canCheckIn(int permission) {
        return (permission & CAN_CHECKIN) != 0;
    }

    public static boolean canCheckOut(int permission) {
        return (permission & CAN_CHECKIN) != 0; // save flag as checkin
    }
    
    public static boolean canLink(int permission) {
        return (permission & CAN_LINK) != 0; 
    }
    
    public static boolean canCopyFrom(int permission) {
        return (permission & CAN_COPY_FROM) != 0;
    }
        
//    public static boolean canSendAndFile(int permission) {
//        return (permission & CAN_SENDANDFILE) != 0; 
//    }
    
    // Compute permissions intersection between object (first paremeter, coming
    // from the file system resource) and subject (other parameters coming from 
    // the current user's file permissions policy).
    public static int computePermission(int permission, boolean canCreate, boolean canEdit, 
            boolean canDelete, boolean canImport, boolean canCheckin, boolean canLink,
            boolean canCopyFrom) {
       LOG.log(Level.FINER, "Initial permissions: {0}", permission);
       permission = (canCreate)      ? permission  : permission & ~CAN_SAVE;
       permission = (canDelete)      ? permission  : permission & ~CAN_DELETE;
       permission = (canImport)      ? permission  : permission & ~CAN_IMPORT;
       permission = (canCheckin)     ? permission  : permission & ~CAN_CHECKIN;
       permission = (canEdit)        ? permission  : permission & ~CAN_EDIT;
       permission = (canLink)        ? permission  : permission & ~CAN_LINK;
       permission = (canCopyFrom)    ? permission  : permission & ~CAN_COPY_FROM;
//       permission = (canSendAndFile) ? permission  : permission & ~CAN_SENDANDFILE;
       LOG.log(Level.FINER, "Computed permissions: {0}" , permission);
       return permission;
    }
}
