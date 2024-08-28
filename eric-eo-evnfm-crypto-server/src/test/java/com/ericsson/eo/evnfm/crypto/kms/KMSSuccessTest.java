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
package com.ericsson.eo.evnfm.crypto.kms;

import com.ericsson.eo.evnfm.crypto.TestUtils;
import com.ericsson.eo.evnfm.crypto.model.DecryptionResponse;
import com.ericsson.eo.evnfm.crypto.model.EncryptionResponse;
import com.ericsson.eo.evnfm.crypto.presentation.model.CipherKey;
import com.ericsson.eo.evnfm.crypto.presentation.services.kms.CryptoCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties.StubsMode.LOCAL;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled
@SpringBootTest(webEnvironment = RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.cloud.kubernetes.enabled = false",
        "kms.enabled=false",
        "spring.cloud.vault.kubernetes.service-account-token-file=./token",
        "spring.cloud.vault.kubernetes.role = secret-crypto-role",
        "spring.cloud.vault.scheme=http",
        "spring.cloud.vault.host=localhost",
        "spring.cloud.vault.port=8200",
})
@AutoConfigureStubRunner(
        stubsMode = LOCAL,
        ids = "com.ericsson.orchestration.mgmt:eric-eo-evnfm-crypto-kms:+:stubs:8200",
        properties = "stubs.find-producer=true"
)
public class KMSSuccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CryptoCacheService cryptoCacheService;

    @BeforeEach
    void setUp() {
        CipherKey cipherKey = TestUtils.generateCipherKey();
        cryptoCacheService.updateLatestKey(cipherKey);
    }

    @Test
    void testEncryption() throws Exception {

        final MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/generic/v1/encryption")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"plaintext\":\"test-data\"}")
                )
                .andExpect(status().is(HttpStatus.OK.value())).andReturn();
        final MockHttpServletResponse response = mvcResult.getResponse();
        assertThat(response.getHeader("Content-Type")).matches("application/json.*");

        final EncryptionResponse encryptionResponse = new ObjectMapper().readValue(response.getContentAsString(), EncryptionResponse.class);
        assertThat(encryptionResponse.getCiphertext()).isNotBlank();
    }

    @Test
    void testDecryption() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/generic/v1/decryption")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"ciphertext\":\"AAHeu8z106VNqpcYd1SnxfYBoYYABCL+q4VzKcaE6f6RQSsaXbCEEAs3qYz8lbYqqGfp+5cpwF2IH/RPlSkTJIplKlPnaJk6P/75ezPnU7QBsA==\"}")
                )
                .andExpect(status().is(HttpStatus.OK.value())).andReturn();
        final MockHttpServletResponse response = mvcResult.getResponse();
        assertThat(response.getHeader("Content-Type")).matches("application/json.*");

        final DecryptionResponse decryptionResponse = new ObjectMapper().readValue(response.getContentAsString(), DecryptionResponse.class);
        assertThat(decryptionResponse.getPlaintext())
                .isEqualTo("test-data");
    }

}
