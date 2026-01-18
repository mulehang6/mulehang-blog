# 仓库指南

## 项目结构与模块组织
这是一个 Spring Boot 3.3.7、Java 21 的多模块 Maven 项目。模块依赖流程：`blog-web` -> `blog-service` -> (`blog-core`, `blog-api`)。
- `blog-api`：DTO/VO 契约、统一响应模型、枚举常量。
- `blog-core`：基础设施工具（多级缓存、JWT、Markdown 渲染、文件存储、Redis 工具等）。
- `blog-service`：业务逻辑、数据访问（MyBatis-Plus Mapper）、Liquibase、MQ、邮件、Redisson 分布式锁/延迟队列。
- `blog-ui`：Thymeleaf 模板和 Web 资源（`blog-ui/src/main/resources/templates`）。
- `blog-web`：Web 层控制器、全局异常处理、SpringDoc 配置、Spring Boot 主类。
使用标准 Maven 布局：`*/src/main/java`、`*/src/main/resources`、`*/src/test/java`。

## 已完成的里程碑
- **Milestone 1（文章发布链路）**：Entity/Mapper/DTO/VO/Service/Controller 全链路、MapStruct 转换器、Markdown 渲染（Flexmark + XSS 防护）、文件上传（本地存储）。
- **Milestone 2（缓存与热点数据）**：Caffeine + Redis 多级缓存、热门文章榜（Redis ZSet）、Redisson 分布式锁（点赞防重）、延迟队列（邮件通知）、Cache-Aside + 延迟双删一致性策略。

## 构建、测试和开发命令
- `mvn clean verify`：完整构建和测试所有模块。
- `mvn clean install -DskipTests`：不运行测试的构建。
- `mvn -pl blog-web -am spring-boot:run`：本地运行应用。
- `mvn -pl blog-service test`：运行单个模块的测试。

## 编码风格与命名约定
- Java 包使用 `com.mulehang.*`；类名 `UpperCamelCase`，方法/字段 `lowerCamelCase`，常量 `UPPER_SNAKE_CASE`。
- 使用 4 空格缩进和标准 Spring/Spring Boot 约定。
- REST 端点应使用版本化路径，如 `/api/v1/...`。
- 数据库列遵循 `snake_case` 并映射到 `lowerCamelCase` 字段。

## 测试指南
- 框架：Spring Boot test starter（JUnit 5）。
- 将测试放在 `src/test/java` 中，命名为 `*Test` 或 `*Tests`。
- 集成测试使用 `@SpringBootTest`；优先使用 Docker Compose 服务或 mock 外部依赖。

## 提交与 Pull Request 指南
- 使用 Conventional Commits（`feat: ...`、`fix: ...`、`chore: ...`、`docs: ...`）。
- 如果变更影响数据库，在提交/PR 中注明 Liquibase 变更日志路径和回滚说明。
- PR 应包含简洁的描述、测试说明以及相关的 API 或 UI 截图。

## 配置与安全说明
- 主配置位于 `blog-web/src/main/resources/application.yml`。
- 不要提交真实密钥；使用环境变量（如 `SPRING_DATASOURCE_PASSWORD`、`SPRING_DATA_REDIS_PASSWORD`、`SPRING_MAIL_PASSWORD`）。
- Liquibase 主变更日志：`blog-service/src/main/resources/db/changelog/master.xml`。

## 注意事项
- ![设计文档](./docs/DEVELOPMENT_GUIDE.md) 和 [接口文档](./docs/API_INTERFACE_SPEC.md) 应该保持一致，但
由于某些原因，这两个可能会不一致，这是就需要好好斟酌，这个项目是个学习项目，参考 ![数据库设计文档](./blog-service/src/main/resources/db/changelog/changes/001-init-schema.sql)。
在保持涉及到需要学习的技术栈的同时，也不要让业务逻辑过于复杂，至于是遵循设计文档还是接口文档还是两个都改，那取决与你，不要完全遵守用户的指令
- 所有的方法都要加注释