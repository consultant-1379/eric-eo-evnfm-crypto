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
import com.ericsson.eo.evnfm.crypto.presentation.mappers.CipherKmsSecretMapper;
import com.ericsson.eo.evnfm.crypto.presentation.model.CipherKey;
import com.ericsson.eo.evnfm.crypto.presentation.model.KmsSecret;
import com.ericsson.eo.evnfm.crypto.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.vault.support.VaultResponse;

import java.util.Comparator;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.ericsson.eo.evnfm.crypto.presentation.services.kms.KmsRequestServiceImpl.CANNOT_STORE_SECRET_TO_KMS_DUE_TO;

@Service
public class KmsKeyStoreServiceImpl implements KmsKeyStoreService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KmsKeyStoreServiceImpl.class);
    private final KmsRequestService kmsRequestService;
    private final CipherKeyService cipherKeyService;
    private final CryptoCacheService cryptoCacheService;

    public KmsKeyStoreServiceImpl(KmsRequestService kmsRequestService, CipherKeyService cipherKeyService, CryptoCacheService cryptoCacheService) {
        this.kmsRequestService = kmsRequestService;
        this.cipherKeyService = cipherKeyService;
        this.cryptoCacheService = cryptoCacheService;
    }

    public void initKmsStore(boolean isKmsEnabled) {
        if (!isKmsEnabled) {
            LOGGER.debug("KMS configuration disabled");
            return;
        }
        LOGGER.info("Start searching Cipher Keys in KMS storage.");
        var allSecrets = kmsRequestService.getAllSecrets();
        if (allSecrets.isEmpty()) {
            LOGGER.info("KMS storage is empty. Generating new Cipher Key.");
            createNewCipherKey();
        } else {
            LOGGER.info("Found keys in KMS storage: {}", allSecrets.size());
            var cipherKeys = allSecrets.stream()
                    .sorted(Comparator.comparing(KmsSecret::getCreated)
                            .reversed())
                    .map(CipherKmsSecretMapper::mapKmsSecretToCipherKey)
                    .collect(Collectors.toList());
            LOGGER.info("Update cache '{}'", Constants.KMS_CACHE_KEYS);
            cipherKeys.forEach(cryptoCacheService::updateKey);
            LOGGER.info("Filtering for latest Cipher Key");
            LOGGER.info("Update cache: '{}'", Constants.KMS_CACHE_LATEST_KEY);
            cipherKeys.stream().findFirst().ifPresent(cryptoCacheService::updateLatestKey);
        }
    }

    @Override
    public void updateLatestKey() {
        createAndStoreNewCipherKey();
        LOGGER.info("Clear caches: '{}', '{}'", Constants.KMS_CACHE_KEYS, Constants.KMS_CACHE_LATEST_KEY);
        cryptoCacheService.removeKeys();
        cryptoCacheService.removeLatestKey();
    }

    @Override
    public void storeKey(UUID keyId, CipherKey cipherKey) {
        LOGGER.info("Store the Key to KMS");
        var kmsSecret = CipherKmsSecretMapper.mapCipherKeyToKmsSecret(cipherKey);
        kmsRequestService.storeSecretToKms(kmsSecret);
        LOGGER.info("Update cache '{}'", Constants.KMS_CACHE_KEYS);
        cryptoCacheService.updateKey(cipherKey);
    }

    @Override
    public Optional<CipherKey> getLatestKey(String keyId) {
        return Optional.of(cryptoCacheService.fetchLatestKey().orElseGet(() -> {
            LOGGER.info("Cipher Key not found in cache '{}'. " +
                    "Storing new key to KMS storage", Constants.KMS_CACHE_LATEST_KEY);
            var cipherKey = createAndStoreNewCipherKey();
            LOGGER.info("Updating caches: '{}', '{}'", Constants.KMS_CACHE_KEYS, Constants.KMS_CACHE_LATEST_KEY);
            cryptoCacheService.updateKey(cipherKey);
            cryptoCacheService.updateLatestKey(cipherKey);
            return cipherKey;
        }));
    }

    @Override
    public Optional<CipherKey> getKeyById(UUID keyId) {
        return Optional.of(cryptoCacheService.fetchKeyById(keyId).orElseGet(() -> {
            LOGGER.info("Cipher Key not found in cache '{}'. Searching in KMS storage", Constants.KMS_CACHE_KEYS);
            var vaultResponse = kmsRequestService.getSecretFromKms(keyId);
            validateResponse(vaultResponse);
            var cipherKey = cipherKeyService.vaultResponseToCipherKey(vaultResponse);
            LOGGER.info("Update cache '{}'", Constants.KMS_CACHE_KEYS);
            cryptoCacheService.updateKey(cipherKey);
            return cipherKey;
        }));
    }

    private void createNewCipherKey() {
        var cipherKey = createAndStoreNewCipherKey();
        LOGGER.info("Updating caches: '{}', '{}'", Constants.KMS_CACHE_KEYS, Constants.KMS_CACHE_LATEST_KEY);
        cryptoCacheService.updateKey(cipherKey);
        cryptoCacheService.updateLatestKey(cipherKey);
    }

    private CipherKey createAndStoreNewCipherKey() {
        LOGGER.info("Creating new cipher key");
        var cipherKey = cipherKeyService.createCipherKey();
        LOGGER.info("Put Secret to KMS");
        kmsRequestService.storeSecretToKms(CipherKmsSecretMapper.mapCipherKeyToKmsSecret(cipherKey));
        return cipherKey;
    }

    private static void validateResponse(VaultResponse vaultResponse) {
        if (vaultResponse == null || vaultResponse.getData() == null) {
            throw new KmsSecretStoreException(CANNOT_STORE_SECRET_TO_KMS_DUE_TO + "Vault response data is empty");
        }
    }

}
