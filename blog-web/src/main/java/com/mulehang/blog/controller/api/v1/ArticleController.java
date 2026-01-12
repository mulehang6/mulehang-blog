package com.mulehang.blog.controller.api.v1;

import com.mulehang.blog.dto.ArticleCreateDTO;
import com.mulehang.blog.dto.ArticleQueryDTO;
import com.mulehang.blog.dto.ArticleUpdateDTO;
import com.mulehang.blog.model.PageResult;
import com.mulehang.blog.model.Result;
import com.mulehang.blog.service.ArticleService;
import com.mulehang.blog.service.DelayedEmailService;
import com.mulehang.blog.service.LikeService;
import com.mulehang.blog.task.EmailTask;
import com.mulehang.blog.vo.ArticleDetailVO;
import com.mulehang.blog.vo.ArticleListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * 文章 REST API。
 */
@RestController
@RequestMapping("/api/v1/articles")
@Tag(name = "文章管理", description = "文章相关接口")
public class ArticleController {

    private final ArticleService articleService;
    private final LikeService likeService;
    private final DelayedEmailService delayedEmailService;

    /**
     * 构造函数（构造器注入）。
     *
     * <p>通过构造器注入依赖，避免字段注入带来的可测试性与可维护性问题。</p>
     */
    public ArticleController(ArticleService articleService,
                             LikeService likeService,
                             DelayedEmailService delayedEmailService) {
        this.articleService = articleService;
        this.likeService = likeService;
        this.delayedEmailService = delayedEmailService;
    }

    /**
     * 创建文章。
     *
     * @param dto 文章创建 DTO
     * @return 文章 ID
     */
    @PostMapping
    @Operation(summary = "创建文章")
    public Result<Long> create(@Valid @RequestBody ArticleCreateDTO dto) {
        return Result.ok(articleService.createArticle(dto));
    }

    /**
     * 更新文章。
     *
     * @param id 文章 ID
     * @param dto 文章更新 DTO
     * @return 操作结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新文章")
    public Result<Void> update(@PathVariable Long id, @RequestBody ArticleUpdateDTO dto) {
        articleService.updateArticle(id, dto);
        return Result.ok();
    }

    /**
     * 发布文章。
     *
     * @param id 文章 ID
     * @return 操作结果
     */
    @PostMapping("/{id}/publish")
    @Operation(summary = "发布文章")
    public Result<Void> publish(@PathVariable Long id) {
        articleService.publishArticle(id);
        return Result.ok();
    }

    /**
     * 获取文章详情。
     *
     * @param id 文章 ID
     * @return 文章详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取文章详情")
    public Result<ArticleDetailVO> getById(@PathVariable Long id) {
        return Result.ok(articleService.getArticleDetail(id));
    }

    /**
     * 通过 slug 获取文章（前台展示）。
     *
     * @param slug 文章 slug
     * @return 文章详情
     */
    @GetMapping("/slug/{slug}")
    @Operation(summary = "通过 slug 获取文章（前台展示）")
    public Result<ArticleDetailVO> getBySlug(@PathVariable String slug) {
        return Result.ok(articleService.getArticleBySlug(slug));
    }

    /**
     * 分页查询文章。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping
    @Operation(summary = "分页查询文章")
    public Result<PageResult<ArticleListVO>> list(ArticleQueryDTO query) {
        return Result.ok(articleService.listArticles(query));
    }

    /**
     * 热门文章 TopN。
     *
     * @param topN 返回数量
     * @return 热门文章列表
     */
    @GetMapping("/hot")
    @Operation(summary = "热门文章 TopN")
    public Result<java.util.List<ArticleListVO>> hot(@RequestParam(defaultValue = "10") int topN) {
        return Result.ok(articleService.listHotArticles(topN));
    }

    /**
     * 点赞文章。
     *
     * @param id 文章 ID
     * @param userId 用户 ID
     * @return 点赞结果（true=成功，false=已点赞或未获取到锁）
     */
    @PostMapping("/{id}/like")
    @Operation(summary = "点赞文章（简单演示：通过 userId 参数传入）")
    public Result<Boolean> like(@PathVariable Long id, @RequestParam Long userId) {
        return Result.ok(likeService.likeArticle(userId, id));
    }

    /**
     * 延迟邮件测试（仅开发/演示）。
     *
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     * @param delaySeconds 延迟秒数
     * @return 操作结果
     */
    @PostMapping("/email/test")
    @Operation(summary = "2.6 延迟邮件测试：投递一条延迟任务（仅开发/演示）")
    public Result<Void> enqueueEmail(@RequestParam String to,
                                     @RequestParam(defaultValue = "Test") String subject,
                                     @RequestParam(defaultValue = "Hello") String content,
                                     @RequestParam(defaultValue = "5") long delaySeconds) {
        delayedEmailService.enqueue(new EmailTask(null, to, subject, content), java.time.Duration.ofSeconds(delaySeconds));
        return Result.ok();
    }

    /**
     * 删除文章。
     *
     * @param id 文章 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除文章")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.ok();
    }
}
