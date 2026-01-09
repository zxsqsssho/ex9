//src/main/java/com/library/service/AuthService.java
package com.library.service;

import com.library.dto.ApiResponse;
import com.library.dto.LoginDTO;
import com.library.dto.UserCreateDTO;

public interface AuthService {
    ApiResponse<?> login(LoginDTO request);
    ApiResponse<?> register(UserCreateDTO request);
    ApiResponse<?> getCurrentUser();
    ApiResponse<?> changePassword(String oldPassword, String newPassword);
    boolean hasPermission(String permission);
    boolean isSystemAdmin();
    boolean isBranchAdmin();
    boolean isTeacher();
    boolean isStudent();
    Integer getCurrentUserBranchId();
    boolean canAccessUser(Integer targetBranchId);
    boolean canModifyUser(Long userId);
    Long getCurrentUserId();
}