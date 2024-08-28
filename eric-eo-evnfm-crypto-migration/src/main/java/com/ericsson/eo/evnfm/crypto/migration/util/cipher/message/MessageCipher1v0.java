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
package com.ericsson.eo.evnfm.crypto.migration.util.cipher.message;

import static com.ericsson.eo.evnfm.crypto.migration.util.Constants.ERROR_DECRYPTING_DATA;
import static com.ericsson.eo.evnfm.crypto.migration.util.Constants.ERROR_ENCRYPTING_DATA;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.eo.evnfm.crypto.migration.exceptions.CryptoException;
import com.ericsson.eo.evnfm.crypto.migration.exceptions.DecryptionException;
import com.ericsson.eo.evnfm.crypto.migration.exceptions.EncryptionException;
import com.ericsson.eo.evnfm.crypto.migration.presentation.components.VectorGenerator;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.CipherKey;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.SecurityConfig;
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.pool.MessageCipherPool;

public class MessageCipher1v0 implements MessageCipher {
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageCipher1v0.class);
    private static final short SUPPORTED_VERSION = 0x0001;

    private final MessageCipherPool pool;

    private final VectorGenerator vectorGenerator;

    private final SecurityConfig securityConfig;

    public MessageCipher1v0(MessageCipherPool pool, VectorGenerator vectorGenerator, SecurityConfig securityConfig) {
        this.pool = pool;
        this.vectorGenerator = vectorGenerator;
        this.securityConfig = securityConfig;
    }

    @Override
    public short getVersion() {
        return SUPPORTED_VERSION;
    }

    @Override
    public String encrypt(final byte[] plaintext, final CipherKey cipherKey) {
        MessageDigest messageDigest = getMessageDigest();
        byte[] plaintextDigest = messageDigest.digest(plaintext);

        byte[] initialVector = vectorGenerator.generateInitialVector();
        IvParameterSpec ivParameterSpec = new IvParameterSpec(initialVector);

        byte[] ciphertextBytes;
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    cipherKey.getKey().getEncoded(), securityConfig.getEncryptionAlgorithm());
            synchronized (this) {
                Cipher encryptCipher = pool.borrowCipher(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

                ciphertextBytes = encryptCipher.doFinal(plaintext);

                pool.returnCipher(encryptCipher);
            }
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.error(ERROR_ENCRYPTING_DATA, e);
            throw new EncryptionException(ERROR_ENCRYPTING_DATA, e);
        }

        byte[] encryptedBytes = combineToByteArray(SUPPORTED_VERSION, cipherKey.getAlias(), plaintextDigest,
                initialVector, ciphertextBytes);
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    @Override
    public String decrypt(final byte[] ciphertext, final CipherKey cipherKey) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(ciphertext);

        MessageDigest messageDigest = getMessageDigest();
        byte[] expectedDigest = new byte[messageDigest.getDigestLength()];
        byteBuffer.get(expectedDigest);

        byte[] initialVector = new byte[securityConfig.getIvLength()];
        byteBuffer.get(initialVector);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(initialVector);

        byte[] cipherText = new byte[byteBuffer.remaining()];
        byteBuffer.get(cipherText);

        String plaintext;
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    cipherKey.getKey().getEncoded(), securityConfig.getEncryptionAlgorithm());
            Cipher decryptCipher = pool.borrowCipher(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            byte[] plaintextBytes = decryptCipher.doFinal(cipherText);

            pool.returnCipher(decryptCipher);

            byte[] plaintextDigest = messageDigest.digest(plaintextBytes);
            if (!Arrays.areEqual(plaintextDigest, expectedDigest)) {
                throw new DecryptionException("Ciphertext was changed");
            }
            plaintext = new String(plaintextBytes, StandardCharsets.UTF_8);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            LOGGER.error(ERROR_DECRYPTING_DATA, e);
            throw new DecryptionException(ERROR_DECRYPTING_DATA, e);
        }

        return plaintext;
    }

    private MessageDigest getMessageDigest() {
        try {
            return MessageDigest.getInstance(securityConfig.getDigestAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException("Unable to obtain message digest instance", e);
        }
    }

    private byte[] combineToByteArray(short version, UUID cipherKeyAlias,
                                             byte[] plaintextDigest, byte[] iv, byte[] ciphertextBytes) {
        ByteBuffer byteBuffer =
                ByteBuffer.allocate(2 + securityConfig.getCipherKeyIdLength()
                                            + plaintextDigest.length + iv.length + ciphertextBytes.length);
        byteBuffer.putShort(version);
        byteBuffer.putLong(cipherKeyAlias.getMostSignificantBits());
        byteBuffer.putLong(cipherKeyAlias.getLeastSignificantBits());
        byteBuffer.put(plaintextDigest);
        byteBuffer.put(iv);
        byteBuffer.put(ciphertextBytes);
        return byteBuffer.array();
    }
}
