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
package com.ericsson.eo.evnfm.crypto.migration.presentation.services.kms;

import com.ericsson.eo.evnfm.crypto.migration.TestUtils;
import com.ericsson.eo.evnfm.crypto.migration.exceptions.UnknownKeyException;
import com.ericsson.eo.evnfm.crypto.migration.presentation.mapper.CipherKmsSecretMapper;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.CipherKey;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.KmsSecret;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.vault.support.VaultResponse;

import java.util.Optional;

import static com.ericsson.eo.evnfm.crypto.migration.TestUtils.convertObjectToMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class KmsKeyStoreServiceTest {
    private final KmsRequestService kmsRequestService = mock(KmsRequestService.class);

    @Test
    void testStoreSecretById() {
        doNothing().when(kmsRequestService).storeSecretToKms(any(), any());
        KmsKeyStoreService kmsKeyStoreService = mock(KmsKeyStoreService.class);
        CipherKey cipherKey = TestUtils.generateCipherKey();
        kmsKeyStoreService.storeKey(cipherKey.getAlias(), cipherKey);
        Mockito.verify(kmsKeyStoreService, times(1)).storeKey(cipherKey.getAlias(), cipherKey);
    }

    @Test
    void testGetSecretById() {
        VaultResponse vaultResponse = new VaultResponse();
        CipherKey cipherKey = TestUtils.generateCipherKey();
        KmsSecret beforekmsSecret = CipherKmsSecretMapper.mapCipherKeyToKmsSecret(cipherKey);
        vaultResponse.setData(convertObjectToMap(beforekmsSecret));
        doReturn(vaultResponse).when(kmsRequestService).getSecretFromKms(any(), any());

        KmsKeyStoreService kmsKeyStoreService = new KmsKeyStoreServiceImpl(kmsRequestService);

        String keyId = beforekmsSecret.getAlias().toString();

        Optional<CipherKey> response = kmsKeyStoreService.getKeyById(keyId);

        Assertions.assertTrue(response.isPresent());
        CipherKey key = response.get();
        Assertions.assertAll("key",
                () -> assertEquals(keyId, key.getAlias().toString()),
                () -> assertEquals(beforekmsSecret.getCreated(), key.getCreated()),
                () -> assertNotNull(key.getKey())
        );
    }

    @Test
    void testStoreSecretByEmptyId() {
        KmsKeyStoreService kmsKeyStoreService = new KmsKeyStoreServiceImpl(kmsRequestService);
        CipherKey cipherKey = TestUtils.generateCipherKey();
        Assertions.assertThrows(UnknownKeyException.class, () -> kmsKeyStoreService.storeKey(null, cipherKey));
    }

    @Test
    void testStoreSecretEmptyCypher() {
        KmsKeyStoreService kmsKeyStoreService = new KmsKeyStoreServiceImpl(kmsRequestService);
        CipherKey cipherKey = TestUtils.generateCipherKey();
        Assertions.assertThrows(UnknownKeyException.class, () -> kmsKeyStoreService.storeKey(cipherKey.getAlias(), null));
    }
}