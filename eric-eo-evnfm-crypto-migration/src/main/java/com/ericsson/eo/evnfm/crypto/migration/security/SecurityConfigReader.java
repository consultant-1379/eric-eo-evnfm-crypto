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
package com.ericsson.eo.evnfm.crypto.migration.security;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.ericsson.eo.evnfm.crypto.migration.exceptions.CryptoException;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.SecurityConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SecurityConfigReader {

    private static final String SENSITIVE_CONSTANTS_ENCRYPTION_ALGORITHM = "AES";
    private static final String SENSITIVE_CONSTANTS_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    private static final int SENSITIVE_CONSTANTS_ENCRYPTION_KEY_LENGTH = 16;
    private static final int SENSITIVE_CONSTANTS_ENCRYPTION_IV_LENGTH = 16;

    /**
     * String value used to replace sensitive data in logs.
     */
    private static final String SENSITIVE_CONSTANTS_REPLACEMENT = "******";

    /**
     * Regular expression for a value of a sensitive constant in decrypted content
     * of {@code /constants} resource file.
     */
    private static final Pattern SENSITIVE_VALUE_REGEX_PATTERN = Pattern.compile("\"\\s?:\\s?(\"[^\"]+\"|[0-9]+)");

    public SecurityConfig readFromInputStream(InputStream input) {
        InputStream inputStream = Optional.ofNullable(input)
                .orElseThrow(() -> new CryptoException(
                        "Cannot read file with constants. Please check that file exists and has access for read."));
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        String constantsFileString = reader.lines().collect(Collectors.joining(System.lineSeparator()));
        byte[] constantsFileBytes = Base64.getDecoder().decode(constantsFileString);
        ByteBuffer buffer = ByteBuffer.wrap(constantsFileBytes);

        byte[] encryptionKey = new byte[SENSITIVE_CONSTANTS_ENCRYPTION_KEY_LENGTH];
        buffer.get(encryptionKey);
        byte[] encryptionIv = new byte[SENSITIVE_CONSTANTS_ENCRYPTION_IV_LENGTH];
        buffer.get(encryptionIv);
        byte[] encryptedJson = new byte[buffer.remaining()];
        buffer.get(encryptedJson);

        String json = decryptJson(encryptedJson, encryptionIv, encryptionKey);
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(json, SecurityConfig.class);
        } catch (JsonProcessingException e) {
            throw new CryptoException("Cannot process json: " + hideSensitiveConstants(e.getMessage(), json), e);
        }
    }

    /**
     * Replaces sensitive data from {@code message} with {@code SENSITIVE_CONSTANTS_REPLACEMENT}.
     * <p>
     * First, this method retrieves values of sensitive constants from {@code sensitiveConstantsJson} using a
     * regular expression. Then it constructs a regular expression which matches every value found in the previous
     * step. Finally, it uses the constructed regular expression to replace all values using
     * {@link String#replaceAll(String, String)}.
     * <p>
     * Note: {@code message} might not contain the entire {@code sensitiveConstantsJson} as a substring.
     * Therefor it is not possible to replace all values with {@link String#replace(CharSequence, CharSequence)}.
     *
     * @param message                string with sensitive constants
     * @param sensitiveConstantsJson json with sensitive constants
     * @return string with replaced sensitive constants
     */

    public String hideSensitiveConstants(String message, String sensitiveConstantsJson) {
        List<String> sensitiveValues = new ArrayList<>();
        Matcher matcher = SENSITIVE_VALUE_REGEX_PATTERN.matcher(sensitiveConstantsJson);

        // compute list of sensitive values
        while (matcher.find()) {
            // obtain the group and skip prefix with "\": "
            String sensitiveValue = matcher.group().substring(3);
            sensitiveValues.add(sensitiveValue);
        }

        // construct regular expression for replacement
        StringBuilder replaceRegexBuilder = new StringBuilder();
        for (int i = 0; i < sensitiveValues.size(); ++i) {
            String sensitiveValue = sensitiveValues.get(i);
            replaceRegexBuilder.append(sensitiveValue);
            if (i < sensitiveValues.size() - 1) {
                replaceRegexBuilder.append("|");
            }
        }

        String replaceRegex = replaceRegexBuilder.toString();
        return message.replaceAll(replaceRegex, SENSITIVE_CONSTANTS_REPLACEMENT);
    }

    private String decryptJson(byte[] encryptedJson, byte[] initialVector, byte[] encryptionKey) {
        SecretKeySpec originalKey = new SecretKeySpec(
                encryptionKey, SENSITIVE_CONSTANTS_ENCRYPTION_ALGORITHM);

        try {
            Cipher cipher = Cipher.getInstance(SENSITIVE_CONSTANTS_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, originalKey, new IvParameterSpec(initialVector));
            byte[] output = cipher.doFinal(encryptedJson);
            return new String(output, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CryptoException("Cannot decrypt constants", e);
        }
    }
}
