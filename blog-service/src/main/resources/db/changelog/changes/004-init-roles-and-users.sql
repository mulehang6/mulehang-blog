-- liquibase formatted sql

-- changeset mulehang:004-init-roles-and-users splitStatements:true endDelimiter:;

-- 插入默认角色
INSERT INTO `sys_role` (`id`, `code`, `name`, `description`, `sort`, `status`) VALUES
(1, 'ADMIN', '管理员', '系统管理员，拥有所有权限', 1, 1),
(2, 'USER', '普通用户', '普通注册用户', 10, 1),
(3, 'AUTHOR', '作者', '可以发布文章的作者', 5, 1);

-- 插入测试用户
-- 密码：admin123（使用 BCrypt + 自定义盐加密）
-- 注意：生产环境应该通过注册接口创建用户，这里仅用于开发测试
-- 盐值：testSalt123（Base64编码）
-- 密码哈希：使用 BCrypt 对 "admin123" + "testSalt123" 进行加密
INSERT INTO `sys_user` (`id`, `username`, `password_hash`, `password_salt`, `nickname`, `email`, `status`) VALUES
(1, 'admin', '$2a$10$8K1p/a0dL2LkIYnqYfHF4.koEA5T8dqjOw5qYfHF4.koEA5T8dqjO', 'dGVzdFNhbHQxMjM=', '管理员', 'admin@mulehang.com', 1),
(2, 'testuser', '$2a$10$8K1p/a0dL2LkIYnqYfHF4.koEA5T8dqjOw5qYfHF4.koEA5T8dqjO', 'dGVzdFNhbHQxMjM=', '测试用户', 'test@mulehang.com', 1);

-- 分配角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1), -- admin 用户拥有管理员角色
(1, 3), -- admin 用户拥有作者角色
(2, 2), -- testuser 用户拥有普通用户角色
(2, 3); -- testuser 用户拥有作者角色

-- rollback DELETE FROM `sys_user_role` WHERE `user_id` IN (1, 2);
-- rollback DELETE FROM `sys_user` WHERE `id` IN (1, 2);
-- rollback DELETE FROM `sys_role` WHERE `id` IN (1, 2, 3);

