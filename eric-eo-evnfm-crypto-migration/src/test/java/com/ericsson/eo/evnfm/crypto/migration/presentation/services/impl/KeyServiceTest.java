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

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import com.ericsson.eo.evnfm.crypto.migration.TestConstants;
import com.ericsson.eo.evnfm.crypto.migration.TestUtils;
import com.ericsson.eo.evnfm.crypto.migration.exceptions.CryptoException;
import com.ericsson.eo.evnfm.crypto.migration.exceptions.UnknownKeyException;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.CipherKey;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.KeystoreUpdateState;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.SecurityConfig;
import com.ericsson.eo.evnfm.crypto.migration.presentation.services.KeystoreService;
import com.ericsson.eo.evnfm.crypto.migration.presentation.services.kms.KmsKeyStoreService;
@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
public class KeyServiceTest {

    private static final SecurityConfig SECURITY_CONFIG = TestUtils.initSecurityConfig();

    private final KeystoreService keystoreService = Mockito.mock(KeystoreService.class);

    private final KmsKeyStoreService kmsKeyStoreService = Mockito.mock(KmsKeyStoreService.class);
    private final UUID latestKeyId = UUID.randomUUID();
    private final ConcurrentMap<UUID, CipherKey> keysCache = new ConcurrentHashMap<>();

    private final Map<UUID, byte[]> passwordsCache = getPasswordsCache();

    @Mock
    private KeyStore keyStore;

    @Mock
    private Path passwordsFilePath;

    @Mock
    private Path keystoreFilePath;

    @Mock
    private ExecutorService executorService;

    private final SecurityConfig securityConfig = TestUtils.initSecurityConfig();

    private final KeyServiceImpl keyService = new KeyServiceImpl(keystoreService, securityConfig, kmsKeyStoreService);

    @BeforeEach
    public void setup() {
        // object fields should be set manually, since there are no setters present
        ReflectionTestUtils.setField(keyService, "keyStore", keyStore);
        ReflectionTestUtils.setField(keyService, "passwordsFilePath", passwordsFilePath);
        ReflectionTestUtils.setField(keyService, "keystoreFilePath", keystoreFilePath);
        ReflectionTestUtils.setField(keyService, "executorService", executorService);
        ReflectionTestUtils.setField(keyService, "latestKeyId", latestKeyId);
        ReflectionTestUtils.setField(keyService, "keysCache", keysCache);
        ReflectionTestUtils.setField(keyService, "passwordsCache", passwordsCache);
    }

    @Test
    public void shouldThrowExceptionWhenLoadingKeystore() {
        File passwordsFileMock = Mockito.mock(File.class);
        File keystoreFileMock = Mockito.mock(File.class);

        Mockito.when(passwordsFileMock.exists()).thenReturn(true);
        Mockito.when(keystoreFileMock.exists()).thenReturn(false);

        Mockito.when(passwordsFilePath.toFile()).thenReturn(passwordsFileMock);
        Mockito.when(keystoreFilePath.toFile()).thenReturn(keystoreFileMock);

        Mockito.when(keystoreService.loadPasswords(passwordsFilePath)).thenReturn(passwordsCache);

        Assertions.assertThrows(CryptoException.class, keyService::init);
        Mockito.verify(keystoreService, Mockito.never()).loadKeystore(Mockito.eq(keystoreFilePath), Mockito.any());
        Mockito.verify(executorService, Mockito.never()).submit(Mockito.any(Runnable.class));
    }

    @Test
    public void shouldLoadKeystore() {
        File passwordsFileMock = Mockito.mock(File.class);
        File keystoreFileMock = Mockito.mock(File.class);

        Mockito.when(passwordsFileMock.exists()).thenReturn(true);
        Mockito.when(keystoreFileMock.exists()).thenReturn(true);

        Mockito.when(passwordsFilePath.toFile()).thenReturn(passwordsFileMock);
        Mockito.when(keystoreFilePath.toFile()).thenReturn(keystoreFileMock);

        Mockito.when(keystoreService.loadPasswords(passwordsFilePath)).thenReturn(passwordsCache);

        keyService.init();

        Mockito.verify(keystoreService, Mockito.times(1))
                .loadKeystore(keystoreFilePath, TestConstants.PASSWORD_VALUE);
        Mockito.verify(keystoreService, Mockito.times(1)).getLatestKeyId(Mockito.any());
        Mockito.verify(executorService, Mockito.times(1)).submit(Mockito.any(Runnable.class));
    }

