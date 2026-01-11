package com.mulehang.blog.service;

import com.mulehang.blog.dto.ArticleCreateDTO;
import com.mulehang.blog.dto.ArticleQueryDTO;
import com.mulehang.blog.dto.ArticleUpdateDTO;
import com.mulehang.blog.model.PageResult;
import com.mulehang.blog.vo.ArticleDetailVO;
import com.mulehang.blog.vo.ArticleListVO;

/**
 * 文章 Service。
 */
public interface ArticleService {

    Long createArticle(ArticleCreateDTO dto);

    void updateArticle(Long id, ArticleUpdateDTO dto);

    void publishArticle(Long id);

    ArticleDetailVO getArticleDetail(Long id);

    ArticleDetailVO getArticleBySlug(String slug);

    PageResult<ArticleListVO> listArticles(ArticleQueryDTO query);

    void deleteArticle(Long id);
}
