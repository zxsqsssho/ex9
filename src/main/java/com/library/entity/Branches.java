//src/main/java/com/library/entity/Branches.java
package com.library.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

@Entity
@Table(name = "branches") // 对应数据库表名
@Data
public class Branches {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自增主键（对应MySQL的AUTO_INCREMENT）
    @jakarta.persistence.Id
    private Integer branchId;        // 分馆ID
    private String branchName;       // 分馆名称
    private String address;          // 分馆地址
    private LocalDateTime createTime;// 创建时间
}
