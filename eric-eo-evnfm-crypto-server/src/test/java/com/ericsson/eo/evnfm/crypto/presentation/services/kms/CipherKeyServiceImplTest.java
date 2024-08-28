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
package com.ericsson.eo.evnfm.crypto.presentation.services.kms;

import com.ericsson.eo.evnfm.crypto.TestUtils;
import com.ericsson.eo.evnfm.crypto.exceptions.CryptoException;
import com.ericsson.eo.evnfm.crypto.presentation.model.KmsSecret;
import com.ericsson.eo.evnfm.crypto.presentation.model.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.vault.support.VaultResponse;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CipherKeyServiceImplTest {

    @Mock
    private SecurityConfig securityConfig;

    @Mock
    private ObjectMapper objectMapper;

    private CipherKeyService cipherKeyService;

    @BeforeEach
    public void init() {
        cipherKeyService = new CipherKeyServiceImpl(securityConfig, objectMapper);
    }

    @Test
    void testCreateCipherKeySuccess() {
        // given
        when(securityConfig.getEncryptionAlgorithm()).thenReturn("AES");
        when(securityConfig.getCipherKeyLength()).thenReturn(128);
        // when
        var cipherKey = cipherKeyService.createCipherKey();
        // then
        assertNotNull(cipherKey.getAlias());
        assertNotNull(cipherKey.getKey());
    }

    @Test
    void testCreateCipherKeyFailedOnWrongAlgorithm() {
        // given
        when(securityConfig.getEncryptionAlgorithm()).thenReturn("Wrong Algorithm");
        // when/then
        assertThrows(CryptoException.class, () -> cipherKeyService.createCipherKey());
    }

    @Test
    void testCreateCipherKeyFailedOnCipherKeyLength() {
        // given
        when(securityConfig.getEncryptionAlgorithm()).thenReturn("AES");
        when(securityConfig.getCipherKeyLength()).thenReturn(0);
        // when/then
        assertThrows(CryptoException.class, () -> cipherKeyService.createCipherKey());
    }

    @Test
    public void testVaultResponseToCipherKey() {
        // given
        var vaultResponse = new VaultResponse();
        when(objectMapper.convertValue(vaultResponse.getData(), KmsSecret.class)).thenReturn(TestUtils.generateKmsSecret());
        // when
        var cipherKey = cipherKeyService.vaultResponseToCipherKey(vaultResponse);
        //then
        assertNotNull(cipherKey.getAlias());
        assertNotNull(cipherKey.getCreated());
        assertNotNull(cipherKey.getKey());
    }

}