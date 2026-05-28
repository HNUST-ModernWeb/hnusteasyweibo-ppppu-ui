-- H2 数据库表结构

-- 用户表
CREATE TABLE IF NOT EXISTS `user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `avatar` VARCHAR(500) DEFAULT NULL,
    `bio` VARCHAR(500) DEFAULT NULL,
    `followers` INT DEFAULT 0,
    `following` INT DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 微博表
CREATE TABLE IF NOT EXISTS `post` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `content` TEXT NOT NULL,
    `author_id` BIGINT NOT NULL,
    `images` VARCHAR(2000) DEFAULT NULL,
    `likes` INT DEFAULT 0,
    `reposts` INT DEFAULT 0,
    `comments_count` INT DEFAULT 0,
    `views` INT DEFAULT 0,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    `updated_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 评论表
CREATE TABLE IF NOT EXISTS `comment` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `post_id` BIGINT NOT NULL,
    `content` TEXT NOT NULL,
    `author_id` BIGINT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 点赞表
CREATE TABLE IF NOT EXISTS `like_record` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `post_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (`post_id`, `user_id`)
);

-- 关注表
CREATE TABLE IF NOT EXISTS `follow` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `follower_id` BIGINT NOT NULL,
    `following_id` BIGINT NOT NULL,
    `created_at` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (`follower_id`, `following_id`)
);

-- 插入测试数据
INSERT INTO `user` (`id`, `username`, `password`, `avatar`, `bio`, `followers`, `following`) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', '博客管理员', 100, 50);

INSERT INTO `post` (`id`, `content`, `author_id`, `images`, `likes`, `reposts`, `comments_count`, `views`, `created_at`) VALUES
(1, '今天天气真好，阳光明媚，适合出去走走！☀️', 1, '["https://picsum.photos/400/400?random=1"]', 23, 5, 5, 128, '2026-05-07 10:30:00'),
(2, '刚刚完成了一个重要的项目，感觉特别有成就感！💪 努力总会有回报的，继续加油！', 1, '["https://picsum.photos/400/400?random=2", "https://picsum.photos/400/400?random=3", "https://picsum.photos/400/400?random=4"]', 45, 8, 12, 256, '2026-05-07 09:15:00'),
(3, '早安，新的一天开始了！今天也要元气满满哦~ 🌅', 1, '[]', 18, 2, 3, 89, '2026-05-07 07:00:00');

INSERT INTO `comment` (`id`, `post_id`, `content`, `author_id`, `created_at`) VALUES
(1, 1, '写得很好，期待更多文章！', 1, '2026-05-07 11:00:00');
