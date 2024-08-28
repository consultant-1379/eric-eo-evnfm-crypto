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

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ericsson.eo.evnfm.crypto.migration.exceptions.CryptoException;
import com.ericsson.eo.evnfm.crypto.migration.presentation.services.CipherVersionService;
import com.ericsson.eo.evnfm.crypto.migration.security.SecurityProvider;
import com.ericsson.eo.evnfm.crypto.migration.util.Constants;
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.password.PasswordsCipher;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.CipherKey;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.SecurityConfig;
import com.ericsson.eo.evnfm.crypto.migration.presentation.services.KeystoreService;

@Service
public class KeystoreServiceImpl implements KeystoreService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeystoreServiceImpl.class);

    private static final Base64.Encoder PASSWORD_ENCODER = Base64.getEncoder().withoutPadding();

    private final CipherVersionService<PasswordsCipher> versionService;

    private final SecurityConfig securityConfig;

    private final SecurityProvider securityProvider;

    @Autowired
    public KeystoreServiceImpl(CipherVersionService<PasswordsCipher> versionService,
                               SecurityConfig securityConfig,
                               SecurityProvider securityProvider) {
        this.versionService = versionService;
        this.securityConfig = securityConfig;
        this.securityProvider = securityProvider;
    }

    public Map<UUID, byte[]> initPasswords(Path path) {
        Map<UUID, byte[]> passwords = new HashMap<>();
        passwords.put(UUID.fromString(securityConfig.getKeystorePasswordAlias()), createPassword());
        savePasswords(passwords, path);
        return passwords;
    }

    public byte[] createPassword() {
        byte[] passwordBytes = new byte[securityConfig.getPasswordLength()];
        securityProvider.getSecureRandom().nextBytes(passwordBytes);
        return passwordBytes;
    }

    public void savePasswords(Map<UUID, byte[]> passwords, Path path) {
        PasswordsCipher passwordsCipher = versionService.getLatestCipher();
        byte[] passwordData = passwordsCipher.encrypt(passwords);
        try (OutputStream passwordsOutputStream = Base64.getEncoder().wrap(Files.newOutputStream(path, CREATE, WRITE, TRUNCATE_EXISTING))) {
            passwordsOutputStream.write(passwordData);
        } catch (IOException e) {
            final String message = String.format("Unable to write passwords file %s", path);
            LOGGER.error(message, e);
            throw new CryptoException(message, e);
        }
    }

    public Map<UUID, byte[]> loadPasswords(Path path) {
        byte[] rawData;
        try {
            rawData = Files.readAllBytes(path);
        } catch (IOException ioe) {
            throw new CryptoException("Error loading keystore password", ioe);
        }
        byte[] passwordData = Base64.getDecoder().decode(rawData);
        ByteBuffer buffer = ByteBuffer.wrap(passwordData);

        checkSignature(buffer.getInt());
        short version = buffer.getShort();
        PasswordsCipher passwordsCipher = versionService.getCipher(version);
        LOGGER.info("Found passwords decoder for version {}.{}", version >> 8, version & 0xFF);
        Map<UUID, byte[]> passwords = passwordsCipher.decrypt(passwordData);
        LOGGER.info("Passwords decoded successfully");

        if (version != securityConfig.getLatestPasswordsObscurityVersion()) {
            savePasswords(passwords, path);
        }
        return passwords;
    }

    public void checkSignature(final int signature) {
        if (signature != securityConfig.getPasswordsFileSignature()) {
            LOGGER.error(Constants.ERROR_BAD_PASSWORDS_SIGNATURE);
            throw new CryptoException(Constants.ERROR_BAD_PASSWORDS_SIGNATURE);
        }
        LOGGER.info("Passwords file signature verified");
    }

    public KeyStore loadKeystore(Path keystorePath, byte[] password) {
        try (InputStream keystoreInputStream = Files.newInputStream(keystorePath, READ)) {
            KeyStore keyStore = KeyStore.getInstance(securityConfig.getKeystoreType());
            keyStore.load(keystoreInputStream, encodePassword(password));
            return keyStore;
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException e) {
            LOGGER.error(Constants.ERROR_OPENING_KEYSTORE, e);
            throw new CryptoException(Constants.ERROR_OPENING_KEYSTORE, e);
        }
    }

    public char[] encodePassword(byte[] password) {
        return PASSWORD_ENCODER.encodeToString(password).toCharArray();
    }

    public KeyStore initKeystore(Path keystorePath, byte[] password) {
        try (OutputStream keystoreOutputStream = Files.newOutputStream(keystorePath, CREATE, WRITE, TRUNCATE_EXISTING)) {
            KeyStore keyStore = KeyStore.getInstance(securityConfig.getKeystoreType());
            char[] actualPassword = encodePassword(password);
            keyStore.load(null, actualPassword);
            keyStore.store(keystoreOutputStream, actualPassword);
            return keyStore;
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            LOGGER.error(Constants.ERROR_CREATING_KEYSTORE, e);
            throw new CryptoException(Constants.ERROR_CREATING_KEYSTORE, e);
        }
    }

    public CipherKey createKey(final KeyStore keyStore, final Map<UUID, byte[]> passwords) {
        final UUID keyId = UUID.randomUUID();
        final String alias = keyId.toString();
        byte[] password = createPassword();
        SecretKey key = createSecretKey();
        KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(key);
        try {
            keyStore.setEntry(alias, secretKeyEntry, new KeyStore.PasswordProtection(encodePassword(password)));
            passwords.put(keyId, password);
            LocalDateTime created = keyStore.getCreationDate(alias).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
            return new CipherKey(keyId, key, created, null);
        } catch (KeyStoreException e) {
            LOGGER.error(Constants.ERROR_ADDING_KEY_TO_KEYSTORE, e);
            throw new CryptoException(Constants.ERROR_ADDING_KEY_TO_KEYSTORE, e);
        }
    }
    public CipherKey createKey() {
        final var keyId = UUID.randomUUID();
        var key = createSecretKey();
            var created = LocalDateTime.now(ZoneId.systemDefault());
            return new CipherKey(keyId, key, created, null);
    }

    public UUID getLatestKeyId(final KeyStore keyStore) {
        Date latest = new Date(0L);
        UUID latestKeyId = null;
        try {
            Enumeration<String> aliases = keyStore.aliases();
            while (aliases.hasMoreElements()) {
                final String alias = aliases.nextElement();
                UUID keyId = UUID.fromString(alias);
                Date created = keyStore.getCreationDate(alias);
                if (created.after(latest)) {
                    latest = created;
                    latestKeyId = keyId;
                }
            }
        } catch (KeyStoreException e) {
            LOGGER.error(Constants.UNABLE_TO_ENUMERATE_KEYS, e);
            throw new CryptoException(Constants.UNABLE_TO_ENUMERATE_KEYS, e);
        }
        if (latestKeyId == null) {
            throw new CryptoException("No valid keys found in keystore");
        }
        return latestKeyId;
    }

    public void saveKeystore(final KeyStore keyStore, final Path keystorePath, byte[] password) {
        try (OutputStream keystoreOutputStream = Files.newOutputStream(keystorePath, CREATE, WRITE, TRUNCATE_EXISTING)) {
            keyStore.store(keystoreOutputStream, encodePassword(password));
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            LOGGER.error(Constants.ERROR_SAVING_KEYSTORE, e);
            throw new CryptoException(Constants.ERROR_SAVING_KEYSTORE, e);
        }
    }

    SecretKey createSecretKey() {
        KeyGenerator keyGenerator;

        try {
            keyGenerator = KeyGenerator.getInstance(securityConfig.getEncryptionAlgorithm());
            keyGenerator.init(securityConfig.getCipherKeyLength(), SecureRandom.getInstanceStrong());
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("Failed to create cipher key generator", e);
        }

        return keyGenerator.generateKey();
    }
}
