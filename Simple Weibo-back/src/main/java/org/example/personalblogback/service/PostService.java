package org.example.personalblogback.service;

import org.example.personalblogback.entity.Post;

import java.util.List;

public interface PostService {
    List<Post> getPosts(Long authorId, int page, int pageSize, Long currentUserId);
    long countPosts(Long authorId);
    Post getPostById(Long id, Long currentUserId);
    Post createPost(Long userId, String content, List<String> images);
    Post updatePost(Long postId, Long userId, String content, List<String> images);
    void deletePost(Long postId, Long userId);
}
