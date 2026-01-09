# 个人博客（Spring Boot 3）学习型技术栈全景复刻计划（基于 paicoding）

> 目标：做一个**个人博客网站**，尽可能覆盖/复刻本仓库（paicoding）中出现的技术栈与工程实践，用“做项目”的方式系统学习。
>
> 唯一明确差异：你的博客后端使用 **Spring Boot 3.x**（建议 3.2/3.3 LTS 线），因此会涉及 **Jakarta / Spring Security / OpenAPI** 等迁移。

---

## 1. paicoding 当前技术栈盘点（基于依赖与配置的“实锤”）

本节以实际 `pom.xml` 和 `application*.yml` 为准；另外把 README/文档里提到但代码未直接引入的内容单独标注为“文档提及”。

### 1.1 语言与构建

- **Maven 多模块**：根 `pom.xml` 聚合
  - 模块：`paicoding-api` / `paicoding-core` / `paicoding-service` / `paicoding-web` / `paicoding-ui`
- **Spring Boot 2.7.1**：根 `pom.xml` parent
- **Java 8**：根与部分模块 `maven.compiler.source/target` 为 1.8/8

> 你的博客升级到 Spring Boot 3 后：建议直接用 **Java 17**（Spring Boot 3 的基线要求）。

### 1.2 Web / MVC / 模板 / 长连接

- `spring-boot-starter-web`
- `spring-boot-starter-websocket`
- **Thymeleaf**：`spring-boot-starter-thymeleaf`（在 `paicoding-ui`）
- Jackson：`jackson-databind` + `jackson-dataformat-xml`

### 1.3 数据库与 ORM

- **MySQL**：`mysql-connector-java`
- **MyBatis-Plus**：`mybatis-plus-boot-starter`（管理版本 3.5.2）
- MyBatis 基础依赖：`mybatis`、`mybatis-spring`
- 连接池：
  - **HikariCP**（依赖存在）
  - **Druid**（`provided`，有配置痕迹）
- 配置里出现“动态数据源”结构（见 `application-dal.yml`），并且主配置开启了 `allow-bean-definition-overriding` 支持自定义 datasource 覆盖。

### 1.4 数据库变更管理

- **Liquibase**：`liquibase-core` + `paicoding-web/src/main/resources/liquibase/master.xml`

### 1.5 缓存（本地 + Redis）

- Spring Cache：`spring-boot-starter-cache`
- **Caffeine**：本地缓存
- **Redis**：`spring-boot-starter-data-redis`
- **Redisson**：`org.redisson:redisson`

### 1.6 搜索

- **Elasticsearch 6.8.2**：`elasticsearch-rest-high-level-client` 等依赖在 `paicoding-service`
- 配置：`application-dal.yml` 有 `elasticsearch.open/hosts/scheme` 等

### 1.7 消息队列

- RabbitMQ：依赖为 `com.rabbitmq:amqp-client`（注意：**不是** `spring-boot-starter-amqp`）
- 配置：`application-rabbitmq.yml` 自定义 `rabbitmq.*` 连接信息 + 开关

### 1.8 安全与认证

- JWT：`com.auth0:java-jwt`
- 密码学：`spring-security-crypto`
- 配置里自定义了 `security.salt` 等密码加盐策略

> 注意：`paicoding-core` 里出现 `spring-security-core:6.3.0`（与 Spring Boot 2.7 的生态并不一致）。你做 Spring Boot 3 版博客时，反而能让 Spring Security 6.x “名正言顺”。

### 1.9 API 文档

- **Knife4j OpenAPI2**：`knife4j-openapi2-spring-boot-starter`

> Spring Boot 3 建议走 **OpenAPI 3（springdoc-openapi）**，Knife4j 也有对应 OpenAPI3 方案。

### 1.10 可观测性（监控/指标）

- `spring-boot-starter-actuator`
- **Micrometer Prometheus**：`micrometer-registry-prometheus`
- `management.*` 相关配置在 `application.yml`

### 1.11 邮件

- `spring-boot-starter-mail`
- 配置：`application-email.yml`

### 1.12 AI 能力

从依赖与配置看，AI 集成是项目的“重点学习方向”之一：

