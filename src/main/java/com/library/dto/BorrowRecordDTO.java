package com.library.dto;

import com.library.entity.BorrowRecord;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BorrowRecordDTO {
    private Long id;           // 借阅记录ID
    private Long userId;       // 用户ID
    private Long bookId;       // 图书ID
    private String bookName;   // 图书名称（新增）
    private String author;     // 图书作者（新增）
    private LocalDateTime borrowTime; // 借阅时间
    private LocalDateTime dueTime;   // 应还时间
    private LocalDateTime returnTime; // 归还时间
    private BorrowRecord.BorrowStatus status; // 借阅状态
    private Integer overdueDays; // 逾期天数
    private Integer branchId;  // 分馆ID
    private String branchName; // 分馆名称（新增）
}