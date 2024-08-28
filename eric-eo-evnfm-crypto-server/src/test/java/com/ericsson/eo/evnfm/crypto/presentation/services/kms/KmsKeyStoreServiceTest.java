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

import com.ericsson.eo.evnfm.crypto.TestConstants;
import com.ericsson.eo.evnfm.crypto.TestUtils;
import com.ericsson.eo.evnfm.crypto.exceptions.KmsSecretStoreException;
import com.ericsson.eo.evnfm.crypto.presentation.model.CipherKey;
import com.ericsson.eo.evnfm.crypto.presentation.model.KmsSecret;
import com.ericsson.eo.evnfm.crypto.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.vault.support.VaultResponse;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KmsKeyStoreServiceTest {

    @Mock
    private KmsRequestService kmsRequestService;

    @Mock
    private CipherKeyService cipherKeyService;

    @Mock
    private CryptoCacheService cryptoCacheService;

    private KmsKeyStoreService kmsKeyStoreService;

    @BeforeEach
    public void setup() {
        kmsKeyStoreService = new KmsKeyStoreServiceImpl(kmsRequestService, cipherKeyService, cryptoCacheService);
    }

    @Test
    public void testInitKmsStoreShouldUpdateBothCaches() {
        // given
        var kmsSecret = TestUtils.generateKmsSecret();
        when(kmsRequestService.getAllSecrets()).thenReturn(Collections.singletonList(kmsSecret));
        // when
        kmsKeyStoreService.initKmsStore(true);
        // then
        verify(cryptoCacheService, times(1)).updateKey(any());
        verify(cryptoCacheService, times(1)).updateLatestKey(any());
    }

    @Test
    public void testInitKmsStoreShouldNotProceedIfKmsDisabled() {
        // when
        kmsKeyStoreService.initKmsStore(false);
        // then
        verifyNoInteractions(kmsRequestService, cipherKeyService, cryptoCacheService);
    }

    @Test
    public void testInitKmsStoreShouldCreteNewCipherKeyIfKmsStoreIsEmpty() {
        // given
        var cipherKey = TestUtils.generateCipherKey();
        when(cipherKeyService.createCipherKey()).thenReturn(cipherKey);
        // when
        kmsKeyStoreService.initKmsStore(true);
        // then
        verify(kmsRequestService, times(1)).getAllSecrets();
        verify(kmsRequestService, times(1))
                .storeSecretToKms(argThat(key -> cipherKey.getAlias().equals(key.getAlias())));
        verify(cryptoCacheService, times(1)).updateKey(cipherKey);
        verify(cryptoCacheService, times(1)).updateLatestKey(cipherKey);
    }

    @Test
    public void testInitKmsStoreShouldStoreLatestCipherKeyInCacheForEncoding() {
        // given
        var oldKey = TestUtils.generateKmsSecret();
        var newKey = TestUtils.generateKmsSecret();
        when(kmsRequestService.getAllSecrets()).thenReturn(Arrays.asList(oldKey, newKey));
        // when
        kmsKeyStoreService.initKmsStore(true);
        // then
        verify(cryptoCacheService, times(2)).updateKey(any());
        var cipherKeyCaptor = ArgumentCaptor.forClass(CipherKey.class);
        verify(cryptoCacheService, times(1)).updateLatestKey(cipherKeyCaptor.capture());
        var cipherKey = cipherKeyCaptor.getValue();
        assertEquals(newKey.getCreated(), cipherKey.getCreated());
    }

    @Test
    public void testUpdateLatestKeyShouldStoreKeyToKmsAndClearBothCaches() {
        // given
        var cipherKey = TestUtils.generateCipherKey();
        when(cipherKeyService.createCipherKey()).thenReturn(cipherKey);
        // when
        kmsKeyStoreService.updateLatestKey();
        // then
        var kmsSecretCaptor = ArgumentCaptor.forClass(KmsSecret.class);
        verify(kmsRequestService, times(1)).storeSecretToKms(kmsSecretCaptor.capture());
        var kmsSecret = kmsSecretCaptor.getValue();
        assertNotNull(kmsSecret);
        assertEquals(cipherKey.getAlias(), kmsSecret.getAlias());
        assertEquals(TestConstants.CYPHER_KEY, kmsSecret.getKey());
        verify(cryptoCacheService, times(1)).removeKeys();
        verify(cryptoCacheService, times(1)).removeLatestKey();;
    }

    @Test
    public void testStoreKeyShouldStoreToKmsAndUpdateCache() {
        // given
        var cipherKey = TestUtils.generateCipherKey();
        // when
        kmsKeyStoreService.storeKey(cipherKey.getAlias(), cipherKey);
        // then
        var kmsSecretCaptor = ArgumentCaptor.forClass(KmsSecret.class);
        verify(kmsRequestService, times(1)).storeSecretToKms(kmsSecretCaptor.capture());
        var kmsSecret = kmsSecretCaptor.getValue();
        assertNotNull(kmsSecret);
        assertEquals(cipherKey.getAlias(), kmsSecret.getAlias());
        assertEquals(TestConstants.CYPHER_KEY, kmsSecret.getKey());
        verify(cryptoCacheService, times(1)).updateKey(cipherKey);
    }

    @Test
    public void testLatestKeyShouldReturnValueFromCache() {
        // given
        var cipherKey = TestUtils.generateCipherKey();
        when(cryptoCacheService.fetchLatestKey()).thenReturn(Optional.of(cipherKey));
        // when
        var latestKeyOpt = kmsKeyStoreService.getLatestKey(Constants.KMS_CACHE_LATEST_KEY);
        // then
        assertTrue(latestKeyOpt.isPresent());
        assertSame(cipherKey, latestKeyOpt.get());
        verifyNoInteractions(kmsRequestService);
    }

    @Test
    public void testLatestKeyShouldCreateNewKeyIfCacheIsEmpty() {
        // given
        var cipherKey = TestUtils.generateCipherKey();
        when(cipherKeyService.createCipherKey()).thenReturn(cipherKey);
        // when
        var latestKeyOpt = kmsKeyStoreService.getLatestKey(Constants.KMS_CACHE_LATEST_KEY);
        // then
        assertTrue(latestKeyOpt.isPresent());
        assertSame(cipherKey, latestKeyOpt.get());
        verify(kmsRequestService, times(1))
                .storeSecretToKms(argThat(key -> cipherKey.getAlias().equals(key.getAlias())));
        verify(cryptoCacheService, times(1)).updateLatestKey(cipherKey);
        verify(cryptoCacheService, times(1)).updateKey(cipherKey);
    }

    @Test
    public void testGetKeyByIdShouldReturnValueFromCache() {
        // given
        var cipherKey = TestUtils.generateCipherKey();
        when(cryptoCacheService.fetchKeyById(cipherKey.getAlias())).thenReturn(Optional.of(cipherKey));
        // when
        var cipherKeyOpt = kmsKeyStoreService.getKeyById(cipherKey.getAlias());
        // then
        assertTrue(cipherKeyOpt.isPresent());
        assertSame(cipherKey, cipherKeyOpt.get());
        verifyNoInteractions(kmsRequestService);
    }
    @Test
    public void testGetKeyByIdShouldReturnValueFromKmsStoreIfCacheIsEmpty() {
        // given
        var cipherKey = TestUtils.generateCipherKey();
        final VaultResponse vaultResponse = TestUtils.generateVaultResponse();
        when(kmsRequestService.getSecretFromKms(cipherKey.getAlias())).thenReturn(vaultResponse);
        when(cipherKeyService.vaultResponseToCipherKey(vaultResponse)).thenReturn(cipherKey);
        // when
        var cipherKeyOpt = kmsKeyStoreService.getKeyById(cipherKey.getAlias());
        // then
        assertTrue(cipherKeyOpt.isPresent());
        assertSame(cipherKey, cipherKeyOpt.get());
        verify(cryptoCacheService, times(1)).updateKey(cipherKey);
    }

    @Test
    public void testGetKeyByIdShouldThrowsErrorIfVaultResponseIsEmpty() {
        // given
        var key = UUID.randomUUID();
        var vaultResponse = TestUtils.generateVaultResponse();
        vaultResponse.setData(null);
        when(kmsRequestService.getSecretFromKms(key)).thenReturn(vaultResponse);
        // when/then
        assertThrows(KmsSecretStoreException.class, () -> kmsKeyStoreService.getKeyById(key));
        verify(cryptoCacheService, times(1)).fetchKeyById(key);
    }

    @Test
    public void testGetKeyByIdShouldThrowsErrorIfVaultResponseIsNull() {
        // given
        var key = UUID.randomUUID();
        // when/then
        assertThrows(KmsSecretStoreException.class, () -> kmsKeyStoreService.getKeyById(key));
        verify(cryptoCacheService, times(1)).fetchKeyById(key);
    }

}