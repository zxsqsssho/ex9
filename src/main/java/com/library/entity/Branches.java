//src/main/java/com/library/entity/Branches.java
package com.library.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "branches") // 对应数据库表名
@Data
public class Branches {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自增主键（对应MySQL的AUTO_INCREMENT）
    @Column(name = "branch_id")
    private Integer branchId;        // 分馆ID
    @Column(name = "branch_name")
    private String branchName;       // 分馆名称
    private String address;          // 分馆地址
    private LocalDateTime createTime;// 创建时间
}
