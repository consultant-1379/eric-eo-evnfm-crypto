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
package com.ericsson.eo.evnfm.crypto.migration.presentation.components;

import java.security.SecureRandom;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ericsson.eo.evnfm.crypto.migration.presentation.model.SecurityConfig;

@Component
public class VectorGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(VectorGenerator.class);

    private final BlockingQueue<SecureRandom> randomPool;
    private final SecureRandom emergencyRandom;

    private final SecurityConfig securityConfig;

    @Autowired
    public VectorGenerator(SecurityConfig securityConfig) {
        this(new LinkedBlockingQueue<>(), new SecureRandom(), securityConfig);
    }

    public VectorGenerator(BlockingQueue<SecureRandom> randomPool,
                           SecureRandom emergencyRandom,
                           SecurityConfig securityConfig) {
        this.randomPool = randomPool;
        this.emergencyRandom = emergencyRandom;
        this.securityConfig = securityConfig;

        initRandomPool();
    }

    public byte[] generateInitialVector() {
        byte[] iv = new byte[securityConfig.getIvLength()];

        try {
            var random = randomPool.take();
            random.nextBytes(iv);
            randomPool.offer(random); // NOSONAR
        } catch (InterruptedException e) {
            LOGGER.warn("Current thread interrupted, operation likely to be aborted.");
            Thread.currentThread().interrupt();
            emergencyRandom.nextBytes(iv);
        }

        return iv;
    }

    private void initRandomPool() {
        int cores = Math.max(4, Runtime.getRuntime().availableProcessors());

        for (int i = 0; i < cores; i++) {
            randomPool.add(new SecureRandom());
        }
    }
}
