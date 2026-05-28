package org.example.personalblogback.service;

import org.example.personalblogback.entity.Notification;

import java.util.List;
import java.util.Map;

public interface NotificationService {

    void createNotification(Long userId, Long senderId, String type, Long relatedId, String content);

    List<Notification> getNotifications(Long userId, int page, int pageSize);

    long countNotifications(Long userId);

    long countUnreadNotifications(Long userId);

    void markAsRead(Long id, Long userId);

    void markAllAsRead(Long userId);

    void deleteNotification(Long id, Long userId);

    void deleteAllNotifications(Long userId);
}
