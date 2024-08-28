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
package com.ericsson.eo.evnfm.crypto.migration.presentation.services;

import java.util.List;
import java.util.UUID;

import com.ericsson.eo.evnfm.crypto.migration.presentation.model.CipherKey;

/**
 * Interface for key management service. Implementation is responsible for creating keys,
 * storing them in a secure manner and retrieve keys to be used for encryption and decryption.
 */
public interface KeyService {
    /**
     * Retrieves a key with given id. Latest key is also available by {@code null} id.
     * @param keyId id of a requested key. {@code null} means the latest key.
     * @return retrieved key
     */
    CipherKey getKey(UUID keyId);

    /**
     * Creates a new key that immediately became available for encryption.
     */
    CipherKey createKey();

    /**
     * Retrieves key id's.
     * @return list of id's.
     */
    List<UUID> getKeyIds();

    UUID getLatestKeyId();
}
