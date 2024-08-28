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
package com.ericsson.eo.evnfm.crypto.migration.presentation.mapper;

import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.ericsson.eo.evnfm.crypto.migration.exceptions.CipherKeyMappingException;
import com.ericsson.eo.evnfm.crypto.migration.util.Constants;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.CipherKey;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.KmsSecret;

public final class CipherKmsSecretMapper {
    private CipherKmsSecretMapper() {
    }

    public static CipherKey mapKmsSecretToCipherKey(KmsSecret kmsSecret) {
        try {
            String kmsSecretKey = kmsSecret.getKey();
            byte[] decodedKey = Base64.getDecoder().decode(kmsSecretKey);
            SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, Constants.ENCRYPTION_ALGORITHM);
            return new CipherKey(kmsSecret.getAlias(), originalKey, kmsSecret.getCreated());
        } catch (RuntimeException e) {
            throw new CipherKeyMappingException(String.format("Cannot map CipherKey due to: %s", e.getMessage()), e);
        }
    }

    public static KmsSecret mapCipherKeyToKmsSecret(CipherKey cipherKey) {
        try {
            byte[] encoded = cipherKey.getKey().getEncoded();
            var encodedKey = Base64.getEncoder().encodeToString(encoded);
            return new KmsSecret(cipherKey.getAlias(), encodedKey, cipherKey.getCreated());
        } catch (RuntimeException e) {
            throw new CipherKeyMappingException(String.format("Cannot map CipherKey due to: %s", e.getMessage()), e);
        }
    }
}
