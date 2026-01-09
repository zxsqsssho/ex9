//src/main/java/com/library/dto/UserResponseDTO.java
package com.library.dto;

import com.library.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

//用于返回用户信息，不包含敏感数据
@Data
public class UserResponseDTO {
    private Long id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private User.UserType userType;
    private User.UserStatus status;
    private RoleDTO role;
    private Integer branchId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

