-- H2 数据库测试数据

-- 插入测试数据（使用 MERGE 避免重复）
MERGE INTO `user` (`id`, `username`, `password`, `avatar`, `bio`, `followers`, `following`) 
VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iAt6Z5EO', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', '博客管理员', 100, 50);

MERGE INTO `post` (`id`, `content`, `author_id`, `images`, `likes`, `reposts`, `comments_count`, `views`, `created_at`) 
VALUES (1, '今天天气真好，阳光明媚，适合出去走走！☀️', 1, '["https://picsum.photos/400/400?random=1"]', 23, 5, 5, 128, '2026-05-07 10:30:00');

MERGE INTO `post` (`id`, `content`, `author_id`, `images`, `likes`, `reposts`, `comments_count`, `views`, `created_at`) 
VALUES (2, '刚刚完成了一个重要的项目，感觉特别有成就感！💪 努力总会有回报的，继续加油！', 1, '["https://picsum.photos/400/400?random=2", "https://picsum.photos/400/400?random=3", "https://picsum.photos/400/400?random=4"]', 45, 8, 12, 256, '2026-05-07 09:15:00');

MERGE INTO `post` (`id`, `content`, `author_id`, `images`, `likes`, `reposts`, `comments_count`, `views`, `created_at`) 
VALUES (3, '早安，新的一天开始了！今天也要元气满满哦~ 🌅', 1, '[]', 18, 2, 3, 89, '2026-05-07 07:00:00');

MERGE INTO `comment` (`id`, `post_id`, `content`, `author_id`, `created_at`) 
VALUES (1, 1, '写得很好，期待更多文章！', 1, '2026-05-07 11:00:00');
