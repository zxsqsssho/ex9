package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.service.FineService;
import com.library.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fines")
@RequiredArgsConstructor
public class FinesController {
    private final FineService fineService;
    private final AuthService authService;

    /**
     * 获取当前用户的罚款记录（支持支付状态筛选）
     * 核心修复：添加 payStatus 参数接收，传递给 Service
     */
    @GetMapping("/my-fines")
    public ApiResponse<?> getMyFines(
            @RequestParam(required = false) String payStatus, // 接收前端传递的支付状态参数
            Pageable pageable) {
        // 调用 Service 方法，传递 2 个参数（与 Service 定义一致）
        return fineService.getMyFines(payStatus, pageable);
    }

    // 其他方法保持不变...
    @PostMapping("/pay/{fineId}")
    public ApiResponse<?> payFine(@PathVariable Long fineId) {
        return fineService.payFine(fineId);
    }

    @PostMapping("/batch-pay")
    public ApiResponse<?> batchPayFines(@RequestBody List<Long> fineIds) {
        return fineService.batchPayFines(fineIds);
    }

    @GetMapping("/{fineId}")
    public ApiResponse<?> getFineDetail(@PathVariable Long fineId) {
        return fineService.getFineDetail(fineId);
    }

    @PostMapping("/{fineId}/apply-reduction")
    public ApiResponse<?> applyFineReduction(
            @PathVariable Long fineId,
            @RequestParam String reason) {
        return fineService.applyFineReduction(fineId, reason);
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> getAllFines(
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String bookName,
            @RequestParam(required = false) String payStatus,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageNum - 1, pageSize);
        Integer branchId = null;
        if (authService.isBranchAdmin()) {
            branchId = authService.getCurrentUserBranchId();
        }
        return fineService.getAllFines(
                userName,
                bookName,
                branchId,
                payStatus,
                pageable
        );
    }

    @PutMapping("/{fineId}/status")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> updateFineStatus(
            @PathVariable Long fineId,
            @RequestParam String status) {
        return fineService.updateFineStatus(fineId, status);
    }
}