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

import com.ericsson.eo.evnfm.crypto.exceptions.CryptoException;
import com.ericsson.eo.evnfm.crypto.presentation.model.CipherKey;
import com.ericsson.eo.evnfm.crypto.presentation.services.KeyService;
import com.ericsson.eo.evnfm.crypto.presentation.services.kms.KmsKeyStoreService;
import com.ericsson.eo.evnfm.crypto.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.UUID;

@Service
public class KeyServiceImpl implements KeyService {

    private final KmsKeyStoreService kmsKeyStoreService;

    private final boolean isKmsEnabled;

    @Autowired
    public KeyServiceImpl(KmsKeyStoreService kmsKeyStoreService, @Value("${kms.enabled}") boolean isKmsEnabled) {
        this.kmsKeyStoreService = kmsKeyStoreService;
        this.isKmsEnabled = isKmsEnabled;
    }

    @Override
    public CipherKey getCipherKeyById(final UUID requestedKeyId) {
        return kmsKeyStoreService.getKeyById(requestedKeyId)
                .orElseThrow(() -> new CryptoException(String.format("Key with Id: %s not found.", requestedKeyId)));
    }

    @Override
    public CipherKey getLatestCipherKey() {
        return kmsKeyStoreService.getLatestKey(Constants.KMS_CACHE_LATEST_KEY)
                .orElseThrow(() -> new CryptoException("Latest Key not found."));
    }

    @Override
    public void updateLatestCipherKey() {
        kmsKeyStoreService.updateLatestKey();
    }

    @PostConstruct
    private void init() {
        kmsKeyStoreService.initKmsStore(isKmsEnabled);
    }

}
