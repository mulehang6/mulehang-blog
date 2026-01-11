package com.mulehang.blog.dto;

import lombok.Data;

/**
 * 评论创建 DTO
 */
@Data
public class CommentCreateDTO {
    private Long articleId;// 文章ID

    private Long parentId;// 父评论ID

    private Long rootId;// 根评论ID

    private Long replyToUser;// 被回复的用户ID

    private String content;// 评论内容
}
