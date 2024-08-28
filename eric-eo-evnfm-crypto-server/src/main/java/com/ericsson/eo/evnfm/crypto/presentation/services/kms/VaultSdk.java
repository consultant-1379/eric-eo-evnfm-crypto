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

import com.ericsson.eo.evnfm.crypto.exceptions.KmsInternalException;
import com.ericsson.eo.evnfm.crypto.presentation.model.KmsSecret;
import org.springframework.stereotype.Service;
import org.springframework.vault.VaultException;
import org.springframework.vault.core.VaultKeyValueOperationsSupport;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.List;
import java.util.UUID;

import static com.ericsson.eo.evnfm.crypto.presentation.services.kms.KmsRequestServiceImpl.KMS_INTERNAL_ISSUE;
import static org.springframework.vault.core.VaultKeyValueOperationsSupport.KeyValueBackend.KV_1;

@Service
public class VaultSdk {

    private static final VaultKeyValueOperationsSupport.KeyValueBackend API_VERSION = KV_1;
    private static final String PATH_DELIMITER = "/";
    private static final String ROOT_PATH = "secret";
    private static final String SECRETS_PATH = "key";
    private static final String LIST_PATH = "secret/key";
    private final VaultTemplate vaultTemplate;

    public VaultSdk(VaultTemplate vaultTemplate) {
        this.vaultTemplate = vaultTemplate;
    }

    public void storeSecret(KmsSecret kmsSecret) {
        try {
            var vaultKeyValueOperations = vaultTemplate.opsForKeyValue(ROOT_PATH, API_VERSION);
            vaultKeyValueOperations.put(buildPath(kmsSecret.getAlias()), kmsSecret);
        } catch (VaultException vaultException) {
            throw buildException(vaultException);
        }
    }

    public VaultResponse getSecret(UUID secretId) {
        try {
            var vaultKeyValueOperations = vaultTemplate.opsForKeyValue(ROOT_PATH, API_VERSION);
            return vaultKeyValueOperations.get(buildPath(secretId));
        } catch (VaultException vaultException) {
            throw buildException(vaultException);
        }
    }

    public List<String> getAllSecrets() {
        try {
            return vaultTemplate.list(LIST_PATH);
        } catch (VaultException vaultException) {
            throw buildException(vaultException);
        }
    }

    private static String buildPath(UUID secretId) {
        return SECRETS_PATH + PATH_DELIMITER + secretId.toString();
    }

    private static KmsInternalException buildException(VaultException vaultException) {
        return new KmsInternalException(KMS_INTERNAL_ISSUE + vaultException.getMessage(), vaultException);
    }
}