    @Test
    public void shouldInitKeystore() {
        File passwordsFileMock = Mockito.mock(File.class);

        Mockito.when(passwordsFileMock.exists()).thenReturn(false);
        Mockito.when(passwordsFilePath.toFile()).thenReturn(passwordsFileMock);

        Mockito.when(keystoreService.initPasswords(passwordsFilePath)).thenReturn(passwordsCache);
        Mockito.when(keystoreService.initKeystore(keystoreFilePath, TestConstants.PASSWORD_VALUE))
                .thenReturn(keyStore);

        Mockito.when(keystoreService.createKey(keyStore, passwordsCache))
                .thenReturn(TestUtils.generateCipherKey());

        keyService.init();

        Mockito.verify(keystoreService, Mockito.times(1))
                .savePasswords(passwordsCache, passwordsFilePath);
        Mockito.verify(keystoreService, Mockito.times(1))
                .saveKeystore(keyStore, keystoreFilePath, TestConstants.PASSWORD_VALUE);
        Mockito.verify(executorService, Mockito.times(1))
                .submit(Mockito.any(Runnable.class));
    }

    @Test
    public void shouldSetKeystoreLocation() {
        String keystorePath = "/path/to/keystore";
        keyService.setKeystoreLocation(keystorePath);

        Path keystoreFilePath = (Path) ReflectionTestUtils.getField(keyService, "keystoreFilePath");
        Assertions.assertEquals(Paths.get(
                keystorePath, SECURITY_CONFIG.getKeystoreFile()), keystoreFilePath);
    }

    @Test
    public void shouldSetPasswordLocation() {
        String passwordsPath = "/path/to/passwords";
        keyService.setPasswordLocation(passwordsPath);

        Path passwordsFilePath = (Path) ReflectionTestUtils.getField(keyService, "passwordsFilePath");
        Assertions.assertEquals(Paths.get(
                passwordsPath, SECURITY_CONFIG.getPasswordsFile()), passwordsFilePath);
    }

    @Test
    public void shouldGetKeyByLatestKeyId() {
        UUID requestedKeyId = UUID.randomUUID();
        UUID latestKeyId = UUID.randomUUID();
        ConcurrentMap<UUID, CipherKey> keysCache = getKeysCache(requestedKeyId, latestKeyId);

        ReflectionTestUtils.setField(keyService, "latestKeyId", latestKeyId);
        ReflectionTestUtils.setField(keyService, "keysCache", keysCache);

        CipherKey key = keyService.getKey(null);

        Assertions.assertNotNull(key);
        Assertions.assertEquals(keysCache.get(latestKeyId), key);
    }

    @Test
    public void shouldGetKeyByRequestedKeyId() {
        UUID requestedKeyId = UUID.randomUUID();
        UUID latestKeyId = UUID.randomUUID();
        ConcurrentMap<UUID, CipherKey> keysCache = getKeysCache(requestedKeyId, latestKeyId);

        ReflectionTestUtils.setField(keyService, "latestKeyId", latestKeyId);
        ReflectionTestUtils.setField(keyService, "keysCache", keysCache);

        CipherKey key = keyService.getKey(requestedKeyId);

        Assertions.assertNotNull(key);
        Assertions.assertEquals(keysCache.get(requestedKeyId), key);
    }

