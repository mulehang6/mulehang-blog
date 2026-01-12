package com.mulehang.blog.service.impl;

import com.mulehang.blog.redis.RedisKeys;
import com.mulehang.blog.service.DelayedEmailService;
import com.mulehang.blog.task.EmailTask;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 延迟邮件 Service。
 *
 * <p>基于 Redisson 延迟队列实现邮件任务的延迟投递。</p>
 */
@Service
@RequiredArgsConstructor
public class DelayedEmailServiceImpl implements DelayedEmailService {

    private final RedissonClient redissonClient;

    /**
     * 投递一个延迟执行的邮件任务。
     *
     * <p>将邮件任务放入 Redisson 延迟队列，到期后由消费者自动处理。</p>
     *
     * @param task 邮件任务
     * @param delay 延迟时间
     * @throws IllegalArgumentException 当 task 为空时抛出
     */
    @Override
    public void enqueue(EmailTask task, Duration delay) {
        if (task == null) {
            throw new IllegalArgumentException("参数 task 不能为空");
        }

        RBlockingQueue<EmailTask> blockingQueue = redissonClient.getBlockingQueue(RedisKeys.DELAYED_EMAIL_QUEUE);
        blockingQueue.offerAsync(task);
    }
}
