package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.service.ReservationService;
import com.library.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 预定相关接口
 */
@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;
    private final AuthService authService;

    /**
     * 获取当前用户的预定列表（支持状态筛选）
     * 核心修复：添加 status 参数接收，传递给 Service
     */
    @GetMapping("/my-reservation")
    public ApiResponse<?> getMyReservations(
            @RequestParam(required = false) String status, // 新增：接收前端传递的状态参数
            Pageable pageable) {
        // 调用 Service 方法，传递 status 和 pageable 两个参数
        return reservationService.getMyReservations(status, pageable);
    }

    /**
     * 预定图书
     */
    @PostMapping("/reserve")
    public ApiResponse<?> reserveBook(
            @RequestParam Long bookId,
            @RequestParam Integer branchId) {
        return reservationService.reserveBook(bookId, branchId);
    }

    /**
     * 取消预定
     */
    @DeleteMapping("/cancel/{reservationId}")
    public ApiResponse<?> cancelReservation(@PathVariable Long reservationId) {
        return reservationService.cancelReservation(reservationId);
    }

    /**
     * 查看图书预定队列
     */
    @GetMapping("/book/{bookId}/queue")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> getBookReservationQueue(@PathVariable Long bookId) {
        return reservationService.getBookReservationQueue(bookId);
    }

    /**
     * 标记预定完成
     */
    @PostMapping("/{reservationId}/complete")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> completeReservation(@PathVariable Long reservationId) {
        return reservationService.completeReservation(reservationId);
    }

    /**
     * 管理员更新预定状态（队列操作专用）
     */
    @PostMapping("/{reservationId}/update-status")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> updateReservationStatus(@PathVariable Long reservationId) {
        return reservationService.updateReservationStatus(reservationId);
    }

    /**
     * 管理员获取所有预定记录（支持多条件筛选+按图书ID排序）
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> getAllReservations(
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String bookName,
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        Pageable pageable = org.springframework.data.domain.PageRequest.of(pageNum - 1, pageSize);
        return reservationService.getAllReservations(userName, bookName, branchId, status, pageable);
    }
}