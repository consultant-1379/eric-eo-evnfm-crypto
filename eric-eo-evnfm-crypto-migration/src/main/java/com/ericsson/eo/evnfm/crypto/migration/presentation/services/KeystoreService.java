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

import java.nio.file.Path;
import java.security.KeyStore;
import java.util.Map;
import java.util.UUID;

import com.ericsson.eo.evnfm.crypto.migration.presentation.model.CipherKey;

public interface KeystoreService {

    Map<UUID, byte[]> initPasswords(Path path);

    byte[] createPassword();

    void savePasswords(Map<UUID, byte[]> passwords, Path path);

    Map<UUID, byte[]> loadPasswords(Path path);

    void checkSignature(int signature);

    KeyStore loadKeystore(Path keystorePath, byte[] password);

    char[] encodePassword(byte[] password);

    KeyStore initKeystore(Path keystorePath, byte[] password);

    CipherKey createKey(KeyStore keyStore, Map<UUID, byte[]> passwords);

    CipherKey createKey();

    UUID getLatestKeyId(KeyStore keyStore);

    void saveKeystore(KeyStore keyStore, Path keystorePath, byte[] password);
}
