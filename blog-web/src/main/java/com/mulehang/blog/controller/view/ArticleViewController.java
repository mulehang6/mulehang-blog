package com.mulehang.blog.controller.view;

import com.mulehang.blog.service.ArticleService;
import com.mulehang.blog.vo.ArticleDetailVO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Thymeleaf 页面：文章展示页（通过 slug）。
 */
@Controller
@RequestMapping("/articles")
public class ArticleViewController {

    private final ArticleService articleService;

    /**
     * 构造函数（构造器注入）。
     *
     * <p>通过构造器注入依赖，避免字段注入带来的可测试性与可维护性问题。</p>
     */
    public ArticleViewController(ArticleService articleService) {
        this.articleService = articleService;
    }

    /**
     * 文章详情页。
     *
     * @param slug 文章 slug
     * @param model 模型
     * @return 视图名称
     */
    @GetMapping("/{slug}")
    public String detail(@PathVariable String slug, Model model) {
        ArticleDetailVO detail = articleService.getArticleBySlug(slug);
        model.addAttribute("article", detail);
        return "article";
    }
}
