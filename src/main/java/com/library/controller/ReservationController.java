package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.ReservationCreateDTO;
import com.library.service.ReservationService;
import com.library.service.AuthService;
import jakarta.validation.Valid;
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
     * 预定图书（核心修改：用@RequestBody接收JSON参数）
     */
    @PostMapping("/reserve")
    public ApiResponse<?> reserveBook(@Valid @RequestBody ReservationCreateDTO dto) {
        // 传递DTO中的bookId和branchId到Service
        return reservationService.reserveBook(dto.getBookId(), dto.getBranchId());
    }
    /**
     * 取消预定
     */
    @DeleteMapping("/cancel/{reservationId}")
    public ApiResponse<?> cancelReservation(@PathVariable Long reservationId) {
        return reservationService.cancelReservation(reservationId);
    }

    /**
     * 获取用户个人预定记录
     */
    @GetMapping("/my-reservation")
    public ApiResponse<?> getMyReservations(@RequestParam(required = false) String status, Pageable pageable) {
        return reservationService.getMyReservations(status, pageable);
    }

    /**
     * 获取图书预定队列（按预定时间排序）
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
     * 管理员获取所有预定记录（支持图书状态筛选）
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> getAllReservations(
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String bookName,
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) String bookStatus,
            Pageable pageable) {
        return reservationService.getAllReservations(userName, bookName, branchId, bookStatus, pageable);
    }
}