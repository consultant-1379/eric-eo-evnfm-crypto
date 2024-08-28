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
package contracts.api.auth.login.positive

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("""
Returns successful scenario for login to KMS by role

```
given:
  client requests to KMS for login token
when:
  a valid request is made to get JWT token from KMS by role
then:
  the valid token
```

""")
    request{
        method POST()
        url "/v1/auth/kubernetes/login"
        headers {
            contentType(applicationJson())
            accept(applicationJson())
        }
        body(file("positiveLoginRequest.json").asString())
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body(file("positiveLoginResponse.json").asString())
    }
}
