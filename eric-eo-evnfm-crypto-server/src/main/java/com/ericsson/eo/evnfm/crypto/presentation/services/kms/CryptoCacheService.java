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

import com.ericsson.eo.evnfm.crypto.presentation.model.CipherKey;
import com.ericsson.eo.evnfm.crypto.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class CryptoCacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CryptoCacheService.class);

    private final CacheManager cacheManager;

    public CryptoCacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public Optional<CipherKey> fetchKeyById(UUID id) {
        return getKeysCache().map(cache -> cache.get(id, CipherKey.class));
    }

    public Optional<CipherKey> fetchLatestKey() {
        return getLatestKeyCache().map(cache -> cache.get(Constants.KMS_CACHE_LATEST_KEY, CipherKey.class));
    }

    public void updateKey(CipherKey cipherKey) {
        getKeysCache().ifPresent(cache -> cache.put(cipherKey.getAlias(), cipherKey));
    }

    public void updateLatestKey(CipherKey cipherKey) {
        getLatestKeyCache().ifPresent(cache -> cache.put(Constants.KMS_CACHE_LATEST_KEY, cipherKey));
    }

    public void removeKeys() {
        getKeysCache().ifPresent(Cache::clear);
    }

    public void removeLatestKey() {
        getLatestKeyCache().ifPresent(cache -> cache.evictIfPresent(Constants.KMS_CACHE_LATEST_KEY));
    }

    private Optional<Cache> getKeysCache() {
        return getCache(Constants.KMS_CACHE_KEYS);
    }

    private Optional<Cache> getLatestKeyCache() {
        return getCache(Constants.KMS_CACHE_LATEST_KEY);
    }

    private Optional<Cache> getCache(String cacheName) {
        var cacheOpt = Optional.ofNullable(cacheManager.getCache(cacheName));
        if (cacheOpt.isEmpty()) {
            LOGGER.warn("Cache '{}' was not found.", cacheName);
        }
        return cacheOpt;
    }

}
