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
package com.ericsson.eo.evnfm.crypto.security;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ericsson.eo.evnfm.crypto.TestUtils;
import com.ericsson.eo.evnfm.crypto.exceptions.CryptoException;
import com.ericsson.eo.evnfm.crypto.presentation.model.SecurityConfig;

public class SecurityProviderTest {

    private SecurityProvider securityProvider;

    @BeforeEach
    public void setup() {
        SecurityConfig securityConfig = TestUtils.initSecurityConfig();
        securityProvider = new SecurityProvider(securityConfig);
    }

    @Test
    public void shouldThrowExceptionWhenWrongSecureRandomAlgorithmSpecified() {
        assertThrows(CryptoException.class, () -> {
            SecurityConfig securityConfig = TestUtils.initSecurityConfig();
            securityConfig.setSecureRandomAlgorithm("wrong algorithm");

            securityProvider = new SecurityProvider(securityConfig);
        });
    }

    @Test
    public void shouldThrowExceptionWhenWrongDigestAlgorithmSpecified() {

        assertThrows(CryptoException.class, () -> {
            SecurityConfig securityConfig = TestUtils.initSecurityConfig();
            securityConfig.setDigestAlgorithm("wrong algorithm");

            securityProvider = new SecurityProvider(securityConfig);
        });
    }

    @Test
    public void shouldReturnSecureRandom() {
        assertNotNull(securityProvider.getSecureRandom());
    }

    @Test
    public void shouldReturnMessageDigest() {
        assertNotNull(securityProvider.getMessageDigest());
    }

    @Test
    public void shouldReturnKeySecureRandom() {
        assertNotNull(securityProvider.getKeySecureRandom());
    }
}
