# 个人博客开发指南

> 基于 Spring Boot 3.3.7 + Java 21 的全栈技术学习实践指南
> 
> 创建时间：2025-12-30

---

## 目录

- [Milestone 1：文章发布链路](#milestone-1文章发布链路3-7天)
- [Milestone 2：缓存与热点数据](#milestone-2缓存与热点数据3-7天)
- [Milestone 3：搜索与异步](#milestone-3搜索与异步1-2周)
- [Milestone 4：安全与运营](#milestone-4安全与运营1-2周)
- [Milestone 5：AI与内容生产](#milestone-5ai与内容生产持续迭代)
- [Milestone 6：部署与运维](#milestone-6部署与运维)

---

## 当前项目状态（Milestone 0 已完成）

### ✅ 已完成的基础设施

| 组件 | 状态 | 说明 |
|------|------|------|
| Maven 多模块骨架 | ✅ | blog-api/core/service/ui/web |
| Spring Boot 3.3.7 | ✅ | Java 21 |
| MySQL + HikariCP | ✅ | 连接池已配置 |
| Redis + Lettuce | ✅ | 基础配置完成 |
| RabbitMQ | ✅ | Spring AMQP 配置完成 |
| Liquibase | ✅ | 初始 schema 已创建 |
| Thymeleaf | ✅ | 模板引擎就绪 |
| Knife4j/OpenAPI3 | ✅ | API 文档配置完成 |
| Actuator + Prometheus | ✅ | 监控端点就绪 |

### ✅ 已创建的数据库表

- `sys_user` - 用户表
- `sys_role` - 角色表
- `sys_user_role` - 用户角色关联表
- `blog_category` - 文章分类
- `blog_tag` - 文章标签
- `blog_column` - 文章专栏/系列
- `blog_article` - 文章主表
- `blog_article_body` - 文章内容表
- `blog_article_tag` - 文章标签关联表
- `blog_comment` - 评论表
- `site_config` - 站点配置表

---

## Milestone 1：文章发布链路（3-7天）

### 🎯 学习目标

| 技术栈 | 学习要点 |
|--------|----------|
| MyBatis-Plus | CRUD、分页、条件构造器、逻辑删除 |
| MapStruct | DO/DTO/VO 转换 |
| Flexmark | Markdown → HTML 渲染 |
| 文件上传 | 本地存储 + OSS 抽象 |
| Spring Validation | 参数校验 |

### 📋 任务清单

#### 1.1 创建实体类（Entity/DO）

**位置**: `blog-service/src/main/java/com/mulehang/blog/entity/`

```
需要创建的实体类：
├── SysUser.java
├── SysRole.java
├── SysUserRole.java
├── BlogCategory.java
├── BlogTag.java
├── BlogColumn.java
├── BlogArticle.java
├── BlogArticleBody.java
├── BlogArticleTag.java
├── BlogComment.java
└── SiteConfig.java
```

**学习要点**：
- 使用 `@TableName` 指定表名
- 使用 `@TableId(type = IdType.AUTO)` 主键策略
- 使用 `@TableLogic` 逻辑删除
- 使用 `@TableField(fill = FieldFill.INSERT)` 自动填充

**示例代码模板**：
```java
@Data
@TableName("blog_article")
public class BlogArticle {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    private String slug;
    private String summary;
    // ... 其他字段
    
    @TableLogic
    @TableField("is_deleted")
    private Integer deleted;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

#### 1.2 创建 Mapper 接口

**位置**: `blog-service/src/main/java/com/mulehang/blog/mapper/`

```
需要创建的 Mapper：
├── SysUserMapper.java
├── BlogCategoryMapper.java
├── BlogTagMapper.java
├── BlogColumnMapper.java
├── BlogArticleMapper.java
├── BlogArticleBodyMapper.java
├── BlogArticleTagMapper.java
└── BlogCommentMapper.java
```

**学习要点**：
- 继承 `BaseMapper<T>` 获得基础 CRUD
- 使用 `@Mapper` 或 `@MapperScan` 扫描

#### 1.3 创建 DTO/VO 类

**位置**: `blog-api/src/main/java/com/mulehang/blog/`

```
目录结构：
├── dto/                    # 数据传输对象（接收请求）
│   ├── article/
│   │   ├── ArticleCreateDTO.java
│   │   ├── ArticleUpdateDTO.java
│   │   └── ArticleQueryDTO.java
│   ├── category/
│   │   └── CategoryDTO.java
│   └── tag/
│       └── TagDTO.java
├── vo/                     # 视图对象（返回响应）
│   ├── article/
│   │   ├── ArticleVO.java
│   │   ├── ArticleDetailVO.java
│   │   └── ArticleListVO.java
│   ├── category/
│   │   └── CategoryVO.java
│   └── common/
│       ├── PageResult.java
│       └── Result.java
└── enums/                  # 枚举
    ├── ArticleStatusEnum.java
    ├── SourceTypeEnum.java
    └── CommentStatusEnum.java
```

#### 1.4 创建 MapStruct 转换器

**位置**: `blog-service/src/main/java/com/mulehang/blog/converter/`

**学习要点**：
- `@Mapper(componentModel = "spring")` 集成 Spring
- 字段名相同自动映射
- `@Mapping` 处理不同名字段
- 集合转换自动生成

```java
@Mapper(componentModel = "spring")
public interface ArticleConverter {
    ArticleVO toVO(BlogArticle entity);
    BlogArticle toEntity(ArticleCreateDTO dto);
    List<ArticleListVO> toListVO(List<BlogArticle> entities);
}
```

#### 1.5 创建 Service 层

**位置**: `blog-service/src/main/java/com/mulehang/blog/service/`

```
目录结构：
├── ArticleService.java           # 接口
├── CategoryService.java
├── TagService.java
├── ColumnService.java
└── impl/
    ├── ArticleServiceImpl.java   # 实现
    ├── CategoryServiceImpl.java
    ├── TagServiceImpl.java
    └── ColumnServiceImpl.java
```

**核心业务逻辑**：

```java
public interface ArticleService {
    // 创建文章（草稿/发布）
    Long createArticle(ArticleCreateDTO dto);
    
    // 更新文章
    void updateArticle(Long id, ArticleUpdateDTO dto);
    
    // 发布文章
    void publishArticle(Long id);
    
    // 获取文章详情
    ArticleDetailVO getArticleDetail(Long id);
    
    // 通过 slug 获取文章（用于前台展示）
    ArticleDetailVO getArticleBySlug(String slug);
    
    // 分页查询文章列表
    PageResult<ArticleListVO> listArticles(ArticleQueryDTO query);
    
    // 删除文章（逻辑删除）
    void deleteArticle(Long id);
}
```

#### 1.6 实现 Markdown 渲染服务

**位置**: `blog-core/src/main/java/com/mulehang/blog/markdown/`

**学习要点**：
- Flexmark 扩展配置
- 代码高亮
- 表格、任务列表支持
- XSS 防护

```java
@Service
public class MarkdownService {
    private final Parser parser;
    private final HtmlRenderer renderer;
    
    public MarkdownService() {
        // 配置 Flexmark 扩展
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, Arrays.asList(
            TablesExtension.create(),
            TaskListExtension.create(),
            AutolinkExtension.create()
            // ... 更多扩展
        ));
        this.parser = Parser.builder(options).build();
        this.renderer = HtmlRenderer.builder(options).build();
    }
    
    public String renderToHtml(String markdown) {
        Document document = parser.parse(markdown);
        return renderer.render(document);
    }
}
```

#### 1.7 创建 Controller 层

**位置**: `blog-web/src/main/java/com/mulehang/blog/controller/`

```
目录结构：
├── api/                          # REST API（前后端分离）
│   └── v1/
│       ├── ArticleController.java
│       ├── CategoryController.java
│       └── TagController.java
└── view/                         # 页面渲染（Thymeleaf）
    ├── HomeController.java
    └── ArticleViewController.java
```

**REST API 示例**：

```java
@RestController
@RequestMapping("/api/v1/articles")
@Tag(name = "文章管理", description = "文章相关接口")
public class ArticleController {
    
    @PostMapping
    @Operation(summary = "创建文章")
    public Result<Long> create(@Valid @RequestBody ArticleCreateDTO dto) {
        // ...
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "获取文章详情")
    public Result<ArticleDetailVO> getById(@PathVariable Long id) {
        // ...
    }
    
    @GetMapping
    @Operation(summary = "分页查询文章")
    public Result<PageResult<ArticleListVO>> list(ArticleQueryDTO query) {
        // ...
    }
}
```

#### 1.8 实现文件上传

**位置**: `blog-core/src/main/java/com/mulehang/blog/storage/`

**学习要点**：
- 策略模式：本地存储 vs OSS
- 文件校验（类型、大小）
- 唯一文件名生成

```
目录结构：
├── StorageService.java           # 接口
├── LocalStorageService.java      # 本地实现
├── OssStorageService.java        # OSS 实现
└── StorageConfig.java            # 配置类
```

### 🔧 配置要点

#### MyBatis-Plus 自动填充配置

```java
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime::now, LocalDateTime.class);
    }
    
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime::now, LocalDateTime.class);
    }
}
```

#### 分页插件配置

```java
@Configuration
public class MyBatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
```

### ✅ 完成标准

- [ ] 能够创建/编辑/删除文章
- [ ] Markdown 正确渲染为 HTML
- [ ] 分页查询正常工作
- [ ] 文件上传功能可用
- [ ] Swagger 文档可访问 (`/doc.html`)
- [ ] 单元测试覆盖核心 Service

---

## Milestone 2：缓存与热点数据（3-7天）

### 🎯 学习目标

| 技术栈 | 学习要点 |
|--------|----------|
| Caffeine | 本地缓存配置、过期策略、统计 |
| Redis | 数据结构（String/Hash/ZSet/List）、过期时间 |
| Spring Cache | @Cacheable/@CacheEvict/@CachePut 注解 |
| Redisson | 分布式锁、延迟队列、限流器 |
| 多级缓存 | L1(Caffeine) + L2(Redis) 架构 |

### 📋 任务清单

#### 2.1 配置 Caffeine 本地缓存

**位置**: `blog-core/src/main/java/com/mulehang/blog/cache/`

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager();
        manager.setCaffeine(Caffeine.newBuilder()
            .initialCapacity(100)
            .maximumSize(1000)
            .expireAfterWrite(Duration.ofMinutes(10))
            .recordStats());  // 开启统计
        return manager;
    }
}
```

