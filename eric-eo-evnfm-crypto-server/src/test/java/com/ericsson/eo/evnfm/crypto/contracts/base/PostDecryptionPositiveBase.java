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

import com.ericsson.eo.evnfm.crypto.model.DecryptionPostRequest;
import com.ericsson.eo.evnfm.crypto.model.DecryptionResponse;
import com.ericsson.eo.evnfm.crypto.presentation.controllers.DecryptionController;
import com.ericsson.eo.evnfm.crypto.presentation.services.CryptoService;

import io.restassured.module.mockmvc.RestAssuredMockMvc;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.cloud.kubernetes.enabled = false",
        "kms.enabled=false",
        "spring.cloud.vault.kubernetes.service-account-token-file=./token"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class PostDecryptionPositiveBase {
    @Mock
    CryptoService cryptoService;

    @InjectMocks
    private DecryptionController decryptionController;

    @BeforeEach
    public void setUp() {
        DecryptionResponse response = new DecryptionResponse();
        response.setPlaintext("test-data");
        given(cryptoService.decrypt(any(DecryptionPostRequest.class))).willReturn(response);
        RestAssuredMockMvc.standaloneSetup(decryptionController);
    }
}