    @Test
    public void shouldCreateKey() {
        CipherKey cipherKey = TestUtils.generateCipherKey();

        Mockito.when(keystoreService.createKey(Mockito.eq(keyStore), Mockito.any())).thenReturn(cipherKey);

        CipherKey cipherKeyResult = keyService.createKey();
        UUID latestKeyIdResult = (UUID) ReflectionTestUtils.getField(keyService, "latestKeyId");

        Assertions.assertEquals(cipherKey, cipherKeyResult);
        Assertions.assertEquals(cipherKey.getAlias(), latestKeyIdResult);

        Mockito.verify(keystoreService, Mockito.times(1))
                .savePasswords(Mockito.any(), Mockito.eq(passwordsFilePath));
        Mockito.verify(keystoreService, Mockito.times(1))
                .saveKeystore(keyStore, keystoreFilePath, TestConstants.PASSWORD_VALUE);
    }

    @Test
    public void shouldSetupWatchKeys() throws IOException {
        WatchService watchServiceMock = Mockito.mock(WatchService.class);
        KeystoreUpdateState state = new KeystoreUpdateState();

        WatchKey passwordWatchKeyMock = Mockito.mock(WatchKey.class);
        WatchKey keystoreWatchKeyMock = Mockito.mock(WatchKey.class);

        Mockito.when(passwordsFilePath.toAbsolutePath()).thenReturn(passwordsFilePath);
        Mockito.when(keystoreFilePath.toAbsolutePath()).thenReturn(keystoreFilePath);

        Mockito.when(passwordsFilePath.getParent()).thenReturn(passwordsFilePath);
        Mockito.when(keystoreFilePath.getParent()).thenReturn(keystoreFilePath);

        Mockito.when(passwordsFilePath.register(
                Mockito.any(WatchService.class),
                Mockito.eq(ENTRY_CREATE),
                Mockito.eq(ENTRY_MODIFY))).thenReturn(passwordWatchKeyMock);
        Mockito.when(keystoreFilePath.register(
                Mockito.any(WatchService.class),
                Mockito.eq(ENTRY_CREATE),
                Mockito.eq(ENTRY_MODIFY))).thenReturn(keystoreWatchKeyMock);

        keyService.setupWatchKeys(watchServiceMock, state);

        Assertions.assertEquals(passwordWatchKeyMock, state.getPasswordsWatchKey());
        Assertions.assertEquals(keystoreWatchKeyMock, state.getKeystoreWatchKey());
    }

    @Test
    public void shouldSetupWatchKeysWhenParentDirIsTheSame() throws IOException {
        WatchService watchServiceMock = Mockito.mock(WatchService.class);
        KeystoreUpdateState state = new KeystoreUpdateState();

        WatchKey passwordWatchKeyMock = Mockito.mock(WatchKey.class);

        Mockito.when(passwordsFilePath.toAbsolutePath()).thenReturn(passwordsFilePath);
        Mockito.when(keystoreFilePath.toAbsolutePath()).thenReturn(keystoreFilePath);

        Mockito.when(passwordsFilePath.getParent()).thenReturn(passwordsFilePath);
        Mockito.when(keystoreFilePath.getParent()).thenReturn(passwordsFilePath);

        Mockito.when(passwordsFilePath.register(
                Mockito.any(WatchService.class),
                Mockito.eq(ENTRY_CREATE),
                Mockito.eq(ENTRY_MODIFY))).thenReturn(passwordWatchKeyMock);

        keyService.setupWatchKeys(watchServiceMock, state);

        Assertions.assertEquals(passwordWatchKeyMock, state.getPasswordsWatchKey());
        Assertions.assertEquals(passwordWatchKeyMock, state.getKeystoreWatchKey());
    }