**缓存名称规划**：
| 缓存名 | 用途 | TTL |
|--------|------|-----|
| `article:detail` | 文章详情 | 10分钟 |
| `category:list` | 分类列表 | 30分钟 |
| `tag:list` | 标签列表 | 30分钟 |
| `hot:articles` | 热门文章 | 5分钟 |

#### 2.2 配置 Redis 缓存

**Redis Key 设计规范**：
```
业务:对象类型:标识[:子类型]

示例：
blog:article:123          # 文章详情
blog:article:123:body     # 文章内容
blog:user:456:profile     # 用户资料
blog:hot:articles         # 热门文章榜
blog:pv:article:123       # 文章PV计数
```

**Redis 工具类封装**：

```java
@Component
public class RedisService {
    private final StringRedisTemplate stringRedisTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    
    // 字符串操作
    public void set(String key, String value, Duration timeout);
    public String get(String key);
    
    // Hash 操作
    public void hSet(String key, String field, Object value);
    public Object hGet(String key, String field);
    
    // ZSet 操作（排行榜）
    public void zIncrBy(String key, String member, double score);
    public Set<ZSetOperations.TypedTuple<Object>> zRevRangeWithScores(String key, long start, long end);
    
    // 计数器
    public Long increment(String key);
    public Long increment(String key, long delta);
}
```

#### 2.3 实现多级缓存

**位置**: `blog-core/src/main/java/com/mulehang/blog/cache/`

```java
@Component
public class MultiLevelCache {
    private final Cache<String, Object> localCache;  // Caffeine
    private final RedisTemplate<String, Object> redisTemplate;
    
    public <T> T get(String key, Class<T> type, Supplier<T> loader) {
        // 1. 先查本地缓存
        T value = (T) localCache.getIfPresent(key);
        if (value != null) {
            return value;
        }
        
        // 2. 查 Redis
        value = (T) redisTemplate.opsForValue().get(key);
        if (value != null) {
            localCache.put(key, value);  // 回填本地
            return value;
        }
        
        // 3. 查数据库
        value = loader.get();
        if (value != null) {
            redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(30));
            localCache.put(key, value);
        }
        return value;
    }
    
    public void evict(String key) {
        localCache.invalidate(key);
        redisTemplate.delete(key);
    }
}
```

#### 2.4 实现热门文章榜（Redis ZSet）

**学习要点**：
- ZSet 有序集合
- ZINCRBY 增加分数
- ZREVRANGE 获取排名

```java
@Service
public class HotArticleService {
    private static final String HOT_ARTICLES_KEY = "blog:hot:articles";
    
    // 增加阅读量
    public void incrementReadCount(Long articleId) {
        redisTemplate.opsForZSet().incrementScore(
            HOT_ARTICLES_KEY, 
            articleId.toString(), 
            1
        );
    }
    
    // 获取热门文章 Top N
    public List<Long> getHotArticleIds(int topN) {
        Set<Object> ids = redisTemplate.opsForZSet()
            .reverseRange(HOT_ARTICLES_KEY, 0, topN - 1);
        return ids.stream()
            .map(id -> Long.parseLong(id.toString()))
            .collect(Collectors.toList());
    }
    
    // 定时任务：每日重置热榜
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetHotArticles() {
        redisTemplate.delete(HOT_ARTICLES_KEY);
    }
}
```

#### 2.5 Redisson 分布式锁

**场景**：防止重复提交、重复点赞

