package com.library.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 接收预定图书的参数DTO
 */
@Data
public class ReservationCreateDTO {
    @NotNull(message = "图书ID不能为空")
    private Long bookId;

    @NotNull(message = "分馆ID不能为空")
    private Integer branchId;
}