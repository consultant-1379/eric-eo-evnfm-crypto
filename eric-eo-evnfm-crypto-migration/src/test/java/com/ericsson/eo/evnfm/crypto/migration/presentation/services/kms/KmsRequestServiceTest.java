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
import com.ericsson.eo.evnfm.crypto.migration.exceptions.KmsSecretStoreException;
import com.ericsson.eo.evnfm.crypto.migration.exceptions.UnknownKeyException;
import com.ericsson.eo.evnfm.crypto.migration.presentation.mapper.CipherKmsSecretMapper;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.CipherKey;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.KmsEndPointEnum;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.KmsSecret;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.vault.core.VaultKeyValueOperations;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Map;
import java.util.UUID;

import static com.ericsson.eo.evnfm.crypto.migration.TestUtils.convertObjectToMap;
import static com.ericsson.eo.evnfm.crypto.migration.TestUtils.getKmsSecret;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class KmsRequestServiceTest {
    private final VaultTemplate vaultTemplate = mock(VaultTemplate.class);
    VaultKeyValueOperations vaultKeyValueOperations = mock(VaultKeyValueOperations.class);

    private static VaultResponse getSecretFromKmsV1(KmsRequestService requestService) {
        return requestService.getSecretFromKms(KmsEndPointEnum.KMS_API_V1, UUID.randomUUID());
    }

    @Test
    void testStoreSecretToKmsById() {
        doReturn(vaultKeyValueOperations).when(vaultTemplate).opsForKeyValue(any(), any());
        KmsRequestService kmsRequestService = mock(KmsRequestService.class);

        KmsSecret secret = getKmsSecret();

        kmsRequestService.storeSecretToKms(KmsEndPointEnum.KMS_API_V1, secret);
        Mockito.verify(kmsRequestService, times(1)).storeSecretToKms(KmsEndPointEnum.KMS_API_V1, secret);
    }

    @Test
    void testStoreSecretToKmsByIdEmptySecret() {
        doThrow(UnsupportedOperationException.class).when(vaultTemplate).opsForKeyValue(any(), any());
        KmsRequestService requestService = new KmsRequestServiceImpl(vaultTemplate);

        Assertions.assertThrows(
                UnknownKeyException.class, () -> requestService.storeSecretToKms(KmsEndPointEnum.KMS_API_V1, new KmsSecret()));
    }

    @Test
    void testStoreSecretToKmsByIdOperationNotSupportedV1() {
        doThrow(UnsupportedOperationException.class).when(vaultTemplate).opsForKeyValue(any(), any());
        KmsRequestService requestService = new KmsRequestServiceImpl(vaultTemplate);
        CipherKey cipherKey = TestUtils.generateCipherKey();
        KmsSecret beforekmsSecret = CipherKmsSecretMapper.mapCipherKeyToKmsSecret(cipherKey);
        Assertions.assertThrows(
                KmsSecretStoreException.class, () -> requestService.storeSecretToKms(KmsEndPointEnum.KMS_API_V1, beforekmsSecret));
    }

    @Test
    void testGetSecretToKmsByIdOperationNotSupportedV1() {
        doThrow(UnsupportedOperationException.class).when(vaultTemplate).opsForKeyValue(any(), any());
        KmsRequestService requestService = new KmsRequestServiceImpl(vaultTemplate);
        Assertions.assertThrows(
                KmsSecretStoreException.class, () -> getSecretFromKmsV1(requestService));
    }

    @Test
    void testGetSecretToKmsById() {
        VaultResponse vaultResponse = new VaultResponse();
        CipherKey cipherKey = TestUtils.generateCipherKey();
        KmsSecret beforekmsSecret = CipherKmsSecretMapper.mapCipherKeyToKmsSecret(cipherKey);
        vaultResponse.setData(convertObjectToMap(beforekmsSecret));

        doReturn(vaultKeyValueOperations).when(vaultTemplate).opsForKeyValue(any(), any());
        doReturn(vaultResponse).when(vaultKeyValueOperations).get(any());
        KmsRequestService requestService = new KmsRequestServiceImpl(vaultTemplate);

        VaultResponse response = getSecretFromKmsV1(requestService);

        String keyId = beforekmsSecret.getAlias().toString();
        Assertions.assertNotNull(response);
        Map<String, Object> responseDataMap = response.getData();

        Assertions.assertAll("responseDataMap",
                () -> assertEquals(keyId, responseDataMap.get("alias")),
                () -> assertNotNull(responseDataMap.get("created")),
                () -> assertNotNull(responseDataMap.get("key"))
        );
    }

}