```java
@Service
public class LikeService {
    private final RedissonClient redissonClient;
    
    public boolean likeArticle(Long userId, Long articleId) {
        String lockKey = "lock:like:" + articleId + ":" + userId;
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            // 尝试获取锁，等待3秒，锁定10秒自动释放
            if (lock.tryLock(3, 10, TimeUnit.SECONDS)) {
                try {
                    // 检查是否已点赞
                    String likeKey = "blog:like:article:" + articleId;
                    Boolean hasLiked = redisTemplate.opsForSet()
                        .isMember(likeKey, userId.toString());
                    
                    if (Boolean.TRUE.equals(hasLiked)) {
                        return false;  // 已点赞
                    }
                    
                    // 记录点赞
                    redisTemplate.opsForSet().add(likeKey, userId.toString());
                    // 更新点赞数
                    articleMapper.incrementLikeCount(articleId);
                    return true;
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return false;
    }
}
```

#### 2.6 Redisson 延迟队列

**场景**：延迟发送邮件通知

```java
@Service
public class DelayedTaskService {
    private final RedissonClient redissonClient;
    
    public void scheduleEmailNotification(String email, String content, Duration delay) {
        RBlockingQueue<EmailTask> blockingQueue = 
            redissonClient.getBlockingQueue("delayed:email:queue");
        RDelayedQueue<EmailTask> delayedQueue = 
            redissonClient.getDelayedQueue(blockingQueue);
        
        EmailTask task = new EmailTask(email, content);
        delayedQueue.offer(task, delay.toSeconds(), TimeUnit.SECONDS);
    }
    
    // 消费者（单独线程）
    @PostConstruct
    public void startConsumer() {
        RBlockingQueue<EmailTask> queue = 
            redissonClient.getBlockingQueue("delayed:email:queue");
        
        new Thread(() -> {
            while (true) {
                try {
                    EmailTask task = queue.take();
                    emailService.send(task.getEmail(), task.getContent());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }
}
```

#### 2.7 缓存一致性策略

**策略选择**：

| 策略 | 适用场景 | 实现方式 |
|------|----------|----------|
| Cache-Aside | 读多写少 | 先更新DB，再删缓存 |
| Write-Through | 数据一致性要求高 | 同时写DB和缓存 |
| Write-Behind | 写多读少 | 异步批量写入DB |

**推荐实现（Cache-Aside + 延迟双删）**：

```java
@Service
public class ArticleServiceImpl implements ArticleService {
    
    @Transactional
    public void updateArticle(Long id, ArticleUpdateDTO dto) {
        // 1. 删除缓存
        cacheService.evict("article:detail:" + id);
        
        // 2. 更新数据库
        articleMapper.updateById(convertToEntity(dto));
        
        // 3. 延迟再删一次（防止并发问题）
        CompletableFuture.delayedExecutor(500, TimeUnit.MILLISECONDS)
            .execute(() -> cacheService.evict("article:detail:" + id));
    }
}
```

### ✅ 完成标准

- [ ] Caffeine 本地缓存生效
- [ ] Redis 缓存正常读写
- [ ] 热门文章榜功能可用
- [ ] 分布式锁防重复点赞
- [ ] 缓存统计指标可观测
- [ ] 编写缓存相关单元测试

---

## Milestone 3：搜索与异步（1-2周）

### 🎯 学习目标

| 技术栈 | 学习要点 |
|--------|----------|
| Elasticsearch 8.x | 索引设计、映射、分词器、查询DSL |
| Spring Data ES | Repository、ElasticsearchClient |
| RabbitMQ | Exchange/Queue/Binding、消息确认、死信队列 |
| 异步处理 | 解耦、削峰、最终一致性 |

### 📋 任务清单

#### 3.1 添加 Elasticsearch 依赖

**位置**: `blog-service/pom.xml`

```xml
<!-- Elasticsearch 8.x Java Client -->
<dependency>
    <groupId>co.elastic.clients</groupId>
    <artifactId>elasticsearch-java</artifactId>
</dependency>
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

**配置**: `application.yml`

```yaml
elasticsearch:
  uris: http://localhost:9200
  username: elastic
  password: your-password
```

#### 3.2 设计文章搜索索引

**索引名**: `blog_article`

**映射设计**：
```json
{
  "mappings": {
    "properties": {
      "id": { "type": "long" },
      "title": { 
        "type": "text",
        "analyzer": "ik_max_word",
        "search_analyzer": "ik_smart"
      },
      "summary": { 
        "type": "text",
        "analyzer": "ik_max_word" 
      },
      "content": { 
        "type": "text",
        "analyzer": "ik_max_word" 
      },
      "categoryId": { "type": "long" },
      "categoryName": { "type": "keyword" },
      "tags": { "type": "keyword" },
      "authorId": { "type": "long" },
      "authorName": { "type": "keyword" },
      "status": { "type": "integer" },
      "publishTime": { "type": "date" },
      "readCount": { "type": "long" },
      "createTime": { "type": "date" }
    }
  }
}
```

#### 3.3 创建 ES 文档类

**位置**: `blog-service/src/main/java/com/mulehang/blog/es/document/`

```java
@Document(indexName = "blog_article")
@Data
public class ArticleDocument {
    @Id
    private Long id;
    
    @Field(type = FieldType.Text, analyzer = "ik_max_word", searchAnalyzer = "ik_smart")
    private String title;
    
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String summary;
    
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String content;
    
    @Field(type = FieldType.Keyword)
    private List<String> tags;
    
    @Field(type = FieldType.Long)
    private Long categoryId;
    
    @Field(type = FieldType.Keyword)
    private String categoryName;
    
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    private LocalDateTime publishTime;
    
    // ... 其他字段
}
```

#### 3.4 实现搜索服务

**位置**: `blog-service/src/main/java/com/mulehang/blog/es/`

```java
@Service
public class ArticleSearchService {
    private final ElasticsearchClient esClient;
    
    /**
     * 全文搜索
     */
    public SearchResult<ArticleDocument> search(String keyword, int page, int size) {
        SearchResponse<ArticleDocument> response = esClient.search(s -> s
            .index("blog_article")
            .query(q -> q
                .multiMatch(m -> m
                    .query(keyword)
                    .fields("title^3", "summary^2", "content")  // 权重
                    .type(TextQueryType.BestFields)
                )
            )
            .highlight(h -> h
                .fields("title", f -> f.preTags("<em>").postTags("</em>"))
                .fields("summary", f -> f.preTags("<em>").postTags("</em>"))
            )
            .from((page - 1) * size)
            .size(size),
            ArticleDocument.class
        );
        
        return convertToResult(response);
    }
    
    /**
     * 同步文章到 ES
     */
    public void indexArticle(ArticleDocument doc) {
        esClient.index(i -> i
            .index("blog_article")
            .id(doc.getId().toString())
            .document(doc)
        );
    }
    
    /**
     * 删除文章索引
     */
    public void deleteArticle(Long id) {
        esClient.delete(d -> d
            .index("blog_article")
            .id(id.toString())
        );
    }
}
```

#### 3.5 配置 RabbitMQ 交换机和队列

**位置**: `blog-service/src/main/java/com/mulehang/blog/mq/config/`

```java
@Configuration
public class RabbitMQConfig {
    
