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
package contracts.api.postKeys.positive

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("""
Represents a negative scenario for update Latest Key

```
given:
  client requests update latest key
when:
  a request is submitted
then:
  the request failed due to Error
```

""")
    request {
        method POST()
        url "/generic/v1/keys"
    }
    response {
        status UNPROCESSABLE_ENTITY()
    }
}