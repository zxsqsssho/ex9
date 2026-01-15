package com.library.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Data
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long bookId;

    @Column(nullable = false)
    private LocalDateTime reserveTime;

    @Column(nullable = false)
    private LocalDateTime expiryTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(nullable = false)
    private Integer branchId;

    // 对齐数据库状态：PENDING/READY/CANCELLED/COMPLETED（删除BORROWING）
    public enum ReservationStatus {
        PENDING,    // 等待中
        READY,      // 可借阅
        CANCELLED,  // 已取消
        COMPLETED   // 已完成
    }
}