//src/main/java/com/library/entity/BorrowRecord.java
package com.library.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "borrow_records")
@Data
public class BorrowRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long bookId;

    @Column(nullable = false)
    private LocalDateTime borrowTime;

    @Column(nullable = false)
    private LocalDateTime dueTime;

    private LocalDateTime returnTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BorrowStatus status;

    private Integer overdueDays;

    @Column(nullable = false)
    private Integer branchId;

    private Integer reminderCount;

    public enum BorrowStatus {
        BORROWED,   // 借阅中
        RETURNED,   // 已归还
        OVERDUE     // 逾期
    }
}