    // ===== 文章相关 =====
    public static final String ARTICLE_EXCHANGE = "blog.article.exchange";
    public static final String ARTICLE_PUBLISH_QUEUE = "blog.article.publish.queue";
    public static final String ARTICLE_DELETE_QUEUE = "blog.article.delete.queue";
    
    @Bean
    public TopicExchange articleExchange() {
        return new TopicExchange(ARTICLE_EXCHANGE, true, false);
    }
    
    @Bean
    public Queue articlePublishQueue() {
        return QueueBuilder.durable(ARTICLE_PUBLISH_QUEUE)
            .deadLetterExchange("blog.dlx.exchange")
            .deadLetterRoutingKey("dlx.article")
            .build();
    }
    
    @Bean
    public Binding articlePublishBinding() {
        return BindingBuilder
            .bind(articlePublishQueue())
            .to(articleExchange())
            .with("article.publish");
    }
    
    // ===== 死信队列 =====
    public static final String DLX_EXCHANGE = "blog.dlx.exchange";
    public static final String DLX_QUEUE = "blog.dlx.queue";
    
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(DLX_EXCHANGE, true, false);
    }
    
    @Bean
    public Queue dlxQueue() {
        return QueueBuilder.durable(DLX_QUEUE).build();
    }
}
```

#### 3.6 消息生产者

**位置**: `blog-service/src/main/java/com/mulehang/blog/mq/producer/`

```java
@Component
@Slf4j
public class ArticleMessageProducer {
    private final RabbitTemplate rabbitTemplate;
    
    /**
     * 发送文章发布消息
     */
    public void sendArticlePublishMessage(Long articleId) {
        ArticleMessage message = new ArticleMessage();
        message.setArticleId(articleId);
        message.setAction("PUBLISH");
        message.setTimestamp(LocalDateTime.now());
        
        rabbitTemplate.convertAndSend(
            RabbitMQConfig.ARTICLE_EXCHANGE,
            "article.publish",
            message,
            msg -> {
                msg.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                msg.getMessageProperties().setMessageId(UUID.randomUUID().toString());
                return msg;
            }
        );
        
        log.info("发送文章发布消息: articleId={}", articleId);
    }
}
```

#### 3.7 消息消费者

**位置**: `blog-service/src/main/java/com/mulehang/blog/mq/consumer/`

```java
@Component
@Slf4j
public class ArticleMessageConsumer {
    private final ArticleSearchService searchService;
    private final ArticleService articleService;
    
    @RabbitListener(queues = RabbitMQConfig.ARTICLE_PUBLISH_QUEUE)
    public void handleArticlePublish(ArticleMessage message, Channel channel, 
                                      @Header(AmqpHeaders.DELIVERY_TAG) long tag) {
        try {
            log.info("收到文章发布消息: {}", message);
            
            // 1. 查询文章详情
            ArticleDetailVO article = articleService.getArticleDetail(message.getArticleId());
            
            // 2. 转换为 ES 文档
            ArticleDocument doc = convertToDocument(article);
            
            // 3. 同步到 ES
            searchService.indexArticle(doc);
            
            // 4. 手动确认
            channel.basicAck(tag, false);
            
            log.info("文章同步到ES成功: articleId={}", message.getArticleId());
        } catch (Exception e) {
            log.error("处理文章发布消息失败", e);
            try {
                // 重试或进入死信队列
                channel.basicNack(tag, false, false);
            } catch (IOException ex) {
                log.error("消息拒绝失败", ex);
            }
        }
    }
}
```

#### 3.8 定时补偿任务

**场景**：MQ 消息丢失时的兜底同步

```java
@Component
@Slf4j
public class ArticleSyncTask {
    private final ArticleMapper articleMapper;
    private final ArticleSearchService searchService;
    
    /**
     * 每小时检查并同步未索引的文章
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void syncMissingArticles() {
        log.info("开始执行文章同步补偿任务");
        
        // 查询最近24小时发布但可能未同步的文章
        LocalDateTime since = LocalDateTime.now().minusHours(24);
        List<BlogArticle> articles = articleMapper.findPublishedSince(since);
        
        for (BlogArticle article : articles) {
            try {
                // 检查 ES 中是否存在
                if (!searchService.exists(article.getId())) {
                    ArticleDocument doc = convertToDocument(article);
                    searchService.indexArticle(doc);
                    log.info("补偿同步文章: id={}", article.getId());
                }
            } catch (Exception e) {
                log.error("补偿同步失败: id={}", article.getId(), e);
            }
        }
    }
}
```

#### 3.9 异步邮件通知

**场景**：评论通知作者

```java
@Component
@Slf4j
public class CommentNotifyConsumer {
    private final JavaMailSender mailSender;
    private final UserService userService;
    
    @RabbitListener(queues = "blog.comment.notify.queue")
    public void handleCommentNotify(CommentNotifyMessage message) {
        // 1. 查询作者信息
        UserVO author = userService.getUserById(message.getAuthorId());
        
        // 2. 发送邮件
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(author.getEmail());
        mail.setSubject("您的文章收到新评论");
        mail.setText(String.format(
            "您好 %s，您的文章《%s》收到了新评论：\n\n%s\n\n点击查看：%s",
            author.getNickname(),
            message.getArticleTitle(),
            message.getCommentContent(),
            message.getArticleUrl()
        ));
        
        mailSender.send(mail);
        log.info("评论通知邮件发送成功: to={}", author.getEmail());
    }
}
```

### ✅ 完成标准

- [ ] ES 索引创建成功
- [ ] 文章搜索功能可用
- [ ] 搜索结果高亮显示
- [ ] RabbitMQ 消息正常收发
- [ ] 手动确认机制正常
- [ ] 死信队列配置正确
- [ ] 定时补偿任务运行正常

---

## Milestone 4：安全与运营（1-2周）

### 🎯 学习目标

| 技术栈 | 学习要点 |
|--------|----------|
| JWT | Token 生成、验证、刷新机制 |
| Spring Security 6 | SecurityFilterChain、认证授权 |
| RBAC | 角色权限模型 |
| Actuator | 健康检查、自定义指标 |
| Prometheus | 指标采集、Grafana 可视化 |
| 敏感词过滤 | sensitive-word 库使用 |
| IP 归属地 | ip2region 使用 |

### 📋 任务清单

#### 4.1 JWT 工具类实现

**位置**: `blog-core/src/main/java/com/mulehang/blog/security/`

```java
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration:86400000}")  // 默认24小时
    private long expiration;
    
    private Algorithm algorithm;
    
    @PostConstruct
    public void init() {
        this.algorithm = Algorithm.HMAC256(secret);
    }
    
    /**
     * 生成 Token
     */
    public String generateToken(Long userId, String username, List<String> roles) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return JWT.create()
            .withSubject(userId.toString())
            .withClaim("username", username)
            .withClaim("roles", roles)
            .withIssuedAt(now)
            .withExpiresAt(expiryDate)
            .sign(algorithm);
    }
    
    /**
     * 验证并解析 Token
     */
    public DecodedJWT verifyToken(String token) {
        JWTVerifier verifier = JWT.require(algorithm).build();
        return verifier.verify(token);
    }
    
    /**
     * 从 Token 获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        DecodedJWT jwt = verifyToken(token);
        return Long.parseLong(jwt.getSubject());
    }
    
    /**
     * 检查 Token 是否过期
     */
    public boolean isTokenExpired(String token) {
        DecodedJWT jwt = verifyToken(token);
        return jwt.getExpiresAt().before(new Date());
    }
}
```

**配置**: `application.yml`

```yaml
jwt:
  secret: ${JWT_SECRET:your-256-bit-secret-key-here-must-be-long-enough}
  expiration: 86400000  # 24小时
  refresh-expiration: 604800000  # 7天
