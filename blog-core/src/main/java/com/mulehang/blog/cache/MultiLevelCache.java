package com.mulehang.blog.cache;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * 多级缓存（本地缓存 + Redis 缓存）。
 */
@Component
@RequiredArgsConstructor
public class MultiLevelCache {

    private static final Duration DEFAULT_REDIS_TTL = Duration.ofMinutes(30);

    private final Cache<String, Object> localCache;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 根据键从多级缓存中获取值。
     * @param key 键
     * @param type 值的类型
     * @param loader 加载器
     * @param <T> 值的类型
     * @return 缓存中的值，或通过加载器获取的值
     */
    public <T> T get(String key, Class<T> type, Supplier<T> loader) {
        return get(key, type, DEFAULT_REDIS_TTL, loader);
    }

    /**
     * 根据键从多级缓存中获取值。
     * @param key 键
     * @param type 值的类型
     * @param redisTtl Redis 缓存的过期时间
     * @param loader 加载器
     * @param <T> 值的类型
     * @return 缓存中的值，或通过加载器获取的值
     */
    public <T> T get(String key, Class<T> type, Duration redisTtl, Supplier<T> loader) {
        Objects.requireNonNull(key, "键不能为空");
        Objects.requireNonNull(type, "值类型不能为空");
        Objects.requireNonNull(redisTtl, "Redis 过期时间不能为空");
        Objects.requireNonNull(loader, "加载器不能为空");

        Object localValue = localCache.getIfPresent(key);
        if (localValue != null) {
            if (type.isInstance(localValue)) {
                return type.cast(localValue);
            }
            localCache.invalidate(key);
        }

        Object redisValue = redisTemplate.opsForValue().get(key);
        if (redisValue != null) {
            if (type.isInstance(redisValue)) {
                localCache.put(key, redisValue);
                return type.cast(redisValue);
            }
            redisTemplate.delete(key);
        }

        T value = loader.get();
        if (value != null) {
            redisTemplate.opsForValue().set(key, value, redisTtl);
            localCache.put(key, value);
        }
        return value;
    }

    /**
     * 从缓存中移除指定的键。
     * @param key 键
     */
    public void evict(String key) {
        if (key == null) {
            return;
        }
        localCache.invalidate(key);
        redisTemplate.delete(key);
    }
}
