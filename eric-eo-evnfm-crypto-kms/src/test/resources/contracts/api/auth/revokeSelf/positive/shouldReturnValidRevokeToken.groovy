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
package contracts.api.auth.revokeSelf.positive

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("""
Returns successful scenario for revoke login in KMS by role

```
given:
  client requests to KMS for revoke token
when:
  a valid request is made to revoke JWT token from KMS by role
then:
  the valid token
```

""")
    request{
        method POST()
        url "/v1/auth/token/revoke-self"
        headers {
            header("X-Vault-Token": "testTokenForContractTests")
        }
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body(file("positiveRevokeResponse.json").asString())
    }
}
