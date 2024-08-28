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
package com.ericsson.eo.evnfm.crypto.presentation.services.config;

import com.ericsson.eo.evnfm.crypto.presentation.model.SecurityConfig;
import com.ericsson.eo.evnfm.crypto.presentation.components.VectorGenerator;
import com.ericsson.eo.evnfm.crypto.util.cipher.message.MessageCipher;
import com.ericsson.eo.evnfm.crypto.util.cipher.message.MessageCipher1v0;
import com.ericsson.eo.evnfm.crypto.util.cipher.pool.MessageCipherPool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class CipherVersionServiceConfig {

    private final MessageCipherPool messageCipherPool;
    private final VectorGenerator vectorGenerator;
    private final SecurityConfig securityConfig;

    @Autowired
    public CipherVersionServiceConfig(MessageCipherPool messageCipherPool,
                                      VectorGenerator vectorGenerator,
                                      SecurityConfig securityConfig) {
        this.messageCipherPool = messageCipherPool;
        this.vectorGenerator = vectorGenerator;
        this.securityConfig = securityConfig;
    }

    @Bean
    public Map<Short, MessageCipher> getMessageCiphers() {
        MessageCipher cipher = new MessageCipher1v0(messageCipherPool, vectorGenerator, securityConfig);
        return Map.of(cipher.getVersion(), cipher);
    }
}
