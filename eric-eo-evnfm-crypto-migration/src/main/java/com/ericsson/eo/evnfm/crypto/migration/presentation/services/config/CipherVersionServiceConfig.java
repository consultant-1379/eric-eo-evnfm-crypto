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
package com.ericsson.eo.evnfm.crypto.migration.presentation.services.config;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.ericsson.eo.evnfm.crypto.migration.security.SecurityProvider;
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.message.MessageCipher1v0;
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.password.PasswordsCipher;
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.password.PasswordsCipher1v0;
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.password.PasswordsCipher1v1;
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.password.PasswordsCipher1v2;
import com.ericsson.eo.evnfm.crypto.migration.presentation.components.VectorGenerator;
import com.ericsson.eo.evnfm.crypto.migration.presentation.model.SecurityConfig;
import com.ericsson.eo.evnfm.crypto.migration.presentation.services.KeystoreService;
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.message.MessageCipher;
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.pool.MessageCipherPool;
import com.ericsson.eo.evnfm.crypto.migration.util.cipher.pool.PasswordsCipherPool;

@Configuration
public class CipherVersionServiceConfig {

    private final MessageCipherPool messageCipherPool;
    private final PasswordsCipherPool passwordsCipherPool;
    private final KeystoreService keystoreService;
    private final VectorGenerator vectorGenerator;
    private final SecurityConfig securityConfig;
    private final SecurityProvider securityProvider;

    @Autowired
    public CipherVersionServiceConfig(MessageCipherPool messageCipherPool,
                                      PasswordsCipherPool passwordsCipherPool,
                                      @Lazy KeystoreService keystoreService,
                                      VectorGenerator vectorGenerator,
                                      SecurityConfig securityConfig,
                                      SecurityProvider securityProvider) {
        this.messageCipherPool = messageCipherPool;
        this.passwordsCipherPool = passwordsCipherPool;
        this.keystoreService = keystoreService;
        this.vectorGenerator = vectorGenerator;
        this.securityConfig = securityConfig;
        this.securityProvider = securityProvider;
    }

    @Bean
    public Map<Short, MessageCipher> getMessageCiphers() {
        MessageCipher cipher = new MessageCipher1v0(messageCipherPool, vectorGenerator, securityConfig);
        return Map.of(cipher.getVersion(), cipher);
    }

    @Bean
    public Map<Short, PasswordsCipher> getPasswordsCiphers() {
        PasswordsCipher passwordsCipher1v0 = new PasswordsCipher1v0(keystoreService, securityConfig, securityProvider);
        PasswordsCipher passwordsCipher1v1 = new PasswordsCipher1v1(
                passwordsCipherPool, keystoreService, securityConfig, securityProvider);
        PasswordsCipher passwordsCipher1v2 = new PasswordsCipher1v2(
                passwordsCipherPool, keystoreService, securityConfig, securityProvider);

        return Map.of(
                passwordsCipher1v0.getVersion(), passwordsCipher1v0,
                passwordsCipher1v1.getVersion(), passwordsCipher1v1,
                passwordsCipher1v2.getVersion(), passwordsCipher1v2);
    }
}
