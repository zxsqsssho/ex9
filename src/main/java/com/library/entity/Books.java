//src/main/java/com/library/entity/Books.java
package com.library.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;


@Entity
@Table(name = "books")
@Data
public class Books {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer bookId;

    private String bookName;
    private String author;
    private String isbn;
    private String category;

    // 用于写入 / 查询条件
    @Column(name = "branch_id")
    private Integer branchId;

    private String bookType;
    private Integer totalNum;
    private Integer availableNum;
    private String status;
    private LocalDateTime createTime;

    // 用于“显示分馆名称”的关联对象
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "branch_id",          // books 表字段
            referencedColumnName = "branch_id", // branches 表主键
            insertable = false,
            updatable = false
    )
    private Branches branch;
}
