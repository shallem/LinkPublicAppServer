/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.helix.mobile.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to identify a field as a sort field. On the client side this
 * will force creation of an index on this field.
 * 
 * @author shallem
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
public @interface ClientSort {
    String displayName() default "[none]";
    String defaultOrder() default "DSC";
    String caseSensitive() default "false";
    String secondarySort() default "";
    String secondaryOrder() default "DSC";
}
