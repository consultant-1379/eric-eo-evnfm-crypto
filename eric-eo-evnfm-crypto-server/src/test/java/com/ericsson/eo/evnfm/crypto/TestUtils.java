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
package com.ericsson.eo.evnfm.crypto;

import com.ericsson.eo.evnfm.crypto.presentation.model.CipherKey;
import com.ericsson.eo.evnfm.crypto.presentation.model.KmsSecret;
import com.ericsson.eo.evnfm.crypto.presentation.model.SecurityConfig;
import com.ericsson.eo.evnfm.crypto.security.SecurityConfigReader;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.springframework.vault.support.VaultResponse;
import org.springframework.vault.support.VaultResponseSupport;

import javax.security.auth.kerberos.EncryptionKey;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

    public static CipherKey generateCipherKey() {
        UUID alias = UUID.fromString("7fc6a72e-45c0-485c-ba0b-84181709007b");
        byte[] bytes = "TestTestTestTest".getBytes();
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

    public static KmsSecret generateKmsSecret() {
        return new KmsSecret(UUID
                .fromString(TestConstants.SECRET_KEY_ALIAS), "DnnCAapOwhkdgE7OLSlzxA==", LocalDateTime.now());
    }

    public static VaultResponse generateVaultResponse() {
        var vaultResponse = new VaultResponse();
        var kmsSecret = generateKmsSecret();
        kmsSecret.setCreated(LocalDateTime.parse(TestConstants.SECRET_KEY_DATE));
        vaultResponse.setData(convertObjectToMap(kmsSecret));
        return vaultResponse;
    }

    public static VaultResponseSupport<JsonNode> generateVaultResponseSupportFromFile(String filePath) {
        var objectMapper = new ObjectMapper();
        var rawResponse = getResourceFileAsString(filePath);
        try {
           return objectMapper.readValue(rawResponse, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error during parsing response: " + e.getMessage(), e);
        }
    }

    public static Map<String, Object> generateVaultResponseSupportMapFromFile(String filePath) {
        var objectMapper = new ObjectMapper();
        var rawResponse = getResourceFileAsString(filePath);
        try {
            return objectMapper.readValue(rawResponse, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error during parsing response: " + e.getMessage(), e);
        }
    }

    private TestUtils() {
    }
}
