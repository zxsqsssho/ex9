//src/main/java/com/library/controller/UserController.java
package com.library.controller;

import com.library.dto.UserCreateDTO;
import com.library.dto.UserUpdateDTO;
import com.library.dto.ApiResponse;
import com.library.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理接口
 * 所有接口都需要认证，具体权限见每个方法的注解
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 创建用户
     * 访问权限：系统管理员(SYSTEM_ADMIN) 或 分馆管理员(BRANCH_ADMIN)
     * 说明：分馆管理员只能创建本分馆的用户
     */
    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> createUser(@Valid @RequestBody UserCreateDTO userDTO) {
        return userService.createUser(userDTO);
    }

    /**
     * 更新用户信息（支持部分更新）
     * 访问权限：系统管理员(SYSTEM_ADMIN) 或 分馆管理员(BRANCH_ADMIN)
     * 说明：分馆管理员只能更新本分馆的用户
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> updateUser(@PathVariable Long id,
                                     @Valid @RequestBody UserUpdateDTO userDTO) {
        return userService.updateUser(id, userDTO);
    }

    /**
     * 删除用户（软删除）
     * 访问权限：系统管理员(SYSTEM_ADMIN)
     * 说明：分馆管理员无此权限
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id);
    }

    /**
     * 根据ID获取用户信息
     * 访问权限：系统管理员(SYSTEM_ADMIN) 或 分馆管理员(BRANCH_ADMIN)
     * 说明：分馆管理员只能查看本分馆的用户
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    /**
     * 分页获取用户列表
     * 访问权限：系统管理员(SYSTEM_ADMIN) 或 分馆管理员(BRANCH_ADMIN)
     * 说明：分馆管理员只能查看本分馆的用户
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param sort 排序字段
     */
    @GetMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        return userService.getAllUsers(pageable);
    }

    /**
     * 按用户类型查询
     * 访问权限：系统管理员(SYSTEM_ADMIN) 或 分馆管理员(BRANCH_ADMIN)
     * 说明：分馆管理员只能查看本分馆的用户
     * @param userType 用户类型：STUDENT, TEACHER, BRANCH_ADMIN, SYSTEM_ADMIN
     */
    @GetMapping("/type/{userType}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> getUsersByType(@PathVariable String userType) {
        return userService.getUsersByType(userType);
    }

    /**
     * 搜索用户（可按用户名、真实姓名、邮箱搜索）
     * 访问权限：系统管理员(SYSTEM_ADMIN) 或 分馆管理员(BRANCH_ADMIN)
     * 说明：分馆管理员只能搜索本分馆的用户
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> searchUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userService.searchUsers(keyword, pageable);
    }

    /**
     * 修改用户状态
     * 访问权限：系统管理员(SYSTEM_ADMIN) 或 分馆管理员(BRANCH_ADMIN)
     * 说明：分馆管理员只能修改本分馆的用户
     * @param status 状态：ACTIVE, INACTIVE, LOCKED, DELETED
     */
    @PatchMapping("/{id}/status/{status}")  // 修改这里
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> changeUserStatus(@PathVariable Long id,
                                           @PathVariable String status) {  // 修改这里
        return userService.changeUserStatus(id, status);
    }

    /**
     * 重置用户密码
     * 访问权限：系统管理员(SYSTEM_ADMIN)
     * 说明：分馆管理员只能修改本分馆的用户
     */
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> resetPassword(@PathVariable Long id) {
        return userService.resetPassword(id);
    }

    /**
     * 获取指定分馆的用户列表
     * 访问权限：系统管理员(SYSTEM_ADMIN) 或 分馆管理员(BRANCH_ADMIN)
     * 说明：分馆管理员无此权限
     */
    @GetMapping("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    @PreAuthorize("hasRole('SYSTEM_ADMIN')")
    public ApiResponse<?> getUsersByBranch(
            @PathVariable Integer branchId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return userService.getUsersByBranch(branchId, pageable);
    }
}