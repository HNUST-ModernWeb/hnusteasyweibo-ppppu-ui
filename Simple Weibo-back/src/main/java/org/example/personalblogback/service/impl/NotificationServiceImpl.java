package org.example.personalblogback.service.impl;

import org.example.personalblogback.entity.Notification;
import org.example.personalblogback.mapper.NotificationMapper;
import org.example.personalblogback.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public void createNotification(Long userId, Long senderId, String type, Long relatedId, String content) {
        if (userId.equals(senderId)) {
            return;
        }

        Notification notification = Notification.builder()
                .userId(userId)
                .senderId(senderId)
                .type(type)
                .relatedId(relatedId)
                .content(content)
                .isRead(false)
                .build();

        notificationMapper.insert(notification);
    }

    @Override
    public List<Notification> getNotifications(Long userId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return notificationMapper.findByUserId(userId, offset, pageSize);
    }

    @Override
    public long countNotifications(Long userId) {
        return notificationMapper.countByUserId(userId);
    }

    @Override
    public long countUnreadNotifications(Long userId) {
        return notificationMapper.countUnreadByUserId(userId);
    }

    @Override
    public void markAsRead(Long id, Long userId) {
        int rows = notificationMapper.markAsRead(id, userId);
        if (rows == 0) {
            throw new RuntimeException("通知不存在或无权限");
        }
    }

    @Override
    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsRead(userId);
    }

    @Override
    public void deleteNotification(Long id, Long userId) {
        int rows = notificationMapper.deleteById(id, userId);
        if (rows == 0) {
            throw new RuntimeException("通知不存在或无权限");
        }
    }

    @Override
    public void deleteAllNotifications(Long userId) {
        notificationMapper.deleteAllByUserId(userId);
    }
}
