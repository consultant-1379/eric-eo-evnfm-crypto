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
package com.ericsson.eo.evnfm.crypto.migration.presentation.services.impl;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.ericsson.eo.evnfm.crypto.migration.presentation.services.AbstractCipherVersionService;
import com.ericsson.eo.evnfm.crypto.migration.presentation.services.CipherVersionService;
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.message.MessageCipher;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.SecurityConfig;

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
