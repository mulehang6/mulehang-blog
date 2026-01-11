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

    public ArticleViewController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping("/{slug}")
    public String detail(@PathVariable String slug, Model model) {
        ArticleDetailVO detail = articleService.getArticleBySlug(slug);
        model.addAttribute("article", detail);
        return "article";
    }
}
