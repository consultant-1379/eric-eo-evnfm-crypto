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

import com.ericsson.eo.evnfm.crypto.exceptions.KmsSecretStoreException;
import com.ericsson.eo.evnfm.crypto.presentation.model.KmsSecret;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.vault.support.VaultResponse;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class KmsRequestServiceImpl implements KmsRequestService {

    public static final String CANNOT_STORE_SECRET_TO_KMS_DUE_TO = "Cannot store secret to KMS due to: ";
    public static final String CANNOT_GET_SECRET_FROM_KMS_DUE_TO = "Cannot get secret from KMS due to: ";
    public static final String KMS_INTERNAL_ISSUE = "KMS Internal issue: ";

    private final VaultSdk vaultSdk;

    private final ObjectMapper objectMapper;

    public KmsRequestServiceImpl(VaultSdk vaultSdk, ObjectMapper objectMapper) {
        this.vaultSdk = vaultSdk;
        this.objectMapper = objectMapper;
    }


    @Override
    public void storeSecretToKms(KmsSecret kmsSecret) {
        try {
            vaultSdk.storeSecret(kmsSecret);
        } catch (UnsupportedOperationException | HttpStatusCodeException e) {
            throw new KmsSecretStoreException(CANNOT_STORE_SECRET_TO_KMS_DUE_TO, e);
        }
    }


    @Override
    public VaultResponse getSecretFromKms(UUID secretId) {
        try {
            return vaultSdk.getSecret(secretId);
        } catch (UnsupportedOperationException | HttpStatusCodeException e) {
            throw new KmsSecretStoreException(CANNOT_GET_SECRET_FROM_KMS_DUE_TO, e);
        }
    }

    @Override
    public List<KmsSecret> getAllSecrets() {
        try {
            final List<String> allSecrets = vaultSdk.getAllSecrets();
            return allSecrets.stream().map(secret -> {
                final VaultResponse vaultResponse = vaultSdk.getSecret(UUID.fromString(secret));
                if (Objects.nonNull(vaultResponse) && Objects.nonNull(vaultResponse.getData())) {
                    return objectMapper.convertValue(vaultResponse.getData(), KmsSecret.class);
                }
                throw new KmsSecretStoreException("Vault response is empty");
            }).collect(Collectors.toList());
        } catch (RuntimeException e) {
            throw new KmsSecretStoreException(CANNOT_GET_SECRET_FROM_KMS_DUE_TO, e);
        }
    }

}
