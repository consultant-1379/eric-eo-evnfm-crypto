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
package contracts.api.postEncryption.positive

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("""
Represents a successful scenario for encryption an empty string

```
given:
  client requests encryption of empty string
when:
  a request is submitted
then:
  the request is accepted
```

""")
    request {
        method POST()
        url "/generic/v1/encryption"
        headers {
            contentType(applicationJson())
            accept(applicationJson())
        }
        body(
                """
            {
                "plaintext": "${value(consumer(anyNonBlankString()), producer(anyNonBlankString()))}"
            }
            """
        )
    }
    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
        body (file("EncryptedStringDetails.json"))
    }
}
