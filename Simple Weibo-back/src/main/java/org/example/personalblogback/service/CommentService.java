package org.example.personalblogback.service;

import org.example.personalblogback.entity.Comment;

import java.util.List;

public interface CommentService {
    List<Comment> getComments(Long postId, int page, int pageSize);
    long countComments(Long postId);
    Comment createComment(Long userId, Long postId, String content);
}
