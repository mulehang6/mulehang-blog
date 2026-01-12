package com.mulehang.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mulehang.blog.entity.BlogArticleTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章标签关联 Mapper 接口
 */
@Mapper
public interface BlogArticleTagMapper extends BaseMapper<BlogArticleTag> {
}
