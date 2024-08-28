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

import com.ericsson.eo.evnfm.crypto.migration.exceptions.UnknownKeyException;
import com.ericsson.eo.evnfm.crypto.migration.presentation.mapper.CipherKmsSecretMapper;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.CipherKey;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.KmsEndPointEnum;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.KmsSecret;
import com.ericsson.eo.evnfm.crypto.migration.util.KmsUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.vault.support.VaultResponse;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class KmsKeyStoreServiceImpl implements KmsKeyStoreService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KmsKeyStoreServiceImpl.class);
    private final KmsRequestService kmsRequestService;

    public KmsKeyStoreServiceImpl(KmsRequestService kmsRequestService) {
        this.kmsRequestService = kmsRequestService;
    }


    @Override
    public void storeKey(UUID keyId, CipherKey privateKey) {
        if (Objects.isNull(keyId) || Objects.isNull(privateKey)) {
            throw new UnknownKeyException("Cannot store Secret to KMS: secret is empty");
        }
        LOGGER.info("Store the Key to KMS");
        var kmsSecret = CipherKmsSecretMapper.mapCipherKeyToKmsSecret(privateKey);
        kmsRequestService.storeSecretToKms(KmsEndPointEnum.KMS_API_V1, kmsSecret);
    }


    @Override
    public Optional<CipherKey> getKeyById(String keyId) {
        return getCipherKey(KmsEndPointEnum.KMS_API_V1, UUID.fromString(keyId));
    }

    private Optional<CipherKey> getCipherKey(KmsEndPointEnum endPoint,
                                             UUID secretId) {
        Optional<CipherKey> cipherKey = Optional.empty();
        VaultResponse vaultResponse = kmsRequestService.getSecretFromKms(endPoint, secretId);
        if (Objects.nonNull(vaultResponse) && Objects.nonNull(vaultResponse.getData())) {
            KmsSecret kmsSecret = KmsUtil.getObjectMapper().convertValue(vaultResponse.getData(), KmsSecret.class);
            cipherKey = Optional.of(CipherKmsSecretMapper.mapKmsSecretToCipherKey(kmsSecret));
        }
        return cipherKey;
    }
}