    @Test
    @SuppressWarnings({"unchecked"})
    public void shouldHandlePasswordsFileChanges() throws InterruptedException {
        UUID alias = UUID.fromString(SECURITY_CONFIG.getKeystorePasswordAlias());
        byte[] password = TestConstants.KEYSTORE_PASSWORD.getBytes();
        Map<UUID, byte[]> passwords = Map.of(alias, password);

        WatchEvent<Path> passwordsWatchEventMock = Mockito.mock(WatchEvent.class);
        WatchKey passwordsWatchKeyMock = Mockito.mock(WatchKey.class);

        WatchService watchServiceMock = Mockito.mock(WatchService.class);
        KeystoreUpdateState state = new KeystoreUpdateState();

        state.setPasswordsWatchKey(passwordsWatchKeyMock);

        Mockito.when(passwordsFilePath.getFileName()).thenReturn(passwordsFilePath);
        Mockito.when(passwordsWatchEventMock.context()).thenReturn(passwordsFilePath);
        Mockito.when(passwordsWatchEventMock.kind()).thenReturn(StandardWatchEventKinds.ENTRY_MODIFY);
        Mockito.when(passwordsWatchKeyMock.pollEvents()).thenReturn(List.of(passwordsWatchEventMock));
        Mockito.when(watchServiceMock.take()).thenReturn(passwordsWatchKeyMock);
        Mockito.when(keystoreService.loadPasswords(passwordsFilePath)).thenReturn(passwords);

        boolean result = keyService.awaitAndHandleFileChanges(watchServiceMock, state);

        Assertions.assertFalse(result);
        Assertions.assertEquals(passwords, state.getPasswordsCache());
        Assertions.assertEquals(passwordsWatchKeyMock, state.getPasswordsWatchKey());
        Assertions.assertTrue(state.isPasswordsUpdated());
    }

    @Test
    @SuppressWarnings({"unchecked"})
    public void shouldHandleKeystoreFileChanges() throws InterruptedException {
        UUID alias = UUID.fromString(SECURITY_CONFIG.getKeystorePasswordAlias());
        byte[] password = TestConstants.KEYSTORE_PASSWORD.getBytes();
        Map<UUID, byte[]> passwords = Map.of(alias, password);

        KeyStore keyStoreMock = Mockito.mock(KeyStore.class);
        WatchEvent<Path> keystoreWatchEventMock = Mockito.mock(WatchEvent.class);
        WatchKey keystoreWatchKeyMock = Mockito.mock(WatchKey.class);

        WatchService watchServiceMock = Mockito.mock(WatchService.class);
        KeystoreUpdateState state = new KeystoreUpdateState();

        state.setKeystoreWatchKey(keystoreWatchKeyMock);
        state.setPasswordsUpdated(true);
        state.setPasswordsCache(passwords);

        Mockito.when(keystoreWatchEventMock.context()).thenReturn(keystoreFilePath);
        Mockito.when(keystoreWatchEventMock.kind()).thenReturn(StandardWatchEventKinds.ENTRY_MODIFY);
        Mockito.when(keystoreFilePath.getFileName()).thenReturn(keystoreFilePath);
        Mockito.when(keystoreWatchKeyMock.pollEvents()).thenReturn(List.of(keystoreWatchEventMock));
        Mockito.when(watchServiceMock.take()).thenReturn(keystoreWatchKeyMock);
        Mockito.when(keystoreService.loadKeystore(keystoreFilePath, password)).thenReturn(keyStoreMock);
        Mockito.when(keystoreService.getLatestKeyId(keyStoreMock)).thenReturn(alias);

        KeyServiceImpl keyServiceSpy = Mockito.spy(keyService);
        Mockito.doReturn(TestUtils.generateCipherKey()).when(keyServiceSpy).loadKey(alias);

        boolean result = keyServiceSpy.awaitAndHandleFileChanges(watchServiceMock, state);

        Assertions.assertFalse(result);
        Assertions.assertFalse(state.isPasswordsUpdated());
        Assertions.assertFalse(state.isKeystoreUpdated());
        Assertions.assertNull(state.getPasswordsCache());
        Assertions.assertNull(state.getKeyStore());
        Assertions.assertNull(state.getPasswordsWatchKey());
        Assertions.assertEquals(keystoreWatchKeyMock, state.getKeystoreWatchKey());
    }

