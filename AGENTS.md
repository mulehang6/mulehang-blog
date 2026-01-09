# Repository Guidelines

## Project Structure & Module Organization
This is a Spring Boot 3.3.7, Java 21, multi-module Maven project. Module dependency flow: `blog-web` -> `blog-service` -> (`blog-core`, `blog-api`).
- `blog-api`: DTO/VO contracts and shared response models.
- `blog-core`: infrastructure utilities (cache, JWT, markdown, OSS, etc.).
- `blog-service`: business logic, data access, Liquibase, MQ, mail, WebSocket.
- `blog-ui`: Thymeleaf templates and web assets (`blog-ui/src/main/resources/templates`).
- `blog-web`: web layer and application entry point (`blog-web/src/main/java`).
Standard Maven layout is used: `*/src/main/java`, `*/src/main/resources`, `*/src/test/java`.

## Build, Test, and Development Commands
- `docker compose up -d`: start MySQL, Redis, RabbitMQ from `compose.yaml`.
- `mvn clean verify`: full build and tests for all modules.
- `mvn clean install -DskipTests`: build without running tests.
- `mvn -pl blog-web -am spring-boot:run`: run the application locally.
- `mvn -pl blog-service test`: run tests for a single module.

## Coding Style & Naming Conventions
- Java packages use `com.mulehang.*`; classes `UpperCamelCase`, methods/fields `lowerCamelCase`, constants `UPPER_SNAKE_CASE`.
- Use 4-space indentation and standard Spring/Spring Boot conventions.
- REST endpoints should be versioned like `/api/v1/...`.
- Database columns follow `snake_case` and map to `lowerCamelCase` fields.

## Testing Guidelines
- Framework: Spring Boot test starter (JUnit 5).
- Place tests in `src/test/java` and name them `*Test` or `*Tests`.
- Use `@SpringBootTest` for integration tests; prefer Docker Compose services or mocks for external dependencies.

## Commit & Pull Request Guidelines
- Use Conventional Commits (`feat: ...`, `fix: ...`, `chore: ...`, `docs: ...`).
- If a change affects the database, mention the Liquibase changelog path and rollback notes in the commit/PR.
- PRs should include a concise description, testing notes, and any relevant API or UI screenshots.

## Configuration & Security Notes
- Main config lives in `blog-web/src/main/resources/application.yml`.
- Do not commit real secrets; use environment variables (e.g., `SPRING_DATASOURCE_PASSWORD`, `SPRING_DATA_REDIS_PASSWORD`, `SPRING_MAIL_PASSWORD`).
- Liquibase master changelog: `blog-web/src/main/resources/db/changelog/master.xml`.
