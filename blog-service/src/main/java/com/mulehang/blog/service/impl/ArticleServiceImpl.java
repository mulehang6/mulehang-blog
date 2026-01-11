package com.mulehang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mulehang.blog.converter.ArticleConverter;
import com.mulehang.blog.dto.ArticleCreateDTO;
import com.mulehang.blog.dto.ArticleQueryDTO;
import com.mulehang.blog.dto.ArticleUpdateDTO;
import com.mulehang.blog.entity.*;
import com.mulehang.blog.mapper.*;
import com.mulehang.blog.model.PageResult;
import com.mulehang.blog.service.ArticleService;
import com.mulehang.blog.util.MarkdownRenderer;
import com.mulehang.blog.vo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl implements ArticleService {

    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_PUBLISHED = 1;
    private static final int SOURCE_ORIGINAL = 1;

    /**
     * TODO: Milestone 4 
     * 
     * 
     * 
     */
    private static final long DEFAULT_AUTHOR_ID = 1L;

    private final BlogArticleMapper articleMapper;
    private final BlogArticleBodyMapper bodyMapper;
    private final BlogArticleTagMapper articleTagMapper;
    private final BlogCategoryMapper categoryMapper;
    private final BlogColumnMapper columnMapper;
    private final BlogTagMapper tagMapper;
    private final SysUserMapper userMapper;
    private final ArticleConverter articleConverter;
    private final MarkdownRenderer markdownRenderer;

    public ArticleServiceImpl(BlogArticleMapper articleMapper,
                              BlogArticleBodyMapper bodyMapper,
                              BlogArticleTagMapper articleTagMapper,
                              BlogCategoryMapper categoryMapper,
                              BlogColumnMapper columnMapper,
                              BlogTagMapper tagMapper,
                              SysUserMapper userMapper,
                              ArticleConverter articleConverter,
                              MarkdownRenderer markdownRenderer) {
        this.articleMapper = articleMapper;
        this.bodyMapper = bodyMapper;
        this.articleTagMapper = articleTagMapper;
        this.categoryMapper = categoryMapper;
        this.columnMapper = columnMapper;
        this.tagMapper = tagMapper;
        this.userMapper = userMapper;
        this.articleConverter = articleConverter;
        this.markdownRenderer = markdownRenderer;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createArticle(ArticleCreateDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("dto is null");
        }
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new IllegalArgumentException("title is blank");
        }
        if (dto.getContentMd() == null) {
            throw new IllegalArgumentException("contentMd is null");
        }

        BlogArticle article = articleConverter.toArticleEntity(dto);
        article.setAuthorId(DEFAULT_AUTHOR_ID);
        article.setStatus(dto.getStatus() == null ? STATUS_DRAFT : dto.getStatus());
        article.setSourceType(dto.getSourceType() == null ? SOURCE_ORIGINAL : dto.getSourceType());
        article.setAllowComment(dto.getAllowComment() == null ? 1 : dto.getAllowComment());
        article.setIsPinned(dto.getIsPinned() == null ? 0 : dto.getIsPinned());

        if (article.getSlug() == null || article.getSlug().isBlank()) {
            article.setSlug(generateSlug(dto.getTitle()));
        }

        article.setWordCount(countWords(dto.getContentMd()));
        article.setReadCount(0L);
        article.setLikeCount(0);
        article.setCommentCount(0);
        if (Objects.equals(article.getStatus(), STATUS_PUBLISHED)) {
            article.setPublishTime(LocalDateTime.now());
        }

        articleMapper.insert(article);

        BlogArticleBody body = articleConverter.toArticleBodyEntity(dto);
        body.setArticleId(article.getId());
        body.setContentMd(dto.getContentMd());
        body.setContentHtml(markdownRenderer.renderToHtml(dto.getContentMd()));
        bodyMapper.insert(body);

        saveArticleTags(article.getId(), dto.getTagIds());
        return article.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticle(Long id, ArticleUpdateDTO dto) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        if (dto == null) {
            throw new IllegalArgumentException("dto is null");
        }

        BlogArticle existing = articleMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("article not found: " + id);
        }

        BlogArticle patch = new BlogArticle();
        patch.setId(id);

        if (dto.getTitle() != null) patch.setTitle(dto.getTitle());
        if (dto.getSlug() != null) patch.setSlug(dto.getSlug());
        if (dto.getSummary() != null) patch.setSummary(dto.getSummary());
        if (dto.getCoverUrl() != null) patch.setCoverUrl(dto.getCoverUrl());
        if (dto.getStatus() != null) patch.setStatus(dto.getStatus());
        if (dto.getSourceType() != null) patch.setSourceType(dto.getSourceType());
        if (dto.getAllowComment() != null) patch.setAllowComment(dto.getAllowComment());
        if (dto.getIsPinned() != null) patch.setIsPinned(dto.getIsPinned());
        if (dto.getCategoryId() != null) patch.setCategoryId(dto.getCategoryId());
        if (dto.getColumnId() != null) patch.setColumnId(dto.getColumnId());
        if (dto.getContentMd() != null) patch.setWordCount(countWords(dto.getContentMd()));

        articleMapper.updateById(patch);

        if (dto.getContentMd() != null) {
            BlogArticleBody body = bodyMapper.selectOne(new LambdaQueryWrapper<BlogArticleBody>()
                    .eq(BlogArticleBody::getArticleId, id));

            if (body == null) {
                body = new BlogArticleBody();
                body.setArticleId(id);
                body.setContentMd(dto.getContentMd());
                body.setContentHtml(markdownRenderer.renderToHtml(dto.getContentMd()));
                bodyMapper.insert(body);
            } else {
                BlogArticleBody bodyPatch = new BlogArticleBody();
                bodyPatch.setId(body.getId());
                bodyPatch.setContentMd(dto.getContentMd());
                bodyPatch.setContentHtml(markdownRenderer.renderToHtml(dto.getContentMd()));
                bodyMapper.updateById(bodyPatch);
            }
        }

        if (dto.getTagIds() != null) {
            articleTagMapper.delete(new QueryWrapper<BlogArticleTag>().eq("article_id", id));
            saveArticleTags(id, dto.getTagIds());
        }

        if (dto.getStatus() != null
                && Objects.equals(dto.getStatus(), STATUS_PUBLISHED)
                && existing.getPublishTime() == null) {
            BlogArticle publishPatch = new BlogArticle();
            publishPatch.setId(id);
            publishPatch.setPublishTime(LocalDateTime.now());
            articleMapper.updateById(publishPatch);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishArticle(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        BlogArticle existing = articleMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("article not found: " + id);
        }
        if (Objects.equals(existing.getStatus(), STATUS_PUBLISHED) && existing.getPublishTime() != null) {
            return;
        }
        BlogArticle patch = new BlogArticle();
        patch.setId(id);
        patch.setStatus(STATUS_PUBLISHED);
        patch.setPublishTime(LocalDateTime.now());
        articleMapper.updateById(patch);
    }

    @Override
    public ArticleDetailVO getArticleDetail(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        BlogArticle article = articleMapper.selectById(id);
        if (article == null) {
            throw new IllegalArgumentException("article not found: " + id);
        }
        return buildDetail(article);
    }

    @Override
    public ArticleDetailVO getArticleBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("slug is blank");
        }
        BlogArticle article = articleMapper.selectOne(new LambdaQueryWrapper<BlogArticle>()
                .eq(BlogArticle::getSlug, slug)
                .eq(BlogArticle::getStatus, STATUS_PUBLISHED));
        if (article == null) {
            throw new IllegalArgumentException("article not found by slug: " + slug);
        }
        return buildDetail(article);
    }

    @Override
    public PageResult<ArticleListVO> listArticles(ArticleQueryDTO query) {
        if (query == null) {
            query = new ArticleQueryDTO();
        }
        long pageNo = query.getPageNo() == null ? 1L : query.getPageNo();
        long pageSize = query.getPageSize() == null ? 10L : query.getPageSize();

        Page<BlogArticle> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<BlogArticle> qw = new LambdaQueryWrapper<>();

        if (query.getStatus() != null) qw.eq(BlogArticle::getStatus, query.getStatus());
        if (query.getCategoryId() != null) qw.eq(BlogArticle::getCategoryId, query.getCategoryId());
        if (query.getColumnId() != null) qw.eq(BlogArticle::getColumnId, query.getColumnId());
        if (query.getAuthorId() != null) qw.eq(BlogArticle::getAuthorId, query.getAuthorId());
        String keyword = query.getKeyword();
        if (keyword != null && !keyword.isBlank()) {
            final String kw = keyword;
            qw.and(w -> w.like(BlogArticle::getTitle, kw)
                    .or().like(BlogArticle::getSummary, kw));
        }

        if (query.getTagId() != null) {
            List<Long> articleIds = articleTagMapper.selectList(new LambdaQueryWrapper<BlogArticleTag>()
                            .eq(BlogArticleTag::getTagId, query.getTagId()))
                    .stream()
                    .map(BlogArticleTag::getArticleId)
                    .distinct()
                    .toList();
            if (articleIds.isEmpty()) {
                return PageResult.empty(pageNo, pageSize);
            }
            qw.in(BlogArticle::getId, articleIds);
        }

        applySort(qw, query.getSortBy(), query.getSortOrder());

        Page<BlogArticle> resultPage = articleMapper.selectPage(page, qw);
        List<BlogArticle> records = resultPage.getRecords();
        if (records == null || records.isEmpty()) {
            return PageResult.empty(pageNo, pageSize);
        }

        List<ArticleListVO> list = buildListVO(records);
        PageResult<ArticleListVO> pr = new PageResult<>();
        pr.setList(list);
        pr.setTotal(resultPage.getTotal());
        pr.setPageNo(pageNo);
        pr.setPageSize(pageSize);
        return pr;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id is null");
        }
        // 逻辑删除即可（MyBatis-Plus 逻辑删除：@TableLogic）。
        articleMapper.deleteById(id);
    }

    private void applySort(LambdaQueryWrapper<BlogArticle> qw, String sortBy, String sortOrder) {
        boolean asc = "asc".equalsIgnoreCase(sortOrder);
        if (sortBy == null || sortBy.isBlank()) {
            qw.orderByDesc(BlogArticle::getPublishTime).orderByDesc(BlogArticle::getCreateTime);
            return;
        }
        switch (sortBy) {
            case "publishTime" -> qw.orderBy(true, asc, BlogArticle::getPublishTime);
            case "createTime" -> qw.orderBy(true, asc, BlogArticle::getCreateTime);
            case "readCount" -> qw.orderBy(true, asc, BlogArticle::getReadCount);
            default -> qw.orderByDesc(BlogArticle::getPublishTime).orderByDesc(BlogArticle::getCreateTime);
        }
    }

    private ArticleDetailVO buildDetail(BlogArticle article) {
        BlogArticleBody body = bodyMapper.selectOne(new LambdaQueryWrapper<BlogArticleBody>()
                .eq(BlogArticleBody::getArticleId, article.getId()));

        ArticleDetailVO vo = new ArticleDetailVO();
        vo.setId(article.getId());
        vo.setTitle(article.getTitle());
        vo.setSlug(article.getSlug());
        vo.setSummary(article.getSummary());
        vo.setCoverUrl(article.getCoverUrl());
        vo.setStatus(article.getStatus());
        vo.setSourceType(article.getSourceType());
        vo.setAllowComment(article.getAllowComment());
        vo.setIsPinned(article.getIsPinned());
        vo.setWordCount(article.getWordCount());
        vo.setReadCount(safeInt(article.getReadCount()));
        vo.setLikeCount(article.getLikeCount());
        vo.setCommentCount(article.getCommentCount());
        vo.setPublishTime(article.getPublishTime());
        vo.setCreateTime(article.getCreateTime());
        vo.setUpdateTime(article.getUpdateTime());

        if (body != null) {
            vo.setContentMd(body.getContentMd());
            vo.setContentHtml(body.getContentHtml());
        } else {
            vo.setContentMd("");
            vo.setContentHtml("");
        }

        vo.setAuthor(toUserVO(userMapper.selectById(article.getAuthorId())));
        vo.setCategory(toCategoryVO(article.getCategoryId() == null ? null : categoryMapper.selectById(article.getCategoryId())));
        vo.setColumn(toColumnVO(article.getColumnId() == null ? null : columnMapper.selectById(article.getColumnId())));
        vo.setTags(loadTagsByArticleId(article.getId()));
        return vo;
    }

    private List<ArticleListVO> buildListVO(List<BlogArticle> articles) {
        Set<Long> authorIds = articles.stream().map(BlogArticle::getAuthorId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> categoryIds = articles.stream().map(BlogArticle::getCategoryId).filter(Objects::nonNull).collect(Collectors.toSet());
        List<Long> articleIds = articles.stream().map(BlogArticle::getId).toList();

        Map<Long, SysUser> userMap = authorIds.isEmpty()
                ? Map.of()
                : userMapper.selectList(new LambdaQueryWrapper<SysUser>().in(SysUser::getId, authorIds))
                .stream()
                .collect(Collectors.toMap(SysUser::getId, Function.identity(), (a, b) -> a));

        Map<Long, BlogCategory> categoryMap = categoryIds.isEmpty()
                ? Map.of()
                : categoryMapper.selectList(new LambdaQueryWrapper<BlogCategory>().in(BlogCategory::getId, categoryIds))
                .stream()
                .collect(Collectors.toMap(BlogCategory::getId, Function.identity(), (a, b) -> a));

        Map<Long, List<Long>> articleTagIds = new HashMap<>();
        if (!articleIds.isEmpty()) {
            List<BlogArticleTag> rels = articleTagMapper.selectList(new LambdaQueryWrapper<BlogArticleTag>()
                    .in(BlogArticleTag::getArticleId, articleIds));
            for (BlogArticleTag rel : rels) {
                articleTagIds.computeIfAbsent(rel.getArticleId(), k -> new ArrayList<>()).add(rel.getTagId());
            }
        }

        Set<Long> allTagIds = articleTagIds.values().stream().flatMap(Collection::stream)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, BlogTag> tagMap = allTagIds.isEmpty()
                ? Map.of()
                : tagMapper.selectList(new LambdaQueryWrapper<BlogTag>().in(BlogTag::getId, allTagIds))
                .stream()
                .collect(Collectors.toMap(BlogTag::getId, Function.identity(), (a, b) -> a));

        List<ArticleListVO> list = new ArrayList<>(articles.size());
        for (BlogArticle a : articles) {
            ArticleListVO vo = new ArticleListVO();
            vo.setId(a.getId());
            vo.setTitle(a.getTitle());
            vo.setSlug(a.getSlug());
            vo.setSummary(a.getSummary());
            vo.setCoverUrl(a.getCoverUrl());
            vo.setStatus(a.getStatus());
            vo.setReadCount(a.getReadCount());
            vo.setLikeCount(a.getLikeCount());
            vo.setCommentCount(a.getCommentCount());
            vo.setPublishTime(a.getPublishTime());

            vo.setAuthor(toUserVO(userMap.get(a.getAuthorId())));
            vo.setCategory(toCategoryVO(a.getCategoryId() == null ? null : categoryMap.get(a.getCategoryId())));

            List<Long> tids = articleTagIds.getOrDefault(a.getId(), Collections.emptyList());
            vo.setTags(tids.stream()
                    .map(tagMap::get)
                    .filter(Objects::nonNull)
                    .map(this::toTagVO)
                    .toList());

            list.add(vo);
        }
        return list;
    }

    private List<TagVO> loadTagsByArticleId(Long articleId) {
        List<BlogArticleTag> rels = articleTagMapper.selectList(new LambdaQueryWrapper<BlogArticleTag>()
                .eq(BlogArticleTag::getArticleId, articleId));
        if (rels == null || rels.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> tagIds = rels.stream().map(BlogArticleTag::getTagId).filter(Objects::nonNull).distinct().toList();
        if (tagIds.isEmpty()) {
            return Collections.emptyList();
        }
        return tagMapper.selectList(new LambdaQueryWrapper<BlogTag>().in(BlogTag::getId, tagIds))
                .stream()
                .map(this::toTagVO)
                .toList();
    }

    private void saveArticleTags(Long articleId, List<Long> tagIds) {
        List<BlogArticleTag> rels = articleConverter.tagIdsToArticleTags(tagIds);
        if (rels == null || rels.isEmpty()) {
            return;
        }
        for (BlogArticleTag rel : rels) {
            rel.setArticleId(articleId);
            articleTagMapper.insert(rel);
        }
    }

    private int countWords(String contentMd) {
        if (contentMd == null || contentMd.isBlank()) {
            return 0;
        }
        int cnt = 0;
        for (int i = 0; i < contentMd.length(); i++) {
            if (!Character.isWhitespace(contentMd.charAt(i))) {
                cnt++;
            }
        }
        return cnt;
    }

    private String generateSlug(String title) {
        String base = title == null ? "" : title.trim().toLowerCase(Locale.ROOT);
        base = base.replaceAll("[^a-z0-9\\s-]", "");
        base = base.replaceAll("\\s+", "-");
        base = base.replaceAll("-+", "-");
        if (base.isBlank()) {
            base = "article";
        }
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        return base + "-" + suffix;
    }

    private int safeInt(Long v) {
        if (v == null) {
            return 0;
        }
        return v > Integer.MAX_VALUE ? Integer.MAX_VALUE : v.intValue();
    }

    private UserVO toUserVO(SysUser u) {
        if (u == null) {
            return null;
        }
        UserVO vo = new UserVO();
        vo.setId(u.getId());
        vo.setUsername(u.getUsername());
        vo.setNickname(u.getNickname());
        vo.setAvatar(u.getAvatar());
        vo.setProfile(u.getProfile());
        return vo;
    }

    private CategoryVO toCategoryVO(BlogCategory c) {
        if (c == null) {
            return null;
        }
        CategoryVO vo = new CategoryVO();
        vo.setId(c.getId());
        vo.setParentId(c.getParentId());
        vo.setName(c.getName());
        vo.setSlug(c.getSlug());
        vo.setDescription(c.getDescription());
        vo.setSort(c.getSort());
        vo.setStatus(c.getStatus());
        return vo;
    }

    private ColumnVO toColumnVO(BlogColumn c) {
        if (c == null) {
            return null;
        }
        ColumnVO vo = new ColumnVO();
        vo.setId(c.getId());
        vo.setName(c.getName());
        vo.setSlug(c.getSlug());
        vo.setCoverUrl(c.getCoverUrl());
        vo.setDescription(c.getDescription());
        vo.setSort(c.getSort());
        vo.setStatus(c.getStatus());
        return vo;
    }

    private TagVO toTagVO(BlogTag t) {
        if (t == null) {
            return null;
        }
        TagVO vo = new TagVO();
        vo.setId(t.getId());
        vo.setName(t.getName());
        vo.setSlug(t.getSlug());
        vo.setColor(t.getColor());
        vo.setDescription(t.getDescription());
        return vo;
    }
}
