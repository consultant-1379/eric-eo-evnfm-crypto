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
import java.util.Map;
import java.util.UUID;

import com.ericsson.eo.evnfm.crypto.migration.exceptions.CryptoException;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.SecurityConfig;
import com.ericsson.eo.evnfm.crypto.migration.presentation.services.KeystoreService;
import com.ericsson.eo.evnfm.crypto.migration.security.SecurityProvider;

/**
 * This version of passwords encryption/decryption is to weak.
 * Please use version {@link PasswordsCipher1v2} or later.
 */
public class PasswordsCipher1v0 extends AbstractPasswordsCipher {
    private static final short SUPPORTED_VERSION = (short) 0x0100;

    public PasswordsCipher1v0(KeystoreService keystoreService,
                              SecurityConfig securityConfig,
                              SecurityProvider securityProvider) {
        super(keystoreService, securityConfig, securityProvider);
    }

    @Override
    public short getVersion() {
        return SUPPORTED_VERSION;
    }

    @Override
    public byte[] encrypt(final Map<UUID, byte[]> passwords) {
        int size = securityConfig.getPasswordsFileHeaderLength()
                + passwords.size() * (securityConfig.getPasswordLength() + securityConfig.getCipherKeyIdLength())
                + securityProvider.getMessageDigest().getDigestLength();

        final byte[] output = new byte[size];
        final ByteBuffer outBuffer = ByteBuffer.wrap(output);

        outBuffer.putInt(securityConfig.getPasswordsFileSignature());
        outBuffer.putShort(SUPPORTED_VERSION);
        writePasswords(passwords, outBuffer);
        securityProvider.getMessageDigest().update(output, 0, outBuffer.position());

        final byte[] digest = securityProvider.getMessageDigest().digest();
        encryptPasswords(output, outBuffer.position());
        outBuffer.put(digest);

        return output;
    }

    @Override
    public Map<UUID, byte[]> decrypt(final byte[] input) {
        final ByteBuffer inBuffer = ByteBuffer.wrap(input);
        keystoreService.checkSignature(inBuffer.getInt());
        checkVersion(inBuffer.getShort(), SUPPORTED_VERSION);

        final int contentLength = input.length - securityProvider.getMessageDigest().getDigestLength();
        final ByteBuffer passwordsBuffer = inBuffer.slice();
        passwordsBuffer.limit(contentLength - securityConfig.getPasswordsFileHeaderLength());

        final byte[] inDigest = new byte[securityProvider.getMessageDigest().getDigestLength()];
        inBuffer.position(contentLength);
        inBuffer.get(inDigest);
        decryptPasswords(input, contentLength);
        securityProvider.getMessageDigest().update(input, 0, contentLength);

        final byte[] checkDigest = securityProvider.getMessageDigest().digest();
        if (!Arrays.equals(inDigest, checkDigest)) {
            throw new CryptoException("Passwords file is corrupted: checksum doesn't match");
        }

        return readPasswords(passwordsBuffer);
    }

    private void encryptPasswords(final byte[] output, final int length) {
        byte xorByte = 't';
        for (int i = securityConfig.getPasswordsFileHeaderLength(); i < length; i++) {
            final byte value = output[i];
            output[i] = (byte) ((value ^ xorByte) & 0xFF);
            xorByte = value;
        }
    }

    private void decryptPasswords(final byte[] input, final int contentLength) {
        byte xorByte = 't';
        for (int i = securityConfig.getPasswordsFileHeaderLength(); i < contentLength; i++) {
            input[i] ^= xorByte;
            xorByte = input[i];
        }
    }
}
