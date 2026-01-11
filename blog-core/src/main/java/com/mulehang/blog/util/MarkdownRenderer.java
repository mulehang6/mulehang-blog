package com.mulehang.blog.util;

import com.mulehang.blog.markdown.MarkdownService;
import org.springframework.stereotype.Component;

/**
 * Markdown 渲染器（Markdown -> HTML）。
 */
@Component
public class MarkdownRenderer {

    private final MarkdownService markdownService;

    public MarkdownRenderer(MarkdownService markdownService) {
        this.markdownService = markdownService;
    }

    public String renderToHtml(String markdown) {
        return markdownService.renderToHtml(markdown);
    }
}
