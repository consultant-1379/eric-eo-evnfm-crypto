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
package com.ericsson.eo.evnfm.crypto.presentation.services.impl;

import com.ericsson.eo.evnfm.crypto.TestUtils;
import com.ericsson.eo.evnfm.crypto.exceptions.CryptoException;
import com.ericsson.eo.evnfm.crypto.presentation.services.KeyService;
import com.ericsson.eo.evnfm.crypto.presentation.services.kms.KmsKeyStoreService;
import com.ericsson.eo.evnfm.crypto.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@SpringBootTest(classes =  KeyServiceImpl.class, properties = "kms.enabled = true")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class KeyServiceTest {

    @MockBean
    private KmsKeyStoreService kmsKeyStoreService;

    @Autowired
    private KeyService keyService;

    @BeforeEach
    public void setup() {
        verify(kmsKeyStoreService, times(1)).initKmsStore(true);
    }

    @Test
    void testGetCipherKeyByIdShouldReturnCipherKey() {
        // given
        var key = UUID.randomUUID();
        var cipherKey = TestUtils.generateCipherKey();
        when(kmsKeyStoreService.getKeyById(key)).thenReturn(Optional.of(cipherKey));
        // when
        var actualCipherKey = keyService.getCipherKeyById(key);
        // then
        assertSame(cipherKey, actualCipherKey);
    }

    @Test
    void testGetCipherKeyByIdShouldThrowErrorIfCipherKeyNotFound() {
        // when/then
        assertThrows(CryptoException.class, () -> keyService.getCipherKeyById(UUID.randomUUID()));
    }

    @Test
    void testGetLatestCipherKeyShouldReturnCipherKey() {
        // given
        var cipherKey = TestUtils.generateCipherKey();
        when(kmsKeyStoreService.getLatestKey(Constants.KMS_CACHE_LATEST_KEY)).thenReturn(Optional.of(cipherKey));
        // when
        var actualCipherKey = keyService.getLatestCipherKey();
        // then
        assertSame(cipherKey, actualCipherKey);
    }

    @Test
    void testGetLatestCipherKeyShouldThrowErrorIfCipherKeyNotFound() {
        // when/then
        assertThrows(CryptoException.class, () -> keyService.getLatestCipherKey());
    }

    @Test
    void testUpdateLatestCipherKeyShouldInvokeUpdateLatestKey() {
        // when
        keyService.updateLatestCipherKey();
        // then
        verify(kmsKeyStoreService, times(1)).updateLatestKey();
    }
}
