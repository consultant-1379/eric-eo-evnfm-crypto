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
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.nio.file.StandardOpenOption.WRITE;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ericsson.eo.evnfm.crypto.migration.TestConstants;
import com.ericsson.eo.evnfm.crypto.migration.TestUtils;
import com.ericsson.eo.evnfm.crypto.migration.exceptions.CryptoException;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.CipherKey;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.SecurityConfig;
import com.ericsson.eo.evnfm.crypto.migration.presentation.services.CipherVersionService;
import com.ericsson.eo.evnfm.crypto.migration.security.SecurityProvider;
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.password.PasswordsCipher;

@ExtendWith(MockitoExtension.class)
public class KeystoreServiceTest {

    @Mock
    private CipherVersionService<PasswordsCipher> versionService;

    private SecurityConfig securityConfig;

    private SecurityProvider securityProvider;

    private KeystoreServiceImpl keystoreService;

    @BeforeEach
    public void setupService() {
        securityConfig = TestUtils.initSecurityConfig();
        securityProvider = new SecurityProvider(securityConfig);
        keystoreService = new KeystoreServiceImpl(versionService, securityConfig, securityProvider);
    }

    @Test
    public void shouldInitPasswords() {
        PasswordsCipher passwordsCipherMock = Mockito.mock(PasswordsCipher.class);
        Mockito.when(passwordsCipherMock.encrypt(Mockito.anyMap())).thenReturn(new byte[] {'a', 'b', 'c'});
        Mockito.when(versionService.getLatestCipher()).thenReturn(passwordsCipherMock);

        Path pathToSavePasswords = TestUtils.getPasswordsPath();
        Map<UUID, byte[]> passwords = keystoreService.initPasswords(pathToSavePasswords);
        assertFalse(passwords.isEmpty());
    }

    @Test
    public void shouldCreatePassword() {
        byte[] password = keystoreService.createPassword();
        assertTrue(password.length > 0);
    }

    @Test
    public void shouldSavePasswords() {
        PasswordsCipher passwordsCipherMock = Mockito.mock(PasswordsCipher.class);
        Mockito.when(passwordsCipherMock.encrypt(Mockito.anyMap())).thenReturn(new byte[] {'a', 'b', 'c'});
        Mockito.when(versionService.getLatestCipher()).thenReturn(passwordsCipherMock);

        Map<UUID, byte[]> passwords = Map.of(UUID.randomUUID(), new byte[] {'a', 'b', 'c'});
        Path pathToSavePasswords = TestUtils.getPasswordsPath();
        keystoreService.savePasswords(passwords, pathToSavePasswords);

        Mockito.verify(versionService, Mockito.times(1)).getLatestCipher();
    }

    @Test
    public void shouldLoadPasswords() {
        Path passwordsPath = TestUtils.getPasswordsPath();

        // prepare a passwords file for the test: write a password into the file
        try (OutputStream passwordsOutputStream = Base64.getEncoder()
                .wrap(Files.newOutputStream(passwordsPath, CREATE, WRITE, TRUNCATE_EXISTING))) {

            passwordsOutputStream.write(TestConstants.KEYSTORE_PASSWORD.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(TestConstants.TEST_FAIL_ERROR_MESSAGE);
        }

        // set the right password signature for the password used for this test
        securityConfig.setPasswordsFileSignature(TestConstants.PASSWORDS_SIGNATURE);

        PasswordsCipher passwordsCipherMock = Mockito.mock(PasswordsCipher.class);
        Mockito.when(passwordsCipherMock.decrypt(Mockito.any()))
                .thenReturn(Map.of(UUID.randomUUID(), new byte[] {'a', 'b', 'c'}));
        Mockito.when(passwordsCipherMock.encrypt(Mockito.anyMap())).thenReturn(new byte[] {'a', 'b', 'c'});
        Mockito.when(versionService.getLatestCipher()).thenReturn(passwordsCipherMock);
        Mockito.when(versionService.getCipher(Mockito.anyShort())).thenReturn(passwordsCipherMock);

        Map<UUID, byte[]> passwords = keystoreService.loadPasswords(passwordsPath);

        assertFalse(passwords.isEmpty());
    }

    @Test
    public void shouldThrowExceptionWhenCheckSignature() {
        assertThrows(CryptoException.class, () -> {
            securityConfig.setPasswordsFileSignature(12);
            keystoreService.checkSignature(10);
        });
    }

    @Test
    public void shouldLoadKeystore() throws KeyStoreException {
        Path keystorePath = TestUtils.getKeystorePath();
        byte[] password = TestConstants.KEYSTORE_ENCODED_PASSWORD.getBytes();
        KeyStore keyStore = keystoreService.loadKeystore(keystorePath, password);

        assertNotNull(keyStore);
        assertEquals(TestConstants.KEYSTORE_TYPE, keyStore.getType());
        assertEquals(TestConstants.SECRET_KEY_ALIAS, keyStore.aliases().nextElement());
    }

    @Test
    public void shouldCreateKey() {
        Path keystorePath = TestUtils.getKeystorePath();
        byte[] password = TestConstants.KEYSTORE_ENCODED_PASSWORD.getBytes();
        KeyStore keyStore = keystoreService.loadKeystore(keystorePath, password);
        Map<UUID, byte[]> passwords = new HashMap<>();

        CipherKey cipherKey = keystoreService.createKey(keyStore, passwords);

        assertNotNull(cipherKey);
        assertFalse(passwords.isEmpty());

        Map.Entry<UUID, byte[]> record = passwords.entrySet().iterator().next();
        UUID generatedKey = record.getKey();
        byte[] generatedPassword = record.getValue();

        assertNotNull(cipherKey.getKey());
        assertEquals(generatedKey, cipherKey.getAlias());
        assertNotNull(cipherKey.getCreated());
        assertNull(cipherKey.getCreator());
    }

    @Test
    public void shouldGetLatestKeyId() {
        Path keystorePath = TestUtils.getKeystorePath();
        byte[] password = TestConstants.KEYSTORE_ENCODED_PASSWORD.getBytes();
        KeyStore keyStore = keystoreService.loadKeystore(keystorePath, password);

        UUID latestKeyId = keystoreService.getLatestKeyId(keyStore);

        assertNotNull(latestKeyId);
        assertEquals(UUID.fromString(TestConstants.SECRET_KEY_ALIAS), latestKeyId);
    }

    @Test
    public void shouldSaveKeystore() {
        Path keystorePath = TestUtils.getKeystorePath();
        byte[] password = TestConstants.KEYSTORE_ENCODED_PASSWORD.getBytes();
        KeyStore keyStore = keystoreService.loadKeystore(keystorePath, password);

        keystoreService.saveKeystore(keyStore, TestUtils.getKeystorePath(),
                                     TestConstants.KEYSTORE_ENCODED_PASSWORD.getBytes());
    }

    @Test
    public void shouldCreateSecretKey() {
        SecretKey secretKey = keystoreService.createSecretKey();
        assertNotNull(secretKey);
    }

    @Test
    public void shouldThrowExceptionWhenWrongAlgorithmOnCreateSecretKey() {
        assertThrows(CryptoException.class, () -> {
            securityConfig.setEncryptionAlgorithm("NON_EXISTING");
            keystoreService.createSecretKey();
        });
    }
}
