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
package com.ericsson.eo.evnfm.crypto.migration.util.cipher.pool;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.pool.impl.StackObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.eo.evnfm.crypto.migration.exceptions.CryptoException;
import com.ericsson.eo.evnfm.crypto.migration.util.Constants;

public class PasswordsCipherPool extends CipherPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordsCipherPool.class);

    public PasswordsCipherPool(StackObjectPool pool) {
        super(pool);
    }

    public Cipher borrowCipher(int mode, SecretKeySpec secretKeySpec) {
        Cipher cipher = getCipher();
        return initCipher(cipher, mode, secretKeySpec);
    }

    private static Cipher initCipher(Cipher cipher, int mode, SecretKeySpec secretKeySpec) {
        try {
            cipher.init(mode, secretKeySpec);
        } catch (Exception e) {
            LOGGER.error(Constants.FAIL_INIT_CIPHER_MESSAGE, e);
            throw new CryptoException(Constants.FAIL_INIT_CIPHER_MESSAGE, e);
        }
        return cipher;
    }
}
