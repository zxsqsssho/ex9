// src/main/java/com/library/service/NotificationService.java
package com.library.service;

import com.library.dto.ApiResponse;
import com.library.dto.NotificationDTO;
import com.library.entity.Notification;
import org.springframework.data.domain.Pageable;

public interface NotificationService {

    /**
     * 获取用户通知列表
     */
    ApiResponse<?> getUserNotifications(Pageable pageable);

    /**
     * 获取用户未读通知数量
     */
    ApiResponse<?> getUnreadCount();

    /**
     * 标记通知为已读
     */
    ApiResponse<?> markAsRead(Long notificationId);

    /**
     * 标记所有通知为已读
     */
    ApiResponse<?> markAllAsRead();

    /**
     * 管理员：获取所有通知
     */
    ApiResponse<?> getAllNotifications(Pageable pageable);

    /**
     * 管理员：发送系统通知
     */
    ApiResponse<?> sendSystemNotification(NotificationDTO notificationDTO);

    /**
     * 管理员：发送重要通知
     */
    ApiResponse<?> sendImportantNotification(NotificationDTO notificationDTO);

    /**
     * 创建通知（内部使用）
     */
    void createNotification(Long userId, String title, String content,
                            Notification.NotificationType type, boolean important);
}