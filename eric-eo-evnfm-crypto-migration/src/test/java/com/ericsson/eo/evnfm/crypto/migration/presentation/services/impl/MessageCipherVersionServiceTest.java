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
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.message.MessageCipher;
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.message.MessageCipher1v0;

public class MessageCipherVersionServiceTest {

    private static final short VERSION1V0 = (short) 0x0001;
    private static final short VERSION1V3 = (short) 0x0003;

    private Map<Short, MessageCipher> versionToCipherMap;

    private SecurityConfig securityConfig;

    private CipherVersionService<MessageCipher> versionService;

    @BeforeEach
    public void init() {
        versionToCipherMap = getMessageCiphers();
        securityConfig = Mockito.mock(SecurityConfig.class);
        versionService = new MessageCipherVersionServiceImpl(versionToCipherMap, securityConfig);
    }

    @Test
    public void shouldReturnLatestCipher() {
        Mockito.when(securityConfig.getLatestEncryptionDecryptionVersion()).thenReturn(VERSION1V0);

        MessageCipher latestCipher = versionService.getLatestCipher();

        assertNotNull(latestCipher);
        assertEquals(versionToCipherMap.get(VERSION1V0), latestCipher);
    }

    @Test
    public void shouldThrowExceptionWhenGettingLatestCipher() {
        Mockito.when(securityConfig.getLatestEncryptionDecryptionVersion()).thenReturn(VERSION1V3);

        CryptoException cryptoException = assertThrows(
                CryptoException.class, () -> versionService.getLatestCipher());
        assertEquals(Constants.ERROR_UNSUPPORTED_ENCRYPTION_DECRYPTION_VERSION, cryptoException.getMessage());
    }

    @Test
    public void shouldReturnCipher() {
        MessageCipher cipher = versionService.getCipher(VERSION1V0);

        assertNotNull(cipher);
        assertEquals(versionToCipherMap.get(VERSION1V0), cipher);
    }

    @Test
    public void shouldThrowExceptionWhenGettingCipher() {
        CryptoException cryptoException = assertThrows(
                CryptoException.class, () -> versionService.getCipher(VERSION1V3));
        assertEquals(Constants.ERROR_UNSUPPORTED_ENCRYPTION_DECRYPTION_VERSION, cryptoException.getMessage());
    }

    private Map<Short, MessageCipher> getMessageCiphers() {
        return Map.of(VERSION1V0, Mockito.mock(MessageCipher1v0.class));
    }
}