- `com.github.plexpt:chatgpt`
- `cn.bigmodel.openapi:oapi-java-sdk`（智谱）
- `com.alibaba:dashscope-sdk-java`（通义）
- `com.volcengine:volcengine-java-sdk-ark-runtime`（豆包/火山）
- 配置：`application-ai.yml`（代理、模型源、key、timeout、上下文条数）

### 1.13 对象存储 / 上传 / 图片

- 阿里云 OSS：`aliyun-sdk-oss`
- 配置：`application-image.yml`（local/oss、路径、host、静态资源映射）

### 1.14 支付与第三方

- 微信支付：`wechatpay-java`
- 配置：`application-pay.yml`
- 登录配置：`application-login.yml`（微信/知识星球）

### 1.15 其他工程能力/工具箱（大量可以“做成练习点”）

- AOP：`aspectjweaver`
- TTL：`transmittable-thread-local`
- 限流/熔断：`sentinel-core`、`sentinel-transport-simple-http`
- Markdown 渲染：`flexmark-all`
- 敏感词：`com.github.houbb:sensitive-word`
- IP 归属：`ip2region`
- MapStruct：`mapstruct` / `mapstruct-processor`
- 二维码：`qrcode-plugin`
- Excel：`fastexcel`
- PDF：`itextpdf`
- 序列化：`kryo`
- 爬虫/HTML 解析：`jsoup`
- 图片/多媒体：`opencv`、`webp-imageio`（test）
- 高性能队列/并发：`disruptor`
- 内存布局分析：`jol-core`
- 汉字转拼音：`pinyin4j`
- 工具类：Guava、Hutool、Apache Commons（io/lang3/collections4/text）、OGNL

### 1.16 “文档提及但本仓库未直接引入（或未检索到依赖）”

以下在 README/页面文案里出现，但在依赖检索中未发现对应 starter/driver（可能是外部化/另一个分支/历史遗留）：

- **MongoDB**：README 提及，但未检索到 `spring-boot-starter-data-mongodb` 等依赖
- **Docker**：README 提及，仓库中未找到项目级 Dockerfile / docker-compose（UI 静态资源里有 codemirror 的 Dockerfile 语法高亮，不代表部署）
- **Nginx / HTTPS**：README 提及，属于部署层组件，本仓库主要提供应用代码

---

## 2. 你的个人博客：用这些技术栈“学得动”的落地架构

为了尽可能复用 paicoding 的学习收益，建议你的博客也保持类似分层（不要求功能做得一样大，但结构要像）：

### 2.1 建议模块结构（与 paicoding 对齐）

> 你可以从单模块起步，稳定后再拆；但如果目标是学习工程化，多模块更贴近真实项目。

- `blog-api`：DO/DTO/VO、枚举、通用响应
- `blog-core`：通用组件（缓存、搜索、MQ、工具类、AOP、AI SDK 封装）
- `blog-service`：业务层（文章、评论、用户、统计、审核、搜索同步等）
- `blog-web`：Controller、鉴权、全局异常、Swagger/OpenAPI、Actuator
- `blog-ui`：Thymeleaf 模板 + 静态资源（EditorMD/Markdown 编辑器等）

### 2.2 功能模块设计（每个模块都绑定“技术栈练习点”）

#### A. 内容系统（博客核心）

- 文章（Markdown）：发布/草稿/版本历史
  - Markdown 转 HTML：flexmark
  - 版本历史（建议补齐 MongoDB）：文章每次保存写入 MongoDB（revision collection），MySQL 存当前发布版本
- 分类/标签
- 专栏/系列文章
- 站内搜索
  - Elasticsearch：文章标题、摘要、正文分词检索
  - ES 同步：
    - 方案 1：DB 写入后发 MQ → 异步更新 ES
    - 方案 2：定时任务补偿（防丢）

#### B. 用户与权限

- 登录：JWT（java-jwt）
- 密码：Spring Security Crypto（BCrypt + 自定义 salt 策略对比学习）
- 权限：最小 RBAC（管理员/作者/访客）

#### C. 评论与互动

- 评论/回复
- WebSocket：
  - 新评论实时通知作者
  - 后台审核通知

#### D. 缓存与性能

