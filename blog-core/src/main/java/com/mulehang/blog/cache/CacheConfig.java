package com.mulehang.blog.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 缓存配置
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * 配置 Caffeine 缓存管理器
     * @return Caffeine 缓存管理器
     */
    @Bean
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager(
                CacheNames.ARTICLE_DETAIL,
                CacheNames.CATEGORY_LIST,
                CacheNames.TAG_LIST,
                CacheNames.HOT_ARTICLES
        );

        manager.setCaffeine(Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1_000)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats());

        return manager;
    }

    /**
     * 多级缓存中的 L1 本地缓存（Caffeine）。
     */
    @Bean
    public Cache<String, Object> localCache() {
        return Caffeine.newBuilder()
                .initialCapacity(100)
                .maximumSize(1_000)
                .expireAfterWrite(Duration.ofMinutes(10))
                .recordStats()
                .build();
    }
}
