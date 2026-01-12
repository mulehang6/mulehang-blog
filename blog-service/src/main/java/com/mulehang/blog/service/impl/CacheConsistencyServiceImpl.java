package com.mulehang.blog.service.impl;

import com.mulehang.blog.cache.MultiLevelCache;
import com.mulehang.blog.redis.RedisKeys;
import com.mulehang.blog.service.CacheConsistencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 缓存一致性 Service（Cache-Aside + 延迟双删实现）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CacheConsistencyServiceImpl implements CacheConsistencyService {

    /**
     * 延迟双删的简单调度器。
     * <p>
     * 对于学习项目来说足够了，后续可替换为 MQ/Redisson 延迟队列。
     */
    private static final ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r);
        t.setName("cache-double-delete");
        t.setDaemon(true);
        return t;
    });

    private static final long SECOND_DELETE_DELAY_MS = 500;

    private final MultiLevelCache multiLevelCache;

    /**
     * 清除文章详情缓存。
     * 使用延迟双删策略：立即删除一次，事务提交后延迟500ms再删除一次，避免并发读写导致的缓存不一致。
     *
     * @param articleId 文章ID
     */
    @Override
    public void evictArticleDetail(Long articleId) {
        if (articleId == null) {
            return;
        }
        String key = RedisKeys.ARTICLE_DETAIL_PREFIX + articleId;

        // 第一次删除（数据库变更前）
        multiLevelCache.evict(key);

        Runnable secondDelete = () -> {
            try {
                multiLevelCache.evict(key);
            } catch (Exception e) {
                log.warn("第二次缓存删除失败, key={}", key, e);
            }
        };

        // 确保第二次删除在事务提交后执行，否则数据库可能回滚
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    SCHEDULER.schedule(secondDelete, SECOND_DELETE_DELAY_MS, TimeUnit.MILLISECONDS);
                }
            });
        } else {
            SCHEDULER.schedule(secondDelete, SECOND_DELETE_DELAY_MS, TimeUnit.MILLISECONDS);
        }
    }
}
