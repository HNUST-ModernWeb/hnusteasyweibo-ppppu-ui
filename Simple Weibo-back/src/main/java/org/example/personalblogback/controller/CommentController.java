package org.example.personalblogback.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.personalblogback.common.Result;
import org.example.personalblogback.entity.Comment;
import org.example.personalblogback.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/comments")
    public Result<Map<String, Object>> getComments(
            @RequestParam Long postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize) {
        
        List<Comment> comments = commentService.getComments(postId, page, pageSize);
        long total = commentService.countComments(postId);

        Map<String, Object> result = new HashMap<>();
        result.put("comments", comments);
        result.put("total", total);

        return Result.success(result);
    }

    @PostMapping("/comment")
    public Result<Comment> createComment(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        Long postId = Long.valueOf(body.get("postId").toString());
        String content = (String) body.get("content");

        try {
            Comment comment = commentService.createComment(userId, postId, content);
            return Result.success(comment);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
