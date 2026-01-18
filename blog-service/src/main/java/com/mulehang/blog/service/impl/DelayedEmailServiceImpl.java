package com.mulehang.blog.service.impl;

import com.mulehang.blog.redis.RedisKeys;
import com.mulehang.blog.service.DelayedEmailService;
import com.mulehang.blog.task.EmailTask;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 延迟邮件 Service。
 *
 * <p>基于 Redisson 延迟队列实现邮件任务的延迟投递。</p>
 *
 * <p><b>关于 RDelayedQueue 弃用说明</b>：</p>
 * <p>Redisson 3.50+ 将 {@code RDelayedQueue} 标记为弃用，推荐使用 {@code RReliableQueue}。
 * 但本项目是学习项目，{@code RDelayedQueue} 功能完全满足需求且更易理解，
 * 因此暂时保留使用并抑制弃用警告。后续如有需要可迁移至 {@code RReliableQueue}。</p>
 */
@Service
@RequiredArgsConstructor
@SuppressWarnings("deprecation")
public class DelayedEmailServiceImpl implements DelayedEmailService {

    private final RedissonClient redissonClient;

    /**
     * 投递一个延迟执行的邮件任务。
     *
     * <p>将邮件任务放入 Redisson 延迟队列，到期后任务自动转移到目标队列，由消费者处理。</p>
     * <p>实现原理：RDelayedQueue 在 Redis 中使用 ZSet 存储延迟任务，score 为到期时间戳。
     * Redisson 内部定时器扫描到期任务并转移到绑定的 RBlockingQueue。</p>
     *
     * @param task  邮件任务
     * @param delay 延迟时间，不能为空或负数
     * @throws IllegalArgumentException 当 task 为空或 delay 为空/负数时抛出
     */
    @Override
    public void enqueue(EmailTask task, Duration delay) {
        if (task == null) {
            throw new IllegalArgumentException("参数 task 不能为空");
        }
        if (delay == null || delay.isNegative()) {
            throw new IllegalArgumentException("参数 delay 不能为空或负数");
        }

        // 获取目标阻塞队列（消费者从此队列 take）
        RBlockingQueue<EmailTask> destinationQueue = redissonClient.getBlockingQueue(RedisKeys.DELAYED_EMAIL_QUEUE);
        // 获取绑定到目标队列的延迟队列
        RDelayedQueue<EmailTask> delayedQueue = redissonClient.getDelayedQueue(destinationQueue);
        // 投递任务，指定延迟时间
        delayedQueue.offer(task, delay.toMillis(), TimeUnit.MILLISECONDS);
    }
}
