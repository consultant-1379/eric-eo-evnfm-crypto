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

import com.ericsson.eo.evnfm.crypto.presentation.controllers.internal.LatestKeyController;
import com.ericsson.eo.evnfm.crypto.presentation.services.KeyService;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willDoNothing;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.cloud.kubernetes.enabled = false",
        "kms.enabled=false",
        "spring.cloud.vault.kubernetes.service-account-token-file=./token"
})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class PostKeysPositiveBase {
    @Mock
    KeyService keyService;

    @InjectMocks
    LatestKeyController latestKeyController;

    @BeforeEach
    public void setUp() {
        willDoNothing().given(keyService).updateLatestCipherKey();

        RestAssuredMockMvc.standaloneSetup(latestKeyController);
    }
}
