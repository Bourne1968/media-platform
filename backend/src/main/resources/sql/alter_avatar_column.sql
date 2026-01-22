-- 修改 avatar 字段类型以支持存储 base64 图片数据
-- 执行方法：mysql -u root -p media_platform < backend/src/main/resources/sql/alter_avatar_column.sql

USE `media_platform`;

-- 将 avatar 字段从 VARCHAR(500) 改为 LONGTEXT 以支持存储 base64 图片
ALTER TABLE `user` 
MODIFY COLUMN `avatar` LONGTEXT DEFAULT NULL COMMENT '头像（base64或URL）';
