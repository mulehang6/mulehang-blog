package com.mulehang.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mulehang.blog.entity.BlogArticle;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
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
     * 点赞数 -1。
     */
    @Update("UPDATE blog_article SET like_count = like_count - 1 WHERE id = #{articleId} AND like_count > 0")
    int decrementLikeCount(@Param("articleId") Long articleId);

    /**
     * 评论数 +1。
     */
    @Update("UPDATE blog_article SET comment_count = comment_count + 1 WHERE id = #{articleId}")
    int incrementCommentCount(@Param("articleId") Long articleId);

    /**
     * 阅读量 +1。
     */
    @Update("UPDATE blog_article SET read_count = read_count + 1 WHERE id = #{articleId}")
    int incrementReadCount(@Param("articleId") Long articleId);

    /**
     * 统计指定分类下的文章数量。
     *
     * @param categoryId 分类ID
     * @return 文章数量
     */
    @Select("SELECT COUNT(*) FROM blog_article WHERE category_id = #{categoryId} AND status = 1")
    int countByCategoryId(@Param("categoryId") Long categoryId);
}
