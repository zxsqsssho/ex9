// src/main/java/com/library/entity/Fine.java
package com.library.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fines")
@Data
public class Fine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long fineId;

    @Column(nullable = false)
    private Long recordId; // 关联借阅记录ID

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal fineAmount; // 罚款金额

    @Column(nullable = false)
    private String payStatus; // unpaid/paid

    private LocalDateTime payTime; // 支付时间
}