```

#### 4.2 Spring Security 6 配置

**位置**: `blog-web/src/main/java/com/mulehang/blog/config/`

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // 开启方法级安全
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtFilter;
    private final JwtAuthenticationEntryPoint entryPoint;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（使用 JWT，无需 CSRF）
            .csrf(csrf -> csrf.disable())
            
            // Session 管理：无状态
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 异常处理
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(entryPoint)
            )
            
            // 请求授权
            .authorizeHttpRequests(auth -> auth
                // 公开接口
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/articles/public/**").permitAll()
                .requestMatchers("/api/v1/categories/**").permitAll()
                .requestMatchers("/api/v1/tags/**").permitAll()
                
                // Swagger 文档
                .requestMatchers("/doc.html", "/webjars/**", "/v3/api-docs/**").permitAll()
                
                // Actuator
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                
                // 管理接口
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                
                // 其他需要认证
                .anyRequest().authenticated()
            )
            
            // 添加 JWT 过滤器
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

#### 4.3 JWT 认证过滤器

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        try {
            String token = extractToken(request);
            
            if (StringUtils.hasText(token) && tokenProvider.verifyToken(token) != null) {
                Long userId = tokenProvider.getUserIdFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(userId.toString());
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.debug("JWT 认证失败: {}", e.getMessage());
        }
        
        chain.doFilter(request, response);
    }
    
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
```

#### 4.4 认证接口

**位置**: `blog-web/src/main/java/com/mulehang/blog/controller/api/v1/`

```java
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "认证管理")
public class AuthController {
    
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        // 1. 验证用户名密码
        // 2. 生成 Token
        // 3. 返回用户信息 + Token
    }
    
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        // 1. 校验用户名/邮箱唯一性
        // 2. 密码加密
        // 3. 创建用户
        // 4. 分配默认角色
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "刷新Token")
    public Result<TokenVO> refresh(@RequestHeader("X-Refresh-Token") String refreshToken) {
        // 1. 验证 refresh token
        // 2. 生成新的 access token
    }
    
    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public Result<Void> logout() {
        // 可选：将 Token 加入黑名单
    }
}
```

#### 4.5 敏感词过滤

**位置**: `blog-core/src/main/java/com/mulehang/blog/security/`

```java
@Component
public class SensitiveWordService {
    private final SensitiveWordBs sensitiveWordBs;
    
    public SensitiveWordService() {
        // 初始化敏感词库
        this.sensitiveWordBs = SensitiveWordBs.newInstance()
            .ignoreCase(true)           // 忽略大小写
            .ignoreWidth(true)          // 忽略全半角
            .ignoreNumStyle(true)       // 忽略数字样式
            .ignoreChineseStyle(true)   // 忽略中文样式
            .ignoreEnglishStyle(true)   // 忽略英文样式
            .ignoreRepeat(true)         // 忽略重复字符
            .enableWordCheck(true)      // 启用词校验
            .init();
    }
    
    /**
     * 检查是否包含敏感词
     */
    public boolean contains(String text) {
        return sensitiveWordBs.contains(text);
    }
    
    /**
     * 获取所有敏感词
     */
    public List<String> findAll(String text) {
        return sensitiveWordBs.findAll(text);
    }
    
    /**
     * 替换敏感词为 ***
     */
    public String replace(String text) {
        return sensitiveWordBs.replace(text);
    }
    
    /**
     * 替换敏感词为指定字符
     */
    public String replace(String text, char replacement) {
        return sensitiveWordBs.replace(text, replacement);
    }
}
```

**使用示例**：

```java
@Service
public class CommentServiceImpl implements CommentService {
    private final SensitiveWordService sensitiveWordService;
    
    public Long createComment(CommentCreateDTO dto) {
        // 敏感词检查
        if (sensitiveWordService.contains(dto.getContent())) {
            List<String> words = sensitiveWordService.findAll(dto.getContent());
            throw new BusinessException("评论包含敏感词: " + words);
        }
        
        // 或者自动替换
        String safeContent = sensitiveWordService.replace(dto.getContent());
        // ...
    }
}
```

#### 4.6 IP 归属地解析

**位置**: `blog-core/src/main/java/com/mulehang/blog/util/`

```java
@Component
public class IpRegionService {
    private Searcher searcher;
    
    @PostConstruct
    public void init() throws Exception {
        // 加载 ip2region.xdb 文件
        String dbPath = "ip2region.xdb";
        byte[] buff = Searcher.loadContentFromFile(dbPath);
        this.searcher = Searcher.newWithBuffer(buff);
    }
    
    /**
     * 解析 IP 归属地
     * @return 格式：国家|区域|省份|城市|ISP
     */
    public String getRegion(String ip) {
        try {
            return searcher.search(ip);
        } catch (Exception e) {
            return "未知";
        }
    }
    
    /**
     * 获取简短归属地（省份 城市）
     */
    public String getShortRegion(String ip) {
        String region = getRegion(ip);
        String[] parts = region.split("\\|");
        if (parts.length >= 4) {
            String province = parts[2];
            String city = parts[3];
            if ("0".equals(city)) {
                return province;
            }
            return province + " " + city;
        }
        return region;
    }
}
```

#### 4.7 自定义 Actuator 指标

**位置**: `blog-web/src/main/java/com/mulehang/blog/metrics/`

```java
@Component
public class BlogMetrics {
    private final MeterRegistry registry;
    
    private final Counter articlePublishCounter;
    private final Counter commentCounter;
    private final AtomicLong activeUsers;
    
    public BlogMetrics(MeterRegistry registry) {
        this.registry = registry;
        
        // 文章发布计数器
        this.articlePublishCounter = Counter.builder("blog.article.publish.total")
            .description("Total number of articles published")
            .register(registry);
        
        // 评论计数器
        this.commentCounter = Counter.builder("blog.comment.total")
            .description("Total number of comments")
            .register(registry);
        
        // 活跃用户数（Gauge）
        this.activeUsers = new AtomicLong(0);
        Gauge.builder("blog.users.active", activeUsers, AtomicLong::get)
            .description("Number of active users")
            .register(registry);
    }
    
    public void incrementArticlePublish() {
        articlePublishCounter.increment();
    }
    
    public void incrementComment() {
        commentCounter.increment();
    }
    
    public void setActiveUsers(long count) {
        activeUsers.set(count);
    }
}
```

