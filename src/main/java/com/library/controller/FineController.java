// src/main/java/com/library/controller/FineController.java
package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.service.FineService;
import com.library.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fines")
@RequiredArgsConstructor
public class FineController {
    private final FineService fineService;

    /**
     * 获取当前用户罚款记录
     */
    @GetMapping("/my-fines")
    public ApiResponse<?> getMyFines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return fineService.getMyFines(pageable);
    }

    /**
     * 支付罚款
     */
    @PostMapping("/pay/{fineId}")
    public ApiResponse<?> payFine(@PathVariable Long fineId) {
        return fineService.payFine(fineId);
    }

    /**
     * 批量支付罚款
     */
    @PostMapping("/batch-pay")
    public ApiResponse<?> batchPayFines(@RequestBody List<Long> fineIds) {
        return fineService.batchPayFines(fineIds);
    }

    /**
     * 获取罚款详情
     */
    @GetMapping("/{fineId}")
    public ApiResponse<?> getFineDetail(@PathVariable Long fineId) {
        return fineService.getFineDetail(fineId);
    }

    /**
     * 申请减免罚款
     */
    @PostMapping("/{fineId}/apply-reduction")
    public ApiResponse<?> applyFineReduction(@PathVariable Long fineId, @RequestParam String reason) {
        return fineService.applyFineReduction(fineId, reason);
    }

    /**
     * 管理员获取所有罚款记录
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> getAllFines(
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) String payStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return fineService.getAllFines(branchId, payStatus, pageable);
    }

    /**
     * 管理员更新罚款状态
     */
    @PutMapping("/{fineId}/status")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> updateFineStatus(@PathVariable Long fineId, @RequestParam String status) {
        return fineService.updateFineStatus(fineId, status);
    }
}