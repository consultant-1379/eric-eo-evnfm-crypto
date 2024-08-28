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
package com.ericsson.eo.evnfm.crypto.presentation.controllers.internal;

import com.ericsson.eo.evnfm.crypto.infrastructure.annotations.DocumentController;
import com.ericsson.eo.evnfm.crypto.presentation.services.KeyService;
import org.openapitools.api.KeysApi;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@DocumentController
@RequestMapping("/generic/v1")
public class LatestKeyController implements KeysApi {
    private final KeyService keyService;

    public LatestKeyController(KeyService keyService) {
        this.keyService = keyService;
    }

    @Override
    public ResponseEntity<Void> keysPost() {
        keyService.updateLatestCipherKey();
        return ResponseEntity.noContent().build();
    }
}
