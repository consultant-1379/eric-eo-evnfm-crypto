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

import java.util.Map;
import java.util.Optional;

import com.ericsson.eo.evnfm.crypto.migration.exceptions.CryptoException;
import com.ericsson.eo.evnfm.crypto.migration.util.Constants;

public abstract class AbstractCipherVersionService<T> implements CipherVersionService<T> {

    private final Map<Short, T> versionToCipherMap;

    public AbstractCipherVersionService(Map<Short, T> versionToCipherMap) {
        this.versionToCipherMap = versionToCipherMap;
    }

    public T getCipher(short version) {
        return Optional.ofNullable(versionToCipherMap.get(version))
                .orElseThrow(() -> new CryptoException(Constants.ERROR_UNSUPPORTED_ENCRYPTION_DECRYPTION_VERSION));
    }
}
