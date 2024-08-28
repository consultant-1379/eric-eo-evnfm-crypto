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
package com.ericsson.eo.evnfm.crypto.util.cipher.pool;

import com.ericsson.eo.evnfm.crypto.exceptions.CryptoException;
import com.ericsson.eo.evnfm.crypto.util.Constants;
import org.apache.commons.pool.impl.StackObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;

public abstract class CipherPool {

    private static final Logger LOGGER = LoggerFactory.getLogger(CipherPool.class);

    protected final StackObjectPool pool;

    public CipherPool(StackObjectPool pool) {
        this.pool = pool;
    }

    public synchronized Cipher borrowCipher(int mode, SecretKeySpec secretKeySpec, IvParameterSpec ivParameterSpec) {
        return initCipher(getCipher(), mode, secretKeySpec, ivParameterSpec);
    }

    public synchronized void returnCipher(Cipher cipher) {
        try {
            pool.returnObject(cipher);
        } catch (Exception e) {
            throw new CryptoException(Constants.ERROR_UNABLE_TO_RETURN_OBJECT, e);
        }
    }

    protected Cipher getCipher() {
        try {
            return (Cipher) pool.borrowObject();
        } catch (Exception e) {
            LOGGER.error(Constants.FAIL_GET_CIPHER_MESSAGE, e);
            throw new CryptoException(Constants.FAIL_GET_CIPHER_MESSAGE, e);
        }
    }

    private static Cipher initCipher(Cipher cipher, int mode,
                                       SecretKeySpec secretKeySpec, IvParameterSpec ivParameterSpec) {
        try {
            cipher.init(mode, secretKeySpec, ivParameterSpec);
        } catch (UnsupportedOperationException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            LOGGER.error(Constants.FAIL_INIT_CIPHER_MESSAGE, e);
            throw new CryptoException(Constants.FAIL_INIT_CIPHER_MESSAGE, e);
        }
        return cipher;
    }
}
