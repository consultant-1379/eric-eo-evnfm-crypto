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
package com.ericsson.eo.evnfm.crypto.migration.util.cipher.pool;

import javax.crypto.Cipher;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.springframework.stereotype.Component;

import com.ericsson.eo.evnfm.crypto.migration.presentation.model.SecurityConfig;

@Component
public class PasswordsCipherPoolableObjectFactory extends BasePoolableObjectFactory {

    private final SecurityConfig securityConfig;

    public PasswordsCipherPoolableObjectFactory(SecurityConfig securityConfig) {
        this.securityConfig = securityConfig;
    }

    @Override
    public Object makeObject() throws Exception {
        return Cipher.getInstance(securityConfig.getPasswordsTransformation());
    }
}
