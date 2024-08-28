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

import java.util.Objects;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;
import org.springframework.web.client.HttpStatusCodeException;

import com.ericsson.eo.evnfm.crypto.migration.exceptions.KmsSecretStoreException;
import com.ericsson.eo.evnfm.crypto.migration.exceptions.UnknownKeyException;
import com.ericsson.eo.evnfm.crypto.migration.util.KmsUtil;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.KmsEndPointEnum;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.KmsSecret;

@Component
public class KmsRequestServiceImpl implements KmsRequestService {
    public static final String CANNOT_STORE_SECRET_TO_KMS_DUE_TO = "Cannot store secret to KMS due to:";
    public static final String CANNOT_GET_SECRET_FROM_KMS_DUE_TO = "Cannot get secret from KMS due to:";
    private final VaultTemplate vaultTemplate;

    public KmsRequestServiceImpl(VaultTemplate vaultTemplate) {
        this.vaultTemplate = vaultTemplate;
    }


    @Override
    public void storeSecretToKms(KmsEndPointEnum endPoint, KmsSecret kmsSecret) {
        try {
            if (Objects.isNull(kmsSecret) || Objects.isNull(kmsSecret.getKey())) {
                throw new UnknownKeyException("Cannot store Secret to KMS: secret is empty");
            }
            String customPath = KmsUtil.getCustomPath(endPoint, kmsSecret.getAlias());
            var vaultKeyValueOperations =
                    vaultTemplate.opsForKeyValue(endPoint.getRootPath(),
                            endPoint.getKeyValueBackend());
            vaultKeyValueOperations.put(customPath, kmsSecret);
        } catch (UnsupportedOperationException | HttpStatusCodeException e) {
            throw new KmsSecretStoreException(CANNOT_STORE_SECRET_TO_KMS_DUE_TO, e);
        }
    }

    @Override
    public VaultResponse getSecretFromKms(KmsEndPointEnum endPoint, UUID secretId) {
        try {
            String customPath = KmsUtil.getCustomPath(endPoint, secretId);
            var vaultKeyValueOperations =
                    vaultTemplate.opsForKeyValue(endPoint.getRootPath(),
                            endPoint.getKeyValueBackend());
            return vaultKeyValueOperations.get(customPath);

        } catch (UnsupportedOperationException | HttpStatusCodeException e) {
            throw new KmsSecretStoreException(CANNOT_GET_SECRET_FROM_KMS_DUE_TO, e);
        }
    }
}
