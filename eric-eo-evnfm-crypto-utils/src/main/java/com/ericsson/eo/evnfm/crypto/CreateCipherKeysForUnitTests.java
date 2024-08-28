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

import static com.ericsson.eo.evnfm.crypto.Constants.CIPHER_KEY_LENGTH;
import static com.ericsson.eo.evnfm.crypto.Constants.ENCRYPTION_ALGORITHM;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to create content for the /olderKey and /cipherKey test resource files from
 * eric-eo-evnfm-crypto-server module.
 */
public final class CreateCipherKeysForUnitTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateCipherKeysForUnitTests.class);

    private CreateCipherKeysForUnitTests() {
    }

    public static void main(final String[] args) {
        try {
            SecureRandom secureRandom = SecureRandom.getInstanceStrong();

            String olderKeyMessage = String.format("/olderKey file content: %s",
                                                   Base64.getEncoder().encodeToString(createSecretKey(secureRandom).getEncoded()));
            String cipherKeyMessage = String.format("/cipherKey file content: %s",
                                                    Base64.getEncoder().encodeToString(createSecretKey(secureRandom).getEncoded()));
            LOGGER.info(olderKeyMessage);
            LOGGER.info(cipherKeyMessage);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Unable to generate secret key", e);
        }
    }

    private static SecretKey createSecretKey(SecureRandom secureRandom) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
        keyGenerator.init(CIPHER_KEY_LENGTH, secureRandom);
        return keyGenerator.generateKey();
    }
}
