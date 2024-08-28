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
package com.ericsson.eo.evnfm.crypto.presentation.services;

import com.ericsson.eo.evnfm.crypto.presentation.model.CipherKey;

import java.util.UUID;

/**
 * Interface for key management service. Implementation is responsible for creating keys,
 * storing them in a secure manner and retrieve keys to be used for encryption and decryption.
 */
public interface KeyService {

    CipherKey getCipherKeyById(UUID keyId);

    CipherKey getLatestCipherKey();

    void updateLatestCipherKey();
}