- 多级缓存：Caffeine（本地） + Redis（分布式）
- Redisson：
  - 分布式锁（防止重复提交/重复点赞）
  - 延迟队列（如：延迟发送邮件/延迟刷新统计）
- Kryo：练习“高性能序列化”缓存对象
- Disruptor：可用于“写日志/统计事件”高吞吐队列（也可仅做实验模块）

#### E. 异步化（MQ）

- RabbitMQ：
  - 文章发布 → 异步生成摘要/同步 ES
  - 评论发布 → 异步邮件通知
  - 图片上传 → 异步生成缩略图（opencv）

#### F. 站点运营能力（统计/报表/导出）

- PV/UV、文章阅读数：
  - Redis ZSET 排行榜（热门文章/作者）
- 导出：
  - Excel（fastexcel）导出文章列表、访问统计
  - PDF（itextpdf）导出文章为 PDF
- IP 归属地：ip2region（在评论/访客记录上展示）

#### G. 内容安全与审核

- 敏感词过滤：sensitive-word（在文章/评论保存前）
- Sentinel：
  - 对“发布文章/AI生成/搜索接口”做限流
  - 熔断保护外部 AI 调用

#### H. AI（把它做成你博客的特色）

- AI 写作助手：
  - 自动生成标题候选
  - 生成文章摘要
  - 生成标签建议
- AI 问答：对你的文章库进行问答（先做“标题/摘要检索 + LLM 生成”，再升级向量检索）

#### I. 文件/图片

- 本地上传（开发环境） + OSS（生产环境）
- opencv：
  - 缩略图裁剪/压缩
  - 简单的图片水印（博客 Logo）
- qrcode-plugin：
  - 生成文章分享二维码

#### J. 可观测性与运维

- Actuator + Prometheus：
  - JVM、HTTP、DB、Redis 指标
  - 自定义业务指标（文章发布量/评论量/AI调用量）
- 日志与告警：
  - 关键异常邮件告警（spring-boot-starter-mail）

#### K. 部署（补齐文档提及的 Docker/Nginx/HTTPS）

- Docker：
  - blog-web 打包成镜像
  - MySQL/Redis/ES/RabbitMQ/MongoDB 用 docker-compose 一键拉起
- Nginx：
  - 反向代理 + 静态资源缓存
- HTTPS：
  - 本地自签/生产 Let’s Encrypt（练习完整链路）

---

## 3. Spring Boot 3 与 paicoding（Spring Boot 2.7）的关键差异与迁移要点

你的博客直接用 Spring Boot 3，可以避免未来二次迁移。下面列出“最容易踩坑且必须掌握”的点。

### 3.1 基线版本升级

- Spring Boot 3.x → **Java 17+**
- Spring Framework 6.x
- 依赖生态整体升级（不少库的 groupId/artifactId/包名会变化）

### 3.2 javax → jakarta（最核心）

- Servlet API：`javax.servlet.*` → `jakarta.servlet.*`
- Validation：`javax.validation.*` → `jakarta.validation.*`
- 其他 Java EE 相关包名类似迁移

### 3.3 Spring Security 迁移（配置方式变化很大）

在 Spring Security 6：

- `WebSecurityConfigurerAdapter` 已移除 → 使用 `SecurityFilterChain` Bean
- `antMatchers` 等旧写法变更
- 方法安全、CSRF、CORS 等配置方式不同

### 3.4 OpenAPI/Swagger 迁移

- paicoding 使用 `knife4j-openapi2`（Swagger2 体系）
- Spring Boot 3 建议：
  - `org.springdoc:springdoc-openapi-starter-webmvc-ui`
  - 若坚持 Knife4j：使用 Knife4j 的 OpenAPI3 方案（与 springdoc 集成）

### 3.5 MyBatis-Plus / MySQL Driver

- MyBatis-Plus 需要使用支持 Spring Boot 3 的版本（通常 3.5.3+，以官方兼容声明为准）
- MySQL 驱动：建议 `com.mysql:mysql-connector-j`（新坐标/版本）

### 3.6 Elasticsearch 客户端建议更新

paicoding 目前是 ES 6.8 的 RestHighLevelClient（已被官方弃用）。你的博客建议直接学“新体系”：

