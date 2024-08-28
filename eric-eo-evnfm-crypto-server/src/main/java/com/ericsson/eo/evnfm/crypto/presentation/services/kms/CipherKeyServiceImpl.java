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
package com.ericsson.eo.evnfm.crypto.presentation.services.kms;

import com.ericsson.eo.evnfm.crypto.exceptions.CryptoException;
import com.ericsson.eo.evnfm.crypto.presentation.mappers.CipherKmsSecretMapper;
import com.ericsson.eo.evnfm.crypto.presentation.model.CipherKey;
import com.ericsson.eo.evnfm.crypto.presentation.model.KmsSecret;
import com.ericsson.eo.evnfm.crypto.presentation.model.SecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.vault.support.VaultResponse;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

@Service
public class CipherKeyServiceImpl implements CipherKeyService {

    private final SecurityConfig securityConfig;
    private final ObjectMapper objectMapper;

    public CipherKeyServiceImpl(SecurityConfig securityConfig, ObjectMapper objectMapper) {
        this.securityConfig = securityConfig;
        this.objectMapper = objectMapper;
    }

    @Override
    public CipherKey createCipherKey() {
        final var keyId = UUID.randomUUID();
        var key = createSecretKey();
        var created = LocalDateTime.now(ZoneId.systemDefault());
        return new CipherKey(keyId, key, created, null);
    }

    @Override
    public CipherKey vaultResponseToCipherKey(VaultResponse vaultResponse) {
        KmsSecret kmsSecret = objectMapper.convertValue(vaultResponse.getData(), KmsSecret.class);
        return CipherKmsSecretMapper.mapKmsSecretToCipherKey(kmsSecret);
    }

    private SecretKey createSecretKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(securityConfig.getEncryptionAlgorithm());
            keyGenerator.init(securityConfig.getCipherKeyLength(), SecureRandom.getInstanceStrong());
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException | InvalidParameterException e) {
            throw new CryptoException("Failed to create cipher key generator", e);
        }
    }

}
