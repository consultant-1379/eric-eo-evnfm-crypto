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
package contracts.api.readKey.byIdSecretV1.negative

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("""
Returns failed scenario for read data from KMS by wrong ID

```
given:
  client requests to KMS data by ID
when:
  a not valid request is made to get data from KMS by ID
then:
  the failed response
```

""")
    request{
        method GET()
        headers {
            header("X-Vault-Token": "notValidToken")
        }
        url "/v1/secret/key/${value(consumer('debbccf5-d3a5-4daa-9718-7754a7c5f601'))}"
    }
    response {
        status BAD_REQUEST()
        headers {
            contentType(applicationJson())
        }
        body(file("v1ApiError.json").asString())
    }
}
