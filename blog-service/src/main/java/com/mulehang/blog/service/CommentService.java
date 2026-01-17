package com.mulehang.blog.service;

import com.mulehang.blog.dto.CommentCreateDTO;
import com.mulehang.blog.model.PageResult;
import com.mulehang.blog.vo.CommentVO;

/**
 * 评论 Service。
 *
 * @author mulehang
 * @since 2026-01-17
 */
public interface CommentService {

    /**
     * 发表评论/回复。
     *
     * @param dto       评论创建 DTO
     * @param ipAddress IP 地址
     * @param userAgent User-Agent
     * @return 评论 ID
     */
    Long create(CommentCreateDTO dto, String ipAddress, String userAgent);

    /**
     * 按文章分页查询评论列表。
     *
     * @param articleId 文章 ID
     * @param pageNo    页码（从 1 开始）
     * @param pageSize  每页大小
     * @return 分页结果
     */
    PageResult<CommentVO> listByArticle(Long articleId, Long pageNo, Long pageSize);
}

