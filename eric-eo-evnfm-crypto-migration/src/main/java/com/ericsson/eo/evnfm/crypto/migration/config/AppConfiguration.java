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
package com.ericsson.eo.evnfm.crypto.migration.config;

import java.io.InputStream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.ericsson.eo.evnfm.crypto.migration.presentation.model.SecurityConfig;
import com.ericsson.eo.evnfm.crypto.migration.security.SecurityConfigReader;

@Configuration
public class AppConfiguration {

    @Bean
    public SecurityConfig securityConfig() {
        InputStream constantsAsStream = AppConfiguration.class.getResourceAsStream("/constants");
        SecurityConfigReader reader = new SecurityConfigReader();
        return reader.readFromInputStream(constantsAsStream);
    }
}
