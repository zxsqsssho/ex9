//src/main/java/com/library/service/UserService.java
package com.library.service;

import com.library.dto.UserCreateDTO;
import com.library.dto.UserUpdateDTO;
import com.library.dto.ApiResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {
    ApiResponse<?> createUser(UserCreateDTO userDTO);
    ApiResponse<?> updateUser(Long id, UserUpdateDTO userDTO);
    ApiResponse<?> deleteUser(Long id);
    ApiResponse<?> getUserById(Long id);
    ApiResponse<?> getAllUsers(Pageable pageable);
    ApiResponse<?> getUsersByType(String userType);
    ApiResponse<?> searchUsers(String keyword, Pageable pageable);
    ApiResponse<?> changeUserStatus(Long id, String status);
    ApiResponse<?> getUsersByBranch(Integer branchId, Pageable pageable);
    ApiResponse<?> resetPassword(Long id);
}