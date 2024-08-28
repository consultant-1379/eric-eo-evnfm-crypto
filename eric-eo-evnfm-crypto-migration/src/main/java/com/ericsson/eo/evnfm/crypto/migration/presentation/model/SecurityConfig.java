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
package com.ericsson.eo.evnfm.crypto.migration.presentation.model;

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
     * Name of the keystore file.
     */
    private String keystoreFile;
    /**
     * Name of the file used to unlock keystore.
     */
    private String passwordsFile;
    /**
     * Alias of the password used to unlock keystore.
     */
    private String keystorePasswordAlias;

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
     * Length of a password in {@link #passwordsFile} in bytes.
     */
    private int passwordLength;
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
     * Encryption algorithm used to encrypt {@link #passwordsFile}.
     */
    private String passwordsEncryptionAlgorithm;
    /**
     * Encryption transformation used to encrypt {@link #passwordsFile}.
     */
    private String passwordsTransformation;
    /**
     * Encryption key used to encrypt {@link #passwordsFile}.
     */
    private String passwordsFileEncryptionKey;
    /**
     * Initial vector used to encrypt {@link #passwordsFile}.
     */
    private String passwordsFileEncryptionIv;

    /**
     * Length of the {@link #passwordsFile} header measured in bytes. Value is equal to
     * $length({{@link #passwordsFileSignature}}) + length({{@link #latestPasswordsObscurityVersion}})$.
     */
    private int passwordsFileHeaderLength;
    /**
     * Signature bytes at the beginning of the {@link #passwordsFile}.
     */
    private int passwordsFileSignature;

    /**
     * Latest encryption/decryption version of sensitive data.
     */
    private short latestEncryptionDecryptionVersion;
    /**
     * Latest version of the {@link #passwordsFile} encryption in a hexadecimal form.
     */
    private short latestPasswordsObscurityVersion;

    public String getKeystoreType() {
        return keystoreType;
    }

    public void setKeystoreType(String keystoreType) {
        this.keystoreType = keystoreType;
    }

    public String getKeystoreFile() {
        return keystoreFile;
    }

    public void setKeystoreFile(String keystoreFile) {
        this.keystoreFile = keystoreFile;
    }

    public String getPasswordsFile() {
        return passwordsFile;
    }

    public void setPasswordsFile(String passwordsFile) {
        this.passwordsFile = passwordsFile;
    }

    public String getKeystorePasswordAlias() {
        return keystorePasswordAlias;
    }

    public void setKeystorePasswordAlias(String keystorePasswordAlias) {
        this.keystorePasswordAlias = keystorePasswordAlias;
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

    public int getPasswordLength() {
        return passwordLength;
    }

    public void setPasswordLength(int passwordLength) {
        this.passwordLength = passwordLength;
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

    public String getPasswordsEncryptionAlgorithm() {
        return passwordsEncryptionAlgorithm;
    }

    public void setPasswordsEncryptionAlgorithm(String passwordsEncryptionAlgorithm) {
        this.passwordsEncryptionAlgorithm = passwordsEncryptionAlgorithm;
    }

    public String getPasswordsTransformation() {
        return passwordsTransformation;
    }

    public void setPasswordsTransformation(String passwordsTransformation) {
        this.passwordsTransformation = passwordsTransformation;
    }

    public String getPasswordsFileEncryptionKey() {
        return passwordsFileEncryptionKey;
    }

    public void setPasswordsFileEncryptionKey(String passwordsFileEncryptionKey) {
        this.passwordsFileEncryptionKey = passwordsFileEncryptionKey;
    }

    public String getPasswordsFileEncryptionIv() {
        return passwordsFileEncryptionIv;
    }

    public void setPasswordsFileEncryptionIv(String passwordsFileEncryptionIv) {
        this.passwordsFileEncryptionIv = passwordsFileEncryptionIv;
    }

    public int getPasswordsFileHeaderLength() {
        return passwordsFileHeaderLength;
    }

    public void setPasswordsFileHeaderLength(int passwordsFileHeaderLength) {
        this.passwordsFileHeaderLength = passwordsFileHeaderLength;
    }

    public int getPasswordsFileSignature() {
        return passwordsFileSignature;
    }

    public void setPasswordsFileSignature(int passwordsFileSignature) {
        this.passwordsFileSignature = passwordsFileSignature;
    }

    public short getLatestEncryptionDecryptionVersion() {
        return latestEncryptionDecryptionVersion;
    }

    public void setLatestEncryptionDecryptionVersion(short latestEncryptionDecryptionVersion) {
        this.latestEncryptionDecryptionVersion = latestEncryptionDecryptionVersion;
    }

    public short getLatestPasswordsObscurityVersion() {
        return latestPasswordsObscurityVersion;
    }

    public void setLatestPasswordsObscurityVersion(short latestPasswordsObscurityVersion) {
        this.latestPasswordsObscurityVersion = latestPasswordsObscurityVersion;
    }
}
