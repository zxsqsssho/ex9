package com.library.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FineDTO {
    private Long fineId;       // 罚款ID
    private Long recordId;     // 关联借阅记录ID
    private BigDecimal fineAmount; // 罚款金额
    private String payStatus;  // 支付状态（unpaid/paid）
    private LocalDateTime payTime; // 支付时间
    private String bookName;   // 关联图书名称（新增）
    private String isbn;       // 图书ISBN（新增）
    private Integer overdueDays; // 逾期天数（新增）
    private String branchName; // 分馆名称（新增）
}