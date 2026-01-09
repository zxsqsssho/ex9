//src/main/java/com/library/service/impl/AuthServiceImpl.java
package com.library.service.impl;

import com.library.dto.ApiResponse;
import com.library.dto.LoginDTO;
import com.library.dto.UserCreateDTO;
import com.library.entity.Role;
import com.library.entity.User;
import com.library.repository.RoleRepository;
import com.library.repository.UserRepository;
import com.library.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Override
    public ApiResponse<?> login(LoginDTO request) {
        try {
            // 验证用户名密码
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 获取用户信息
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 检查用户状态
            if (user.getStatus() == User.UserStatus.INACTIVE) {
                return ApiResponse.error(403, "账户已停用");
            }
            if (user.getStatus() == User.UserStatus.LOCKED) {
                return ApiResponse.error(403, "账户已锁定");
            }
            if (user.getStatus() == User.UserStatus.DELETED) {
                return ApiResponse.error(403, "账户不存在");
            }

            return ApiResponse.success("登录成功", convertToResponseDTO(user));
        } catch (AuthenticationException e) {
            return ApiResponse.error(401, "用户名或密码错误");
        } catch (Exception e) {
            return ApiResponse.error(500, "登录失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public ApiResponse<?> register(UserCreateDTO request) {
        try {
            // 检查用户名和邮箱是否已存在
            if (userRepository.existsByUsername(request.getUsername())) {
                return ApiResponse.error(400, "用户名已存在");
            }
            if (userRepository.existsByEmail(request.getEmail())) {
                return ApiResponse.error(400, "邮箱已存在");
            }

            // 验证用户类型（只允许注册学生和教师）
            if (!request.isRegisterAllowed()) {
                return ApiResponse.error(400, "只能注册学生或教师账号");
            }

            // 创建新用户
            User user = new User();
            user.setUsername(request.getUsername());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setRealName(request.getRealName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setUserType(request.getUserType());
            user.setBranchId(request.getBranchId());
            user.setStatus(User.UserStatus.ACTIVE);

            // 根据用户类型自动分配默认角色（删除了roleId的查找和设置）
            String roleName = "ROLE_" + request.getUserType().name();
            Role defaultRole = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("默认角色不存在: " + roleName));
            user.setRole(defaultRole);

            User savedUser = userRepository.save(user);

            return ApiResponse.success("注册成功", convertToResponseDTO(savedUser));
        } catch (Exception e) {
            return ApiResponse.error(500, "注册失败: " + e.getMessage());
        }
    }

    @Override
    public ApiResponse<?> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return ApiResponse.error(401, "未登录");
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        return ApiResponse.success(convertToResponseDTO(user));
    }

    @Override
    @Transactional
    public ApiResponse<?> changePassword(String oldPassword, String newPassword) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ApiResponse.error(401, "未登录");
            }

            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));

            // 验证旧密码
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                return ApiResponse.error(400, "原密码错误");
            }

            // 更新密码
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            return ApiResponse.success("密码修改成功");
        } catch (Exception e) {
            return ApiResponse.error(500, "修改密码失败: " + e.getMessage());
        }
    }

    @Override
    public boolean hasPermission(String permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority ->
                        grantedAuthority.getAuthority().equals(permission));
    }

    @Override
    public boolean isSystemAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority ->
                        grantedAuthority.getAuthority().equals("ROLE_SYSTEM_ADMIN"));
    }

    @Override
    public boolean isBranchAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority ->
                        grantedAuthority.getAuthority().equals("ROLE_BRANCH_ADMIN"));
    }

    @Override
    public boolean isTeacher() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority ->
                        grantedAuthority.getAuthority().equals("ROLE_TEACHER"));
    }

    @Override
    public boolean isStudent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority ->
                        grantedAuthority.getAuthority().equals("ROLE_STUDENT"));
    }

    @Override
    public Integer getCurrentUserBranchId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .map(User::getBranchId)
                .orElse(null);
    }

    @Override
    public boolean canAccessUser(Integer targetBranchId) {
        // 系统管理员可以访问所有用户
        if (isSystemAdmin()) {
            return true;
        }

        // 分馆管理员只能访问自己分馆的用户
        if (isBranchAdmin()) {
            Integer currentBranchId = getCurrentUserBranchId();
            return currentBranchId != null && currentBranchId.equals(targetBranchId);
        }

        // 教师和学生只能访问自己
        return false;
    }

    @Override
    public boolean canModifyUser(Long userId) {
        if (isSystemAdmin()) {
            return true;
        }

        if (isBranchAdmin()) {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) return false;
            Integer currentBranchId = getCurrentUserBranchId();
            return currentBranchId != null && currentBranchId.equals(user.getBranchId());
        }

        return false;
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

    @Override
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return user.getId();
    }
}