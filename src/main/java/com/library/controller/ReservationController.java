// ReservationController.java
package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.service.ReservationService;
import com.library.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservation")
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;

    /**
     * 用户预定图书
     */
    @PostMapping("/reserve")
    public ApiResponse<?> reserveBook(@RequestParam Long bookId, @RequestParam Integer branchId) {
        return reservationService.reserveBook(bookId, branchId);
    }

    /**
     * 用户取消预定
     */
    @DeleteMapping("/cancel/{reservationId}")
    public ApiResponse<?> cancelReservation(@PathVariable Long reservationId) {
        return reservationService.cancelReservation(reservationId);
    }

    /**
     * 获取当前用户预定列表
     */
    @GetMapping("/my-reservation")
    public ApiResponse<?> getMyReservations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reservationService.getMyReservations(pageable);
    }

    /**
     * 获取图书的预定队列
     */
    @GetMapping("/book/{bookId}/queue")
    public ApiResponse<?> getBookReservationQueue(@PathVariable Long bookId) {
        return reservationService.getBookReservationQueue(bookId);
    }

    /**
     * 管理员完成预定
     */
    @PostMapping("/{reservationId}/complete")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> completeReservation(@PathVariable Long reservationId) {
        return reservationService.completeReservation(reservationId);
    }

    /**
     * 管理员获取所有预定记录
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> getAllReservations(
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return reservationService.getAllReservations(branchId, status, pageable);
    }
}