// ReservationService.java
package com.library.service;

import org.springframework.data.domain.Pageable;
import com.library.dto.ApiResponse;

public interface ReservationService {
    ApiResponse<?> reserveBook(Long bookId, Integer branchId);
    ApiResponse<?> cancelReservation(Long reservationId);
    ApiResponse<?> getMyReservations(Pageable pageable);
    ApiResponse<?> getBookReservationQueue(Long bookId);
    ApiResponse<?> completeReservation(Long reservationId);
    ApiResponse<?> getAllReservations(Integer branchId, String status, Pageable pageable);
}