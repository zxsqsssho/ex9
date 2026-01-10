// BookCreateDTO.java
package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BookCreateDTO {
    @NotBlank(message = "图书名称不能为空")
    private String bookName;

    @NotBlank(message = "作者不能为空")
    private String author;

    @NotBlank(message = "ISBN不能为空")
    private String isbn;

    @NotBlank(message = "分类不能为空")
    private String category;

    @NotNull(message = "分馆ID不能为空")
    private Integer branchId;

    @NotBlank(message = "图书类型不能为空")
    private String bookType; // book/magazine

    @NotNull(message = "总数量不能为空")
    @Positive(message = "总数量必须大于0")
    private Integer totalNum;

    @NotNull(message = "可借数量不能为空")
    private Integer availableNum;
}