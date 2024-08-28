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
package com.ericsson.eo.evnfm.crypto.migration.presentation.services.kms;

import java.util.UUID;

import org.springframework.vault.support.VaultResponse;

import com.ericsson.eo.evnfm.crypto.migration.presentation.model.KmsEndPointEnum;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.KmsSecret;

public interface KmsRequestService {
    void storeSecretToKms(KmsEndPointEnum endPoint, KmsSecret kmsSecret);
    VaultResponse getSecretFromKms(KmsEndPointEnum endPoint, UUID secretId);
}
