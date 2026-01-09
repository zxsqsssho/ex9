//src/main/java/com/library/entity/Branches.java
package com.library.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("branches")
public class Branches {
    @TableId(type = IdType.AUTO)
    private Integer branchId;        // 分馆ID
    private String branchName;       // 分馆名称
    private String address;          // 分馆地址
    private LocalDateTime createTime;// 创建时间
}
