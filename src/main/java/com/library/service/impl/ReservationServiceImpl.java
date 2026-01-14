//src/main/java/com/library/service/impl/ReservationServiceImpl.java
package com.library.service.impl;

import com.library.dto.ApiResponse;
import com.library.entity.Books;
import com.library.entity.Reservation;
import com.library.entity.BorrowRecord;
import com.library.repository.ReservationRepository;
import com.library.repository.BooksRepository;
import com.library.repository.UserRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.service.EmailNotificationService;
import com.library.service.ReservationService;
import com.library.service.AuthService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository reservationRepository;
    private final BooksRepository booksRepository;
    private final UserRepository userRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final AuthService authService;
    private final EmailNotificationService emailNotificationService;

    @Override
    @Transactional
    public ApiResponse<?> reserveBook(Long bookId, Integer branchId) {
        Long userId = authService.getCurrentUserId();
        // 移除未使用的 user 变量（修复第8个错误）
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));

        // 检查图书是否存在
        Books book = booksRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("图书不存在"));
        if (!book.getBranchId().equals(branchId)) {
            return ApiResponse.error(400, "图书不属于该分馆");
        }

        // 检查用户是否已预定该图书（修复第1个错误：使用新增的 Repository 方法）
        boolean hasReserved = reservationRepository.existsByUserIdAndBookIdAndStatusIn(
                userId, bookId, List.of(Reservation.ReservationStatus.PENDING, Reservation.ReservationStatus.READY));
        if (hasReserved) {
            return ApiResponse.error(400, "您已预定该图书，无需重复预定");
        }

        // 检查用户是否已借阅该图书（修复第2个错误：使用新增的 Repository 方法）
        boolean hasBorrowed = borrowRecordRepository.existsByUserIdAndBookIdAndStatus(
                userId, bookId, BorrowRecord.BorrowStatus.BORROWED);
        if (hasBorrowed) {
            return ApiResponse.error(400, "您已借阅该图书，无需预定");
        }

        // 检查图书是否可借，如可借则提示直接借阅
        if (book.getAvailableNum() > 0) {
            return ApiResponse.error(400, "该图书当前可借，建议直接借阅");
        }

        // 创建预定记录，有效期7天
        LocalDateTime reserveTime = LocalDateTime.now();
        LocalDateTime expiryTime = reserveTime.plusDays(7);
        Reservation reservation = new Reservation();
        reservation.setUserId(userId);
        reservation.setBookId(bookId);
        reservation.setReserveTime(reserveTime);
        reservation.setExpiryTime(expiryTime);
        reservation.setStatus(Reservation.ReservationStatus.PENDING);
        reservation.setBranchId(branchId);
        reservationRepository.save(reservation);

        // 通知当前借阅者该图书已被预定
        List<BorrowRecord> currentBorrows = borrowRecordRepository
                .findByBookIdAndStatus(bookId, BorrowRecord.BorrowStatus.BORROWED);

        if (!currentBorrows.isEmpty()) {
            try {
                emailNotificationService.notifyCurrentBorrowerForReservation(bookId, userId);
                log.info("已发送预定通知给当前借阅者，图书ID: {}", bookId);
            } catch (Exception e) {
                log.error("发送预定通知失败，图书ID: {}", bookId, e);
                // 通知失败不影响预定成功
            }
        }

        return ApiResponse.success("预定成功，预定有效期7天", reservation);
    }

    @Override
    @Transactional
    public ApiResponse<?> cancelReservation(Long reservationId) {
        Long userId = authService.getCurrentUserId();
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("预定记录不存在"));

        // 检查是否是本人预定或管理员
        if (!reservation.getUserId().equals(userId) && !authService.isSystemAdmin() && !authService.isBranchAdmin()) {
            return ApiResponse.error(403, "无权取消他人的预定");
        }

        // 已取消或已完成则无需处理
        if (reservation.getStatus() == Reservation.ReservationStatus.CANCELLED ||
                reservation.getStatus() == Reservation.ReservationStatus.COMPLETED) {
            return ApiResponse.error(400, "该预定已取消或已完成");
        }

        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);

        return ApiResponse.success("取消预定成功");
    }

    @Override
    public ApiResponse<?> getMyReservations(Pageable pageable) {
        Long userId = authService.getCurrentUserId();
        // 修复第3、9个错误：使用新增的 findByUserId 方法（Pageable）
        Page<Reservation> reservations = reservationRepository.findByUserId(userId, pageable);
        return ApiResponse.success(reservations);
    }

    @Override
    public ApiResponse<?> getBookReservationQueue(Long bookId) {
        // 修复第4个错误：使用新增的排序方法
        List<Reservation> reservations = reservationRepository.findByBookIdAndStatusOrderByReserveTimeAsc(
                bookId, Reservation.ReservationStatus.PENDING);
        return ApiResponse.success(reservations);
    }

    @Override
    @Transactional
    public ApiResponse<?> completeReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("预定记录不存在"));
        reservation.setStatus(Reservation.ReservationStatus.COMPLETED);
        reservationRepository.save(reservation);
        return ApiResponse.success("预定完成");
    }

    @Override
    public ApiResponse<?> getAllReservations(Integer branchId, String status, Pageable pageable) {
        Page<Reservation> reservations;
        if (branchId != null && status != null) {
            // 修复第5个错误：使用新增的 findByBranchIdAndStatus 方法
            reservations = reservationRepository.findByBranchIdAndStatus(
                    branchId, Reservation.ReservationStatus.valueOf(status), pageable);
        } else if (branchId != null) {
            // 修复第6个错误：使用新增的 findByBranchId 方法
            reservations = reservationRepository.findByBranchId(branchId, pageable);
        } else if (status != null) {
            reservations = reservationRepository.findByStatus(Reservation.ReservationStatus.valueOf(status), pageable);
        } else {
            reservations = reservationRepository.findAll(pageable);
        }
        return ApiResponse.success(reservations);
    }
}