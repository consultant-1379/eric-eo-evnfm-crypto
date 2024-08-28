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
package com.ericsson.eo.evnfm.crypto.util.cipher.pool.config;

import com.ericsson.eo.evnfm.crypto.util.cipher.pool.MessageCipherPool;
import com.ericsson.eo.evnfm.crypto.util.cipher.pool.MessageCipherPoolableObjectFactory;
import org.apache.commons.pool.impl.StackObjectPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CipherPoolConfig {
    private final MessageCipherPoolableObjectFactory messageFactory;

    @Autowired
    public CipherPoolConfig(MessageCipherPoolableObjectFactory messageFactory) {
        this.messageFactory = messageFactory;
    }

    @Bean
    public MessageCipherPool getMessagePool() {
        return new MessageCipherPool(new StackObjectPool(messageFactory));
    }
}
