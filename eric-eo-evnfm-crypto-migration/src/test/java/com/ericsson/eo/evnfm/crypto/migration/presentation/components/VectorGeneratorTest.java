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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.SecureRandom;
import java.util.concurrent.BlockingQueue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ericsson.eo.evnfm.crypto.migration.presentation.model.SecurityConfig;

@ExtendWith(MockitoExtension.class)
public class VectorGeneratorTest {

    private static final int IV_LENGTH = 128;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Mock
    private BlockingQueue<SecureRandom> randomPool;

    @Mock
    private SecureRandom emergencyRandom;

    @Mock
    private SecurityConfig securityConfig;

    private VectorGenerator vectorGenerator;

    @BeforeEach
    public void initService() {
        vectorGenerator = new VectorGenerator(randomPool, emergencyRandom, securityConfig);
    }

    @Test
    public void shouldGenerateInitialVector() throws InterruptedException {

        Mockito.when(securityConfig.getIvLength()).thenReturn(IV_LENGTH);
        Mockito.when(randomPool.take()).thenReturn(SECURE_RANDOM);

        byte[] bytes = vectorGenerator.generateInitialVector();

        Mockito.verify(randomPool, Mockito.times(Math.max(4, Runtime.getRuntime().availableProcessors())))
                .add(Mockito.any(SecureRandom.class));
        Mockito.verify(randomPool, Mockito.times(1)).take();
        Mockito.verify(randomPool, Mockito.times(1)).offer(SECURE_RANDOM);

        assertEquals(IV_LENGTH, bytes.length);
    }

    @Test
    public void shouldGenerateInitialVectorWhenInterruptedExceptionOccurred() throws InterruptedException {
        Mockito.when(securityConfig.getIvLength()).thenReturn(IV_LENGTH);
        Mockito.when(randomPool.take()).thenThrow(new InterruptedException());

        byte[] bytes = vectorGenerator.generateInitialVector();

        Mockito.verify(randomPool, Mockito.times(Math.max(4, Runtime.getRuntime().availableProcessors())))
                .add(Mockito.any(SecureRandom.class));
        Mockito.verify(emergencyRandom, Mockito.times(1)).nextBytes(new byte[IV_LENGTH]);

        assertEquals(IV_LENGTH, bytes.length);
    }
}
