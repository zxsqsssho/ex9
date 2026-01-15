package com.library.dto;

import com.library.entity.Reservation;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReservationDTO {
    private Long id;           // 预定ID
    private Long userId;       // 用户ID
    private Long bookId;       // 图书ID
    private String bookName;   // 图书名称（新增）
    private String author;     // 图书作者（新增）
    private LocalDateTime reserveTime; // 预定时间
    private LocalDateTime expiryTime; // 预定有效期
    private Reservation.ReservationStatus status; // 预定状态
    private Integer branchId;  // 分馆ID
    private String branchName; // 分馆名称（新增）
}