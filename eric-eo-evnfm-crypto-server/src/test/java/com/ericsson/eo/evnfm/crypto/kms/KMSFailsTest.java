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

import com.ericsson.eo.evnfm.crypto.exceptions.KmsInternalException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
        "spring.cloud.vault.kubernetes.service-account-token-file=./wrong-token",
        "spring.cloud.vault.kubernetes.role = secret-crypto-role",
        "spring.cloud.vault.scheme=http",
        "spring.cloud.vault.host=localhost",
        "spring.cloud.vault.port=8200"
})
@AutoConfigureStubRunner(
        stubsMode = LOCAL,
        ids = "com.ericsson.orchestration.mgmt:eric-eo-evnfm-crypto-kms:+:stubs:8200",
        properties = "stubs.find-producer=true"
)
public class KMSFailsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testEncryption() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/generic/v1/encryption")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"plaintext\":\"test-data\"}")
                )
                .andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .andReturn();

       assertThat(mvcResult.getResolvedException()).isInstanceOf(KmsInternalException.class);
       assertThat(mvcResult.getResolvedException().getMessage())
               .isEqualTo("KMS Internal issue: Status 500 Server Error [secret-v2]: 1 error occurred:\n\t* permission denied\n\n; nested exception is org.springframework.web.client.HttpServerErrorException$InternalServerError: 500 Server Error: \"{\"errors\":[\"1 error occurred:\\n\\t* permission denied\\n\\n\"]}\"");
    }

    @Test
    void testDecryption() throws Exception {
        final MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.post("/generic/v1/decryption")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"ciphertext\":\"AAHeu8z106VNqpcYd1SnxfYBoYYABCL+q4VzKcaE6f6RQSsaXbCEEAs3qYz8lbYqqGfp+5cpwF2IH/RPlSkTJIplKlPnaJk6P/75ezPnU7QBsA==\"}")
                )
                .andExpect(status().is(HttpStatus.INTERNAL_SERVER_ERROR.value())).andReturn();

        assertThat(mvcResult.getResolvedException()).isInstanceOf(KmsInternalException.class);
        assertThat(mvcResult.getResolvedException().getMessage())
                .isEqualTo("KMS Internal issue: Status 400 Bad Request [secret]: []; nested exception is org.springframework.web.client.HttpClientErrorException$BadRequest: 400 Bad Request: \"{\"errors\":[]}\"");
    }

}
