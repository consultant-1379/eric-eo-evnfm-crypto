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
package com.ericsson.eo.evnfm.crypto.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import com.ericsson.eo.evnfm.crypto.TestUtils;
import com.ericsson.eo.evnfm.crypto.exceptions.CryptoException;
import com.ericsson.eo.evnfm.crypto.presentation.model.SecurityConfig;

public class SecurityConfigReaderTest {

    private final SecurityConfigReader securityConfigReader = new SecurityConfigReader();

    @Test
    public void shouldReadFromInputStream() {
        InputStream constantsAsStream = SecurityConfigReaderTest.class.getResourceAsStream("/constants");
        SecurityConfig securityConfig = securityConfigReader.readFromInputStream(constantsAsStream);

        assertNotNull(securityConfig);
    }

    @Test
    public void shouldThrowExceptionWhenNullOnReadFromInputStream() {
        assertThrows(CryptoException.class, () -> {
            securityConfigReader.readFromInputStream(null);
        });
    }

    @Test
    public void shouldHideSensitiveConstants() {
        String message = TestUtils.getResourceFileAsString("sensitive/errorMessage.txt");
        String sensitiveConstantsJson = TestUtils.getResourceFileAsString("sensitive/sensitiveConstants.json");

        String result = securityConfigReader.hideSensitiveConstants(message, sensitiveConstantsJson);
        assertNotNull(result);

        Pattern pattern = Pattern.compile("\"[^\"]+\"\\s*:\\s*\"?([^,\"]+)\"?,");
        Matcher matcher = pattern.matcher(result);

        while (matcher.find()) {
            String replacer = matcher.group(1);
            assertEquals("******", replacer);
        }
    }
}
