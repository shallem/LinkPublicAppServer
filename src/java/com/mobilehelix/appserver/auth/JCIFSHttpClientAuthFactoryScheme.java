/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mobilehelix.appserver.auth;

import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthSchemeFactory;
import org.apache.http.impl.auth.NTLMScheme;
import org.apache.http.params.HttpParams;

public class JCIFSHttpClientAuthFactoryScheme implements AuthSchemeFactory {

    @Override
    public AuthScheme newInstance(final HttpParams params) {
        return new NTLMScheme(new JCIFSHttpClientEngine());
    }

}
