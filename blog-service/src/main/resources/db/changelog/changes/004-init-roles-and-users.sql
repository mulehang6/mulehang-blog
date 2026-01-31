-- liquibase formatted sql

-- changeset mulehang:004-init-roles-and-users splitStatements:true endDelimiter:;

-- 插入默认角色
INSERT IGNORE INTO `sys_role` (`id`, `code`, `name`, `description`, `sort`, `status`) VALUES
(1, 'ADMIN', '管理员', '系统管理员，拥有所有权限', 1, 1),
(2, 'USER', '普通用户', '普通注册用户', 10, 1);

-- 插入默认管理员
-- 密码：mulehang（使用 BCrypt + 自定义盐加密）
-- 注意：生产环境应该通过注册接口创建用户，这里仅用于开发测试
-- 盐值：见 password_salt（Base64 字符串）
-- 密码哈希：使用 BCrypt 对 "mulehang" + password_salt 进行加密
INSERT IGNORE INTO `sys_user` (`username`, `password_hash`, `password_salt`, `nickname`, `email`, `status`) VALUES
('admin', '$2a$10$1PUPrLToxeD2Ktfun6373.yOaDJHDlAtDxcGVWUHxpIHa3ILWS6GS', 'ZUc1eFE2bk4ydkIxbUIyZ0Q4Y1I1dlM0', '管理员', '15871935167@163.com', 1);

-- 分配角色
INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `sys_user` u
JOIN `sys_role` r ON r.code = 'ADMIN'
WHERE u.username = 'admin';

INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `sys_user` u
JOIN `sys_role` r ON r.code = 'USER'
WHERE u.username = 'admin';

-- rollback DELETE FROM `sys_user_role` WHERE `user_id` IN (SELECT `id` FROM `sys_user` WHERE `username` = 'admin');
-- rollback DELETE FROM `sys_user` WHERE `username` = 'admin';
-- rollback DELETE FROM `sys_role` WHERE `id` IN (1, 2);
