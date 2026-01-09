# mulehang-blog

> 基于 Spring Boot 3.3.7 + Java 21 的全栈博客系统学习实践项目。

## 🚀 项目简介

`mulehang-blog` 是一个旨在通过实践掌握现代 Java 后端技术栈的个人博客系统。项目采用多模块架构，集成了缓存、消息队列、搜索引擎及 AI 辅助内容生产等核心功能。
至于为什么要叫`mulehang-blog`并且我的用户名也叫`mulehang`，暂且按下不表。

## 🛠 技术栈

- **核心框架**: Java 21, Spring Boot 3.3.7
- **持久层**: MySQL 8.0, MyBatis-Plus, Liquibase (数据库版本管理)
- **缓存/中间件**: Redis, RabbitMQ
- **前端/视图**: Thymeleaf, Flexmark (Markdown 渲染)
- **文档/监控**: Knife4j (OpenAPI3), Actuator + Prometheus
- **工具库**: MapStruct, Lombok

## 📂 项目结构

```text
mulehang-blog
├── blog-api      # 接口层：DTO/VO 定义、外部接口协议
├── blog-core     # 核心层：基础工具类、通用常量、异常处理
├── blog-service  # 业务层：核心业务逻辑、Mapper、Entity、Service
├── blog-ui       # 表现层：Thymeleaf 模板、CSS/JS 静态资源
├── blog-web      # 启动层：项目配置、主启动类
└── docs          # 文档：开发指南、技术栈规划
```

## 📈 当前进度

目前已完成刚把mapper新建完。。尽量2月份之前学完

## 🛠️ 环境要求

- **JDK**: 21
- **Maven**: 3.9+
- **MySQL**: 8.0+
- **Redis**: 7.0+
- **RabbitMQ**: 3.x+ 非必需

## 🚦 快速启动

1. **配置环境**: 修改 `blog-web/src/main/resources/application.yml` 中的数据源及中间件连接信息。
2. **构建项目**:
   ```powershell
   mvn clean install
   ```
3. **启动应用**: 运行 `com.mulehang.BlogWebApplication` 启动类。
4. **访问文档**: 项目启动后访问 `http://localhost:8080/doc.html` 查看 API 文档。

## 📄 许可证

本项目采用 [Apache License 2.0](LICENSE) 许可。