#### 4.8 健康检查端点

```java
@Component
public class ElasticsearchHealthIndicator implements HealthIndicator {
    private final ElasticsearchClient esClient;
    
    @Override
    public Health health() {
        try {
            boolean ping = esClient.ping().value();
            if (ping) {
                return Health.up()
                    .withDetail("cluster", "connected")
                    .build();
            } else {
                return Health.down()
                    .withDetail("error", "Ping failed")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

### ✅ 完成标准

- [ ] JWT 登录/注册功能正常
- [ ] Token 刷新机制可用
- [ ] 权限控制生效（Admin/User）
- [ ] 敏感词过滤功能正常
- [ ] IP 归属地解析正确
- [ ] Prometheus 指标可采集
- [ ] `/actuator/prometheus` 返回指标数据

---

## Milestone 5：AI与内容生产（持续迭代）

### 🎯 学习目标

| 技术栈 | 学习要点 |
|--------|----------|
| AI SDK 封装 | 多模型统一接口设计 |
| Sentinel | 限流、熔断、降级 |
| WebSocket | 实时推送、流式响应 |
| 异步流处理 | SSE、Flux 响应式 |

### 📋 任务清单

#### 5.1 AI 服务抽象层设计

**位置**: `blog-core/src/main/java/com/mulehang/blog/ai/`

```
目录结构：
├── AiService.java                    # 统一接口
├── AiServiceFactory.java             # 工厂类
├── model/
│   ├── AiRequest.java
│   ├── AiResponse.java
│   └── AiConfig.java
├── provider/
│   ├── OpenAiProvider.java           # OpenAI/ChatGPT
│   ├── ZhipuAiProvider.java          # 智谱 GLM
│   └── QwenAiProvider.java           # 通义千问
└── exception/
    └── AiServiceException.java
```

**统一接口设计**：

```java
public interface AiService {
    /**
     * 同步调用
     */
    AiResponse chat(AiRequest request);
    
    /**
     * 流式调用（SSE）
     */
    Flux<String> chatStream(AiRequest request);
    
    /**
     * 生成文章摘要
     */
    String generateSummary(String content, int maxLength);
    
    /**
     * 生成标题建议
     */
    List<String> suggestTitles(String content, int count);
    
    /**
     * 生成标签建议
     */
    List<String> suggestTags(String content, int count);
}
```

#### 5.2 OpenAI Provider 实现

```java
@Service
@ConditionalOnProperty(name = "ai.provider", havingValue = "openai")
public class OpenAiProvider implements AiService {
    
    @Value("${ai.openai.api-key}")
    private String apiKey;
    
    @Value("${ai.openai.base-url:https://api.openai.com}")
    private String baseUrl;
    
    @Value("${ai.openai.model:gpt-3.5-turbo}")
    private String model;
    
    private final WebClient webClient;
    
    @Override
    public AiResponse chat(AiRequest request) {
        // 构建请求体
        Map<String, Object> body = Map.of(
            "model", model,
            "messages", request.getMessages(),
            "temperature", request.getTemperature(),
            "max_tokens", request.getMaxTokens()
        );
        
        // 调用 API
        return webClient.post()
            .uri(baseUrl + "/v1/chat/completions")
            .header("Authorization", "Bearer " + apiKey)
            .bodyValue(body)
            .retrieve()
            .bodyToMono(AiResponse.class)
            .block();
    }
    
    @Override
    public Flux<String> chatStream(AiRequest request) {
        Map<String, Object> body = Map.of(
            "model", model,
            "messages", request.getMessages(),
            "stream", true
        );
        
        return webClient.post()
            .uri(baseUrl + "/v1/chat/completions")
            .header("Authorization", "Bearer " + apiKey)
            .bodyValue(body)
            .retrieve()
            .bodyToFlux(String.class)
            .map(this::parseStreamChunk);
    }
    
    @Override
    public String generateSummary(String content, int maxLength) {
        String prompt = String.format(
            "请为以下文章生成一段不超过%d字的摘要：\n\n%s",
            maxLength, content
        );
        
        AiRequest request = AiRequest.builder()
            .messages(List.of(new Message("user", prompt)))
            .maxTokens(maxLength * 2)
            .build();
        
        return chat(request).getContent();
    }
}
```

#### 5.3 Sentinel 限流配置

**添加依赖**: `blog-web/pom.xml`

```xml
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-core</artifactId>
    <version>1.8.7</version>
</dependency>
<dependency>
    <groupId>com.alibaba.csp</groupId>
    <artifactId>sentinel-annotation-aspectj</artifactId>
    <version>1.8.7</version>
</dependency>
```

**配置类**：

```java
@Configuration
public class SentinelConfig {
    
    @PostConstruct
    public void initRules() {
        // AI 接口限流规则
        FlowRule aiRule = new FlowRule();
        aiRule.setResource("ai-chat");
        aiRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        aiRule.setCount(10);  // 每秒10次
        aiRule.setLimitApp("default");
        
        // 搜索接口限流规则
        FlowRule searchRule = new FlowRule();
        searchRule.setResource("article-search");
        searchRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        searchRule.setCount(50);  // 每秒50次
        
        FlowRuleManager.loadRules(Arrays.asList(aiRule, searchRule));
        
        // 熔断规则
        DegradeRule degradeRule = new DegradeRule();
        degradeRule.setResource("ai-chat");
        degradeRule.setGrade(CircuitBreakerStrategy.ERROR_RATIO.getType());
        degradeRule.setCount(0.5);  // 错误率50%
        degradeRule.setTimeWindow(30);  // 熔断30秒
        degradeRule.setMinRequestAmount(10);
        
        DegradeRuleManager.loadRules(Collections.singletonList(degradeRule));
    }
}
```

**使用注解**：

```java
@RestController
@RequestMapping("/api/v1/ai")
public class AiController {
    
    @PostMapping("/chat")
    @SentinelResource(value = "ai-chat", 
                      blockHandler = "chatBlockHandler",
                      fallback = "chatFallback")
    public Result<String> chat(@RequestBody AiChatDTO dto) {
        return Result.success(aiService.chat(dto.toRequest()).getContent());
    }
    
    // 限流处理
    public Result<String> chatBlockHandler(AiChatDTO dto, BlockException e) {
        return Result.fail(429, "请求过于频繁，请稍后再试");
    }
    
    // 降级处理
    public Result<String> chatFallback(AiChatDTO dto, Throwable e) {
        return Result.fail(503, "AI 服务暂时不可用，请稍后再试");
    }
}
```

#### 5.4 WebSocket 实时通知

**位置**: `blog-service/src/main/java/com/mulehang/blog/websocket/`

```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(notificationHandler(), "/ws/notifications")
                .setAllowedOrigins("*")
                .addInterceptors(new JwtHandshakeInterceptor());
    }
    
    @Bean
    public NotificationWebSocketHandler notificationHandler() {
        return new NotificationWebSocketHandler();
    }
}
```

```java
@Component
@Slf4j
public class NotificationWebSocketHandler extends TextWebSocketHandler {
    
