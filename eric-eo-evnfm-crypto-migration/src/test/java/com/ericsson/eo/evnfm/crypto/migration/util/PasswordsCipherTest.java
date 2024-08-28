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
package com.ericsson.eo.evnfm.crypto.migration.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.ericsson.eo.evnfm.crypto.migration.TestUtils;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.SecurityConfig;
import com.ericsson.eo.evnfm.crypto.migration.presentation.services.KeystoreService;
import com.ericsson.eo.evnfm.crypto.migration.security.SecurityProvider;
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.password.PasswordsCipher;
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.password.PasswordsCipher1v0;
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.password.PasswordsCipher1v1;
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.password.PasswordsCipher1v2;
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.pool.PasswordsCipherPool;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.cloud.kubernetes.enabled = false",
        "spring.cloud.vault.kubernetes.service-account-token-file=src/test/resources/token",
        "evnfm.crypto.paths.keystore=./",
        "evnfm.crypto.paths.password=./"
})
public class PasswordsCipherTest {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_NUMBER_OF_PASSWORDS = 3;
    private static final SecurityConfig SECURITY_CONFIG = TestUtils.initSecurityConfig();
    private static final SecurityProvider SECURITY_PROVIDER = new SecurityProvider(SECURITY_CONFIG);

    @Autowired
    private KeystoreService keystoreService;
    @Autowired
    private PasswordsCipherPool pool;

    private static List<Pair<UUID, byte[]>> testPasswordEntries;

    @BeforeAll
    public static void setup() {
        testPasswordEntries = new ArrayList<>(MAX_NUMBER_OF_PASSWORDS);
        for (int i = 0; i < MAX_NUMBER_OF_PASSWORDS; ++i) {
            testPasswordEntries.add(generateRandomPasswordEntry());
        }
    }

    @Test
    public void testEncryptDecrypt() {
        List<PasswordsCipher> passwordsCiphers = Arrays.asList(
                new PasswordsCipher1v0(keystoreService, SECURITY_CONFIG, SECURITY_PROVIDER),
                new PasswordsCipher1v2(pool, keystoreService, SECURITY_CONFIG, SECURITY_PROVIDER)
        );
        for (int passwordsMapSize = 1; passwordsMapSize < testPasswordEntries.size(); ++passwordsMapSize) {
            Map<UUID, byte[]> passwordsMap = getPasswordsMap(testPasswordEntries, passwordsMapSize);
            for (PasswordsCipher passwordsCipher : passwordsCiphers) {
                runEncryptDecryptTest(passwordsCipher, passwordsMap);
            }
        }
    }

    @Test
    public void testEncryptDecryptPasswordsCipher1v1() {
        PasswordsCipher passwordsCipher = new PasswordsCipher1v1(pool, keystoreService, SECURITY_CONFIG, SECURITY_PROVIDER);
        Map<UUID, byte[]> passwordsMap = getPasswordsMap(testPasswordEntries, 2);
        runEncryptDecryptTest(passwordsCipher, passwordsMap);
    }

    private static Pair<UUID, byte[]> generateRandomPasswordEntry() {
        UUID passwordUUID = new UUID(RANDOM.nextLong(), RANDOM.nextLong());
        byte[] password = new byte[SECURITY_CONFIG.getPasswordLength()];
        RANDOM.nextBytes(password);
        return Pair.of(passwordUUID, password);
    }

    private static void runEncryptDecryptTest(PasswordsCipher passwordsCipher, Map<UUID, byte[]> passwordsMap) {
        Map<UUID, byte[]> decryptedPasswordsMap = passwordsCipher.decrypt(passwordsCipher.encrypt(passwordsMap));
        assertEqualMap(passwordsMap, decryptedPasswordsMap);
    }

    private static void assertEqualMap(Map<UUID, byte[]> expected, Map<UUID, byte[]> actual) {
        assertEquals(expected.size(), actual.size());
        for (Map.Entry<UUID, byte[]> actualEntry : actual.entrySet()) {
            UUID key = actualEntry.getKey();
            byte[] value = actualEntry.getValue();
            byte[] actualValue = actual.get(key);
            assertNotNull(actualValue);
            assertArrayEquals(value, actualValue);
        }
    }

    private static Map<UUID, byte[]> getPasswordsMap(List<Pair<UUID, byte[]>> passwords, int passwordsMapSize) {
        Map<UUID, byte[]> result = new HashMap<>();
        for (int i = 0; i < passwordsMapSize; ++i) {
            Pair<UUID, byte[]> passwordEntry = passwords.get(i);
            result.put(passwordEntry.getKey(), passwordEntry.getValue());
        }
        return result;
    }
}
