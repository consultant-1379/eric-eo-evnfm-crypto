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
import static com.ericsson.eo.evnfm.crypto.Constants.ENCRYPTION_IV_LENGTH;
import static com.ericsson.eo.evnfm.crypto.Constants.TRANSFORMATION;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.stream.Collectors;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is used to create content for the /constatns resource file from
 * eric-eo-evnfm-crypto-server module.
 */
public final class CreateEncryptedConstantsFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(CreateEncryptedConstantsFile.class);

    private CreateEncryptedConstantsFile() {
    }

    public static void main(String... args) {
        try {
            String constantsFilePath = CreateEncryptedConstantsFile.class.getClassLoader().getResource("constants.json").getPath();
            String constantsJson = Files.readAllLines(Paths.get(constantsFilePath)).stream().collect(Collectors.joining(System.lineSeparator()));

            SecureRandom secureRandom = SecureRandom.getInstanceStrong();

            // generate secret key
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ENCRYPTION_ALGORITHM);
            keyGenerator.init(CIPHER_KEY_LENGTH, secureRandom);
            SecretKey secretKey = keyGenerator.generateKey();
            byte[] secretKeyBytes = secretKey.getEncoded();

            // generate initial vector
            byte[] initialVector = new byte[ENCRYPTION_IV_LENGTH];
            secureRandom.nextBytes(initialVector);

            // encrypt json
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(initialVector));
            byte[] jsonBytes = constantsJson.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedJsonBytes = cipher.doFinal(jsonBytes);

            // construct /constants file content
            byte[] constantsBytes = new byte[CIPHER_KEY_LENGTH / 8 +
                    ENCRYPTION_IV_LENGTH +
                    encryptedJsonBytes.length];
            ByteBuffer buffer = ByteBuffer.wrap(constantsBytes);
            buffer.put(secretKeyBytes);
            buffer.put(initialVector);
            buffer.put(encryptedJsonBytes);

            // encode /constants file to string and print
            String encodedConstants = Base64.getEncoder().encodeToString(constantsBytes);
            String constantsMessage = String.format("/constants file content: %s", encodedConstants);
            LOGGER.info(constantsMessage);
        } catch (NoSuchAlgorithmException | InvalidKeyException | InvalidAlgorithmParameterException |
                NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | IOException e) {
            LOGGER.error("Unable to create encrypted /constatns file content", e);
        }
    }
}
