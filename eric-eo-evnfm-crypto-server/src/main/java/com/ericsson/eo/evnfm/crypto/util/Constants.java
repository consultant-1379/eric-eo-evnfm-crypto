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
package com.ericsson.eo.evnfm.crypto.util;

public final class Constants {

    public static final String ERROR_ENCRYPTING_DATA = "Exception while encrypting sensitive data";
    public static final String ERROR_DECRYPTING_DATA = "Exception while decrypting sensitive data";
    public static final String ERROR_EMPTY_BODY = "Empty body request";
    public static final String ERROR_DECODING_DATA = "Exception while decoding sensitive data";
    public static final String ERROR_UNSUPPORTED_ENCRYPTION_DECRYPTION_VERSION = "Unsupported encryption/decryption version";
    public static final String FAIL_INIT_CIPHER_MESSAGE = "Unable to initialize cipher instance for encryption";
    public static final String FAIL_GET_CIPHER_MESSAGE = "Unable to get cipher instance for encryption";
    public static final String ERROR_UNABLE_TO_RETURN_OBJECT = "Unable to return object to StackObjectPool";
    public static final String ENCRYPTION_ALGORITHM = "AES";

    public static final String KMS_CACHE_LATEST_KEY = "latestKey";

    public static final String KMS_CACHE_KEYS = "keys";

    private Constants() {
    }
}