    @Test
    public void shouldThrowUnknownKeyExceptionOnLoadKey() {
        UUID keyId = UUID.randomUUID();

        UnknownKeyException exception = Assertions.assertThrows(UnknownKeyException.class, () -> {
            keyService.loadKey(keyId);
        });

        Assertions.assertEquals(String.format("No mapping set for key %s", keyId), exception.getMessage());
    }

    @Test
    public void shouldThrowExceptionOnLoadKey()
            throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException {

        UUID keyId = UUID.fromString(SECURITY_CONFIG.getKeystorePasswordAlias());

        Mockito.when(keystoreService.encodePassword(TestConstants.PASSWORD_VALUE))
                .thenReturn(("AAGSklKKu4FKFocvHdxY9tgxp+jXzJrtEz3zUc7QIKciEDrpFT6nnFBagdoTvYFrB2OMm2WeDuDkEyxRTRx130y" +
                        "0jRjM0ZNL6llJiN4IQnnAFg==").toCharArray());
        Mockito.when(keyStore.getEntry(Mockito.any(), Mockito.any())).thenReturn(null);

        UnknownKeyException exception = Assertions.assertThrows(UnknownKeyException.class, () -> {
            keyService.loadKey(keyId);
        });

        Assertions.assertEquals(String.format("Key %s not found in keystore", keyId), exception.getMessage());
    }

    @Test
    public void shouldThrowNoSuchAlgorithmExceptionOnLoadKey()
            throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException {

        UUID keyId = UUID.fromString(SECURITY_CONFIG.getKeystorePasswordAlias());

        Mockito.when(keystoreService.encodePassword(TestConstants.PASSWORD_VALUE))
                .thenReturn(("AAGSklKKu4FKFocvHdxY9tgxp+jXzJrtEz3zUc7QIKciEDrpFT6nnFBagdoTvYFrB2OMm2WeDuDkEyxRTRx130y" +
                        "0jRjM0ZNL6llJiN4IQnnAFg==").toCharArray());
        Mockito.when(keyStore.getEntry(Mockito.any(), Mockito.any())).thenThrow(NoSuchAlgorithmException.class);

        Assertions.assertThrows(CryptoException.class, () -> keyService.loadKey(keyId));
    }

    @Test
    public void shouldLoadKey() throws UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException {
        UUID keyId = UUID.fromString(SECURITY_CONFIG.getKeystorePasswordAlias());
        SecretKey secretKey = new SecretKeySpec(TestConstants.PASSWORD_VALUE, securityConfig.getEncryptionAlgorithm());

        final KeyStore.SecretKeyEntry entry = Mockito.mock(KeyStore.SecretKeyEntry.class);
        Mockito.when(entry.getSecretKey()).thenReturn(secretKey);

        Mockito.when(keystoreService.encodePassword(TestConstants.PASSWORD_VALUE))
                .thenReturn(("AAGSklKKu4FKFocvHdxY9tgxp+jXzJrtEz3zUc7QIKciEDrpFT6nnFBagdoTvYFrB2OMm2WeDuDkEyxRTRx130y" +
                        "0jRjM0ZNL6llJiN4IQnnAFg==").toCharArray());
        Mockito.when(keyStore.getEntry(Mockito.any(), Mockito.any())).thenReturn(entry);
        Mockito.when(keyStore.getCreationDate(keyId.toString())).thenReturn(new Date());

        CipherKey cipherKey = keyService.loadKey(keyId);

        Assertions.assertNotNull(cipherKey);
        Assertions.assertEquals(keyId, cipherKey.getAlias());
    }

    private ConcurrentMap<UUID, CipherKey> getKeysCache(UUID requestedKeyId, UUID latestKeyId) {
        return new ConcurrentHashMap<>(Map.of(
                latestKeyId, TestUtils.generateCipherKey(),
                requestedKeyId, TestUtils.generateCipherKey()));
    }

    private Map<UUID, byte[]> getPasswordsCache() {
        return Map.of(UUID.fromString(SECURITY_CONFIG.getKeystorePasswordAlias()), TestConstants.PASSWORD_VALUE);
    }
}
