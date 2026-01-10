// BorrowCreateDTO.java
package com.library.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BorrowCreateDTO {
    @NotNull(message = "图书ID不能为空")
    private Long bookId;

    @NotNull(message = "分馆ID不能为空")
    private Integer branchId;
}