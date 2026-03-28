package com.mulehang.blog.service.impl;

import com.mulehang.blog.entity.BlogTag;
import com.mulehang.blog.mapper.BlogArticleTagMapper;
import com.mulehang.blog.mapper.BlogTagMapper;
import com.mulehang.blog.mapper.dto.TagArticleCountDTO;
import com.mulehang.blog.vo.TagVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

/**
 * TagServiceImpl 单元测试。
 */
class TagServiceImplTest {

    private BlogTagMapper tagMapper;

    private BlogArticleTagMapper articleTagMapper;

    private TagServiceImpl tagService;

    /**
     * 初始化测试依赖。
     */
    @BeforeEach
    void setUp() {
        tagMapper = Mockito.mock(BlogTagMapper.class);
        articleTagMapper = Mockito.mock(BlogArticleTagMapper.class);
        tagService = new TagServiceImpl(tagMapper, articleTagMapper);
    }

    /**
     * 验证标签详情中的文章数只统计公开可见文章。
     */
    @Test
    void getById_shouldUseVisibleArticleCount() {
        BlogTag tag = new BlogTag();
        tag.setId(1L);
        tag.setName("项目上线");
        when(tagMapper.selectById(1L)).thenReturn(tag);
        when(articleTagMapper.countVisibleByTagId(1L)).thenReturn(0L);

        TagVO result = tagService.getById(1L);

        assertNotNull(result);
        assertEquals(0L, result.getArticleCount());
    }

    /**
     * 验证标签列表中的文章数只统计公开可见文章。
     */
    @Test
    void listAll_shouldUseVisibleArticleCountMap() {
        BlogTag first = new BlogTag();
        first.setId(1L);
        first.setName("项目上线");
        BlogTag second = new BlogTag();
        second.setId(2L);
        second.setName("测试");

        TagArticleCountDTO count = new TagArticleCountDTO();
        count.setTagId(1L);
        count.setArticleCount(2L);

        when(tagMapper.selectList(Mockito.any())).thenReturn(List.of(first, second));
        when(articleTagMapper.selectVisibleTagArticleCounts()).thenReturn(List.of(count));

        List<TagVO> result = tagService.listAll();

        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getArticleCount());
        assertEquals(0L, result.get(1).getArticleCount());
    }
}
