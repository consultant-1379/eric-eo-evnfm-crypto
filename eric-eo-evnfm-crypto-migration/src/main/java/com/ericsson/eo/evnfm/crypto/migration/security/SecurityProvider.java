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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.springframework.stereotype.Component;

import com.ericsson.eo.evnfm.crypto.migration.exceptions.CryptoException;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.SecurityConfig;

@Component
public class SecurityProvider {

    /**
     * RNG instance used to generate passwords and initial vectors for encryption/decryption.
     */
    private final SecureRandom secureRandom;

    /**
     * Hashing algorithm instance used to compute digests of sensitive data.
     */
    private final MessageDigest messageDigest;

    /**
     * Strong RNG instance used to create cipher keys.
     */
    private final SecureRandom keySecureRandom;

    public SecurityProvider(SecurityConfig securityConfig) {
        try {
            secureRandom = SecureRandom.getInstance(securityConfig.getSecureRandomAlgorithm());
            messageDigest = MessageDigest.getInstance(securityConfig.getDigestAlgorithm());
            keySecureRandom = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("No suitable SecureRandom and/or MessageDigest instance can be obtained", e);
        }
    }

    public SecureRandom getSecureRandom() {
        return secureRandom;
    }

    public MessageDigest getMessageDigest() {
        return messageDigest;
    }

    public SecureRandom getKeySecureRandom() {
        return keySecureRandom;
    }
}
