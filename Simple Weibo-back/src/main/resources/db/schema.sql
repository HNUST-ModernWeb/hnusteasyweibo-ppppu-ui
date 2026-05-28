-- 个人博客数据库表结构

-- 创建数据库
CREATE DATABASE IF NOT EXISTS personal_blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE personal_blog;

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    `bio` VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
    `followers` INT DEFAULT 0 COMMENT '粉丝数',
    `following` INT DEFAULT 0 COMMENT '关注数',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 微博表
CREATE TABLE IF NOT EXISTS `post` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '微博ID',
    `content` TEXT NOT NULL COMMENT '内容',
    `author_id` BIGINT NOT NULL COMMENT '作者ID',
    `images` JSON DEFAULT NULL COMMENT '图片URL数组',
    `likes` INT DEFAULT 0 COMMENT '点赞数',
    `reposts` INT DEFAULT 0 COMMENT '转发数',
    `comments_count` INT DEFAULT 0 COMMENT '评论数',
    `views` INT DEFAULT 0 COMMENT '浏览量',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_author_id` (`author_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='微博表';

-- 评论表
CREATE TABLE IF NOT EXISTS `comment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `post_id` BIGINT NOT NULL COMMENT '微博ID',
    `content` TEXT NOT NULL COMMENT '内容',
    `author_id` BIGINT NOT NULL COMMENT '作者ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_post_id` (`post_id`),
    INDEX `idx_author_id` (`author_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- 点赞表
CREATE TABLE IF NOT EXISTS `like_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `post_id` BIGINT NOT NULL COMMENT '微博ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_post_user` (`post_id`, `user_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞记录表';

-- 关注表
CREATE TABLE IF NOT EXISTS `follow` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `follower_id` BIGINT NOT NULL COMMENT '关注者ID',
    `following_id` BIGINT NOT NULL COMMENT '被关注者ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follower_following` (`follower_id`, `following_id`),
    INDEX `idx_following_id` (`following_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关注关系表';

-- 插入测试数据
INSERT INTO `user` (`id`, `username`, `password`, `avatar`, `bio`, `followers`, `following`) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', '博客管理员', 100, 50);

INSERT INTO `post` (`id`, `content`, `author_id`, `images`, `likes`, `reposts`, `comments_count`, `views`, `created_at`) VALUES
(1, '今天天气真好，阳光明媚，适合出去走走！☀️', 1, '["https://picsum.photos/400/400?random=1"]', 23, 5, 5, 128, '2026-05-07 10:30:00'),
(2, '刚刚完成了一个重要的项目，感觉特别有成就感！💪 努力总会有回报的，继续加油！', 1, '["https://picsum.photos/400/400?random=2", "https://picsum.photos/400/400?random=3", "https://picsum.photos/400/400?random=4"]', 45, 8, 12, 256, '2026-05-07 09:15:00'),
(3, '早安，新的一天开始了！今天也要元气满满哦~ 🌅', 1, '[]', 18, 2, 3, 89, '2026-05-07 07:00:00');

INSERT INTO `comment` (`id`, `post_id`, `content`, `author_id`, `created_at`) VALUES
(1, 1, '写得很好，期待更多文章！', 1, '2026-05-07 11:00:00');
