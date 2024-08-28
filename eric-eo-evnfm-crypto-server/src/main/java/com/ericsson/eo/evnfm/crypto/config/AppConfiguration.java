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
package com.ericsson.eo.evnfm.crypto.config;

import com.ericsson.eo.evnfm.crypto.presentation.model.SecurityConfig;
import com.ericsson.eo.evnfm.crypto.security.SecurityConfigReader;
import com.ericsson.eo.evnfm.crypto.util.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;

@Configuration
public class AppConfiguration {

    @Bean
    public SecurityConfig securityConfig() {
        InputStream constantsAsStream = AppConfiguration.class.getResourceAsStream("/constants");
        SecurityConfigReader reader = new SecurityConfigReader();
        return reader.readFromInputStream(constantsAsStream);
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(Constants.KMS_CACHE_LATEST_KEY, Constants.KMS_CACHE_KEYS);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }

}
