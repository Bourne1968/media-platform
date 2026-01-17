-- 添加新增表的SQL脚本
-- 如果 user 和 creation_record 表已存在，只需执行此脚本即可

USE `media_platform`;

-- ============================================
-- 风格模板表（必需）
-- ============================================
CREATE TABLE IF NOT EXISTS `style_template` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    `name` VARCHAR(100) NOT NULL COMMENT '模板名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '模板描述',
    `prompt` VARCHAR(500) DEFAULT NULL COMMENT '模板提示词',
    `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_enabled` (`enabled`),
    INDEX `idx_sort` (`sort`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='风格模板表';

-- ============================================
-- 以下表是可选表（收藏和分类功能暂未实现）
-- 如果暂时不需要这些功能，可以先不创建
-- ============================================

-- 收藏表（可选）
CREATE TABLE IF NOT EXISTS `favorite` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `record_id` BIGINT NOT NULL COMMENT '创作记录ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_record` (`user_id`, `record_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_record_id` (`record_id`),
    CONSTRAINT `fk_favorite_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_favorite_record` FOREIGN KEY (`record_id`) REFERENCES `creation_record` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- 分类表（可选）
CREATE TABLE IF NOT EXISTS `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `name` VARCHAR(100) NOT NULL COMMENT '分类名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '分类描述',
    `color` VARCHAR(20) DEFAULT NULL COMMENT '分类颜色',
    `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    CONSTRAINT `fk_category_user` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- 记录分类关联表（可选）
CREATE TABLE IF NOT EXISTS `record_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `record_id` BIGINT NOT NULL COMMENT '创作记录ID',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_record_category` (`record_id`, `category_id`),
    INDEX `idx_record_id` (`record_id`),
    INDEX `idx_category_id` (`category_id`),
    CONSTRAINT `fk_record_category_record` FOREIGN KEY (`record_id`) REFERENCES `creation_record` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_record_category_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='记录分类关联表';

-- ============================================
-- 插入默认风格模板数据（必需）
-- ============================================
INSERT INTO `style_template` (`name`, `description`, `prompt`, `enabled`, `sort`, `create_time`, `update_time`) VALUES
('轻松活泼', '适合日常、娱乐类内容', '请使用轻松活泼的语气和风格', 1, 1, NOW(), NOW()),
('正式严谨', '适合商务、专业类内容', '请使用正式严谨的语言风格', 1, 2, NOW(), NOW()),
('幽默风趣', '适合搞笑、段子类内容', '请使用幽默风趣的语调', 1, 3, NOW(), NOW()),
('文艺清新', '适合文艺、情感类内容', '请使用文艺清新的文字风格', 1, 4, NOW(), NOW()),
('吸引眼球', '适合标题、营销类内容', '请使用吸引眼球、具有冲击力的表达方式', 1, 5, NOW(), NOW())
ON DUPLICATE KEY UPDATE `name`=`name`;

-- 验证：查看创建的表
SHOW TABLES;

-- 验证：查看风格模板数据
SELECT * FROM `style_template`;
