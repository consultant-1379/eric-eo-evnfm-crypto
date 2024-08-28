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

import com.ericsson.eo.evnfm.crypto.presentation.model.SecurityConfig;
import com.ericsson.eo.evnfm.crypto.presentation.services.AbstractCipherVersionService;
import com.ericsson.eo.evnfm.crypto.presentation.services.CipherVersionService;
import com.ericsson.eo.evnfm.crypto.util.cipher.message.MessageCipher;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MessageCipherVersionServiceImpl extends AbstractCipherVersionService<MessageCipher>
        implements CipherVersionService<MessageCipher> {

    private final SecurityConfig securityConfig;

    public MessageCipherVersionServiceImpl(Map<Short, MessageCipher> versionToCipherMap,
                                           SecurityConfig securityConfig) {
        super(versionToCipherMap);
        this.securityConfig = securityConfig;
    }

    @Override
    public MessageCipher getLatestCipher() {
        return getCipher(securityConfig.getLatestEncryptionDecryptionVersion());
    }
}
