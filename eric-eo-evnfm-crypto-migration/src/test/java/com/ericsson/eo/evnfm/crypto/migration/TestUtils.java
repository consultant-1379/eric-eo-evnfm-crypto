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
package com.ericsson.eo.evnfm.crypto.migration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import javax.security.auth.kerberos.EncryptionKey;

import com.ericsson.eo.evnfm.crypto.migration.presentation.model.CipherKey;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.KmsSecret;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.SecurityConfig;
import com.ericsson.eo.evnfm.crypto.migration.security.SecurityConfigReader;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.io.Files;
import com.google.common.io.Resources;

public final class TestUtils {

    @SuppressWarnings("UnstableApiUsage")
    public static String getResourceFileAsString(String path) {
        try {
            File file = new File(Resources.getResource(path).toURI());
            return Files.asCharSource(file, StandardCharsets.UTF_8).read();
        } catch (URISyntaxException | IOException e) {
            throw new IllegalArgumentException(TestConstants.WRONG_FILE_PATH_MSG + e.getMessage(), e.getCause());
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public static Path getKeystorePath()  {
        try {
            return Paths.get(Resources.getResource(String.format("%s/%s",
                                                                 TestConstants.SENSITIVE_PATH, TestConstants.KEYSTORE_FILE)).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(TestConstants.TEST_FAIL_ERROR_MESSAGE, e);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public static Path getPasswordsPath()  {
        try {
            return Paths.get(Resources.getResource(String.format("%s/%s",
                                                                 TestConstants.SENSITIVE_PATH, TestConstants.PASSWORDS_FILE)).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(TestConstants.TEST_FAIL_ERROR_MESSAGE, e);
        }
    }

    public static CipherKey generateCipherKey() {
        UUID alias = UUID.randomUUID();
        byte[] bytes = "Test".getBytes();
        return new CipherKey(alias, new EncryptionKey(bytes, 18), LocalDateTime.now(), null);
    }

    public static SecurityConfig initSecurityConfig() {
        InputStream constantsAsStream = TestUtils.class.getResourceAsStream("/constants");
        SecurityConfigReader reader = new SecurityConfigReader();
        return reader.readFromInputStream(constantsAsStream);
    }

    public static Map<String,Object> convertObjectToMap(KmsSecret test) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper.convertValue(test,new TypeReference<HashMap<String,Object>>(){});
    }

    public static KmsSecret getKmsSecret() {
        return new KmsSecret(UUID.randomUUID(), "DnnCAapOwhkdgE7OLSlzxA==", LocalDateTime.now());
    }

    private TestUtils() {
    }
}
