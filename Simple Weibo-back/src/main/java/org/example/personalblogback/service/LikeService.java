package org.example.personalblogback.service;

import java.util.Map;

public interface LikeService {
    Map<String, Object> toggleLike(Long userId, Long postId);
    boolean isLiked(Long userId, Long postId);
}
