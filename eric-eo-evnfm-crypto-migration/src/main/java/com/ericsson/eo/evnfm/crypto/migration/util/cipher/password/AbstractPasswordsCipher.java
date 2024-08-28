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

import static com.ericsson.eo.evnfm.crypto.migration.util.Constants.ERROR_UNKNOWN_OBSCURITY_METHOD;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.eo.evnfm.crypto.migration.exceptions.CryptoException;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.SecurityConfig;
import com.ericsson.eo.evnfm.crypto.migration.presentation.services.KeystoreService;
import com.ericsson.eo.evnfm.crypto.migration.security.SecurityProvider;

public abstract class AbstractPasswordsCipher implements PasswordsCipher {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPasswordsCipher.class);

    protected KeystoreService keystoreService;

    protected final SecurityConfig securityConfig;

    protected final SecurityProvider securityProvider;

    public AbstractPasswordsCipher(KeystoreService keystoreService,
                                   SecurityConfig securityConfig,
                                   SecurityProvider securityProvider) {
        this.keystoreService = keystoreService;
        this.securityConfig = securityConfig;
        this.securityProvider = securityProvider;
    }

    protected static void checkVersion(final short version, short supportedVersion) {
        if (version != supportedVersion) {
            LOGGER.error(ERROR_UNKNOWN_OBSCURITY_METHOD);
            throw new CryptoException(ERROR_UNKNOWN_OBSCURITY_METHOD);
        }
    }

    protected Map<UUID, byte[]> readPasswords(final ByteBuffer passwordsBuffer) {
        Map<UUID, byte[]> passwords = new HashMap<>();
        while (passwordsBuffer.hasRemaining()) {
            UUID keyId = new UUID(passwordsBuffer.getLong(), passwordsBuffer.getLong());
            byte[] password = new byte[securityConfig.getPasswordLength()];
            passwordsBuffer.get(password);
            passwords.put(keyId, password);
        }
        return passwords;
    }

    protected static void writePasswords(final Map<UUID, byte[]> passwords, final ByteBuffer outBuffer) {
        passwords.forEach((keyId, password) -> {
            outBuffer.putLong(keyId.getMostSignificantBits());
            outBuffer.putLong(keyId.getLeastSignificantBits());
            outBuffer.put(password);
        });
    }
}
