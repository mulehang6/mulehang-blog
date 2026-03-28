package com.mulehang.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mulehang.blog.entity.BlogArticleTag;
import com.mulehang.blog.mapper.dto.TagArticleCountDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文章标签关联 Mapper 接口
 */
@Mapper
public interface BlogArticleTagMapper extends BaseMapper<BlogArticleTag> {

    /**
     * 根据文章 ID 查询所有关联的标签 ID
     *
     * @param articleId 文章 ID
     * @return 标签 ID 列表
     */
    @Select("SELECT tag_id FROM blog_article_tag WHERE article_id = #{articleId}")
    List<Long> selectAllTagIdsByArticleId(@Param("articleId") Long articleId);

    /**
     * 统计每个标签的文章数量。
     *
     * @return 标签文章数量列表
     */
    @Select("SELECT tag_id AS tagId, COUNT(*) AS articleCount FROM blog_article_tag GROUP BY tag_id")
    List<TagArticleCountDTO> selectTagArticleCounts();

    /**
     * 统计指定标签的文章数量。
     *
     * @param tagId 标签 ID
     * @return 文章数量
     */
    @Select("SELECT COUNT(*) FROM blog_article_tag WHERE tag_id = #{tagId}")
    Long countByTagId(@Param("tagId") Long tagId);

    /**
     * 统计每个标签的可见文章数量。
     *
     * <p>
     * 只统计未逻辑删除且已发布的文章，避免标签计数包含失效关联。
     * </p>
     *
     * @return 标签可见文章数量列表
     */
    @Select("""
            SELECT at.tag_id AS tagId, COUNT(*) AS articleCount
            FROM blog_article_tag at
            INNER JOIN blog_article a ON a.id = at.article_id
            WHERE at.is_deleted = 0
              AND a.is_deleted = 0
              AND a.status = 1
            GROUP BY at.tag_id
            """)
    List<TagArticleCountDTO> selectVisibleTagArticleCounts();

    /**
     * 统计指定标签下的可见文章数量。
     *
     * <p>
     * 只统计未逻辑删除且已发布的文章，保证与公开文章列表口径一致。
     * </p>
     *
     * @param tagId 标签 ID
     * @return 标签可见文章数量
     */
    @Select("""
            SELECT COUNT(*)
            FROM blog_article_tag at
            INNER JOIN blog_article a ON a.id = at.article_id
            WHERE at.tag_id = #{tagId}
              AND at.is_deleted = 0
              AND a.is_deleted = 0
              AND a.status = 1
            """)
    Long countVisibleByTagId(@Param("tagId") Long tagId);
}
