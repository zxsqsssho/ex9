// BookUpdateDTO.java
package com.library.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class BookUpdateDTO {
    private String bookName;
    private String author;
    private String category;
    private String bookType;
    private Integer totalNum;

    @PositiveOrZero(message = "可借数量不能为负数")
    private Integer availableNum;

    private String status; // normal/out_of_stock
}