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
package com.ericsson.eo.evnfm.crypto.presentation.controllers;

import org.openapitools.api.EncryptionApi;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ericsson.eo.evnfm.crypto.infrastructure.annotations.DocumentController;
import com.ericsson.eo.evnfm.crypto.model.EncryptionPostRequest;
import com.ericsson.eo.evnfm.crypto.model.EncryptionResponse;
import com.ericsson.eo.evnfm.crypto.presentation.services.CryptoService;

@RestController
@DocumentController
@RequestMapping("/generic/v1")
public class EncryptionController implements EncryptionApi {

    private final CryptoService cryptoService;

    public EncryptionController(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @Override
    public ResponseEntity<EncryptionResponse> encryptionPost(
            String accept,
            String contentType,
        @RequestBody(required = false) EncryptionPostRequest encryptionRequest) {

        return new ResponseEntity<>(cryptoService.encrypt(encryptionRequest), HttpStatus.OK);
    }
}
