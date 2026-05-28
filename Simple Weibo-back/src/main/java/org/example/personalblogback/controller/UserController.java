package org.example.personalblogback.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.personalblogback.common.Result;
import org.example.personalblogback.entity.User;
import org.example.personalblogback.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/user/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(user);
    }

    @PutMapping("/user/{id}")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        if (!userId.equals(id)) {
            return Result.error(403, "没有权限修改其他用户信息");
        }

        String username = (String) body.get("username");
        String bio = (String) body.get("bio");
        String avatar = (String) body.get("avatar");

        try {
            User user = userService.updateUser(id, username, bio, avatar);
            return Result.success(user);
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }
}
