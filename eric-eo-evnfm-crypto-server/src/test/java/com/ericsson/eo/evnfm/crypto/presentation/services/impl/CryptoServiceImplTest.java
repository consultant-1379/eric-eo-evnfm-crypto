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
package com.ericsson.eo.evnfm.crypto.presentation.services.impl;

import com.ericsson.eo.evnfm.crypto.TestUtils;
import com.ericsson.eo.evnfm.crypto.exceptions.CryptoException;
import com.ericsson.eo.evnfm.crypto.exceptions.DecryptionException;
import com.ericsson.eo.evnfm.crypto.exceptions.EmptyBodyRequestException;
import com.ericsson.eo.evnfm.crypto.model.DecryptionPostRequest;
import com.ericsson.eo.evnfm.crypto.model.DecryptionResponse;
import com.ericsson.eo.evnfm.crypto.model.EncryptionPostRequest;
import com.ericsson.eo.evnfm.crypto.model.EncryptionResponse;
import com.ericsson.eo.evnfm.crypto.presentation.components.VectorGenerator;
import com.ericsson.eo.evnfm.crypto.presentation.model.CipherKey;
import com.ericsson.eo.evnfm.crypto.presentation.model.SecurityConfig;
import com.ericsson.eo.evnfm.crypto.presentation.services.KeyService;
import com.ericsson.eo.evnfm.crypto.util.cipher.message.MessageCipher;
import com.ericsson.eo.evnfm.crypto.util.cipher.message.MessageCipher1v0;
import com.ericsson.eo.evnfm.crypto.util.cipher.pool.MessageCipherPool;
import com.ericsson.eo.evnfm.crypto.util.cipher.pool.MessageCipherPoolableObjectFactory;
import org.apache.commons.pool.impl.StackObjectPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class CryptoServiceImplTest {

    private static final String EXPECTED = "- Sure we can!\n- No, you can't.";
    private static final SecurityConfig SECURITY_CONFIG = TestUtils.initSecurityConfig();
    private static final SecureRandom SECURE_RANDOM;

    private static CipherKey cipherKey;
    private static CipherKey olderKey;

    private CryptoServiceImpl cryptoServiceImpl;

    private Cipher encryptCipher;
    private Cipher decryptCipher;

    static {
        try {
            SECURE_RANDOM = SecureRandom.getInstance(SECURITY_CONFIG.getSecureRandomAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    public void setup() throws NoSuchPaddingException, NoSuchAlgorithmException {
        KeyService keyService = mock(KeyService.class);
        MessageCipherVersionServiceImpl versionService = mock(MessageCipherVersionServiceImpl.class);
        MessageCipher messageCipher = createMessageCipher();

        cipherKey = readSecretKey("cipherKey");
        olderKey = readSecretKey("olderKey");

        encryptCipher = Cipher.getInstance(SECURITY_CONFIG.getTransformation());
        decryptCipher = Cipher.getInstance(SECURITY_CONFIG.getTransformation());

        MockitoAnnotations.openMocks(this);

        when(keyService.getCipherKeyById(cipherKey.getAlias())).thenReturn(cipherKey);
        when(keyService.getLatestCipherKey()).thenReturn(cipherKey);
        when(keyService.getCipherKeyById(olderKey.getAlias())).thenReturn(olderKey);

        when(versionService.getLatestCipher()).thenReturn(messageCipher);
        when(versionService.getCipher(SECURITY_CONFIG.getLatestEncryptionDecryptionVersion()))
                .thenReturn(messageCipher);

        cryptoServiceImpl = new CryptoServiceImpl(keyService, versionService, SECURITY_CONFIG);
    }

    @Test
    public void testEncrypt() throws IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, InvalidKeyException, NoSuchAlgorithmException {

        EncryptionPostRequest request = new EncryptionPostRequest();
        request.setPlaintext(EXPECTED);
        EncryptionResponse response = cryptoServiceImpl.encrypt(request);
        checkEncrypted(response.getCiphertext(), EXPECTED);
    }

    @Test
    public void testEncryptEmptyString() throws IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, InvalidKeyException, NoSuchAlgorithmException {

        EncryptionPostRequest request = new EncryptionPostRequest();
        request.setPlaintext("");
        EncryptionResponse response = cryptoServiceImpl.encrypt(request);
        checkEncrypted(response.getCiphertext(), "");
    }

    @Test
    public void shouldThrowExceptionWhenEmptyRequestOnEncrypt() {
        assertThrows(EmptyBodyRequestException.class, () -> {
            cryptoServiceImpl.encrypt(null);
        });
    }

    @Test
    public void testDecrypt() throws IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, InvalidKeyException, NoSuchAlgorithmException {

        DecryptionPostRequest request = new DecryptionPostRequest();
        request.setCiphertext(createEncrypted(cipherKey, EXPECTED));
        assertEquals(EXPECTED, cryptoServiceImpl.decrypt(request).getPlaintext());
    }

    @Test
    public void testDecryptEmptyString() throws IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, InvalidKeyException, NoSuchAlgorithmException {

        DecryptionPostRequest request = new DecryptionPostRequest();
        request.setCiphertext(createEncrypted(cipherKey, ""));
        assertEquals("", cryptoServiceImpl.decrypt(request).getPlaintext());
    }

    @Test
    public void testDecryptWithOlderKey() throws IllegalBlockSizeException, BadPaddingException,
            InvalidAlgorithmParameterException, InvalidKeyException, NoSuchAlgorithmException {

        DecryptionPostRequest request = new DecryptionPostRequest();
        request.setCiphertext(createEncrypted(olderKey, EXPECTED));
        assertEquals(EXPECTED, cryptoServiceImpl.decrypt(request).getPlaintext());
    }

    @Test
    public void shouldThrowExceptionWhenEmptyRequestOnDecrypt() {
        assertThrows(EmptyBodyRequestException.class, () -> {
            cryptoServiceImpl.decrypt(null);
        });
    }

    @Test
    public void shouldThrowExceptionWhenNotValidRequestOnDecrypt() {
        assertThrows(DecryptionException.class, () -> {
            DecryptionPostRequest decryptionPostRequest = new DecryptionPostRequest();
            decryptionPostRequest.setCiphertext("Y2lwaGVyIHRleHQ=");

            cryptoServiceImpl.decrypt(decryptionPostRequest);
        });
    }

    @Test
    public void runEncryptionDecryptionTest() {
        EncryptionPostRequest request = new EncryptionPostRequest();
        request.setPlaintext(EXPECTED);
        EncryptionResponse response = cryptoServiceImpl.encrypt(request);
        String cipherText = response.getCiphertext();

        DecryptionPostRequest decryptionRequest = new DecryptionPostRequest();
        decryptionRequest.setCiphertext(cipherText);
        DecryptionResponse decryptionResponse = cryptoServiceImpl.decrypt(decryptionRequest);
        assertEquals(EXPECTED, decryptionResponse.getPlaintext());
    }

    private MessageCipher createMessageCipher() {
        VectorGenerator vectorGenerator = new VectorGenerator(SECURITY_CONFIG);
        MessageCipherPoolableObjectFactory poolableObjectFactory =
                new MessageCipherPoolableObjectFactory(SECURITY_CONFIG);
        StackObjectPool stackObjectPool = new StackObjectPool(poolableObjectFactory);
        MessageCipherPool cipherPool = new MessageCipherPool(stackObjectPool);

        return new MessageCipher1v0(cipherPool, vectorGenerator, SECURITY_CONFIG);
    }

    private CipherKey readSecretKey(String name) {
        Path path = Paths.get(getClass().getClassLoader().getResource(name).getPath());

        byte[] cipherKeyData = new byte[SECURITY_CONFIG.getCipherKeyLength() / 8];
        try (InputStream keyInputStream = Base64.getDecoder().wrap(Files.newInputStream(path))) {
            int size = keyInputStream.read(cipherKeyData);
            if (size != cipherKeyData.length) {
                throw new CryptoException(String.format("Test secret key at %s is corrupted.", path));
            }

            ByteBuffer buffer = ByteBuffer.wrap(cipherKeyData);
            byte[] cipherKey = new byte[SECURITY_CONFIG.getCipherKeyLength() / 8];
            buffer.get(cipherKey);

            SecretKey secretKey = new SecretKeySpec(cipherKey, 0, cipherKey.length,
                                                    SECURITY_CONFIG.getEncryptionAlgorithm());
            return new CipherKey(UUID.randomUUID(), secretKey, LocalDateTime.now().minusDays(1L), null);
        } catch (IOException ioe) {
            throw new CryptoException(String.format("Error loading test secret key %s", path), ioe);
        }
    }

    private void checkEncrypted(String encrypted, String expected) throws InvalidAlgorithmParameterException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException {

        byte[] payload = Base64.getDecoder().decode(encrypted);
        ByteBuffer buffer = ByteBuffer.wrap(payload);

        short version = buffer.getShort();
        assertEquals(SECURITY_CONFIG.getLatestEncryptionDecryptionVersion(), version);

        UUID keyId = new UUID(buffer.getLong(), buffer.getLong());
        assertEquals(cipherKey.getAlias(), keyId);

        MessageDigest messageDigest = MessageDigest.getInstance(SECURITY_CONFIG.getDigestAlgorithm());
        byte[] expectedDigest = new byte[messageDigest.getDigestLength()];
        buffer.get(expectedDigest);

        byte[] initialVector = new byte[SECURITY_CONFIG.getIvLength()];
        buffer.get(initialVector);

        byte[] ciphertext = new byte[buffer.remaining()];
        buffer.get(ciphertext);

        IvParameterSpec ivParameterSpec = new IvParameterSpec(initialVector);
        decryptCipher.init(Cipher.DECRYPT_MODE,
                           new SecretKeySpec(cipherKey.getKey().getEncoded(), SECURITY_CONFIG.getEncryptionAlgorithm()),
                           ivParameterSpec);
        byte[] decryptedData = decryptCipher.doFinal(ciphertext);

        byte[] plaintextDigest = messageDigest.digest(decryptedData);
        assertArrayEquals(expectedDigest, plaintextDigest);

        String decrypted = new String(decryptedData, StandardCharsets.UTF_8);
        assertEquals(expected, decrypted);
    }

    private String createEncrypted(CipherKey key, String plaintext) throws InvalidAlgorithmParameterException,
            InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchAlgorithmException {

        byte[] plaintextBytes = plaintext.getBytes(StandardCharsets.UTF_8);

        byte[] initialVector = new byte[SECURITY_CONFIG.getIvLength()];
        SECURE_RANDOM.nextBytes(initialVector);

        MessageDigest messageDigest = MessageDigest.getInstance(SECURITY_CONFIG.getDigestAlgorithm());
        byte[] plaintextDigest = messageDigest.digest(plaintextBytes);

        IvParameterSpec ivSpec = new IvParameterSpec(initialVector);
        encryptCipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key.getKey().getEncoded(), SECURITY_CONFIG.getEncryptionAlgorithm()), ivSpec);
        byte[] encryptedData = encryptCipher.doFinal(plaintextBytes);

        int resultLength = 2 + 16 + messageDigest.getDigestLength() + SECURITY_CONFIG.getIvLength() + encryptedData.length;
        byte[] result = new byte[resultLength];

        ByteBuffer buffer = ByteBuffer.wrap(result);
        buffer.putShort(SECURITY_CONFIG.getLatestEncryptionDecryptionVersion());
        buffer.putLong(key.getAlias().getMostSignificantBits());
        buffer.putLong(key.getAlias().getLeastSignificantBits());
        buffer.put(plaintextDigest);
        buffer.put(initialVector);
        buffer.put(encryptedData);

        return Base64.getEncoder().encodeToString(result);
    }
}