    // 用户ID -> Session 映射
    private final ConcurrentHashMap<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            sessions.put(userId, session);
            log.info("WebSocket 连接建立: userId={}", userId);
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            sessions.remove(userId);
            log.info("WebSocket 连接关闭: userId={}", userId);
        }
    }
    
    /**
     * 向指定用户推送消息
     */
    public void sendToUser(Long userId, NotificationMessage message) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
            } catch (Exception e) {
                log.error("发送 WebSocket 消息失败", e);
            }
        }
    }
    
    /**
     * 广播消息给所有在线用户
     */
    public void broadcast(NotificationMessage message) {
        String json = objectMapper.writeValueAsString(message);
        TextMessage textMessage = new TextMessage(json);
        
        sessions.values().parallelStream()
            .filter(WebSocketSession::isOpen)
            .forEach(session -> {
                try {
                    session.sendMessage(textMessage);
                } catch (Exception e) {
                    log.error("广播消息失败", e);
                }
            });
    }
}
```

#### 5.5 AI 写作助手功能

```java
@Service
public class AiWritingAssistant {
    private final AiService aiService;
    
    /**
     * 生成文章大纲
     */
    public List<String> generateOutline(String topic) {
        String prompt = "请为主题\"" + topic + "\"生成一个详细的文章大纲，用换行分隔每个要点：";
        AiResponse response = aiService.chat(AiRequest.of(prompt));
        return Arrays.asList(response.getContent().split("\n"));
    }
    
    /**
     * 续写文章
     */
    public Flux<String> continueWriting(String existingContent) {
        String prompt = "请继续完成以下文章：\n\n" + existingContent;
        return aiService.chatStream(AiRequest.of(prompt));
    }
    
    /**
     * 润色文章
     */
    public String polish(String content) {
        String prompt = "请对以下文章进行润色，提升文字质量，但保持原意：\n\n" + content;
        return aiService.chat(AiRequest.of(prompt)).getContent();
    }
    
    /**
     * 翻译
     */
    public String translate(String content, String targetLanguage) {
        String prompt = String.format("请将以下内容翻译成%s：\n\n%s", targetLanguage, content);
        return aiService.chat(AiRequest.of(prompt)).getContent();
    }
}
```

#### 5.6 SSE 流式响应接口

```java
@RestController
@RequestMapping("/api/v1/ai")
public class AiStreamController {
    
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatStream(@RequestParam String prompt) {
        return aiService.chatStream(AiRequest.of(prompt))
            .map(chunk -> ServerSentEvent.<String>builder()
                .data(chunk)
                .build())
            .concatWith(Flux.just(ServerSentEvent.<String>builder()
                .event("done")
                .data("[DONE]")
                .build()));
    }
}
```

### ✅ 完成标准

- [ ] AI 服务抽象层可切换 Provider
- [ ] AI 摘要生成功能正常
- [ ] AI 标题/标签建议功能正常
- [ ] Sentinel 限流生效
- [ ] 熔断降级正常工作
- [ ] WebSocket 连接稳定
- [ ] 流式响应正常输出

---

## Milestone 6：部署与运维

### 🎯 学习目标

| 技术栈 | 学习要点 |
|--------|----------|
| Docker | Dockerfile 编写、镜像构建 |
| Docker Compose | 多容器编排 |
| Nginx | 反向代理、静态资源、HTTPS |
| GitHub Actions | CI/CD 流水线 |

### 📋 任务清单

#### 6.1 编写 Dockerfile

**位置**: 项目根目录 `Dockerfile`

```dockerfile
# 多阶段构建
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# 复制 Maven 配置
COPY pom.xml .
COPY blog-api/pom.xml blog-api/
COPY blog-core/pom.xml blog-core/
COPY blog-service/pom.xml blog-service/
COPY blog-ui/pom.xml blog-ui/
COPY blog-web/pom.xml blog-web/

# 下载依赖（利用缓存）
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B

# 复制源代码
COPY . .

# 构建
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# 创建非 root 用户
RUN addgroup -g 1000 blog && \
    adduser -u 1000 -G blog -D blog

# 复制构建产物
COPY --from=builder /app/blog-web/target/blog-web-*.jar app.jar

# 设置权限
RUN chown -R blog:blog /app
USER blog

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

# 暴露端口
EXPOSE 8080

# JVM 参数
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

#### 6.2 完善 Docker Compose

**位置**: `compose.yaml`

```yaml
version: '3.8'

services:
  # 应用服务
  blog-app:
    build:
      context: .
      dockerfile: Dockerfile
    container_name: blog-app
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/mulehang_blog?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
      - SPRING_DATASOURCE_USERNAME=blog
      - SPRING_DATASOURCE_PASSWORD=${MYSQL_PASSWORD}
      - SPRING_DATA_REDIS_HOST=redis
      - SPRING_DATA_REDIS_PASSWORD=${REDIS_PASSWORD}
      - SPRING_RABBITMQ_HOST=rabbitmq
      - SPRING_RABBITMQ_USERNAME=${RABBITMQ_USER}
      - SPRING_RABBITMQ_PASSWORD=${RABBITMQ_PASSWORD}
      - ELASTICSEARCH_URIS=http://elasticsearch:9200
      - JWT_SECRET=${JWT_SECRET}
    depends_on:
      mysql:
        condition: service_healthy
      redis:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
    networks:
      - blog-network

  # MySQL
  mysql:
    image: mysql:8.0
    container_name: blog-mysql
    restart: unless-stopped
    ports:
      - "3306:3306"
    environment:
      - MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD}
      - MYSQL_DATABASE=mulehang_blog
      - MYSQL_USER=blog
      - MYSQL_PASSWORD=${MYSQL_PASSWORD}
    volumes:
      - mysql-data:/var/lib/mysql
      - ./docker/mysql/conf.d:/etc/mysql/conf.d
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - blog-network

  # Redis
  redis:
    image: redis:7-alpine
    container_name: blog-redis
    restart: unless-stopped
    ports:
      - "6379:6379"
    command: redis-server --requirepass ${REDIS_PASSWORD} --appendonly yes
    volumes:
      - redis-data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "-a", "${REDIS_PASSWORD}", "ping"]
      interval: 10s
      timeout: 5s
      retries: 5
    networks:
      - blog-network

  # RabbitMQ
  rabbitmq:
    image: rabbitmq:3-management-alpine
    container_name: blog-rabbitmq
    restart: unless-stopped
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      - RABBITMQ_DEFAULT_USER=${RABBITMQ_USER}
      - RABBITMQ_DEFAULT_PASS=${RABBITMQ_PASSWORD}
    volumes:
      - rabbitmq-data:/var/lib/rabbitmq
    healthcheck:
      test: ["CMD", "rabbitmq-diagnostics", "check_running"]
      interval: 30s
      timeout: 10s
      retries: 5
    networks:
      - blog-network

  # Elasticsearch
  elasticsearch:
    image: elasticsearch:8.11.0
    container_name: blog-elasticsearch
    restart: unless-stopped
    ports:
      - "9200:9200"
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
    volumes:
      - es-data:/usr/share/elasticsearch/data
    healthcheck:
      test: ["CMD-SHELL", "curl -s http://localhost:9200/_cluster/health | grep -q 'green\\|yellow'"]
      interval: 30s
      timeout: 10s
      retries: 5
    networks:
      - blog-network

  # Nginx
  nginx:
    image: nginx:alpine
    container_name: blog-nginx
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./docker/nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./docker/nginx/conf.d:/etc/nginx/conf.d:ro
      - ./docker/nginx/ssl:/etc/nginx/ssl:ro
      - ./docker/nginx/html:/usr/share/nginx/html:ro
    depends_on:
      - blog-app
    networks:
      - blog-network

  # Prometheus
  prometheus:
    image: prom/prometheus:latest
    container_name: blog-prometheus
    restart: unless-stopped
    ports:
      - "9090:9090"
    volumes:
      - ./docker/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - prometheus-data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
    networks:
      - blog-network

  # Grafana
  grafana:
    image: grafana/grafana:latest
    container_name: blog-grafana
    restart: unless-stopped
    ports:
      - "3000:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_PASSWORD}
    volumes:
      - grafana-data:/var/lib/grafana
      - ./docker/grafana/provisioning:/etc/grafana/provisioning:ro
    depends_on:
      - prometheus
    networks:
      - blog-network

volumes:
  mysql-data:
  redis-data:
  rabbitmq-data:
  es-data:
  prometheus-data:
  grafana-data:

networks:
  blog-network:
    driver: bridge
```

