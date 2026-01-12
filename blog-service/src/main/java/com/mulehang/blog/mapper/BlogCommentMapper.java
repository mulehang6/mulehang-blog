package com.mulehang.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mulehang.blog.entity.BlogComment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章评论 Mapper 接口
 */
@Mapper
public interface BlogCommentMapper extends BaseMapper<BlogComment> {
}
