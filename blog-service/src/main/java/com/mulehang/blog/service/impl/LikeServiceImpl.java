package com.mulehang.blog.service.impl;

import com.mulehang.blog.mapper.BlogArticleMapper;
import com.mulehang.blog.redis.RedisKeys;
import com.mulehang.blog.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 点赞 Service。
 *
 * <p>使用 Redisson 分布式锁防止重复点赞，点赞记录存储在 Redis Set 中。</p>
 */
@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final RedissonClient redissonClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final BlogArticleMapper articleMapper;

    /**
     * 点赞文章。
     *
     * <p>使用 Redisson 分布式锁防止并发重复点赞，获取锁超时 3 秒，锁自动释放时间 10 秒。</p>
     *
     * @param userId 用户 ID
     * @param articleId 文章 ID
     * @return true=点赞成功；false=已点赞或未获取到锁
     * @throws IllegalArgumentException 当 userId 或 articleId 为空时抛出
     */
    @Override
    public boolean likeArticle(Long userId, Long articleId) {
        if (userId == null || articleId == null) {
            throw new IllegalArgumentException("参数 userId/articleId 不能为空");
        }

        String lockKey = RedisKeys.LOCK_LIKE_PREFIX + articleId + ":" + userId;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                try {
                    String likeKey = RedisKeys.ARTICLE_LIKE_SET_PREFIX + articleId;
                    Boolean hasLiked = redisTemplate.opsForSet().isMember(likeKey, userId.toString());
                    if (Boolean.TRUE.equals(hasLiked)) {
                        return false;
                    }

                    redisTemplate.opsForSet().add(likeKey, userId.toString());
                    articleMapper.incrementLikeCount(articleId);
                    return true;
                } finally {
                    if (lock.isHeldByCurrentThread()) {
                        lock.unlock();
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }
}
