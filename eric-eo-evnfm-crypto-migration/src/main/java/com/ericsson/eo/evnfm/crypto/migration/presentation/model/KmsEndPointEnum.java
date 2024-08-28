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
package com.ericsson.eo.evnfm.crypto.migration.presentation.model;

import org.springframework.vault.core.VaultKeyValueOperationsSupport;

public enum KmsEndPointEnum {
    KMS_API_V1("secret","key/", VaultKeyValueOperationsSupport.KeyValueBackend.KV_1);
    private final String rootPath;
    private final String customSecretPath;

    private final VaultKeyValueOperationsSupport.KeyValueBackend keyValueBackend;
    KmsEndPointEnum(String rootPath,
                    String customSecretPath,
                    VaultKeyValueOperationsSupport.KeyValueBackend keyValueBackend) {
        this.rootPath = rootPath;
        this.customSecretPath = customSecretPath;
        this.keyValueBackend = keyValueBackend;

    }

    public VaultKeyValueOperationsSupport.KeyValueBackend getKeyValueBackend() {
        return keyValueBackend;
    }

    public String getRootPath() {
        return rootPath;
    }

    public String getCustomSecretPath() {
        return customSecretPath;
    }
}
