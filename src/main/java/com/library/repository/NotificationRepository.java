// src/main/java/com/library/repository/NotificationRepository.java
package com.library.repository;

import com.library.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 查找用户的所有通知（按时间倒序）
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 查找用户未读通知
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Long userId);

    // 统计用户未读通知数量
    long countByUserIdAndReadFalse(Long userId);

    // 查找所有通知（管理员用）
    Page<Notification> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 按类型查找用户通知
    Page<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(
            Long userId, Notification.NotificationType type, Pageable pageable);
}