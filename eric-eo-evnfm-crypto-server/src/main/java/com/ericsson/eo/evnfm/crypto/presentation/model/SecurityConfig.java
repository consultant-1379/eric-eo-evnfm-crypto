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
package com.ericsson.eo.evnfm.crypto.presentation.model;

/**
 * This class stores parameters for encryption and decryption which should be kept private.
 * During startup of the Crypto service value for the fields in this class are loaded from
 * {@code /constants} resource file. For more details on how to change values of the constatns
 * stored in {@code /constants} refer to {@code eric-eo-evnfm-crypto-utils} submodule of this
 * process.
 */
public class SecurityConfig {
    /**
     * Type of the keystore used by this service.
     */
    private String keystoreType;

    /**
     * Name of the algorithm used to encrypt sensitive data.
     */
    private String encryptionAlgorithm;
    /**
     * Encryption transformation used by this service.
     */
    private String transformation;
    /**
     * Hashing algorithm used by this service.
     */
    private String digestAlgorithm;
    /**
     * Name of the RNG algorithm used by this service.
     */
    private String secureRandomAlgorithm;
    /**
     * Length of the key id stored in keystore measured in bytes.
     */
    private int cipherKeyIdLength;
    /**
     * Length of a key stored in keystore measured in bits.
     */
    private int cipherKeyLength;
    /**
     * Length of the initial vector used for encryption/decryption measured in bytes.
     */
    private int ivLength;

    /**
     * Latest encryption/decryption version of sensitive data.
     */
    private short latestEncryptionDecryptionVersion;

    public String getKeystoreType() {
        return keystoreType;
    }

    public void setKeystoreType(String keystoreType) {
        this.keystoreType = keystoreType;
    }

    public String getEncryptionAlgorithm() {
        return encryptionAlgorithm;
    }

    public void setEncryptionAlgorithm(String encryptionAlgorithm) {
        this.encryptionAlgorithm = encryptionAlgorithm;
    }

    public String getTransformation() {
        return transformation;
    }

    public void setTransformation(String transformation) {
        this.transformation = transformation;
    }

    public String getDigestAlgorithm() {
        return digestAlgorithm;
    }

    public void setDigestAlgorithm(String digestAlgorithm) {
        this.digestAlgorithm = digestAlgorithm;
    }

    public String getSecureRandomAlgorithm() {
        return secureRandomAlgorithm;
    }

    public void setSecureRandomAlgorithm(String secureRandomAlgorithm) {
        this.secureRandomAlgorithm = secureRandomAlgorithm;
    }

    public int getCipherKeyIdLength() {
        return cipherKeyIdLength;
    }

    public void setCipherKeyIdLength(int cipherKeyIdLength) {
        this.cipherKeyIdLength = cipherKeyIdLength;
    }

    public int getCipherKeyLength() {
        return cipherKeyLength;
    }

    public void setCipherKeyLength(int cipherKeyLength) {
        this.cipherKeyLength = cipherKeyLength;
    }

    public int getIvLength() {
        return ivLength;
    }

    public void setIvLength(int ivLength) {
        this.ivLength = ivLength;
    }

    public short getLatestEncryptionDecryptionVersion() {
        return latestEncryptionDecryptionVersion;
    }

    public void setLatestEncryptionDecryptionVersion(short latestEncryptionDecryptionVersion) {
        this.latestEncryptionDecryptionVersion = latestEncryptionDecryptionVersion;
    }
}
