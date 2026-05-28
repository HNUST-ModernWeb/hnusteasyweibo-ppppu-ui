package org.example.personalblogback.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.personalblogback.common.Result;
import org.example.personalblogback.entity.Post;
import org.example.personalblogback.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PostController {

    @Autowired
    private PostService postService;

    @GetMapping("/posts")
    public Result<Map<String, Object>> getPosts(
            @RequestParam(required = false) Long authorId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpServletRequest request) {
        
        Long currentUserId = (Long) request.getAttribute("userId");
        List<Post> posts = postService.getPosts(authorId, page, pageSize, currentUserId);
        long total = postService.countPosts(authorId);

        Map<String, Object> result = new HashMap<>();
        result.put("posts", posts);
        result.put("total", total);
        result.put("page", page);
        result.put("pageSize", pageSize);

        return Result.success(result);
    }

    @GetMapping("/post/{id}")
    public Result<Post> getPostById(@PathVariable Long id, HttpServletRequest request) {
        Long currentUserId = (Long) request.getAttribute("userId");
        try {
            Post post = postService.getPostById(id, currentUserId);
            return Result.success(post);
        } catch (RuntimeException e) {
            return Result.error(404, e.getMessage());
        }
    }

    @PostMapping("/post")
    public Result<Post> createPost(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        String content = (String) body.get("content");
        List<String> images = (List<String>) body.get("images");

        try {
            Post post = postService.createPost(userId, content, images);
            return Result.success(post);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PutMapping("/post/{id}")
    public Result<Post> updatePost(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        String content = (String) body.get("content");
        List<String> images = (List<String>) body.get("images");

        try {
            Post post = postService.updatePost(id, userId, content, images);
            return Result.success(post);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @DeleteMapping("/post/{id}")
    public Result<Void> deletePost(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        try {
            postService.deletePost(id, userId);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
