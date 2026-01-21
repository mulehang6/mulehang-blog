package com.mulehang.blog.redis;

/**
 * Redis Key 统一定义。
 */
public final class RedisKeys {

    private RedisKeys() {
    }

    /**
     * 热门文章榜（ZSet）：member=articleId, score=hot score（可用 PV/阅读量累加）。
     */
    public static final String HOT_ARTICLES_ZSET = "blog:hot:articles";

    /**
     * 文章详情缓存 Key 前缀。
     */
    public static final String ARTICLE_DETAIL_PREFIX = "article:detail:";

    /**
     * 文章点赞用户集合 Key 前缀（Set）：member=userId。
     */
    public static final String ARTICLE_LIKE_SET_PREFIX = "blog:like:article:";

    /**
     * 分布式锁：点赞防重 Key 前缀。
     */
    public static final String LOCK_LIKE_PREFIX = "lock:like:";

    /**
     * Redisson RDelayedQueue：邮件通知（配合 RBlockingQueue 使用）。
     */
    public static final String DELAYED_EMAIL_QUEUE = "delayed:email:queue";

    /**
     * 今日网站 PV（页面浏览量）。
     */
    public static final String SITE_PV_TODAY = "blog:stats:pv:today";

    /**
     * 今日网站 UV（独立访客数，HyperLogLog）。
     */
    public static final String SITE_UV_TODAY = "blog:stats:uv:today";

    /**
     * 总网站 PV。
     */
    public static final String SITE_PV_TOTAL = "blog:stats:pv:total";

    /**
     * 总网站 UV（HyperLogLog）。
     */
    public static final String SITE_UV_TOTAL = "blog:stats:uv:total";
}
