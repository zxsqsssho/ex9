//src/main/java/com/library/entity/Books.java
package com.library.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("books")
public class Books {
    @TableId(type = IdType.AUTO)
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
