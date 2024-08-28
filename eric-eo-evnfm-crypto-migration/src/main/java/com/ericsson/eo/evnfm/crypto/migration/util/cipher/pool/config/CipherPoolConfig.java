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
package com.ericsson.eo.evnfm.crypto.migration.util.cipher.pool.config;

import org.apache.commons.pool.impl.StackObjectPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ericsson.eo.evnfm.crypto.migration.util.cipher.pool.MessageCipherPool;
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.pool.MessageCipherPoolableObjectFactory;
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.pool.PasswordsCipherPool;
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.pool.PasswordsCipherPoolableObjectFactory;

@Configuration
public class CipherPoolConfig {
    private final MessageCipherPoolableObjectFactory messageFactory;
    private final PasswordsCipherPoolableObjectFactory passwordsFactory;

    @Autowired
    public CipherPoolConfig(MessageCipherPoolableObjectFactory messageFactory,
                            PasswordsCipherPoolableObjectFactory passwordsFactory) {
        this.messageFactory = messageFactory;
        this.passwordsFactory = passwordsFactory;
    }

    @Bean
    public MessageCipherPool getMessagePool() {
        return new MessageCipherPool(new StackObjectPool(messageFactory));
    }

    @Bean
    public PasswordsCipherPool getPasswordsPool() {
        return new PasswordsCipherPool(new StackObjectPool(passwordsFactory));
    }
}
