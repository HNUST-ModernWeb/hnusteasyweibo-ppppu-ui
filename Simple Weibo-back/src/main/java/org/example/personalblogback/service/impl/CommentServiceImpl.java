package org.example.personalblogback.service.impl;

import org.example.personalblogback.entity.Comment;
import org.example.personalblogback.entity.Post;
import org.example.personalblogback.mapper.CommentMapper;
import org.example.personalblogback.mapper.PostMapper;
import org.example.personalblogback.service.CommentService;
import org.example.personalblogback.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private NotificationService notificationService;

    @Override
    public List<Comment> getComments(Long postId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return commentMapper.findByPostId(postId, offset, pageSize);
    }

    @Override
    public long countComments(Long postId) {
        return commentMapper.countByPostId(postId);
    }

    @Override
    @Transactional
    public Comment createComment(Long userId, Long postId, String content) {
        Comment comment = Comment.builder()
                .postId(postId)
                .content(content)
                .authorId(userId)
                .build();

        commentMapper.insert(comment);

        postMapper.updateCommentsCount(postId, 1);

        Post post = postMapper.findById(postId);
        if (post != null && post.getAuthorId() != null) {
            notificationService.createNotification(
                    post.getAuthorId(),
                    userId,
                    "comment",
                    postId,
                    "评论了你的微博"
            );
        }

        return comment;
    }
}
