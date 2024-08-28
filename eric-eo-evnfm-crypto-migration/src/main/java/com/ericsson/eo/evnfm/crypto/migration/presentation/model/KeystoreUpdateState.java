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
package com.ericsson.eo.evnfm.crypto.migration.presentation.model;

import java.nio.file.WatchKey;
import java.security.KeyStore;
import java.util.Map;
import java.util.UUID;

public class KeystoreUpdateState {
    private boolean passwordsUpdated;
    private boolean keystoreUpdated;
    private Map<UUID, byte[]> passwordsCache;
    private KeyStore keyStore;
    private WatchKey passwordsWatchKey;
    private WatchKey keystoreWatchKey;

    public boolean isPasswordsUpdated() {
        return passwordsUpdated;
    }

    public void setPasswordsUpdated(final boolean passwordsUpdated) {
        this.passwordsUpdated = passwordsUpdated;
    }

    public boolean isKeystoreUpdated() {
        return keystoreUpdated;
    }

    public void setKeystoreUpdated(final boolean keystoreUpdated) {
        this.keystoreUpdated = keystoreUpdated;
    }

    public Map<UUID, byte[]> getPasswordsCache() {
        return passwordsCache;
    }

    public void setPasswordsCache(final Map<UUID, byte[]> passwordsCache) {
        this.passwordsCache = passwordsCache;
    }

    public KeyStore getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(final KeyStore keyStore) {
        this.keyStore = keyStore;
    }

    public WatchKey getPasswordsWatchKey() {
        return passwordsWatchKey;
    }

    public void setPasswordsWatchKey(final WatchKey passwordsWatchKey) {
        this.passwordsWatchKey = passwordsWatchKey;
    }

    public WatchKey getKeystoreWatchKey() {
        return keystoreWatchKey;
    }

    public void setKeystoreWatchKey(final WatchKey keystoreWatchKey) {
        this.keystoreWatchKey = keystoreWatchKey;
    }

    public boolean isUpdateReady() {
        return passwordsUpdated && passwordsCache != null && keystoreUpdated && keyStore != null;
    }

    public void reset() {
        passwordsUpdated = false;
        keystoreUpdated = false;
        keyStore = null;
        passwordsCache = null;
    }
}
