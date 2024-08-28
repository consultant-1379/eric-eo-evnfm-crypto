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
package com.ericsson.eo.evnfm.crypto.contracts.base;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import com.ericsson.eo.evnfm.crypto.model.EncryptionPostRequest;
import com.ericsson.eo.evnfm.crypto.model.EncryptionResponse;
import com.ericsson.eo.evnfm.crypto.presentation.controllers.EncryptionController;
import com.ericsson.eo.evnfm.crypto.presentation.services.CryptoService;

import io.restassured.module.mockmvc.RestAssuredMockMvc;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.cloud.kubernetes.enabled = false",
        "kms.enabled=false",
        "spring.cloud.vault.kubernetes.service-account-token-file=./token"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class PostEncryptionPositiveBase {
    @Mock
    CryptoService cryptoService;

    @InjectMocks
    EncryptionController encryptionController;

    @BeforeEach
    public void setUp() {
        EncryptionResponse response = new EncryptionResponse();
        response.setCiphertext("vwjfCrCLYHgY9zeY2yU4XS8FM+D32+MUXKLF+VLDAlQ=");
        given(cryptoService.encrypt(any(EncryptionPostRequest.class))).willReturn(response);

        RestAssuredMockMvc.standaloneSetup(encryptionController);
    }
}
