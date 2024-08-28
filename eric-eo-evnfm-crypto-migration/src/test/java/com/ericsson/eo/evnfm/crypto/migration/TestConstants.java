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
package com.ericsson.eo.evnfm.crypto.migration;

public final class TestConstants {

    public static final byte[] PASSWORD_VALUE = new byte[] {'a', 'b', 'c'};

    public static final String KEYSTORE_FILE = "keystore.jks";
    public static final String PASSWORDS_FILE =  "passwords";

    public static final String SENSITIVE_PATH = "sensitive";

    public static final String KEYSTORE_PASSWORD = "MTIzNDU2";
    public static final String KEYSTORE_ENCODED_PASSWORD = "123456";
    public static final String KEYSTORE_TYPE = "JCEKS";

    public static final String SECRET_KEY_ALIAS = "123e4567-e89b-42d3-a456-556642440001";

    public static final int PASSWORDS_SIGNATURE = 1297369466;

    public static final String WRONG_FILE_PATH_MSG = "Wrong file path or file is not accessible";
    public static final String TEST_FAIL_ERROR_MESSAGE = "Test fails and should be fixed";

    private TestConstants() {
    }
}
