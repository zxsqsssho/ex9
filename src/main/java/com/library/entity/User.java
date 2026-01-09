//src/main/java/com/library/entity/User.java
package com.library.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户表
 */
@Entity
@Table(name = "users")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String realName;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    //用于映射枚举类型的字段到数据库列
    @Enumerated(EnumType.STRING)//EnumType.STRING：表示将枚举的名称（字符串）存储到数据库中。
    @Column(nullable = false)
    private UserType userType; // STUDENT(学生), TEACHER(老师), BRANCH_ADMIN(管理员), SYSTEM_ADMIN(系统管理员)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;
    // ACTIVE(活跃状态，用户可以正常使用系统。), INACTIVE(未激活状态，可能是注册后未激活，或者暂时停用。)
    // LOCKED(锁定状态，通常是因为多次登录失败或其他安全原因被锁定，需要管理员解锁。)
    // DELETED(删除状态，表示用户已被删除（可能是软删除）。)

    @ManyToOne//表示多对一关系。多个用户（User）可以属于同一个角色（Role）。这是JPA中定义多对一关联的注解。
    @JoinColumn(name = "role_id")//指定外键的列名。这里表示在users表中会有一个名为role_id的列，用来存储指向roles表的外键。
    private Role role;

    private Integer branchId; // 分馆ID，对于分馆管理员

    @Column(updatable = false)//表示该列在更新（update）操作时不可更新。也就是说，一旦创建，这个字段的值就不能再改变。
    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @PrePersist//在实体被持久化（即插入到数据库）之前调用。
    protected void onCreate() {
        createTime = LocalDateTime.now();
        updateTime = LocalDateTime.now();
    }

    @PreUpdate//在实体被更新（即更新到数据库）之前调用。
    protected void onUpdate() {
        updateTime = LocalDateTime.now();
    }

    public enum UserType {
        STUDENT, TEACHER, BRANCH_ADMIN, SYSTEM_ADMIN
    }

    public enum UserStatus {
        ACTIVE, INACTIVE, LOCKED, DELETED
    }
}