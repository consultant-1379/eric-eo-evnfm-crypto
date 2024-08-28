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

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.security.KeyStore;
import java.security.KeyStore.SecretKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.ericsson.eo.evnfm.crypto.migration.exceptions.CryptoException;
import com.ericsson.eo.evnfm.crypto.migration.exceptions.UnknownKeyException;
import com.ericsson.eo.evnfm.crypto.migration.presentation.services.KeyService;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.CipherKey;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.KeystoreUpdateState;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.SecurityConfig;
import com.ericsson.eo.evnfm.crypto.migration.presentation.services.KeystoreService;
import com.ericsson.eo.evnfm.crypto.migration.presentation.services.kms.KmsKeyStoreService;


@Service
public class KeyServiceImpl implements KeyService, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeyServiceImpl.class);
    private static final String UNABLE_TO_LOAD_KEY_IDS = "Unable to load key ids from keyStore.";

    private Path passwordsFilePath;
    private Path keystoreFilePath;

    private Map<UUID, byte[]> passwordsCache;

    private KeyStore keyStore;
    private UUID latestKeyId;
    private final ConcurrentMap<UUID, CipherKey> keysCache;
    private final ExecutorService executorService;
    private final ReadWriteLock lock;

    private final KeystoreService keystoreService;

    private final SecurityConfig securityConfig;

    private final KmsKeyStoreService kmsKeyStoreService;


    @Autowired
    public KeyServiceImpl(KeystoreService keystoreService, SecurityConfig securityConfig, KmsKeyStoreService kmsKeyStoreService) {
        this.keystoreService = keystoreService;
        this.securityConfig = securityConfig;
        this.kmsKeyStoreService = kmsKeyStoreService;
        keysCache = new ConcurrentHashMap<>();
        executorService = Executors.newSingleThreadExecutor();
        lock = new ReentrantReadWriteLock();
    }

    @Override
    public CipherKey getKey(final UUID requestedKeyId) {
        lock.readLock().lock();
        final UUID keyId = (requestedKeyId == null) ? latestKeyId : requestedKeyId;
        try {
            return keysCache.computeIfAbsent(keyId, this::loadKey);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public CipherKey createKey() {
        CipherKey cipherKey = keystoreService.createKey(keyStore, passwordsCache);
        keystoreService.savePasswords(passwordsCache, passwordsFilePath);
        keystoreService.saveKeystore(keyStore, keystoreFilePath,
                passwordsCache.get(UUID.fromString(securityConfig.getKeystorePasswordAlias())));
        latestKeyId = cipherKey.getAlias();
        return cipherKey;
    }

    @Override
    public List<UUID> getKeyIds() {
        try {
            return Collections.list(keyStore.aliases()).stream()
                    .map(UUID::fromString)
                    .collect(Collectors.toList());
        } catch (KeyStoreException e) {
            LOGGER.error(UNABLE_TO_LOAD_KEY_IDS, e);
            throw new CryptoException(UNABLE_TO_LOAD_KEY_IDS, e);
        }
    }

    @Override
    public UUID getLatestKeyId() {
        return latestKeyId;
    }

    @Autowired
    public void setKeystoreLocation(@Value("${evnfm.crypto.paths.keystore}") final String keystoreLocation) {
        keystoreFilePath = Paths.get(keystoreLocation, securityConfig.getKeystoreFile());
    }

    @Autowired
    public void setPasswordLocation(@Value("${evnfm.crypto.paths.password}") final String passwordLocation) {
        passwordsFilePath = Paths.get(passwordLocation, securityConfig.getPasswordsFile());
    }

    @PostConstruct
    public void init() {
        if (passwordsFilePath.toFile().exists()) {
            LOGGER.info("Loading keystore");
            passwordsCache = keystoreService.loadPasswords(passwordsFilePath);
            if (keystoreFilePath.toFile().exists()) {
                byte[] keystorePassword = passwordsCache.get(UUID.fromString(securityConfig.getKeystorePasswordAlias()));
                keyStore = keystoreService.loadKeystore(keystoreFilePath, keystorePassword);
                latestKeyId = keystoreService.getLatestKeyId(keyStore);
                LOGGER.debug("Keystore loaded");
            } else {
                throw new CryptoException("Passwords loaded but keystore doesn't exist.");
            }
        } else {
            LOGGER.debug("Initializing keystore");
            passwordsCache = keystoreService.initPasswords(passwordsFilePath);
            byte[] keystorePassword = passwordsCache.get(UUID.fromString(securityConfig.getKeystorePasswordAlias()));
            keyStore = keystoreService.initKeystore(keystoreFilePath, keystorePassword);
            CipherKey key = keystoreService.createKey(keyStore, passwordsCache);
            latestKeyId = key.getAlias();
            keystoreService.savePasswords(passwordsCache, passwordsFilePath);
            keystoreService.saveKeystore(keyStore, keystoreFilePath, keystorePassword);
            LOGGER.debug("Keystore initialization completed");
        }
        executorService.submit(this);
    }


    CipherKey loadKey(UUID keyId) {
        final String alias = keyId.toString();
        KeyStore.PasswordProtection protection = Optional.ofNullable(passwordsCache.get(keyId))
                .map(keystoreService::encodePassword)
                .map(KeyStore.PasswordProtection::new)
                .orElseThrow(() -> new UnknownKeyException("No mapping set for key " + alias));
        final SecretKeyEntry entry;
        try {
            entry = (SecretKeyEntry) keyStore.getEntry(alias, protection);
            if (entry == null) {
                final String message = "Key " + alias + " not found in keystore";
                LOGGER.error(message);
                throw new UnknownKeyException(message);
            }
            final LocalDateTime created = keyStore.getCreationDate(alias)
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            return new CipherKey(keyId, entry.getSecretKey(), created, null);
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException e) {
            final String message = "Unable to load key " + alias;
            LOGGER.error(message, e);
            throw new CryptoException(message, e);
        }
    }

    @Override
    public void run() {
        LOGGER.info("Initializing file watcher for keystore updates");
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            KeystoreUpdateState state = new KeystoreUpdateState();
            setupWatchKeys(watchService, state);
            boolean interrupted;
            LOGGER.info("File watcher initialized");
            do {
                interrupted = awaitAndHandleFileChanges(watchService, state);
            } while (!interrupted);
        } catch (IOException e) {
            LOGGER.error("Unable to setup file watch for keystore and passwords: ", e);
            LOGGER.error("Keystore updates must be handled manually restarting a service");
        }
    }


    void setupWatchKeys(final WatchService watchService, KeystoreUpdateState state) throws IOException {
        Path passwordsDirectory = passwordsFilePath.toAbsolutePath().getParent();
        WatchKey passwordsKey = passwordsDirectory.register(watchService, ENTRY_CREATE, ENTRY_MODIFY);
        Path keystoreDirectory = keystoreFilePath.toAbsolutePath().getParent();
        WatchKey keystoreKey;
        if (!passwordsDirectory.equals(keystoreDirectory)) {
            keystoreKey = keystoreDirectory.register(watchService, ENTRY_CREATE, ENTRY_MODIFY);
        } else {
            keystoreKey = passwordsKey;
        }
        state.setPasswordsWatchKey(passwordsKey);
        state.setKeystoreWatchKey(keystoreKey);
    }

    boolean awaitAndHandleFileChanges(final WatchService watchService, final KeystoreUpdateState state) {
        try {
            WatchKey key = watchService.take();
            handleEvents(key, state);
            if (state.isUpdateReady()) {
                applyKeystoreUpdate(state);
            } else if (state.isKeystoreUpdated() && state.isPasswordsUpdated()) {
                LOGGER.error("Reloading updated keys failed. Service may be in unusable state");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private void handleEvents(final WatchKey key, final KeystoreUpdateState state) {
        boolean keystoreEventHandled = false;
        boolean passwordsEventHandled = false;
        for (WatchEvent<?> watchEvent : key.pollEvents()) {
            if (watchEvent.kind().equals(OVERFLOW)) {
                continue;
            }
            WatchEvent<Path> event = (WatchEvent<Path>) watchEvent;
            Path path = event.context();
            if (key == state.getPasswordsWatchKey()
                    && path.equals(this.passwordsFilePath.getFileName())
                    && !passwordsEventHandled) {
                reloadPasswords(state);
                passwordsEventHandled = true;
            } else if (key == state.getKeystoreWatchKey()
                    && path.equals(this.keystoreFilePath.getFileName())
                    && !keystoreEventHandled) {
                reloadKeystore(state);
                keystoreEventHandled = true;
            }
        }
        key.reset();
    }

    private void reloadKeystore(final KeystoreUpdateState state) {
        LOGGER.info("Keystore file update detected");
        state.setKeystoreUpdated(true);
        if (state.isPasswordsUpdated()) {
            loadUpdatedKeystore(state);
        }
    }

    private void reloadPasswords(final KeystoreUpdateState state) {
        LOGGER.info("Passwords file update detected, reloading passwords");
        try {
            Map<UUID, byte[]> passwords = keystoreService.loadPasswords(passwordsFilePath);
            state.setPasswordsCache(passwords);
            state.setPasswordsUpdated(true);
        } catch (CryptoException e) {
            LOGGER.warn("Updated passwords can't be loaded", e);
            return;
        }
        LOGGER.info("Passwords reloaded successfully.");
        if (state.isKeystoreUpdated()) {
            loadUpdatedKeystore(state);
        }
    }

    private void loadUpdatedKeystore(final KeystoreUpdateState state) {
        LOGGER.info("Reloading keystore");
        try {
            UUID keystorePasswordId = UUID.fromString(securityConfig.getKeystorePasswordAlias());
            byte[] password = state.getPasswordsCache().get(keystorePasswordId);
            KeyStore updatedKeystore = keystoreService.loadKeystore(this.keystoreFilePath, password);
            state.setKeyStore(updatedKeystore);
            LOGGER.info("Keystore reloaded successfully");
        } catch (CryptoException e) {
            LOGGER.error("Updated keystore can't be opened using updated password", e);
        }
    }

    private void applyKeystoreUpdate(final KeystoreUpdateState state) {
        lock.writeLock().lock();
        keyStore = state.getKeyStore();
        passwordsCache = state.getPasswordsCache();
        keysCache.clear();
        latestKeyId = keystoreService.getLatestKeyId(keyStore);
        keysCache.computeIfAbsent(latestKeyId, this::loadKey); // Cache latest key early
        state.reset();
        lock.writeLock().unlock();
    }
}
