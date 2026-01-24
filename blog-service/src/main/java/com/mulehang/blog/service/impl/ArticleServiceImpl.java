package com.mulehang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mulehang.blog.context.UserContext;
import com.mulehang.blog.converter.ArticleConverter;
import com.mulehang.blog.dto.ArticleCreateDTO;
import com.mulehang.blog.dto.ArticleQueryDTO;
import com.mulehang.blog.dto.ArticleUpdateDTO;
import com.mulehang.blog.entity.*;
import com.mulehang.blog.mapper.*;
import com.mulehang.blog.metrics.BlogMetrics;
import com.mulehang.blog.mq.producer.ArticleMessageProducer;
import com.mulehang.blog.model.PageResult;
import com.mulehang.blog.cache.MultiLevelCache;
import com.mulehang.blog.redis.RedisKeys;
import com.mulehang.blog.service.ArticleService;
import com.mulehang.blog.service.CacheConsistencyService;
import com.mulehang.blog.service.HotArticleService;
import com.mulehang.blog.util.MarkdownRenderer;
import com.mulehang.blog.vo.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 文章 Service。
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    private static final int STATUS_DRAFT = 0;
    private static final int STATUS_PUBLISHED = 1;
    private static final int SOURCE_ORIGINAL = 1;

    /**
     * 用户角色：管理员。
     */
    private static final String ROLE_ADMIN = "ADMIN";

    private final BlogArticleMapper articleMapper;
    private final BlogArticleBodyMapper bodyMapper;
    private final BlogArticleTagMapper articleTagMapper;
    private final BlogCategoryMapper categoryMapper;
    private final BlogColumnMapper columnMapper;
    private final BlogTagMapper tagMapper;
    private final SysUserMapper userMapper;
    private final ArticleConverter articleConverter;
    private final MarkdownRenderer markdownRenderer;
    private final HotArticleService hotArticleService;
    private final CacheConsistencyService cacheConsistencyService;
    private final MultiLevelCache multiLevelCache;
    private final BlogMetrics blogMetrics;

    /**
     * 文章消息生产者（可选）。
     * <p>
     * 说明：MQ 组件属于 Milestone 3 的可选组件，因此这里用 {@link ObjectProvider} 做"可选注入"，
     * 未启用 RabbitMQ 时不会影响文章主流程。
     * </p>
     */
    private final ObjectProvider<ArticleMessageProducer> articleMessageProducerProvider;

    /**
     * 构造函数（构造器注入）。
     *
     * <p>
     * 通过构造器注入依赖，避免字段注入带来的可测试性与可维护性问题。
     * </p>
     */
    public ArticleServiceImpl(BlogArticleMapper articleMapper,
            BlogArticleBodyMapper bodyMapper,
            BlogArticleTagMapper articleTagMapper,
            BlogCategoryMapper categoryMapper,
            BlogColumnMapper columnMapper,
            BlogTagMapper tagMapper,
            SysUserMapper userMapper,
            ArticleConverter articleConverter,
            MarkdownRenderer markdownRenderer,
            HotArticleService hotArticleService,
            CacheConsistencyService cacheConsistencyService,
            MultiLevelCache multiLevelCache,
            BlogMetrics blogMetrics,
            ObjectProvider<ArticleMessageProducer> articleMessageProducerProvider) {
        this.articleMapper = articleMapper;
        this.bodyMapper = bodyMapper;
        this.articleTagMapper = articleTagMapper;
        this.categoryMapper = categoryMapper;
        this.columnMapper = columnMapper;
        this.tagMapper = tagMapper;
        this.userMapper = userMapper;
        this.articleConverter = articleConverter;
        this.markdownRenderer = markdownRenderer;
        this.hotArticleService = hotArticleService;
        this.cacheConsistencyService = cacheConsistencyService;
        this.multiLevelCache = multiLevelCache;
        this.blogMetrics = blogMetrics;
        this.articleMessageProducerProvider = articleMessageProducerProvider;
    }

    /**
     * 创建文章。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createArticle(ArticleCreateDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("dto 为空");
        }
        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new IllegalArgumentException("标题为空");
        }
        if (dto.getContentMd() == null) {
            throw new IllegalArgumentException("内容为空");
        }

        Long currentUserId = requireCurrentUserId();
        BlogArticle article = articleConverter.toArticleEntity(dto);
        article.setAuthorId(currentUserId);
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

        if (Objects.equals(article.getStatus(), STATUS_PUBLISHED)) {
            blogMetrics.incrementArticlePublish();
        }

        // Cache-Aside（旁路缓存）：创建操作写入后应淘汰缓存
        cacheConsistencyService.evictArticleDetail(article.getId());

        // MQ 消息：事务提交后发送 UPSERT 消息（MQ 可选组件，未启用则跳过）
        sendArticleUpsertMqIfEnabled(article.getId(), "create");
        return article.getId();
    }

    /**
     * 更新文章。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateArticle(Long id, ArticleUpdateDTO dto) {
        if (id == null) {
            throw new IllegalArgumentException("id 为空");
        }
        if (dto == null) {
            throw new IllegalArgumentException("dto 为空");
        }

        BlogArticle existing = articleMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("找不到文章: " + id);
        }
        assertCanOperate(existing);

        BlogArticle patch = new BlogArticle();
        patch.setId(id);

        if (dto.getTitle() != null)
            patch.setTitle(dto.getTitle());
        if (dto.getSlug() != null)
            patch.setSlug(dto.getSlug());
        if (dto.getSummary() != null)
            patch.setSummary(dto.getSummary());
        if (dto.getCoverUrl() != null)
            patch.setCoverUrl(dto.getCoverUrl());
        if (dto.getStatus() != null)
            patch.setStatus(dto.getStatus());
        if (dto.getSourceType() != null)
            patch.setSourceType(dto.getSourceType());
        if (dto.getAllowComment() != null)
            patch.setAllowComment(dto.getAllowComment());
        if (dto.getIsPinned() != null)
            patch.setIsPinned(dto.getIsPinned());
        if (dto.getCategoryId() != null)
            patch.setCategoryId(dto.getCategoryId());
        if (dto.getColumnId() != null)
            patch.setColumnId(dto.getColumnId());
        if (dto.getContentMd() != null)
            patch.setWordCount(countWords(dto.getContentMd()));

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
            List<Long> inputTagIds = dto.getTagIds().stream()
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            List<Long> existingTagIds = articleTagMapper.selectAllTagIdsByArticleId(id);
            Set<Long> existingTagIdSet = existingTagIds == null ? Collections.emptySet() : new HashSet<>(existingTagIds);

            articleTagMapper.delete(new QueryWrapper<BlogArticleTag>().eq("article_id", id));

            if (!inputTagIds.isEmpty()) {
                List<Long> restoreTagIds = inputTagIds.stream()
                        .filter(existingTagIdSet::contains)
                        .toList();
                if (!restoreTagIds.isEmpty()) {
                    UpdateWrapper<BlogArticleTag> restoreWrapper = new UpdateWrapper<>();
                    restoreWrapper.eq("article_id", id)
                            .in("tag_id", restoreTagIds)
                            .set("is_deleted", 0)
                            .set("update_time", LocalDateTime.now());
                    articleTagMapper.update(null, restoreWrapper);
                }

                List<Long> newTagIds = inputTagIds.stream()
                        .filter(tagId -> !existingTagIdSet.contains(tagId))
                        .toList();
                saveArticleTags(id, newTagIds);
            }
        }

        if (dto.getStatus() != null
                && Objects.equals(dto.getStatus(), STATUS_PUBLISHED)
                && existing.getPublishTime() == null) {
            BlogArticle publishPatch = new BlogArticle();
            publishPatch.setId(id);
            publishPatch.setPublishTime(LocalDateTime.now());
            articleMapper.updateById(publishPatch);
        }

        // Cache-Aside（旁路缓存）+ 延迟双删（Delayed Double Delete）
        cacheConsistencyService.evictArticleDetail(id);

        // MQ 消息：事务提交后发送 UPSERT 消息（MQ 可选组件，未启用则跳过）
        sendArticleUpsertMqIfEnabled(id, "update");
    }

    /**
     * 发布文章。
     *
     * <p>
     * 将文章状态设置为已发布，并在首次发布时写入发布时间。
     * </p>
     *
     * @param id 文章 ID
     * @throws IllegalArgumentException 当 id 为空或文章不存在时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publishArticle(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id 为空");
        }
        BlogArticle existing = articleMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("文章未找到: " + id);
        }
        assertCanOperate(existing);
        if (Objects.equals(existing.getStatus(), STATUS_PUBLISHED) && existing.getPublishTime() != null) {
            return;
        }
        BlogArticle patch = new BlogArticle();
        patch.setId(id);
        patch.setStatus(STATUS_PUBLISHED);
        patch.setPublishTime(LocalDateTime.now());
        articleMapper.updateById(patch);

        // 发布后应淘汰文章详情缓存
        cacheConsistencyService.evictArticleDetail(id);

        blogMetrics.incrementArticlePublish();

        // MQ 消息：事务提交后发送 UPSERT 消息（MQ 可选组件，未启用则跳过）
        sendArticleUpsertMqIfEnabled(id, "publish");
    }

    /**
     * 根据文章 ID 获取文章详情。
     *
     * <p>
     * 使用多级缓存读取详情（Cache-Aside），读取成功后会对热榜阅读计数进行累加。
     * </p>
     *
     * @param id 文章 ID
     * @return 文章详情
     * @throws IllegalArgumentException 当 id 为空或文章不存在时抛出
     */
    @Override
    public ArticleDetailVO getArticleDetail(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id 为空");
        }

        // Cache-Aside（旁路缓存）：通过 ID 获取文章详情应读取缓存
        String cacheKey = RedisKeys.ARTICLE_DETAIL_PREFIX + id;
        ArticleDetailVO vo = multiLevelCache.get(cacheKey, ArticleDetailVO.class, () -> {
            BlogArticle article = articleMapper.selectById(id);
            if (article == null) {
                return null;
            }
            return buildDetail(article);
        });
        if (vo == null) {
            throw new IllegalArgumentException("通过 ID 找不到文章: " + id);
        }
        // 访问详情时，增加热榜阅读计数（ZINCRBY）
        hotArticleService.incrementReadCount(id);
        // 增加数据库阅读量
        articleMapper.incrementReadCount(id);
        // 刷新缓存中的阅读量
        vo.setReadCount(vo.getReadCount() + 1);
        return vo;
    }

    /**
     * 根据 slug 获取文章详情。
     *
     * <p>
     * 仅查询已发布文章，并复用与 {@link #getArticleDetail(Long)} 相同的缓存 Key。
     * </p>
     *
     * @param slug 文章 slug
     * @return 文章详情
     * @throws IllegalArgumentException 当 slug 为空或文章不存在时抛出
     */
    @Override
    public ArticleDetailVO getArticleBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("slug 为空");
        }
        BlogArticle article = articleMapper.selectOne(new LambdaQueryWrapper<BlogArticle>()
                .eq(BlogArticle::getSlug, slug)
                .eq(BlogArticle::getStatus, STATUS_PUBLISHED));
        if (article == null) {
            throw new IllegalArgumentException("通过 slug 找不到文章: " + slug);
        }
        // Cache-Aside（旁路缓存）：复用与 getArticleDetail(id) 相同的缓存 Key
        Long id = article.getId();
        String cacheKey = RedisKeys.ARTICLE_DETAIL_PREFIX + id;
        ArticleDetailVO vo = multiLevelCache.get(cacheKey, ArticleDetailVO.class, () -> buildDetail(article));

        // 访问详情时，增加热榜阅读计数（ZINCRBY）
        hotArticleService.incrementReadCount(id);
        // 增加数据库阅读量
        articleMapper.incrementReadCount(id);
        // 刷新缓存中的阅读量
        vo.setReadCount(vo.getReadCount() + 1);
        return vo;
    }

    /**
     * 根据 slug 获取前台文章详情（不含 contentMd）。
     */
    @Override
    public ArticlePublicVO getPublicArticleBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("slug 为空");
        }
        BlogArticle article = articleMapper.selectOne(new LambdaQueryWrapper<BlogArticle>()
                .eq(BlogArticle::getSlug, slug)
                .eq(BlogArticle::getStatus, STATUS_PUBLISHED));
        if (article == null) {
            throw new IllegalArgumentException("通过 slug 找不到文章: " + slug);
        }
        Long id = article.getId();
        String cacheKey = RedisKeys.ARTICLE_DETAIL_PREFIX + id;
        ArticleDetailVO detailVO = multiLevelCache.get(cacheKey, ArticleDetailVO.class, () -> buildDetail(article));

        ArticlePublicVO vo = convertToPublicVO(detailVO);

        hotArticleService.incrementReadCount(id);
        // 增加数据库阅读量
        articleMapper.incrementReadCount(id);
        // 刷新缓存中的阅读量
        vo.setReadCount(vo.getReadCount() + 1);
        return vo;
    }

    /**
     * 获取热榜文章列表。
     *
     * <p>
     * 先从 Redis 热榜中获取文章 ID，再按 ID 查询文章并按热榜顺序返回。
     * </p>
     *
     * @param topN 返回数量（TopN）
     * @return 热榜文章列表
     */
    @Override
    public List<ArticleListVO> listHotArticles(int topN) {
        List<Long> ids = hotArticleService.getHotArticleIds(topN);
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<BlogArticle> articles = articleMapper.selectList(new LambdaQueryWrapper<BlogArticle>()
                .in(BlogArticle::getId, ids));
        if (articles == null || articles.isEmpty()) {
            return Collections.emptyList();
        }

        // 从 Redis 热榜中获取文章 ID，再按 ID 查询文章。
        Map<Long, BlogArticle> map = articles.stream()
                .collect(Collectors.toMap(BlogArticle::getId, Function.identity(), (a, b) -> a));
        List<BlogArticle> ordered = ids.stream()
                .map(map::get)
                .filter(Objects::nonNull)
                .toList();
        return buildListVO(ordered);
    }

    /**
     * 分页查询文章列表。
     *
     * <p>
     * 支持按状态/分类/专栏/作者/标签/关键词过滤，并支持排序字段与顺序。
     * </p>
     *
     * @param query 查询条件（允许为空，空则使用默认分页参数）
     * @return 分页结果
     */
    @Override
    public PageResult<ArticleListVO> listArticles(ArticleQueryDTO query) {
        if (query == null) {
            query = new ArticleQueryDTO();
        }
        long pageNo = query.getPageNo() == null ? 1L : query.getPageNo();
        long pageSize = query.getPageSize() == null ? 10L : query.getPageSize();

        Page<BlogArticle> page = new Page<>(pageNo, pageSize);
        LambdaQueryWrapper<BlogArticle> qw = new LambdaQueryWrapper<>();

        if (query.getStatus() != null)
            qw.eq(BlogArticle::getStatus, query.getStatus());
        if (query.getCategoryId() != null)
            qw.eq(BlogArticle::getCategoryId, query.getCategoryId());
        if (query.getColumnId() != null)
            qw.eq(BlogArticle::getColumnId, query.getColumnId());
        if (query.getAuthorId() != null)
            qw.eq(BlogArticle::getAuthorId, query.getAuthorId());
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

    /**
     * 删除文章。
     *
     * <p>
     * 当前采用逻辑删除（由 MyBatis-Plus 逻辑删除机制处理），并淘汰文章详情缓存。
     * </p>
     *
     * @param id 文章 ID
     * @throws IllegalArgumentException 当 id 为空时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("id 为空");
        }
        BlogArticle existing = articleMapper.selectById(id);
        if (existing == null) {
            throw new IllegalArgumentException("文章未找到: " + id);
        }
        assertCanOperate(existing);
        // 逻辑删除即可（MyBatis-Plus 逻辑删除：@TableLogic）。
        articleMapper.deleteById(id);

        // 删除后应淘汰文章详情缓存
        cacheConsistencyService.evictArticleDetail(id);

        // MQ 消息：事务提交后发送 DELETE 消息（MQ 可选组件，未启用则跳过）
        sendArticleDeleteMqIfEnabled(id);
    }

    /**
     * 获取当前登录用户 ID（强制要求已登录）。
     *
     * @return 当前用户 ID
     * @throws IllegalStateException 当未登录时抛出
     */
    private Long requireCurrentUserId() {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("未登录或登录已过期");
        }
        return userId;
    }

    /**
     * 判断当前用户是否为管理员。
     *
     * @return true-管理员，false-非管理员
     */
    private boolean isAdmin() {
        UserInfoVO user = UserContext.getCurrentUser();
        if (user == null || user.getRoles() == null) {
            return false;
        }
        return user.getRoles().stream().anyMatch(ROLE_ADMIN::equalsIgnoreCase);
    }

    /**
     * 权限校验：仅作者本人或管理员可操作文章。
     *
     * @param article 文章实体
     * @throws IllegalArgumentException 当无权限操作时抛出
     */
    private void assertCanOperate(BlogArticle article) {
        if (article == null) {
            throw new IllegalArgumentException("文章不存在");
        }
        if (isAdmin()) {
            return;
        }
        Long currentUserId = requireCurrentUserId();
        if (!Objects.equals(article.getAuthorId(), currentUserId)) {
            throw new IllegalArgumentException("无权限操作该文章");
        }
    }

    /**
     * 应用排序规则。
     *
     * @param qw        查询条件构造器
     * @param sortBy    排序字段（如 publishTime/createTime/readCount）
     * @param sortOrder 排序方向（asc/desc，不区分大小写）
     */
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

    /**
     * 组装文章详情 VO。
     *
     * <p>
     * 包含正文、作者、分类、专栏、标签等关联信息。
     * </p>
     *
     * @param article 文章实体
     * @return 详情 VO
     */
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
        vo.setReadCount(article.getReadCount());
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
        vo.setCategory(toCategoryVO(
                article.getCategoryId() == null ? null : categoryMapper.selectById(article.getCategoryId())));
        vo.setColumn(toColumnVO(article.getColumnId() == null ? null : columnMapper.selectById(article.getColumnId())));
        vo.setTags(loadTagsByArticleId(article.getId()));
        return vo;
    }

    /**
     * 将文章实体列表转换为列表页 VO。
     *
     * <p>
     * 为减少 N+1 查询，会批量加载作者/分类/标签等数据后再组装。
     * </p>
     *
     * @param articles 文章实体列表
     * @return 列表页 VO
     */
    private List<ArticleListVO> buildListVO(List<BlogArticle> articles) {
        Set<Long> authorIds = articles.stream().map(BlogArticle::getAuthorId).filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<Long> categoryIds = articles.stream().map(BlogArticle::getCategoryId).filter(Objects::nonNull)
                .collect(Collectors.toSet());
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
            vo.setCreateTime(a.getCreateTime());
            vo.setUpdateTime(a.getUpdateTime());

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

    /**
     * 根据文章 ID 加载标签列表。
     *
     * @param articleId 文章 ID
     * @return 标签列表
     */
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

    /**
     * 保存文章与标签的关联关系。
     *
     * @param articleId 文章 ID
     * @param tagIds    标签 ID 列表
     */
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

    /**
     * 如果启用了 RabbitMQ，则发送文章 UPSERT 消息到 MQ。
     *
     * <p>
     * 说明：
     * </p>
     * <ul>
     * <li>MQ 作为可选组件：未启用时，该方法会直接返回。</li>
     * <li>消息发送发生在事务提交后：由 {@link ArticleMessageProducer} 内部保证。</li>
     * <li>Consumer 收到消息后会查 DB 并同步到 ES。</li>
     * </ul>
     *
     * @param articleId 文章 ID
     * @param reason    触发原因（create / update / publish）
     */
    private void sendArticleUpsertMqIfEnabled(Long articleId, String reason) {
        ArticleMessageProducer producer = articleMessageProducerProvider.getIfAvailable();
        if (producer == null) {
            return;
        }
        producer.sendUpsert(articleId, reason);
    }

    /**
     * 如果启用了 RabbitMQ，则发送文章 DELETE 消息到 MQ。
     *
     * @param articleId 文章 ID
     */
    private void sendArticleDeleteMqIfEnabled(Long articleId) {
        ArticleMessageProducer producer = articleMessageProducerProvider.getIfAvailable();
        if (producer == null) {
            return;
        }
        producer.sendDelete(articleId);
    }

    /**
     * 统计 Markdown 内容的“字数”。
     *
     * <p>
     * 这里采用简单策略：统计非空白字符数量。
     * </p>
     *
     * @param contentMd Markdown 内容
     * @return 字数（非空白字符数）
     */
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

    /**
     * 根据标题生成文章 slug。
     *
     * <p>
     * 会将标题规整为小写、去除非法字符、空格替换为连字符，并追加随机后缀避免冲突。
     * </p>
     *
     * @param title 文章标题
     * @return slug
     */
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

    /**
     * 将用户实体转换为用户 VO。
     *
     * @param u 用户实体
     * @return 用户 VO
     */
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

    /**
     * 将分类实体转换为分类 VO。
     *
     * @param c 分类实体
     * @return 分类 VO
     */
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

    /**
     * 将专栏实体转换为专栏 VO。
     *
     * @param c 专栏实体
     * @return 专栏 VO
     */
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

    /**
     * 将标签实体转换为标签 VO。
     *
     * @param t 标签实体
     * @return 标签 VO
     */
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

    /**
     * 将 ArticleDetailVO 转换为 ArticlePublicVO（不含 contentMd）。
     */
    private ArticlePublicVO convertToPublicVO(ArticleDetailVO detail) {
        if (detail == null) {
            return null;
        }
        ArticlePublicVO vo = new ArticlePublicVO();
        vo.setId(detail.getId());
        vo.setTitle(detail.getTitle());
        vo.setSlug(detail.getSlug());
        vo.setSummary(detail.getSummary());
        vo.setCoverUrl(detail.getCoverUrl());
        vo.setStatus(detail.getStatus());
        vo.setSourceType(detail.getSourceType());
        vo.setAllowComment(detail.getAllowComment());
        vo.setIsPinned(detail.getIsPinned());
        vo.setAuthor(detail.getAuthor());
        vo.setCategory(detail.getCategory());
        vo.setColumn(detail.getColumn());
        vo.setTags(detail.getTags());
        vo.setWordCount(detail.getWordCount());
        vo.setReadCount(detail.getReadCount());
        vo.setLikeCount(detail.getLikeCount());
        vo.setCommentCount(detail.getCommentCount());
        vo.setPublishTime(detail.getPublishTime());
        vo.setCreateTime(detail.getCreateTime());
        vo.setUpdateTime(detail.getUpdateTime());
        vo.setContentHtml(detail.getContentHtml());
        return vo;
    }
}
