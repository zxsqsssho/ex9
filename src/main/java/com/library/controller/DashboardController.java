// src/main/java/com/library/controller/DashboardController.java
package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final AuthService authService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<?> getDashboard() {
        try {
            // 模拟数据，实际应该从数据库统计
            Map<String, Object> data = new HashMap<>();

            // 根据用户角色返回不同的数据
            if (authService.isSystemAdmin()) {
                data.put("currentBorrow", 156);
                data.put("overdue", 12);
                data.put("reservationReminder", 8);
                data.put("fineAmount", 245.5);
                data.put("totalUsers", 532);
                data.put("totalBooks", 15689);
            } else if (authService.isBranchAdmin()) {
                data.put("currentBorrow", 45);
                data.put("overdue", 3);
                data.put("reservationReminder", 2);
                data.put("fineAmount", 65.0);
                data.put("branchId", authService.getCurrentUserBranchId());
            } else if (authService.isTeacher()) {
                data.put("currentBorrow", 8);
                data.put("overdue", 0);
                data.put("reservationReminder", 1);
                data.put("fineAmount", 0);
                data.put("borrowLimit", "无限制");
            } else {
                data.put("currentBorrow", 5);
                data.put("overdue", 0);
                data.put("reservationReminder", 1);
                data.put("fineAmount", 0);
                data.put("borrowLimit", "16本");
            }

            return ApiResponse.success("获取仪表板数据成功", data);
        } catch (Exception e) {
            return ApiResponse.error(500, "获取仪表板数据失败: " + e.getMessage());
        }
    }
}