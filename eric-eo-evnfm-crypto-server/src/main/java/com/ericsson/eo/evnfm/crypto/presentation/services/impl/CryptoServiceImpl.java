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
package com.ericsson.eo.evnfm.crypto.presentation.services.impl;

import static com.ericsson.eo.evnfm.crypto.util.Constants.ERROR_DECODING_DATA;
import static com.ericsson.eo.evnfm.crypto.util.Constants.ERROR_EMPTY_BODY;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ericsson.eo.evnfm.crypto.exceptions.DecryptionException;
import com.ericsson.eo.evnfm.crypto.exceptions.EmptyBodyRequestException;
import com.ericsson.eo.evnfm.crypto.model.DecryptionPostRequest;
import com.ericsson.eo.evnfm.crypto.model.DecryptionResponse;
import com.ericsson.eo.evnfm.crypto.model.EncryptionPostRequest;
import com.ericsson.eo.evnfm.crypto.model.EncryptionResponse;
import com.ericsson.eo.evnfm.crypto.presentation.model.CipherKey;
import com.ericsson.eo.evnfm.crypto.presentation.model.SecurityConfig;
import com.ericsson.eo.evnfm.crypto.presentation.services.CipherVersionService;
import com.ericsson.eo.evnfm.crypto.presentation.services.CryptoService;
import com.ericsson.eo.evnfm.crypto.presentation.services.KeyService;
import com.ericsson.eo.evnfm.crypto.util.cipher.message.MessageCipher;

@Service
public class CryptoServiceImpl implements CryptoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoServiceImpl.class);
    private static final Logger LOG = LoggerFactory.getLogger("performanceMeasurements");

    private final KeyService keyService;

    private final CipherVersionService<MessageCipher> versionService;

    private final SecurityConfig securityConfig;

    public CryptoServiceImpl(KeyService keyService,
                             CipherVersionService<MessageCipher> versionService,
                             SecurityConfig securityConfig) {
        this.keyService = keyService;
        this.versionService = versionService;
        this.securityConfig = securityConfig;
    }

    public EncryptionResponse encrypt(EncryptionPostRequest encryptionRequest) {
        if (encryptionRequest == null) {
            throw new EmptyBodyRequestException(ERROR_EMPTY_BODY);
        }

        byte[] plaintextBytes = encryptionRequest.getPlaintext().getBytes(StandardCharsets.UTF_8);
        long keyLoadingStartedTimestamp = System.currentTimeMillis();
        CipherKey cipherKey = keyService.getLatestCipherKey();
        LOG.debug("Overall key loading took {} ms", System.currentTimeMillis() - keyLoadingStartedTimestamp);

        MessageCipher messageCipher = versionService.getLatestCipher();
        long encryptionStartedTimestamp = System.currentTimeMillis();
        String ciphertext = messageCipher.encrypt(plaintextBytes, cipherKey);
        LOG.debug("Overall encryption of {} bytes took {} ms", plaintextBytes.length,
                                 System.currentTimeMillis() - encryptionStartedTimestamp);

        return new EncryptionResponse().ciphertext(ciphertext);
    }

    @Override
    public DecryptionResponse decrypt(final DecryptionPostRequest decryptionRequest) {
        if (decryptionRequest == null) {
            throw new EmptyBodyRequestException(ERROR_EMPTY_BODY);
        }

        byte[] decodedBytes;
        int versionAndKeyIdLength = 2 + securityConfig.getCipherKeyIdLength();
        ByteBuffer byteBuffer;

        try {
            decodedBytes = Base64.getDecoder().decode(decryptionRequest.getCiphertext());
            byteBuffer = ByteBuffer.wrap(decodedBytes, 0, versionAndKeyIdLength);
        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            LOGGER.error(ERROR_DECODING_DATA, e);
            throw new DecryptionException(ERROR_DECODING_DATA, e);
        }

        // compute encryption version and encryption key id
        short encryptionVersion = byteBuffer.getShort();
        UUID encryptionKeyId = new UUID(byteBuffer.getLong(), byteBuffer.getLong());
        CipherKey cipherKey = keyService.getCipherKeyById(encryptionKeyId);

        // perform decryption
        MessageCipher messageCipher = versionService.getCipher(encryptionVersion);
        byte[] ciphertextBytes = Arrays.copyOfRange(decodedBytes, versionAndKeyIdLength, decodedBytes.length);
        long decryptionStartedTimestamp = System.currentTimeMillis();
        String plaintext = messageCipher.decrypt(ciphertextBytes, cipherKey);
        LOG.debug("Overall decryption of {} characters took {} ms", plaintext.length(),
                                 System.currentTimeMillis() - decryptionStartedTimestamp);

        DecryptionResponse response = new DecryptionResponse();
        response.setPlaintext(plaintext);

        return response;
    }
}
