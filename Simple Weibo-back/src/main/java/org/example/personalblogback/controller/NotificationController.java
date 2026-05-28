package org.example.personalblogback.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.personalblogback.common.Result;
import org.example.personalblogback.entity.Notification;
import org.example.personalblogback.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/notifications")
    public Result<Map<String, Object>> getNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            HttpServletRequest request) {
        
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        List<Notification> notifications = notificationService.getNotifications(userId, page, pageSize);
        long total = notificationService.countNotifications(userId);
        long unread = notificationService.countUnreadNotifications(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("notifications", notifications);
        result.put("total", total);
        result.put("unread", unread);
        result.put("page", page);
        result.put("pageSize", pageSize);

        return Result.success(result);
    }

    @GetMapping("/notifications/unread/count")
    public Result<Map<String, Long>> getUnreadCount(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        long unread = notificationService.countUnreadNotifications(userId);
        Map<String, Long> result = new HashMap<>();
        result.put("count", unread);

        return Result.success(result);
    }

    @PutMapping("/notification/{id}/read")
    public Result<Void> markAsRead(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        try {
            notificationService.markAsRead(id, userId);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PutMapping("/notifications/read/all")
    public Result<Void> markAllAsRead(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        notificationService.markAllAsRead(userId);
        return Result.success();
    }

    @DeleteMapping("/notification/{id}")
    public Result<Void> deleteNotification(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        try {
            notificationService.deleteNotification(id, userId);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @DeleteMapping("/notifications")
    public Result<Void> deleteAllNotifications(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        notificationService.deleteAllNotifications(userId);
        return Result.success();
    }
}
