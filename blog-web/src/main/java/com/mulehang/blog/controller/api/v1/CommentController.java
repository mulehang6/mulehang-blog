package com.mulehang.blog.controller.api.v1;

import com.mulehang.blog.dto.CommentCreateDTO;
import com.mulehang.blog.model.PageResult;
import com.mulehang.blog.model.Result;
import com.mulehang.blog.service.CommentService;
import com.mulehang.blog.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评论 REST API。
 *
 * @author mulehang
 * @since 2026-01-17
 */
@RestController
@Tag(name = "评论管理", description = "评论相关接口")
public class CommentController {

    private final CommentService commentService;

    /**
     * 构造函数。
     *
     * @param commentService 评论服务
     */
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 发表评论/回复。
     *
     * @param dto     评论创建 DTO
     * @param request HttpServletRequest
     * @return 评论 ID
     */
    @PostMapping("/api/v1/comments")
    @Operation(summary = "发表评论/回复")
    public Result<Long> create(@RequestBody CommentCreateDTO dto, HttpServletRequest request) {
        String ip = request == null ? null : request.getRemoteAddr();
        String userAgent = request == null ? null : request.getHeader("User-Agent");
        return Result.ok(commentService.create(dto, ip, userAgent));
    }

    /**
     * 按文章分页查询评论列表。
     *
     * @param articleId 文章 ID
     * @param pageNo    页码（从 1 开始）
     * @param pageSize  每页大小
     * @return 分页结果
     */
    @GetMapping("/api/v1/articles/{articleId}/comments")
    @Operation(summary = "按文章分页查询评论列表")
    public Result<PageResult<CommentVO>> listByArticle(@PathVariable Long articleId,
                                                       Long pageNo,
                                                       Long pageSize) {
        return Result.ok(commentService.listByArticle(articleId, pageNo, pageSize));
    }
}

