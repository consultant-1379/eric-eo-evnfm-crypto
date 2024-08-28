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
package com.ericsson.eo.evnfm.crypto.migration.presentation.services.impl;

import static java.lang.Thread.sleep;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.ericsson.eo.evnfm.crypto.migration.exceptions.CryptoException;
import com.ericsson.eo.evnfm.crypto.migration.presentation.services.KeyService;
import com.ericsson.eo.evnfm.crypto.migration.presentation.services.MigrationService;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.CipherKey;
import com.ericsson.eo.evnfm.crypto.migration.presentation.services.kms.KmsKeyStoreService;

@Service
public class MigrationServiceImpl implements MigrationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationServiceImpl.class);
    @Autowired
    private KeyService keyService;

    @Autowired
    private KmsKeyStoreService kmsStorageAgent;

    @Autowired
    private ApplicationContext appContext;

    @Value("${keys.migration.enabled:false}")
    private boolean isMigrationEnabled;

    @Override
    public void migrateKeysToKMS() {
        UUID latestKeyId = keyService.getLatestKeyId();
        Map<UUID, CipherKey> uuidCipherKeyMap = getUUIDCipherKeyMap();
        CipherKey latestCipherKey = keyService.getKey(latestKeyId);

        uuidCipherKeyMap.entrySet().stream()
                .forEach(uuidCipherKeyEntry -> {
                    UUID keyId = uuidCipherKeyEntry.getKey();
                    CipherKey cipherKey = uuidCipherKeyEntry.getValue();
                    boolean isKeyAbsentInKMS = kmsStorageAgent.getKeyById(keyId.toString()).isEmpty();

                    if (isKeyAbsentInKMS) {
                        kmsStorageAgent.storeKey(keyId, cipherKey);
                    }
                });

        try {
            sleep(2000);
        } catch (InterruptedException e) {
            throw new CryptoException("Thread sleep was interrupted", e);
        }

        System.exit(0);
    }

    @EventListener({ ApplicationReadyEvent.class })
    void initMigration() {
        if (isMigrationEnabled) {
            LOGGER.info("Starting migration.");
            migrateKeysToKMS();
        } else {
            LOGGER.info("Migration is disabled.");
        }
    }

    private Map<UUID, CipherKey> getUUIDCipherKeyMap() {
        Map<UUID, CipherKey> uuidCipherKeyMap = new HashMap<>();

        keyService.getKeyIds().forEach(uuid -> {
            uuidCipherKeyMap.put(uuid, keyService.getKey(uuid));
        });
        return uuidCipherKeyMap;
    }
}
