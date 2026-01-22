-- 为用户表添加偏好设置列
-- 使用方法：mysql -u root -p media_platform < add_preferences_column.sql

USE `media_platform`;

ALTER TABLE `user`
ADD COLUMN `preferences` JSON DEFAULT NULL COMMENT '用户偏好设置（JSON格式）' AFTER `avatar`;
