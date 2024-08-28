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
package com.ericsson.eo.evnfm.crypto.migration.util.cipher.password;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.ericsson.eo.evnfm.crypto.migration.exceptions.CryptoException;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.SecurityConfig;
import com.ericsson.eo.evnfm.crypto.migration.presentation.services.KeystoreService;
import com.ericsson.eo.evnfm.crypto.migration.security.SecurityProvider;
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.pool.PasswordsCipherPool;

public class PasswordsCipher1v2 extends AbstractPasswordsCipher {
    private static final short SUPPORTED_VERSION = (short) 0x0102;

    private final PasswordsCipherPool pool;

    public PasswordsCipher1v2(PasswordsCipherPool pool,
                              KeystoreService keystoreService,
                              SecurityConfig securityConfig,
                              SecurityProvider securityProvider) {
        super(keystoreService, securityConfig, securityProvider);
        this.pool = pool;
    }

    @Override
    public short getVersion() {
        return SUPPORTED_VERSION;
    }

    @Override
    public byte[] encrypt(final Map<UUID, byte[]> passwords) {
        // compute hash-digest of passwords and perform encryption
        byte[] passwordsArray = passwordsToArray(passwords);
        byte[] digest = securityProvider.getMessageDigest().digest(passwordsArray);
        byte[] iv = Base64.getDecoder().decode(securityConfig.getPasswordsFileEncryptionIv());
        byte[] encryptedPasswords = encryptPasswords(passwordsArray, iv);

        // create and fill output array
        int outputLength = securityConfig.getPasswordsFileHeaderLength() +
                securityProvider.getMessageDigest().getDigestLength() +
                securityConfig.getIvLength() +
                encryptedPasswords.length;
        byte[] result = new byte[outputLength];
        ByteBuffer resultBuffer = ByteBuffer.wrap(result);
        resultBuffer.putInt(securityConfig.getPasswordsFileSignature());
        resultBuffer.putShort(SUPPORTED_VERSION);
        resultBuffer.put(digest);
        resultBuffer.put(iv);
        resultBuffer.put(encryptedPasswords);
        return result;
    }

    @Override
    public Map<UUID, byte[]> decrypt(final byte[] input) {
        ByteBuffer inputBuffer = ByteBuffer.wrap(input);
        // check signature and obscurity version
        keystoreService.checkSignature(inputBuffer.getInt());
        checkVersion(inputBuffer.getShort(), SUPPORTED_VERSION);

        // load checksum, initial vector and encrypted passwords
        byte[] checksum = new byte[securityProvider.getMessageDigest().getDigestLength()];
        inputBuffer.get(checksum);
        byte[] iv = new byte[securityConfig.getIvLength()];
        inputBuffer.get(iv);
        byte[] encryptedPasswords = new byte[inputBuffer.remaining()];
        inputBuffer.get(encryptedPasswords);

        // decrypt passwords and validate using checksum
        byte[] decryptedPasswords = decryptPasswords(encryptedPasswords, iv);
        byte[] digest = securityProvider.getMessageDigest().digest(decryptedPasswords);
        if (!Arrays.equals(digest, checksum)) {
            throw new CryptoException("Passwords file is corrupted: checksum doesn't match");
        }
        return readPasswords(ByteBuffer.wrap(decryptedPasswords));
    }

    private byte[] encryptPasswords(final byte[] passwords, final byte[] iv) {
        // decode key for encryption
        byte[] secretKey = Base64.getDecoder().decode(securityConfig.getPasswordsFileEncryptionKey());
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, securityConfig.getPasswordsEncryptionAlgorithm());

        // encrypt passwords
        try {
            synchronized (this) {
                Cipher cipher = pool.borrowCipher(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
                byte[] encryptedPasswords = cipher.doFinal(passwords);
                pool.returnCipher(cipher);
                return encryptedPasswords;
            }
        } catch (Exception e) {
            throw new CryptoException("Cannot encrypt passwords", e);
        }
    }

    private byte[] decryptPasswords(final byte[] encryptedPasswords, final byte[] iv) {
        // decode key for decryption
        byte[] secretKey = Base64.getDecoder().decode(securityConfig.getPasswordsFileEncryptionKey());
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, securityConfig.getPasswordsEncryptionAlgorithm());

        // decrypt passwords
        try {
            synchronized (this) {
                Cipher cipher = pool.borrowCipher(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
                byte[] decryptedPasswords = cipher.doFinal(encryptedPasswords);
                pool.returnCipher(cipher);
                return decryptedPasswords;
            }
        } catch (Exception e) {
            throw new CryptoException("Cannot decrypt passwords", e);
        }
    }

    private byte[] passwordsToArray(final Map<UUID, byte[]> passwords) {
        byte[] result = new byte[passwords.size() *
                (securityConfig.getPasswordLength() + securityConfig.getCipherKeyIdLength())];
        writePasswords(passwords, ByteBuffer.wrap(result));
        return result;
    }
}
