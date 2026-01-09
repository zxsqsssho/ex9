//src/main/java/com/library/dto/UserUpdateDTO.java
package com.library.dto;

import com.library.entity.User;
import jakarta.validation.constraints.Email;
import lombok.Data;

//用于更新用户信息
@Data
public class UserUpdateDTO {
    private String realName;

    @Email(message = "邮箱格式不正确")
    private String email;

    private String phone;
    private User.UserType userType;
    private Long roleId;
    private Integer branchId; // 分馆ID
    private User.UserStatus status;
    private String password; // 可选：重置密码
}