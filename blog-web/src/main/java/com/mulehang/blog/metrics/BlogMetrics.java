package com.mulehang.blog.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 博客自定义业务指标
 */
@Component
public class BlogMetrics {

    private final Counter articlePublishCounter;
    private final Counter commentCounter;
    private final AtomicLong activeUsers;

    /**
     * 初始化自定义指标
     *
     * @param registry Micrometer 指标注册表
     */
    public BlogMetrics(MeterRegistry registry) {
        // 文章发布计数器
        this.articlePublishCounter = Counter.builder("blog.article.publish.total")
                .description("Total number of articles published")
                .register(registry);

        // 评论计数器
        this.commentCounter = Counter.builder("blog.comment.total")
                .description("Total number of comments")
                .register(registry);

        // 活跃用户数（Gauge）
        this.activeUsers = new AtomicLong(0);
        Gauge.builder("blog.users.active", activeUsers, AtomicLong::get)
                .description("Number of active users")
                .register(registry);
    }

    /**
     * 增加文章发布计数
     */
    public void incrementArticlePublish() {
        articlePublishCounter.increment();
    }

    /**
     * 增加评论计数
     */
    public void incrementComment() {
        commentCounter.increment();
    }

    /**
     * 设置活跃用户数
     *
     * @param count 活跃用户数
     */
    public void setActiveUsers(long count) {
        activeUsers.set(count);
    }
}
