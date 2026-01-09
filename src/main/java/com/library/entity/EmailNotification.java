//src/main/java/com/library/entity/EmailNotification.java
package com.library.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_notifications")
@Data
public class EmailNotification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(nullable = false)
    private LocalDateTime createTime;

    private LocalDateTime sendTime;

    private String errorMessage;

    @PrePersist
    protected void onCreate() {
        createTime = LocalDateTime.now();
        status = NotificationStatus.PENDING;
    }

    public enum NotificationType {
        RESERVATION_AVAILABLE,    // 预定图书可借
        RESERVATION_NOTIFY_LENDER, // 图书被预定通知当前借阅者
        OVERDUE_REMINDER,          // 逾期提醒
        RESERVATION_EXPIRY         // 预定到期
    }

    public enum NotificationStatus {
        PENDING,    // 待发送
        SENT,       // 已发送
        FAILED      // 发送失败
    }
}
