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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpServerErrorException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KmsRequestServiceTest {

    @Mock
    private VaultSdk vaultSdk;
    private KmsRequestService kmsRequestService;
    @BeforeEach
    public void setup() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        kmsRequestService = new KmsRequestServiceImpl(vaultSdk, mapper);
    }

    @Test
    public void testStoreSecretToKmsShouldStoreSecret() {
        // given
        var kmsSecret = TestUtils.generateKmsSecret();
        // when
        kmsRequestService.storeSecretToKms(kmsSecret);
        // then
        verify(vaultSdk, times(1)).storeSecret(kmsSecret);
    }

    @Test
    public void testStoreSecretToKmsShouldThrowsErrorIfUnsupportedOperation() {
        // given
        var kmsSecret = TestUtils.generateKmsSecret();
        doThrow(new UnsupportedOperationException()).when(vaultSdk).storeSecret(kmsSecret);
        // when/then
        assertThrows(KmsSecretStoreException.class, () -> kmsRequestService.storeSecretToKms(kmsSecret));
    }

    @Test
    public void testStoreSecretToKmsShouldThrowsErrorIfHttpException() {
        // given
        var kmsSecret = TestUtils.generateKmsSecret();
        var exception = HttpServerErrorException
                .create(HttpStatus.BAD_GATEWAY, "error", new HttpHeaders(), null, null);
        doThrow(exception).when(vaultSdk).storeSecret(kmsSecret);
        // when/then
        assertThrows(KmsSecretStoreException.class, () -> kmsRequestService.storeSecretToKms(kmsSecret));
    }

    @Test
    public void testGetSecretFromKmsShouldReturnVaultResponse() {
        // given
        var vaultResponse = TestUtils.generateVaultResponse();
        var key = UUID.randomUUID();
        when(vaultSdk.getSecret(key)).thenReturn(vaultResponse);
        // when
        var actuaVaultResponse = kmsRequestService.getSecretFromKms(key);
        // then
        assertSame(vaultResponse, actuaVaultResponse);
    }

    @Test
    public void testGetSecretFromKmsShouldThrowsErrorIfUnsupportedOperation() {
        // given
        var key = UUID.randomUUID();
        when(vaultSdk.getSecret(key)).thenThrow(new UnsupportedOperationException());
        // when/then
        assertThrows(KmsSecretStoreException.class, () -> kmsRequestService.getSecretFromKms(key));
    }

    @Test
    public void testGetSecretFromKmsShouldThrowsErrorIfHttpException() {
        // given
        var key = UUID.randomUUID();
        var exception = HttpServerErrorException
                .create(HttpStatus.BAD_GATEWAY, "error", new HttpHeaders(), null, null);
        when(vaultSdk.getSecret(key)).thenThrow(exception);
        // when/then
        assertThrows(KmsSecretStoreException.class, () -> kmsRequestService.getSecretFromKms(key));
    }
    @Test
    public void testGetAllSecretsShouldReturnKmsSecret() {
        // given
        var kmsSecret = TestUtils.generateKmsSecret();
        var key = kmsSecret.getAlias();
        when(vaultSdk.getAllSecrets()).thenReturn(Collections.singletonList(key.toString()));
        when(vaultSdk.getSecret(key)).thenReturn(TestUtils.generateVaultResponse());
        // when
        var secrets = kmsRequestService.getAllSecrets();
        // then
        assertEquals(1, secrets.size());
        var actualKmsSecret = secrets.get(0);
        assertEquals(UUID.fromString(TestConstants.SECRET_KEY_ALIAS), actualKmsSecret.getAlias());
        assertEquals(LocalDateTime.parse(TestConstants.SECRET_KEY_DATE), actualKmsSecret.getCreated());
        assertEquals(kmsSecret.getKey(), actualKmsSecret.getKey());
    }

    @Test
    public void testGetAllSecretsShouldReturnEmptyListIfSecretsNotFound() {
        // when
        var secrets = kmsRequestService.getAllSecrets();
        // then
        assertTrue(secrets.isEmpty());
        verify(vaultSdk, never()).getSecret(any());
    }

    @Test
    public void testGetAllSecretsShouldThrowsErrorIfVaultResponseDataIsEmpty() {
        // given
        var kmsSecret = TestUtils.generateKmsSecret();
        var key = kmsSecret.getAlias();
        var vaultResponse = TestUtils.generateVaultResponse();
        vaultResponse.setData(null);
        when(vaultSdk.getAllSecrets()).thenReturn(Collections.singletonList(key.toString()));
        when(vaultSdk.getSecret(key)).thenReturn(vaultResponse);
        // when/then
        assertThrows(KmsSecretStoreException.class, () -> kmsRequestService.getAllSecrets());
    }

    @Test
    public void testGetAllSecretsShouldThrowsErrorIfVaultResponseIsNull() {
        // given
        var kmsSecret = TestUtils.generateKmsSecret();
        var key = kmsSecret.getAlias();
        when(vaultSdk.getAllSecrets()).thenReturn(Collections.singletonList(key.toString()));
        // when/then
        assertThrows(KmsSecretStoreException.class, () -> kmsRequestService.getAllSecrets());
    }

    @Test
    public void testGetAllSecretsShouldThrowsErrorIfRuntimeException() {
        // given
        when(vaultSdk.getAllSecrets()).thenThrow(new RuntimeException());
        // when/then
        assertThrows(KmsSecretStoreException.class, () -> kmsRequestService.getAllSecrets());
    }

}