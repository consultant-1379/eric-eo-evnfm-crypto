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
package com.ericsson.eo.evnfm.crypto;

/**
 * This class stores parameters for encryption and decryption of sensitive constants.
 */
public final class Constants {
    public static final String ENCRYPTION_ALGORITHM = "AES";
    public static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";
    /**
     * Length of a cipher key in bits.
     */
    public static final int CIPHER_KEY_LENGTH = 128;
    /**
     * Length initial vector in bytes.
     */
    public static final int ENCRYPTION_IV_LENGTH = 16;

    private Constants() {
    }
}