- 如果用 Elasticsearch 8：使用官方 **Java API Client**（co.elastic.clients）
- 或者用 OpenSearch：对应客户端

> 学习建议：先把“检索能力”做出来，再逐步替换客户端，不要一上来就被版本兼容卡住。

---

## 4. 推荐的“按周/里程碑”学习路线（把技术栈按依赖关系拆开）

### Milestone 0：工程骨架（1~2 天）

- 建 Spring Boot 3 多模块骨架
- 接入 Thymeleaf（或先做纯后端 API 也行）
- 接入 Liquibase 初始化数据库

### Milestone 1：文章发布链路（3~7 天）

- MySQL + MyBatis-Plus：文章 CRUD
- Markdown → HTML 渲染
- 文件上传（本地）

### Milestone 2：缓存与热点数据（3~7 天）

- Caffeine + Redis 多级缓存
- Redis ZSET 热门文章榜
- Redisson 分布式锁（防重复发布/重复点赞）

### Milestone 3：搜索与异步（1~2 周）

- Elasticsearch 建索引 + 搜索接口
- RabbitMQ：文章发布事件 → 异步更新 ES
- 定时任务做补偿（你可以自己实现一个简单 Scheduler，也可以用 Spring Scheduling）

### Milestone 4：安全与运营（1~2 周）

- JWT 登录
- 权限（管理员/作者）
- Actuator + Prometheus 指标
- 邮件通知

### Milestone 5：AI 与内容生产（持续迭代）

- AI 摘要、AI 标题、AI 标签
- Sentinel 限流保护 AI 接口
- MongoDB：存 AI 对话历史/文章修订版

---

## 5. 你在博客里“复刻技术栈”的对照表（速查）

| 技术栈 | paicoding 现状 | 你博客建议怎么用 | 学习产出（可写进简历/博客） |
|---|---|---|---|
| Spring Boot | 2.7.1 | 3.2/3.3 | Jakarta 迁移实践、现代化依赖管理 |
| MyBatis-Plus + MySQL | ✅ | ✅ | 文章/评论/用户等核心表设计 |
| Liquibase | ✅ | ✅ | 版本化 schema 变更流程 |
| Redis + Caffeine | ✅ | ✅ | 多级缓存、一致性策略 |
| Redisson | ✅ | ✅ | 分布式锁/延迟队列 |
| RabbitMQ | ✅（原生 amqp-client） | ✅（建议用 Spring AMQP 或继续原生） | 异步解耦、削峰填谷 |
| Elasticsearch | ✅（6.8 客户端） | ✅（建议 8.x 新客户端） | 搜索、索引设计、同步策略 |
| WebSocket | ✅ | ✅ | 实时通知/消息推送 |
| JWT | ✅ | ✅ | 无状态认证、权限控制 |
| Knife4j | ✅（OpenAPI2） | ⚠️（换 springdoc/OpenAPI3） | API 文档工程化 |
| Micrometer + Prometheus | ✅ | ✅ | 指标体系、监控可视化 |
| OSS 上传 | ✅ | ✅ | 图片存储、CDN 链路 |
| Email | ✅ | ✅ | 异常告警/订阅通知 |
| AI SDK（多家） | ✅ | ✅ | AI 写作助手/内容自动化 |
| MongoDB | 文档提及（未见依赖） | ✅（补齐） | 版本历史/操作日志/对话存储 |
| Docker/Nginx/HTTPS | 文档提及 | ✅（补齐） | 一键部署、反代与 TLS |

---

## 6. 附：落地时的“版本选择”建议（避免兼容性浪费时间）

- Spring Boot：3.2.x 或 3.3.x
- JDK：17
- MySQL：8.0+
- Redis：6/7
- RabbitMQ：3.11+
- Elasticsearch：8.x（或改用 OpenSearch）
- MongoDB：6+

---

## 7. 备注：本文件如何使用

1. 你可以把每个 Milestone 当成一个 Git 分支/PR 来做（更利于复盘）。
2. 每个模块完成后写一篇博客总结（例如“Redis ZSET 做热门榜 + 缓存一致性策略”）。
3. 若你希望我后续继续辅助：你可以告诉我你准备先做哪个 Milestone，我可以按 paicoding 的实现方式带你一步步复刻到 Spring Boot 3。
