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
package com.ericsson.eo.evnfm.crypto.migration.presentation.services.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.ericsson.eo.evnfm.crypto.migration.exceptions.CryptoException;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.SecurityConfig;
import com.ericsson.eo.evnfm.crypto.migration.presentation.services.CipherVersionService;
import com.ericsson.eo.evnfm.crypto.migration.util.Constants;
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.password.PasswordsCipher;
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.password.PasswordsCipher1v0;
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.password.PasswordsCipher1v1;
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.password.PasswordsCipher1v2;

public class PasswordsCipherVersionServiceTest {

    private static final short VERSION1V0 = (short) 0x0100;
    private static final short VERSION1V1 = (short) 0x0101;
    private static final short VERSION1V2 = (short) 0x0102;
    private static final short VERSION1V3 = (short) 0x0103;

    private Map<Short, PasswordsCipher> versionToCipherMap;

    private SecurityConfig securityConfig;

    private CipherVersionService<PasswordsCipher> versionService;

    @BeforeEach
    public void init() {
        versionToCipherMap = getPasswordsCiphers();
        securityConfig = Mockito.mock(SecurityConfig.class);
        versionService = new PasswordsCipherVersionServiceImpl(versionToCipherMap, securityConfig);
    }

    @Test
    public void shouldReturnLatestCipher() {
        Mockito.when(securityConfig.getLatestPasswordsObscurityVersion()).thenReturn(VERSION1V0);

        PasswordsCipher latestCipher = versionService.getLatestCipher();

        assertNotNull(latestCipher);
        assertEquals(versionToCipherMap.get(VERSION1V0), latestCipher);
    }

    @Test
    public void shouldThrowExceptionWhenGettingLatestCipher() {
        Mockito.when(securityConfig.getLatestPasswordsObscurityVersion()).thenReturn(VERSION1V3);

        CryptoException cryptoException = assertThrows(
                CryptoException.class, () -> versionService.getLatestCipher());
        assertEquals(Constants.ERROR_UNSUPPORTED_ENCRYPTION_DECRYPTION_VERSION, cryptoException.getMessage());
    }

    @Test
    public void shouldReturnCipher() {
        PasswordsCipher cipher = versionService.getCipher(VERSION1V0);

        assertNotNull(cipher);
        assertEquals(versionToCipherMap.get(VERSION1V0), cipher);
    }

    @Test
    public void shouldThrowExceptionWhenGettingCipher() {
        CryptoException cryptoException = assertThrows(
                CryptoException.class, () -> versionService.getCipher(VERSION1V3));
        assertEquals(Constants.ERROR_UNSUPPORTED_ENCRYPTION_DECRYPTION_VERSION, cryptoException.getMessage());
    }

    private Map<Short, PasswordsCipher> getPasswordsCiphers() {
        return Map.of(
                VERSION1V0, Mockito.mock(PasswordsCipher1v0.class),
                VERSION1V1, Mockito.mock(PasswordsCipher1v1.class),
                VERSION1V2, Mockito.mock(PasswordsCipher1v2.class));
    }
}
