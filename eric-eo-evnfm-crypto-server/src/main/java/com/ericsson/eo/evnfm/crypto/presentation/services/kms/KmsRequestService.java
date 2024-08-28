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

import com.ericsson.eo.evnfm.crypto.presentation.model.KmsSecret;
import org.springframework.vault.support.VaultResponse;

import java.util.List;
import java.util.UUID;

public interface KmsRequestService {

    void storeSecretToKms(KmsSecret kmsSecret);

    VaultResponse getSecretFromKms(UUID secretId);

    List<KmsSecret> getAllSecrets();
}
