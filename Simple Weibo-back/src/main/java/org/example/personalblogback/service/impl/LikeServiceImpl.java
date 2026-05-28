package org.example.personalblogback.service.impl;

import org.example.personalblogback.entity.LikeRecord;
import org.example.personalblogback.entity.Post;
import org.example.personalblogback.mapper.LikeRecordMapper;
import org.example.personalblogback.mapper.PostMapper;
import org.example.personalblogback.service.LikeService;
import org.example.personalblogback.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class LikeServiceImpl implements LikeService {

    @Autowired
    private LikeRecordMapper likeRecordMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private NotificationService notificationService;

    @Override
    @Transactional
    public Map<String, Object> toggleLike(Long userId, Long postId) {
        LikeRecord existing = likeRecordMapper.findByPostIdAndUserId(postId, userId);
        
        boolean liked;
        if (existing != null) {
            likeRecordMapper.delete(postId, userId);
            postMapper.updateLikes(postId, -1);
            liked = false;
        } else {
            LikeRecord record = LikeRecord.builder()
                    .postId(postId)
                    .userId(userId)
                    .build();
            likeRecordMapper.insert(record);
            postMapper.updateLikes(postId, 1);
            liked = true;

            Post post = postMapper.findById(postId);
            if (post != null && post.getAuthorId() != null) {
                notificationService.createNotification(
                        post.getAuthorId(),
                        userId,
                        "like",
                        postId,
                        "赞了你的微博"
                );
            }
        }

        long likes = likeRecordMapper.countByPostId(postId);

        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);
        result.put("likes", (int) likes);
        return result;
    }

    @Override
    public boolean isLiked(Long userId, Long postId) {
        if (userId == null) return false;
        return likeRecordMapper.findByPostIdAndUserId(postId, userId) != null;
    }
}
