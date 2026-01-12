// src/main/java/com/library/service/impl/NotificationServiceImpl.java
package com.library.service.impl;

import com.library.dto.ApiResponse;
import com.library.dto.NotificationDTO;
import com.library.entity.Notification;
import com.library.entity.User;
import com.library.repository.NotificationRepository;
import com.library.repository.UserRepository;
import com.library.service.AuthService;
import com.library.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final AuthService authService;

    @Override
    public ApiResponse<?> getUserNotifications(Pageable pageable) {
        Long userId = authService.getCurrentUserId();
        Page<Notification> notifications =
                notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        return ApiResponse.success(notifications);
    }

    @Override
    public ApiResponse<?> getUnreadCount() {
        Long userId = authService.getCurrentUserId();
        long count = notificationRepository.countByUserIdAndReadFalse(userId);
        return ApiResponse.success(count);
    }

    @Override
    @Transactional
    public ApiResponse<?> markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("通知不存在"));

        // 检查权限：只能操作自己的通知
        Long userId = authService.getCurrentUserId();
        if (!notification.getUserId().equals(userId)) {
            return ApiResponse.error(403, "无权操作他人通知");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
        return ApiResponse.success("标记为已读");
    }

    @Override
    @Transactional
    public ApiResponse<?> markAllAsRead() {
        Long userId = authService.getCurrentUserId();
        List<Notification> unreadNotifications =
                notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);

        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
        return ApiResponse.success("全部标记为已读");
    }

    @Override
    public ApiResponse<?> getAllNotifications(Pageable pageable) {
        // 检查是否为管理员
        if (!authService.isSystemAdmin()) {
            return ApiResponse.error(403, "需要管理员权限");
        }

        Page<Notification> notifications =
                notificationRepository.findAllByOrderByCreatedAtDesc(pageable);
        return ApiResponse.success(notifications);
    }

    @Override
    @Transactional
    public ApiResponse<?> sendSystemNotification(NotificationDTO notificationDTO) {
        // 检查是否为管理员
        if (!authService.isSystemAdmin()) {
            return ApiResponse.error(403, "需要管理员权限");
        }

        // 如果指定了用户ID，发送给单个用户
        if (notificationDTO.getUserId() != null) {
            User user = userRepository.findById(notificationDTO.getUserId())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            createNotification(
                    user.getId(),
                    notificationDTO.getTitle(),
                    notificationDTO.getContent(),
                    Notification.NotificationType.SYSTEM_NOTIFICATION,
                    false
            );
        } else {
            // 发送给所有用户
            List<User> allUsers = userRepository.findAll();
            for (User user : allUsers) {
                createNotification(
                        user.getId(),
                        notificationDTO.getTitle(),
                        notificationDTO.getContent(),
                        Notification.NotificationType.SYSTEM_NOTIFICATION,
                        false
                );
            }
        }

        return ApiResponse.success("系统通知发送成功");
    }

    @Override
    @Transactional
    public ApiResponse<?> sendImportantNotification(NotificationDTO notificationDTO) {
        if (!authService.isSystemAdmin()) {
            return ApiResponse.error(403, "需要管理员权限");
        }

        List<User> allUsers = userRepository.findAll();
        for (User user : allUsers) {
            createNotification(
                    user.getId(),
                    notificationDTO.getTitle(),
                    notificationDTO.getContent(),
                    Notification.NotificationType.SYSTEM_NOTIFICATION,
                    true  // 重要通知
            );
        }

        return ApiResponse.success("重要通知发送成功");
    }

    @Override
    @Transactional
    public void createNotification(Long userId, String title, String content,
                                   Notification.NotificationType type, boolean important) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setImportant(important);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);

        // 同时发送邮件（重要通知或系统通知才发邮件）
        if (important || type == Notification.NotificationType.SYSTEM_NOTIFICATION) {
            // 这里可以调用邮件服务发送邮件
            // 为简化，我们只记录日志
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                System.out.println("发送通知邮件给: " + user.getEmail());
                System.out.println("标题: " + title);
                System.out.println("内容: " + content);
            }
        }
    }
}