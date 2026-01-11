package com.mulehang.blog.controller.api.v1;

import com.mulehang.blog.dto.ArticleCreateDTO;
import com.mulehang.blog.dto.ArticleQueryDTO;
import com.mulehang.blog.dto.ArticleUpdateDTO;
import com.mulehang.blog.model.PageResult;
import com.mulehang.blog.model.Result;
import com.mulehang.blog.service.ArticleService;
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

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping
    @Operation(summary = "创建文章")
    public Result<Long> create(@Valid @RequestBody ArticleCreateDTO dto) {
        return Result.ok(articleService.createArticle(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新文章")
    public Result<Void> update(@PathVariable Long id, @RequestBody ArticleUpdateDTO dto) {
        articleService.updateArticle(id, dto);
        return Result.ok();
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "发布文章")
    public Result<Void> publish(@PathVariable Long id) {
        articleService.publishArticle(id);
        return Result.ok();
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取文章详情")
    public Result<ArticleDetailVO> getById(@PathVariable Long id) {
        return Result.ok(articleService.getArticleDetail(id));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "通过 slug 获取文章（前台展示）")
    public Result<ArticleDetailVO> getBySlug(@PathVariable String slug) {
        return Result.ok(articleService.getArticleBySlug(slug));
    }

    @GetMapping
    @Operation(summary = "分页查询文章")
    public Result<PageResult<ArticleListVO>> list(ArticleQueryDTO query) {
        return Result.ok(articleService.listArticles(query));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除文章")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.ok();
    }
}
