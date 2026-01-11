//src/main/java/com/library/service/impl/UserServiceImpl.java
package com.library.service.impl;

import com.library.dto.UserCreateDTO;
import com.library.dto.UserUpdateDTO;
import com.library.dto.ApiResponse;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.repository.RoleRepository;
import com.library.repository.UserRepository;
import com.library.service.AuthService;
import com.library.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    @Override
    @Transactional
    public ApiResponse<?> createUser(UserCreateDTO userDTO) {
        try {
            // 检查权限
            if (!authService.isSystemAdmin() && !authService.isBranchAdmin()) {
                return ApiResponse.error(403, "权限不足");
            }

            // 检查用户名和邮箱是否已存在
            if (userRepository.existsByUsername(userDTO.getUsername())) {
                return ApiResponse.error(400, "用户名已存在");
            }
            if (userRepository.existsByEmail(userDTO.getEmail())) {
                return ApiResponse.error(400, "邮箱已存在");
            }

            // 创建新用户
            User user = new User();
            user.setUsername(userDTO.getUsername());
            user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            user.setRealName(userDTO.getRealName());
            user.setEmail(userDTO.getEmail());
            user.setPhone(userDTO.getPhone());
            user.setUserType(userDTO.getUserType());
            user.setStatus(User.UserStatus.ACTIVE);

            // 设置分馆
            if (authService.isBranchAdmin()) {
                // 分馆管理员只能创建本分馆的用户
                user.setBranchId(authService.getCurrentUserBranchId());
            } else {
                user.setBranchId(userDTO.getBranchId());
            }

            // 根据用户类型自动分配默认角色（删除了roleId的判断）
            String roleName = "ROLE_" + userDTO.getUserType().name();
            Role defaultRole = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("默认角色不存在: " + roleName));
            user.setRole(defaultRole);

            User savedUser = userRepository.save(user);
            return ApiResponse.success("用户创建成功", convertToResponseDTO(savedUser));
        } catch (Exception e) {
            return ApiResponse.error(500, "创建用户失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResponse<?> updateUser(Long id, UserUpdateDTO userDTO) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("用户不存在"));

            // 检查权限
            if (!authService.canModifyUser(id)) {
                return ApiResponse.error(403, "无权修改该用户");
            }

            // 记录原始值，用于检查是否有修改
            User.UserType originalUserType = user.getUserType();
            Integer originalBranchId = user.getBranchId();

            // 更新用户信息（只更新非空字段）
            if (userDTO.getRealName() != null) {
                user.setRealName(userDTO.getRealName());
            }
            if (userDTO.getEmail() != null) {
                // 检查邮箱是否重复（排除自己）
                if (!user.getEmail().equals(userDTO.getEmail()) &&
                        userRepository.existsByEmail(userDTO.getEmail())) {
                    return ApiResponse.error(400, "邮箱已存在");
                }
                user.setEmail(userDTO.getEmail());
            }
            if (userDTO.getPhone() != null) {
                user.setPhone(userDTO.getPhone());
            }

            // 检查用户类型修改权限
            if (userDTO.getUserType() != null) {
                // 检查是否实际修改了用户类型
                if (!originalUserType.equals(userDTO.getUserType())) {
                    // 只有系统管理员可以修改用户类型
                    if (!authService.isSystemAdmin()) {
                        return ApiResponse.error(403, "分馆管理员无权修改用户类型");
                    }
                    user.setUserType(userDTO.getUserType());
                }
            }

            // 检查分馆ID修改权限
            if (userDTO.getBranchId() != null) {
                // 检查是否实际修改了分馆ID
                if (!originalBranchId.equals(userDTO.getBranchId())) {
                    // 只有系统管理员可以修改分馆ID
                    if (!authService.isSystemAdmin()) {
                        return ApiResponse.error(403, "分馆管理员无权修改分馆ID");
                    }
                    user.setBranchId(userDTO.getBranchId());
                }
            }

            // 检查状态修改权限
            if (userDTO.getStatus() != null && authService.canModifyUser(id)) {
                // 检查分馆管理员不能修改状态为DELETED
                if (authService.isBranchAdmin() &&
                        userDTO.getStatus() == User.UserStatus.DELETED) {
                    return ApiResponse.error(403, "分馆管理员不能删除用户");
                }
                user.setStatus(userDTO.getStatus());
            }

            // 检查角色ID修改权限
            if (userDTO.getRoleId() != null) {
                // 只有系统管理员可以修改角色
                if (!authService.isSystemAdmin()) {
                    return ApiResponse.error(403, "分馆管理员无权修改用户角色");
                }
                Role role = roleRepository.findById(userDTO.getRoleId())
                        .orElseThrow(() -> new EntityNotFoundException("角色不存在"));
                user.setRole(role);
            }

            // 检查密码修改权限
            if (userDTO.getPassword() != null) {
                // 只有系统管理员可以重置密码
                if (!authService.isSystemAdmin()) {
                    return ApiResponse.error(403, "分馆管理员无权重置密码");
                }
                user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            }

            User updatedUser = userRepository.save(user);
            return ApiResponse.success("用户更新成功", convertToResponseDTO(updatedUser));
        } catch (Exception e) {
            return ApiResponse.error(500, "更新用户失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResponse<?> changeUserStatus(Long id, String status) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("用户不存在"));

            // 检查权限（系统管理员和分馆管理员可以修改状态）
            if (!authService.canModifyUser(id)) {
                return ApiResponse.error(403, "无权修改用户状态");
            }

            // 检查分馆管理员不能将用户状态改为DELETED
            User.UserStatus newStatus = User.UserStatus.valueOf(status.toUpperCase());
            if (authService.isBranchAdmin() && newStatus == User.UserStatus.DELETED) {
                return ApiResponse.error(403, "分馆管理员不能删除用户");
            }

            user.setStatus(newStatus);
            userRepository.save(user);
            return ApiResponse.success("用户状态更新成功", convertToResponseDTO(user));
        } catch (Exception e) {
            return ApiResponse.error(500, "更新用户状态失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResponse<?> resetPassword(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("用户不存在"));

            // 检查权限（系统管理员和分馆管理员可以重置密码）
            if (!authService.canModifyUser(id)) {
                return ApiResponse.error(403, "无权重置密码");
            }

            // 重置为默认密码
            user.setPassword(passwordEncoder.encode("123456"));
            userRepository.save(user);
            return ApiResponse.success("密码重置成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "重置密码失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResponse<?> deleteUser(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("用户不存在"));

            // 检查权限（只有系统管理员可以删除）
            if (!authService.isSystemAdmin()) {
                return ApiResponse.error(403, "无权删除用户");
            }

            // 不能删除系统管理员
            if (user.getUserType() == User.UserType.SYSTEM_ADMIN) {
                return ApiResponse.error(403, "不能删除系统管理员");
            }

            // 软删除：修改状态为DELETED
            user.setStatus(User.UserStatus.DELETED);
            userRepository.save(user);
            return ApiResponse.success("用户删除成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "删除用户失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<?> getUserById(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("用户不存在"));

            // 检查权限
            if (!authService.canAccessUser(user.getBranchId())) {
                return ApiResponse.error(403, "无权访问该用户");
            }

            return ApiResponse.success(convertToResponseDTO(user));
        } catch (Exception e) {
            return ApiResponse.error(500, "获取用户失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<?> getAllUsers(Pageable pageable) {
        try {
            if (authService.isSystemAdmin()) {
                // 系统管理员：获取所有用户
                Page<User> userPage = userRepository.findAll(pageable);
                return ApiResponse.success(userPage.map(this::convertToResponseDTO));
            } else if (authService.isBranchAdmin()) {
                // 分馆管理员：只获取本分馆用户
                Integer branchId = authService.getCurrentUserBranchId();
                List<User> branchUsers = userRepository.findByBranchId(branchId);

                // 手动分页
                int start = (int) pageable.getOffset();
                int end = Math.min((start + pageable.getPageSize()), branchUsers.size());
                List<User> pagedUsers = branchUsers.subList(start, end);

                return ApiResponse.success(Map.of(
                        "content", pagedUsers.stream().map(this::convertToResponseDTO).collect(Collectors.toList()),
                        "total", branchUsers.size(),
                        "page", pageable.getPageNumber(),
                        "size", pagedUsers.size()
                ));
            } else {
                return ApiResponse.error(403, "权限不足");
            }
        } catch (Exception e) {
            return ApiResponse.error(500, "获取用户列表失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<?> getUsersByType(String userType) {
        try {
            User.UserType type = User.UserType.valueOf(userType.toUpperCase());
            List<User> users = userRepository.findByUserType(type);

            // 权限过滤
            if (authService.isBranchAdmin()) {
                Integer branchId = authService.getCurrentUserBranchId();
                users = users.stream()
                        .filter(user -> branchId.equals(user.getBranchId()))
                        .collect(Collectors.toList());
            }

            return ApiResponse.success(users.stream().map(this::convertToResponseDTO).collect(Collectors.toList()));
        } catch (Exception e) {
            return ApiResponse.error(400, "无效的用户类型");
        }
    }

    @Override
    public ApiResponse<?> searchUsers(String keyword, Pageable pageable) {
        try {
            List<User> allUsers = userRepository.findAll();

            // 权限过滤
            if (authService.isBranchAdmin()) {
                Integer branchId = authService.getCurrentUserBranchId();
                allUsers = allUsers.stream()
                        .filter(user -> branchId.equals(user.getBranchId()))
                        .collect(Collectors.toList());
            }

            // 关键词搜索
            if (keyword != null && !keyword.trim().isEmpty()) {
                final String searchKey = keyword.toLowerCase();
                allUsers = allUsers.stream()
                        .filter(user ->
                                user.getUsername().toLowerCase().contains(searchKey) ||
                                        user.getRealName().toLowerCase().contains(searchKey) ||
                                        user.getEmail().toLowerCase().contains(searchKey))
                        .collect(Collectors.toList());
            }

            // 手动分页
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), allUsers.size());
            List<User> pagedUsers = allUsers.subList(start, end);

            return ApiResponse.success(Map.of(
                    "content", pagedUsers.stream().map(this::convertToResponseDTO).collect(Collectors.toList()),
                    "total", allUsers.size(),
                    "page", pageable.getPageNumber(),
                    "size", pagedUsers.size()
            ));
        } catch (Exception e) {
            return ApiResponse.error(500, "搜索用户失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<?> getUsersByBranch(Integer branchId, Pageable pageable) {
        try {
            // 只有系统管理员可以查询所有分馆的用户
            if (!authService.isSystemAdmin()) {
                return ApiResponse.error(403, "权限不足");
            }

            List<User> branchUsers = userRepository.findByBranchId(branchId);

            // 手动分页
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), branchUsers.size());
            List<User> pagedUsers = branchUsers.subList(start, end);

            return ApiResponse.success(Map.of(
                    "content", pagedUsers.stream().map(this::convertToResponseDTO).collect(Collectors.toList()),
                    "total", branchUsers.size(),
                    "page", pageable.getPageNumber(),
                    "size", pagedUsers.size()
            ));
        } catch (Exception e) {
            return ApiResponse.error(500, "获取分馆用户失败: " + e.getMessage());
        }
    }

    // 转换为响应DTO
    private com.library.dto.UserResponseDTO convertToResponseDTO(User user) {
        com.library.dto.UserResponseDTO dto = new com.library.dto.UserResponseDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setUserType(user.getUserType());
        dto.setStatus(user.getStatus());
        dto.setBranchId(user.getBranchId());
        dto.setCreateTime(user.getCreateTime());
        dto.setUpdateTime(user.getUpdateTime());

        if (user.getRole() != null) {
            com.library.dto.RoleDTO roleDTO = new com.library.dto.RoleDTO();
            roleDTO.setId(user.getRole().getId());
            roleDTO.setName(user.getRole().getName());
            roleDTO.setDescription(user.getRole().getDescription());
            roleDTO.setSystemRole(user.getRole().isSystemRole());
            dto.setRole(roleDTO);
        }

        return dto;
    }
}