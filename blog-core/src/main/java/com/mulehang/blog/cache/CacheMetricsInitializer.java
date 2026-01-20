package com.mulehang.blog.cache;

import com.github.benmanes.caffeine.cache.Cache;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 缓存指标初始化器（将 Caffeine 缓存统计暴露给 Micrometer）。
 */
@Component
public class CacheMetricsInitializer implements SmartInitializingSingleton {

    private static final String MULTI_LEVEL_LOCAL_CACHE = "multilevel_local";

    private final CacheManager cacheManager;
    private final Cache<String, Object> localCache;
    private final MeterRegistry meterRegistry;

    /**
     * 构造缓存指标初始化器。
     * @param cacheManager Spring Cache 管理器
     * @param localCache 多级缓存的 L1 本地缓存
     * @param meterRegistryProvider MeterRegistry 提供者（可为空）
     */
    public CacheMetricsInitializer(CacheManager cacheManager,
                                   Cache<String, Object> localCache,
                                   ObjectProvider<MeterRegistry> meterRegistryProvider) {
        this.cacheManager = Objects.requireNonNull(cacheManager, "cacheManager 不能为空");
        this.localCache = Objects.requireNonNull(localCache, "localCache 不能为空");
        this.meterRegistry = meterRegistryProvider.getIfAvailable();
    }

    /**
     * 在所有单例初始化完成后注册缓存指标。
     */
    @Override
    public void afterSingletonsInstantiated() {
        if (meterRegistry == null) {
            return;
        }
        bindSpringCaches();
        bindLocalCache();
    }

    /**
     * 绑定 Spring Cache 管理器中的 Caffeine 缓存指标。
     */
    private void bindSpringCaches() {
        for (String cacheName : cacheManager.getCacheNames()) {
            org.springframework.cache.Cache cache = cacheManager.getCache(cacheName);
            if (cache instanceof CaffeineCache caffeineCache) {
                CaffeineCacheMetrics.monitor(meterRegistry, caffeineCache.getNativeCache(), cacheName);
            }
        }
    }

    /**
     * 绑定多级缓存 L1 本地缓存指标。
     */
    private void bindLocalCache() {
        CaffeineCacheMetrics.monitor(
                meterRegistry,
                localCache,
                MULTI_LEVEL_LOCAL_CACHE,
                Tags.of("cache_manager", "caffeine", "name", MULTI_LEVEL_LOCAL_CACHE)
        );
    }
}
