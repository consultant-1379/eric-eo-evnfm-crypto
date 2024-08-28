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
package com.ericsson.eo.evnfm.crypto.presentation.services.kms;

import com.ericsson.eo.evnfm.crypto.TestConstants;
import com.ericsson.eo.evnfm.crypto.TestUtils;
import com.ericsson.eo.evnfm.crypto.presentation.model.CipherKey;
import com.ericsson.eo.evnfm.crypto.util.Constants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CryptoCacheServiceTest {

    @Mock
    private CacheManager cacheManager;
    @Mock
    private Cache cache;

    private CryptoCacheService cryptoCacheService;

    @BeforeEach
    void setUp() {
        cryptoCacheService = new CryptoCacheService(cacheManager);
    }

    @Test
    void testFetchKeyByIdShouldReturnResult() {
        // given
        var key = UUID.fromString(TestConstants.SECRET_KEY_ALIAS);
        when(cacheManager.getCache(Constants.KMS_CACHE_KEYS)).thenReturn(cache);
        when(cache.get(key, CipherKey.class)).thenReturn(TestUtils.generateCipherKey());
        // when
        final Optional<CipherKey> cipherKey = cryptoCacheService.fetchKeyById(key);
        // then
        assertTrue(cipherKey.isPresent());
    }

    @Test
    void testFetchKeyByIdShouldNotReturnResultIfCacheNotFound() {
        // given
        var key = UUID.fromString(TestConstants.SECRET_KEY_ALIAS);
        // when
        final Optional<CipherKey> cipherKey = cryptoCacheService.fetchKeyById(key);
        // then
        assertTrue(cipherKey.isEmpty());
    }

    @Test
    void testFetchKeyByIdShouldNotReturnResultIfCacheValueNotFound() {
        // given
        var key = UUID.fromString(TestConstants.SECRET_KEY_ALIAS);
        when(cacheManager.getCache(Constants.KMS_CACHE_KEYS)).thenReturn(cache);
        // when
        final Optional<CipherKey> cipherKey = cryptoCacheService.fetchKeyById(key);
        // then
        assertTrue(cipherKey.isEmpty());
    }

    @Test
    void testFetchLatestKeyShouldReturnResult() {
        // given
        when(cacheManager.getCache(Constants.KMS_CACHE_LATEST_KEY)).thenReturn(cache);
        when(cache.get(Constants.KMS_CACHE_LATEST_KEY, CipherKey.class)).thenReturn(TestUtils.generateCipherKey());
        // when
        final Optional<CipherKey> cipherKey = cryptoCacheService.fetchLatestKey();
        // then
        assertTrue(cipherKey.isPresent());
    }

    @Test
    void testFetchLatestKeyShouldNotReturnResultIfCacheNotFound() {
        // when
        final Optional<CipherKey> cipherKey = cryptoCacheService.fetchLatestKey();
        // then
        assertTrue(cipherKey.isEmpty());
    }

    @Test
    void testFetchLatestKeyShouldNotReturnResultIfCacheValueNotFound() {
        // given
        when(cacheManager.getCache(Constants.KMS_CACHE_LATEST_KEY)).thenReturn(cache);
        // when
        final Optional<CipherKey> cipherKey = cryptoCacheService.fetchLatestKey();
        // then
        assertTrue(cipherKey.isEmpty());
    }

    @Test
    void testUpdateKeyShouldSaveResultInCache() {
        // given
        var cipherKey = TestUtils.generateCipherKey();
        when(cacheManager.getCache(Constants.KMS_CACHE_KEYS)).thenReturn(cache);
        // when
        cryptoCacheService.updateKey(cipherKey);
        // then
        verify(cache).put(cipherKey.getAlias(), cipherKey);
    }

    @Test
    void testUpdateKeyShouldNotSaveResultIfCacheNotFound() {
        // when
        cryptoCacheService.updateKey(TestUtils.generateCipherKey());
        // then
        verifyNoInteractions(cache);
    }

    @Test
    void testUpdateLatestKeyShouldSaveResultInCache() {
        // given
        var cipherKey = TestUtils.generateCipherKey();
        when(cacheManager.getCache(Constants.KMS_CACHE_LATEST_KEY)).thenReturn(cache);
        // when
        cryptoCacheService.updateLatestKey(cipherKey);
        // then
        verify(cache).put(Constants.KMS_CACHE_LATEST_KEY, cipherKey);
    }

    @Test
    void testUpdateLatestKeyShouldNotSaveResultIfCacheNotFound() {
        // when
        cryptoCacheService.updateLatestKey(TestUtils.generateCipherKey());
        // then
        verifyNoInteractions(cache);
    }

    @Test
    void removeKeys() {
    }

    @Test
    void testRemoveKeysShouldRemoveKeysFromCache() {
        // given
        when(cacheManager.getCache(Constants.KMS_CACHE_KEYS)).thenReturn(cache);
        // when
        cryptoCacheService.removeKeys();
        // then
        verify(cache).clear();
    }

    @Test
    void testRemoveKeysShouldNotRemoveKeysFromCachetIfCacheNotFound() {
        // when
        cryptoCacheService.removeKeys();
        // then
        verifyNoInteractions(cache);
    }

    @Test
    void testRemoveLatestKeyShouldRemoveLatestKeyFromCache() {
        // given
        when(cacheManager.getCache(Constants.KMS_CACHE_LATEST_KEY)).thenReturn(cache);
        // when
        cryptoCacheService.removeLatestKey();
        // then
        verify(cache).evictIfPresent(Constants.KMS_CACHE_LATEST_KEY);
    }

    @Test
    void testRemoveLatestKeyShouldNotRemoveLatestKeyFromCacheIfCacheNotFound() {
        // when
        cryptoCacheService.removeLatestKey();
        // then
        verifyNoInteractions(cache);
    }

}