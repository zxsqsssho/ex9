//src/main/java/com/library/dto/LoginDTO.java
package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

//登录专用
@Data
public class LoginDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;
}