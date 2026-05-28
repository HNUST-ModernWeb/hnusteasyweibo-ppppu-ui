package org.example.personalblogback.service.impl;

import org.example.personalblogback.entity.Post;
import org.example.personalblogback.mapper.LikeRecordMapper;
import org.example.personalblogback.mapper.PostMapper;
import org.example.personalblogback.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostServiceImpl implements PostService {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private LikeRecordMapper likeRecordMapper;

    @Override
    public List<Post> getPosts(Long authorId, int page, int pageSize, Long currentUserId) {
        int offset = (page - 1) * pageSize;
        List<Post> posts = postMapper.findList(authorId, offset, pageSize);

        // 设置当前用户的点赞状态
        if (currentUserId != null) {
            for (Post post : posts) {
                post.setLiked(likeRecordMapper.findByPostIdAndUserId(post.getId(), currentUserId) != null);
            }
        } else {
            for (Post post : posts) {
                post.setLiked(false);
            }
        }

        return posts;
    }

    @Override
    public long countPosts(Long authorId) {
        return postMapper.count(authorId);
    }

    @Override
    public Post getPostById(Long id, Long currentUserId) {
        Post post = postMapper.findById(id);
        if (post == null) {
            throw new RuntimeException("微博不存在");
        }

        // 增加浏览量
        postMapper.incrementViews(id);
        post.setViews(post.getViews() + 1);

        // 设置当前用户的点赞状态
        if (currentUserId != null) {
            post.setLiked(likeRecordMapper.findByPostIdAndUserId(id, currentUserId) != null);
        } else {
            post.setLiked(false);
        }

        return post;
    }

    @Override
    public Post createPost(Long userId, String content, List<String> images) {
        Post post = Post.builder()
                .content(content)
                .authorId(userId)
                .images(images)
                .likes(0)
                .reposts(0)
                .commentsCount(0)
                .views(0)
                .build();

        postMapper.insert(post);
        return postMapper.findById(post.getId());
    }

    @Override
    public Post updatePost(Long postId, Long userId, String content, List<String> images) {
        Post post = postMapper.findById(postId);
        if (post == null) {
            throw new RuntimeException("微博不存在");
        }

        if (!post.getAuthorId().equals(userId)) {
            throw new RuntimeException("没有权限编辑此微博");
        }

        post.setContent(content);
        post.setImages(images);
        postMapper.update(post);

        return postMapper.findById(postId);
    }

    @Override
    public void deletePost(Long postId, Long userId) {
        Post post = postMapper.findById(postId);
        if (post == null) {
            throw new RuntimeException("微博不存在");
        }

        if (!post.getAuthorId().equals(userId)) {
            throw new RuntimeException("没有权限删除此微博");
        }

        postMapper.delete(postId);
    }
}
