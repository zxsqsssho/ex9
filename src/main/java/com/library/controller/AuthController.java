//src/main/java/com/library/controller/AuthController.java
package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.LoginDTO;
import com.library.dto.UserCreateDTO;
import com.library.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证相关接口
 * 包含注册、登录等公共接口
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录接口
     * 访问权限：公开
     * 使用方式：Basic Auth或调用此接口获取用户信息
     */
    @PostMapping("/login")
    public ApiResponse<?> login(@Valid @RequestBody LoginDTO request) {
        return authService.login(request);
    }

    /**
     * 用户注册接口
     * 访问权限：公开
     * 可注册类型：学生(STUDENT)、教师(TEACHER)
     */
    @PostMapping("/register")
    public ApiResponse<?> register(@Valid @RequestBody UserCreateDTO request) {
        return authService.register(request);
    }

    /**
     * 获取当前登录用户信息
     * 访问权限：已登录用户
     * 说明：返回当前用户的完整信息，包括角色、权限、个人资料等
     */
    @GetMapping("/current-user")
    public ApiResponse<?> getCurrentUser() {
        return authService.getCurrentUser();
    }

    /**
     * 检查当前用户是否有指定权限
     * 访问权限：已登录用户
     * @param permission 权限字符串，如 "user:create"
     */
    @GetMapping("/check-permission/{permission}")
    public ApiResponse<?> checkPermission(@PathVariable String permission) {
        boolean hasPermission = authService.hasPermission(permission);
        return ApiResponse.success(hasPermission);
    }

    /**
     * 修改当前用户密码
     * 访问权限：已登录用户
     */
    @PostMapping("/change-password")
    public ApiResponse<?> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        return authService.changePassword(oldPassword, newPassword);
    }
}