/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.xmlobjects;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author shallem
 */
@XmlRootElement
public class GenericResponse {
    public int status;
    public String message;
}
