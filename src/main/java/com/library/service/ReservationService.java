package com.library.service;

import org.springframework.data.domain.Pageable;
import com.library.dto.ApiResponse;

public interface ReservationService {
    ApiResponse<?> reserveBook(Long bookId, Integer branchId);

    ApiResponse<?> cancelReservation(Long reservationId);

    // 核心修改：添加 String status 参数，支持状态筛选
    ApiResponse<?> getMyReservations(String status, Pageable pageable);

    ApiResponse<?> getBookReservationQueue(Long bookId);

    ApiResponse<?> completeReservation(Long reservationId);

    ApiResponse<?> getAllReservations(String userName, String bookName, Integer branchId, String bookStatus, Pageable pageable);

    // 新增：状态更新接口（供队列操作调用）
    ApiResponse<?> updateReservationStatus(Long reservationId);

    // 新增：自动取消过期预定（定时任务）
    void autoCancelExpiredReservations();
}