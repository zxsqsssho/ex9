//src/main/java/com/library/dto/BookQueryDTO.java
package com.library.dto;

import lombok.Data;

@Data
public class BookQueryDTO {
    private String bookName;        // 图书名称（模糊）
    private String author;          // 作者（模糊）
    private String isbn;            // ISBN（精确）
    private String category;        // 分类
    private Integer branchId;       // 分馆ID
    private String bookType;        // 类型：book/magazine
    private String status;          // 状态：normal/out_of_stock
    private Integer pageNum = 1;    // 页码
    private Integer pageSize = 10;  // 页大小
}
