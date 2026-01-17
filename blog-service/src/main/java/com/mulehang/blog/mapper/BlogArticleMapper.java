package com.mulehang.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mulehang.blog.entity.BlogArticle;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章 Mapper 接口
 */
@Mapper
public interface BlogArticleMapper extends BaseMapper<BlogArticle> {

    /**
     * 点赞数 +1。
     */
    @Update("UPDATE blog_article SET like_count = like_count + 1 WHERE id = #{articleId}")
    int incrementLikeCount(@Param("articleId") Long articleId);

    /**
     * 评论数 +1。
     */
    @Update("UPDATE blog_article SET comment_count = comment_count + 1 WHERE id = #{articleId}")
    int incrementCommentCount(@Param("articleId") Long articleId);
}
