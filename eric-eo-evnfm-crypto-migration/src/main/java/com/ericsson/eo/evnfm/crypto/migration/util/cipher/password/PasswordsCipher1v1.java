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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.ericsson.eo.evnfm.crypto.migration.exceptions.CryptoException;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.SecurityConfig;
import com.ericsson.eo.evnfm.crypto.migration.presentation.services.KeystoreService;
import com.ericsson.eo.evnfm.crypto.migration.security.SecurityProvider;
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.pool.PasswordsCipherPool;

/**
 * This version of passwords encryption/decryption works correctly only for the
 * number of passwords equal to $2$. Please use version {@link PasswordsCipher1v2} or later.
 */
public class PasswordsCipher1v1 extends AbstractPasswordsCipher {
    private static final short SUPPORTED_VERSION = (short) 0x0101;
    private static final byte[] GARBAGE = "garbage".getBytes(StandardCharsets.UTF_16);
    private static final int ENCRYPTION_GARBAGE_SIZE = GARBAGE.length;
    private static final int DECRYPTION_GARBAGE_SIZE = 20;

    private final PasswordsCipherPool pool;

    public PasswordsCipher1v1(PasswordsCipherPool pool,
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
        byte[] output = new byte[securityConfig.getPasswordsFileHeaderLength() + ENCRYPTION_GARBAGE_SIZE
                + passwords.size() * (securityConfig.getPasswordLength()
                + securityConfig.getCipherKeyIdLength() + 2)
                + securityProvider.getMessageDigest().getDigestLength()];
        ByteBuffer outBuffer = ByteBuffer.wrap(output);
        outBuffer.putInt(securityConfig.getPasswordsFileSignature());
        outBuffer.putShort(SUPPORTED_VERSION);
        writePasswords(passwords, outBuffer);
        securityProvider.getMessageDigest().update(output, 0, outBuffer.position());
        byte[] digest = securityProvider.getMessageDigest().digest();
        encryptPasswords(output, outBuffer.position(), ENCRYPTION_GARBAGE_SIZE + passwords.size() * 2);
        outBuffer.position(outBuffer.position() + ENCRYPTION_GARBAGE_SIZE + passwords.size() * 2);
        outBuffer.put(digest);
        return output;
    }

    @Override
    public Map<UUID, byte[]> decrypt(final byte[] input) {
        ByteBuffer inBuffer = ByteBuffer.wrap(input);
        keystoreService.checkSignature(inBuffer.getInt());
        checkVersion(inBuffer.getShort(), SUPPORTED_VERSION);
        int contentLength = input.length - securityProvider.getMessageDigest().getDigestLength();
        ByteBuffer passwordsBuffer = inBuffer.slice();
        passwordsBuffer.limit(contentLength - securityConfig.getPasswordsFileHeaderLength() - DECRYPTION_GARBAGE_SIZE);
        byte[] inDigest = new byte[securityProvider.getMessageDigest().getDigestLength()];
        inBuffer.position(contentLength);
        inBuffer.get(inDigest);
        decryptPasswords(input, contentLength);
        securityProvider.getMessageDigest().update(input, 0, contentLength - DECRYPTION_GARBAGE_SIZE);
        byte[] checkDigest = securityProvider.getMessageDigest().digest();
        if (!Arrays.equals(inDigest, checkDigest)) {
            throw new CryptoException("Passwords file is corrupted: checksum doesn't match");
        }
        return readPasswords(passwordsBuffer);
    }

    private void encryptPasswords(byte[] output, final int length, final int extraSize) {
        byte xorByte = 't';
        for (int i = securityConfig.getPasswordsFileHeaderLength(); i < length; i++) {
            byte value = output[i];
            output[i] = (byte) ((value ^ xorByte) & 0xFF);
            xorByte = value;
        }

        byte[] passwords = Arrays.copyOfRange(output, securityConfig.getPasswordsFileHeaderLength(), length);
        byte[] input = new byte[ENCRYPTION_GARBAGE_SIZE + passwords.length];

        ByteBuffer byteBuffer = ByteBuffer.wrap(input);
        byteBuffer.put(GARBAGE);
        byteBuffer.put(passwords);

        byte[] decodedKey = Base64.getDecoder().decode(securityConfig.getPasswordsFileEncryptionKey());

        SecretKeySpec originalKey = new SecretKeySpec(decodedKey, securityConfig.getPasswordsEncryptionAlgorithm());

        try {
            passwords = getEncryptedPasswords(input, originalKey);
        } catch (Exception e) {
            throw new CryptoException("Cannot encrypt passwords", e);
        }

        if (length + extraSize - securityConfig.getPasswordsFileHeaderLength() >= 0) {
            System.arraycopy(passwords,
                             securityConfig.getPasswordsFileHeaderLength() - 6,
                             output,
                             securityConfig.getPasswordsFileHeaderLength(),
                             length + extraSize - securityConfig.getPasswordsFileHeaderLength());
        }
    }

    private synchronized byte[] getEncryptedPasswords(byte[] input, SecretKeySpec originalKey) throws IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = pool.borrowCipher(Cipher.ENCRYPT_MODE, originalKey);
        byte[] passwords = cipher.doFinal(input);
        pool.returnCipher(cipher);
        return passwords;
    }

    private void decryptPasswords(byte[] input, final int contentLength) {
        byte[] passwords = Arrays.copyOfRange(input, securityConfig.getPasswordsFileHeaderLength(), contentLength);
        byte[] decodedKey = Base64.getDecoder().decode(securityConfig.getPasswordsFileEncryptionKey());

        SecretKeySpec originalKey = new SecretKeySpec(decodedKey, securityConfig.getPasswordsEncryptionAlgorithm());

        byte[] iv = new byte[securityConfig.getIvLength()];
        securityProvider.getSecureRandom().nextBytes(iv);

        try {
            synchronized (this) {
                Cipher cipher = pool.borrowCipher(Cipher.DECRYPT_MODE, originalKey, new IvParameterSpec(iv));
                passwords = cipher.doFinal(passwords);
                pool.returnCipher(cipher);
            }
        } catch (Exception e) {
            throw new CryptoException("Cannot decrypt passwords", e);
        }

        byte[] output = Arrays.copyOfRange(passwords, securityConfig.getIvLength(), passwords.length);

        if (contentLength - DECRYPTION_GARBAGE_SIZE - securityConfig.getPasswordsFileHeaderLength() >= 0) {
            System.arraycopy(output,
                             securityConfig.getPasswordsFileHeaderLength() - 6,
                             input,
                             securityConfig.getPasswordsFileHeaderLength(),
                             contentLength - DECRYPTION_GARBAGE_SIZE - securityConfig.getPasswordsFileHeaderLength());
        }

        byte xorByte = 't';
        for (int i = securityConfig.getPasswordsFileHeaderLength(); i < contentLength - DECRYPTION_GARBAGE_SIZE; i++) {
            input[i] ^= xorByte;
            xorByte = input[i];
        }
    }
}
