package com.mulehang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mulehang.blog.dto.CommentCreateDTO;
import com.mulehang.blog.entity.BlogArticle;
import com.mulehang.blog.entity.BlogComment;
import com.mulehang.blog.enums.CommentStatusEnum;
import com.mulehang.blog.mapper.BlogArticleMapper;
import com.mulehang.blog.mapper.BlogCommentMapper;
import com.mulehang.blog.mq.producer.CommentNotifyProducer;
import com.mulehang.blog.model.PageResult;
import com.mulehang.blog.service.CommentService;
import com.mulehang.blog.vo.CommentVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

/**
 * 评论 Service 实现。
 *
 * @author mulehang
 * @since 2026-01-17
 */
@Service
public class CommentServiceImpl implements CommentService {

    private final BlogCommentMapper commentMapper;
    private final BlogArticleMapper articleMapper;
    private final CommentNotifyProducer commentNotifyProducer;

    /**
     * 构造函数。
     *
     * @param commentMapper         评论 Mapper
     * @param articleMapper         文章 Mapper
     * @param commentNotifyProducer 评论通知 Producer
     */
    public CommentServiceImpl(BlogCommentMapper commentMapper,
            BlogArticleMapper articleMapper,
            CommentNotifyProducer commentNotifyProducer) {
        this.commentMapper = commentMapper;
        this.articleMapper = articleMapper;
        this.commentNotifyProducer = commentNotifyProducer;
    }

    /**
     * 发表评论/回复。
     *
     * @param dto       评论创建 DTO
     * @param ipAddress IP 地址
     * @param userAgent User-Agent
     * @return 评论 ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CommentCreateDTO dto, String ipAddress, String userAgent) {
        if (dto == null) {
            throw new IllegalArgumentException("dto 为空");
        }
        if (dto.getArticleId() == null) {
            throw new IllegalArgumentException("articleId 为空");
        }
        if (dto.getContent() == null || dto.getContent().isBlank()) {
            throw new IllegalArgumentException("content 为空");
        }

        BlogArticle article = articleMapper.selectById(dto.getArticleId());
        if (article == null) {
            throw new IllegalArgumentException("文章未找到: " + dto.getArticleId());
        }

        BlogComment comment = new BlogComment();
        comment.setArticleId(dto.getArticleId());
        comment.setParentId(normalizeId(dto.getParentId()));
        comment.setReplyToUser(dto.getReplyToUser());
        comment.setContent(dto.getContent());
        comment.setStatus(CommentStatusEnum.APPROVED.getCode());
        comment.setLikeCount(0);
        comment.setIsTop(0);
        comment.setIpAddress(ipAddress);
        comment.setUserAgent(userAgent);

        if (!Objects.equals(comment.getParentId(), 0L)) {
            BlogComment parent = commentMapper.selectById(comment.getParentId());
            if (parent == null || !Objects.equals(parent.getArticleId(), dto.getArticleId())) {
                throw new IllegalArgumentException("父评论不存在或不属于当前文章: parentId=" + comment.getParentId());
            }
            if (parent.getRootId() != null && parent.getRootId() != 0L) {
                comment.setRootId(parent.getRootId());
            } else {
                comment.setRootId(parent.getId());
            }
        } else {
            comment.setRootId(0L);
        }

        commentMapper.insert(comment);
        Long commentId = comment.getId();
        if (commentId == null) {
            throw new IllegalStateException("评论创建失败：未返回 id");
        }

        articleMapper.incrementCommentCount(dto.getArticleId());

        commentNotifyProducer.sendNotify(dto.getArticleId(), commentId);
        return commentId;
    }

    /**
     * 按文章分页查询评论列表。
     *
     * @param articleId 文章 ID
     * @param pageNo    页码（从 1 开始）
     * @param pageSize  每页大小
     * @return 分页结果
     */
    @Override
    public PageResult<CommentVO> listByArticle(Long articleId, Long pageNo, Long pageSize) {
        if (articleId == null) {
            throw new IllegalArgumentException("articleId 为空");
        }
        long pn = pageNo == null ? 1L : pageNo;
        long ps = pageSize == null ? 10L : pageSize;

        Page<BlogComment> page = new Page<>(pn, ps);
        Page<BlogComment> result = commentMapper.selectPage(page, new LambdaQueryWrapper<BlogComment>()
                .eq(BlogComment::getArticleId, articleId)
                .eq(BlogComment::getStatus, CommentStatusEnum.APPROVED.getCode())
                .orderByDesc(BlogComment::getIsTop)
                .orderByDesc(BlogComment::getCreateTime));

        List<CommentVO> list = result.getRecords().stream().map(this::toVO).toList();
        PageResult<CommentVO> pageResult = new PageResult<>();
        pageResult.setList(list);
        pageResult.setPageNo(pn);
        pageResult.setPageSize(ps);
        pageResult.setTotal(result.getTotal());
        return pageResult;
    }

    /**
     * 将评论实体转换为 VO。
     *
     * @param comment 评论实体
     * @return 评论 VO
     */
    private CommentVO toVO(BlogComment comment) {
        CommentVO vo = new CommentVO();
        vo.setId(comment.getId());
        vo.setArticleId(comment.getArticleId());
        vo.setRootId(comment.getRootId());
        vo.setParentId(comment.getParentId());
        vo.setUserId(comment.getUserId());
        vo.setReplyToUser(comment.getReplyToUser());
        vo.setContent(comment.getContent());
        vo.setLikeCount(comment.getLikeCount());
        vo.setStatus(comment.getStatus());
        vo.setLocation(comment.getLocation());
        vo.setIsTop(comment.getIsTop());
        vo.setCreateTime(comment.getCreateTime());
        return vo;
    }

    /**
     * 规范化 ID（null -> 0）。
     *
     * @param id 原始 ID
     * @return 规范化后的 ID
     */
    private Long normalizeId(Long id) {
        return id == null ? 0L : id;
    }
}