#### 6.3 Nginx 配置

**位置**: `docker/nginx/conf.d/blog.conf`

```nginx
upstream blog_backend {
    server blog-app:8080;
    keepalive 32;
}

server {
    listen 80;
    server_name your-domain.com www.your-domain.com;
    
    # HTTP 重定向到 HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com www.your-domain.com;
    
    # SSL 配置
    ssl_certificate /etc/nginx/ssl/fullchain.pem;
    ssl_certificate_key /etc/nginx/ssl/privkey.pem;
    ssl_session_timeout 1d;
    ssl_session_cache shared:SSL:50m;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256;
    ssl_prefer_server_ciphers off;
    
    # HSTS
    add_header Strict-Transport-Security "max-age=63072000" always;
    
    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2)$ {
        root /usr/share/nginx/html;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
    
    # API 代理
    location /api/ {
        proxy_pass http://blog_backend;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    # WebSocket
    location /ws/ {
        proxy_pass http://blog_backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_read_timeout 3600s;
    }
    
    # 页面渲染（Thymeleaf）
    location / {
        proxy_pass http://blog_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # Swagger 文档
    location /doc.html {
        proxy_pass http://blog_backend;
        proxy_set_header Host $host;
    }
    
    # 健康检查
    location /health {
        proxy_pass http://blog_backend/actuator/health;
    }
    
    # 错误页面
    error_page 500 502 503 504 /50x.html;
    location = /50x.html {
        root /usr/share/nginx/html;
    }
}
```

#### 6.4 Prometheus 配置

**位置**: `docker/prometheus/prometheus.yml`

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

scrape_configs:
  - job_name: 'blog-app'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['blog-app:8080']
    relabel_configs:
      - source_labels: [__address__]
        target_label: instance
        replacement: 'blog-app'

  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']
```

#### 6.5 GitHub Actions CI/CD

**位置**: `.github/workflows/ci.yml`

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven
      
      - name: Build with Maven
        run: mvn clean verify -B
      
      - name: Upload test results
        uses: actions/upload-artifact@v4
        if: always()
        with:
          name: test-results
          path: '**/target/surefire-reports/*.xml'

  docker:
    needs: build
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    permissions:
      contents: read
      packages: write
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Log in to Container Registry
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}
      
      - name: Extract metadata
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
          tags: |
            type=sha,prefix=
            type=raw,value=latest
      
      - name: Build and push Docker image
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          labels: ${{ steps.meta.outputs.labels }}

  deploy:
    needs: docker
    runs-on: ubuntu-latest
    if: github.ref == 'refs/heads/main'
    
    steps:
      - name: Deploy to server
        uses: appleboy/ssh-action@v1.0.0
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SERVER_SSH_KEY }}
          script: |
            cd /opt/blog
            docker compose pull blog-app
            docker compose up -d blog-app
            docker image prune -f
```

#### 6.6 环境变量文件

**位置**: `.env.example`

```bash
# MySQL
MYSQL_ROOT_PASSWORD=your_root_password
MYSQL_PASSWORD=your_blog_password

# Redis
REDIS_PASSWORD=your_redis_password

# RabbitMQ
RABBITMQ_USER=blog
RABBITMQ_PASSWORD=your_rabbitmq_password

# JWT
JWT_SECRET=your-256-bit-secret-key-here-must-be-long-enough

# Grafana
GRAFANA_PASSWORD=your_grafana_password

# AI (可选)
AI_PROVIDER=openai
AI_OPENAI_API_KEY=sk-xxx
```

### ✅ 完成标准

- [ ] Docker 镜像构建成功
- [ ] `docker compose up` 一键启动所有服务
- [ ] Nginx 反向代理正常
- [ ] HTTPS 证书配置正确
- [ ] Prometheus 采集指标正常
- [ ] Grafana 仪表盘可访问
- [ ] CI/CD 流水线正常运行

---

## 附录

### A. 技术栈版本参考

| 组件 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.3.7 | LTS |
| Java | 21 | LTS |
| MySQL | 8.0+ | |
| Redis | 7.x | |
| RabbitMQ | 3.11+ | |
| Elasticsearch | 8.x | |
| MyBatis-Plus | 3.5.12 | |
| Redisson | 3.50.0 | |

### B. 推荐学习资源

1. **Spring Boot 3 官方文档**: https://docs.spring.io/spring-boot/docs/current/reference/html/
2. **MyBatis-Plus 官方文档**: https://baomidou.com/
3. **Elasticsearch 8.x 指南**: https://www.elastic.co/guide/en/elasticsearch/reference/current/
4. **Spring Security 6**: https://docs.spring.io/spring-security/reference/
5. **Docker 最佳实践**: https://docs.docker.com/develop/develop-images/dockerfile_best-practices/

### C. 常见问题排查

1. **MySQL 连接失败**: 检查 `allowPublicKeyRetrieval=true` 参数
2. **Redis 连接超时**: 检查密码配置和网络
3. **ES 启动失败**: 检查内存配置 `ES_JAVA_OPTS`
4. **JWT 验证失败**: 检查 secret 长度（至少 256 位）
5. **跨域问题**: 检查 CORS 和 Security 配置

---

*文档版本: 1.0.0 | 最后更新: 2025-12-30*
