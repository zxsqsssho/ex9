//src/main/java/com/library/dto/UserCreateDTO.java
package com.library.dto;

import com.library.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserCreateDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "邮箱不能为空")
    private String email;

    private String phone;

    @NotNull(message = "用户类型不能为空")
    private User.UserType userType; // STUDENT, TEACHER, BRANCH_ADMIN, SYSTEM_ADMIN

    private Integer branchId; // 分馆ID

    // 用于注册验证，检查是否是允许注册的用户类型
    public boolean isRegisterAllowed() {
        return userType == User.UserType.STUDENT || userType == User.UserType.TEACHER;
    }
}