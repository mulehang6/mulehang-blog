package com.mulehang.blog.task;

import com.mulehang.blog.redis.RedisKeys;
import com.mulehang.blog.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Redisson 延迟队列消费者。
 *
 * <p>从 Redisson 延迟队列中消费邮件任务，并调用 EmailService 发送邮件。</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DelayedEmailConsumer {

    private final RedissonClient redissonClient;
    private final EmailService emailService;

    private ExecutorService executor;

    /**
     * 启动消费者线程。
     *
     * <p>在 Bean 初始化后自动启动一个守护线程，持续从延迟队列中消费任务。</p>
     */
    @PostConstruct
    public void start() {
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("delayed-email-consumer");
            t.setDaemon(true);
            return t;
        });

        executor.submit(() -> {
            RBlockingQueue<EmailTask> queue = redissonClient.getBlockingQueue(RedisKeys.DELAYED_EMAIL_QUEUE);
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    EmailTask task = queue.take();
                    emailService.sendText(task.getTo(), task.getSubject(), task.getContent());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    log.error("延迟邮件消费者异常", e);
                }
            }
        });
    }

    /**
     * 停止消费者线程。
     *
     * <p>在 Bean 销毁前自动关闭线程池，中断正在执行的任务。</p>
     */
    @PreDestroy
    public void stop() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
