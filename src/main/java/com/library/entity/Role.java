//src/main/java/com/library/entity/Role.java
package com.library.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Set;

@Entity
@Table(name = "roles")
@Data
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;

    @ElementCollection(fetch = FetchType.EAGER)//1. 这是一个String集合，EAGER表示立即加载
    @CollectionTable(// 2. 指定关联表信息
            name = "role_permissions",// 表名为role_permissions
            joinColumns = @JoinColumn(name = "role_id")) // 外键列，引用roles表的主键
    @Column(name = "permission") // 3. permission表中存储权限的列名
    private Set<String> permissions;           // 4. 存储权限字符串的集合

    @Column(nullable = false)
    private boolean systemRole = false; // 系统内置角色，不可删除
}