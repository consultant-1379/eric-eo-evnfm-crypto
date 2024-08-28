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
package com.ericsson.eo.evnfm.crypto.presentation.mappers;

import com.ericsson.eo.evnfm.crypto.exceptions.CipherKeyMappingException;
import com.ericsson.eo.evnfm.crypto.presentation.model.CipherKey;
import com.ericsson.eo.evnfm.crypto.presentation.model.KmsSecret;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.ericsson.eo.evnfm.crypto.TestUtils.generateCipherKey;
import static com.ericsson.eo.evnfm.crypto.TestUtils.generateKmsSecret;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CipherKmsSecretMapperTest {

    @Test
    void testMapKmsSecretToCipher() {
        KmsSecret secret = generateKmsSecret();
        CipherKey cipherKey = CipherKmsSecretMapper.mapKmsSecretToCipherKey(secret);
        Assertions.assertAll("cipherKey",
                () -> assertEquals(secret.getAlias(), cipherKey.getAlias()),
                () -> assertEquals(secret.getCreated(), cipherKey.getCreated()),
                () -> assertNotNull(cipherKey.getKey())
        );
    }

    @Test
    void testMapKmsSecretToCipherWrongSecret() {
        KmsSecret secret = new KmsSecret();
        Assertions.assertThrows(
                CipherKeyMappingException.class, () -> CipherKmsSecretMapper.mapKmsSecretToCipherKey(secret));
    }

    @Test
    void testMapCipherToKmsSecret() {
        CipherKey secret = generateCipherKey();
        KmsSecret kmsSecret = CipherKmsSecretMapper.mapCipherKeyToKmsSecret(secret);
        Assertions.assertAll("cipherKey",
                () -> assertEquals(secret.getAlias(), kmsSecret.getAlias()),
                () -> assertEquals(secret.getCreated(), kmsSecret.getCreated()),
                () -> assertNotNull(kmsSecret.getKey())
        );
    }

    @Test
    void testMapCipherToKmsSecretWrongSecret() {
        CipherKey secret = new CipherKey();
        Assertions.assertThrows(
                CipherKeyMappingException.class, () -> CipherKmsSecretMapper.mapCipherKeyToKmsSecret(secret));
    }
}