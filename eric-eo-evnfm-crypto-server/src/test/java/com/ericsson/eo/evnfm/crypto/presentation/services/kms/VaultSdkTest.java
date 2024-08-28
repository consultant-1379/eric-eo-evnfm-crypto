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
import com.ericsson.eo.evnfm.crypto.exceptions.KmsInternalException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.vault.VaultException;
import org.springframework.vault.client.RestTemplateBuilder;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VaultSdkTest {

    private static final String ROOT_PATH = "secret";

    @Mock
    private RestTemplateBuilder restTemplateBuilder;
    @Mock
    private RestTemplate restTemplate;

    private VaultSdk vaultSdk;

    @BeforeEach
    void setUp() {
        when(restTemplateBuilder.build()).thenReturn(restTemplate);
        vaultSdk = new VaultSdk(new VaultTemplate(restTemplateBuilder));
    }

    @Test
    void testStoreSecretShouldStoreSecretToStore() {
        // given
        var kmsSecret = TestUtils.generateKmsSecret();
        var path = ROOT_PATH + "/key/" + kmsSecret.getAlias();
        var vaultResponse = TestUtils.generateVaultResponse();
        when(restTemplate
                .exchange(eq(path), eq(HttpMethod.POST), any(), eq(VaultResponse.class)))
                .thenReturn(new ResponseEntity<>(vaultResponse, HttpStatus.OK));
        // when/then
        vaultSdk.storeSecret(kmsSecret);
    }

    @Test
    void testStoreSecretShouldThrowsErrorIfExceptionOccur() {
        // given
        var kmsSecret = TestUtils.generateKmsSecret();
        var path = ROOT_PATH + "/key/" + kmsSecret.getAlias();
        when(restTemplate
                .exchange(eq(path), eq(HttpMethod.POST), any(), eq(VaultResponse.class)))
                .thenThrow(new VaultException("Error"));
        // when/then
        assertThrows(KmsInternalException.class, () -> vaultSdk.storeSecret(kmsSecret));
    }

    @Test
    void testGetSecretShouldReturnVaultResponse() {
        // given
        var key = UUID.randomUUID();
        var path = ROOT_PATH + "/key/" + key;
        var vaultResponseSupport = TestUtils.generateVaultResponseSupportFromFile("validResponseByKey.json");
        when(restTemplate
                .exchange(eq(path), eq(HttpMethod.GET), any(),
                        ArgumentMatchers.<ParameterizedTypeReference<Object>>any()))
                .thenReturn(new ResponseEntity<>(vaultResponseSupport, HttpStatus.OK));
        // when
        var vaultResponse = vaultSdk.getSecret(key);
        // then
        assertNotNull(vaultResponse);
        var responseDataMap = vaultResponse.getData();
        assertEquals(TestConstants.SECRET_KEY_ALIAS, responseDataMap.get("alias"));
        assertEquals(TestConstants.CYPHER_KEY, responseDataMap.get("key"));
        assertNotNull(responseDataMap.get("created"));
    }

    @Test
    void testGetSecretShouldThrowsErrorIfExceptionOccur() {
        // given
        var key = UUID.randomUUID();
        var path = ROOT_PATH + "/key/" + key;
        when(restTemplate
                .exchange(eq(path), eq(HttpMethod.GET), any(),
                        ArgumentMatchers.<ParameterizedTypeReference<Object>>any()))
                .thenThrow(new VaultException("Error"));
        // when/then
        assertThrows(KmsInternalException.class, () -> vaultSdk.getSecret(key));
    }

    @Test
    void testGetAllSecretsShouldReturnEmptyCollectionIfKeysNotFound() {
        // when
        var secrets = vaultSdk.getAllSecrets();
        // then
        assertTrue(secrets.isEmpty());
    }

    @Test
    void testGetAllSecretsShouldThrowsErrorIfExceptionOccur() {
        // given
        var path = ROOT_PATH + "/key/?list=true";
        when(restTemplate.getForObject(eq(path), any())).thenThrow(new VaultException("Error"));
        // when/then
        assertThrows(KmsInternalException.class, () -> vaultSdk.getAllSecrets());
    }

}