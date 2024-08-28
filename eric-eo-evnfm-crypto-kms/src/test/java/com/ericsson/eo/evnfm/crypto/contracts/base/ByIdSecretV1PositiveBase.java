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

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.restassured.module.mockmvc.specification.MockMvcRequestSpecification;
import io.restassured.response.ResponseOptions;
import org.junit.jupiter.api.Test;

import static com.toomuchcoding.jsonassert.JsonAssertion.assertThatJson;
import static io.restassured.module.mockmvc.RestAssuredMockMvc.given;
import static org.assertj.core.api.Assertions.assertThat;

public class ByIdSecretV1PositiveBase extends GeneralBase {


    @Test
    public void validate_shouldReturnStoredDataForValidKey() throws Exception {
        // given:
        MockMvcRequestSpecification request = given()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .body("{\"ciphertext\":\"CBFUVMRTZFVUNSQCTPOZ\"}");

        // when:
        ResponseOptions<?> response = given().spec(request)
                .get("/v1/secret/key/debbccf5-d3a5-4daa-9718-7754a7c5f607");

        // then:
        assertThat(response.statusCode()).isEqualTo(200);

        // and:
        DocumentContext parsedJson = JsonPath.parse(response.getBody().asString());
        assertThatJson(parsedJson).field("['plaintext']").isEqualTo("test-data");
    }
}
