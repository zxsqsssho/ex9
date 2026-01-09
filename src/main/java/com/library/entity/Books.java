//src/main/java/com/library/entity/Books.java
package com.library.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;


@Entity
@Table(name = "books") // 对应数据库表名
@Data
public class Books {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自增主键（对应MySQL的AUTO_INCREMENT）
    @jakarta.persistence.Id
    private Integer bookId;          // 图书ID
    private String bookName;         // 图书名称
    private String author;           // 作者
    private String isbn;             // ISBN编号
    private String category;         // 图书分类
    private Integer branchId;        // 所属分馆ID
    private String bookType;         // 类型：book/magazine
    private Integer totalNum;        // 总数量
    private Integer availableNum;    // 可借数量
    private String status;           // 状态：normal/out_of_stock
    private LocalDateTime createTime;// 创建时间
}
