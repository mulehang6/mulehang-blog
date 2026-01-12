package com.mulehang.blog.service;

/**
 * 点赞服务（Redisson 分布式锁防重复点赞）。
 */
public interface LikeService {

    /**
     * 点赞文章。
     *
     * @return true=点赞成功；false=已点赞或未获取到锁
     */
    boolean likeArticle(Long userId, Long articleId);
}
