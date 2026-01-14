//src/main/java/com/library/dto/BookListDTO.java
//查询里返回所属分馆名用 DTO
package com.library.dto;

import lombok.Data;

@Data
public class BookListDTO {
    private Integer bookId;
    private String bookName;
    private String author;
    private String isbn;
    private String category;
    private String bookType;
    private Integer availableNum;
    private String status;

    private Integer branchId;
    private String branchName;   // ⭐ 前端要的
}

