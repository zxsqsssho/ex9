package com.library.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

// 通知实体类
@Entity
@Table(name = "notifications")
@Data
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;  // 接收用户ID

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;  // 通知类型

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(nullable = false)
    private boolean important = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum NotificationType {
        RESERVATION_AVAILABLE,    // 预定可借
        RESERVATION_REMINDER,     // 预定提醒
        OVERDUE_REMINDER,         // 逾期提醒
        SYSTEM_NOTIFICATION       // 系统通知
    }
}