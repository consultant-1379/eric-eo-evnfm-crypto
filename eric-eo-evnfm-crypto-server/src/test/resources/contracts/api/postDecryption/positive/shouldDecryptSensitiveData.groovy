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
package contracts.api.postDecryption.positive

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description("""
Represents a successful scenario of decrypting sensitive data.

```
given:
  client requests to decrypt a cipher text, where it is encrypted.
when:
  a valid request is submitted
then:
  plain Text is returned.
```

""")
    request {
        method 'POST'
        url "/generic/v1/decryption"
        headers {
            contentType(applicationJson())
            accept(applicationJson())
        }

        body(
                """
            {
                "ciphertext": "${value(consumer(anyNonBlankString()), producer(anyNonBlankString()))}"
            }
            """
        )

    }
    response {
        status OK()
        body(
                "plaintext": "test-data"

        )
    }
}