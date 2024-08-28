/*
 * COPYRIGHT Ericsson 2024
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 */
package com.ericsson.eo.evnfm.crypto.util.cipher.pool;

import javax.crypto.Cipher;

import com.ericsson.eo.evnfm.crypto.presentation.model.SecurityConfig;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.springframework.stereotype.Component;


@Component
public class MessageCipherPoolableObjectFactory extends BasePoolableObjectFactory {

    private final SecurityConfig securityConfig;

    public MessageCipherPoolableObjectFactory(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    @Override
    public Cipher makeObject() throws Exception {
        return Cipher.getInstance(securityConfig.getTransformation());
    }
}
