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

import com.ericsson.eo.evnfm.crypto.migration.presentation.model.CipherKey;

import java.util.Optional;
import java.util.UUID;

public interface KmsKeyStoreService {

    void storeKey(UUID keyId, CipherKey privateKey);

    Optional<CipherKey> getKeyById(String keyId);
}
