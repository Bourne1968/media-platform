-- 为用户表添加email字段
-- 如果表已存在，执行此SQL添加email字段

ALTER TABLE `user` 
ADD COLUMN `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱' AFTER `username`,
ADD UNIQUE INDEX `idx_email` (`email`);
