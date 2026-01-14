//src/main/java/com/library/service/impl/BorrowServiceImpl.java
package com.library.service.impl;

import com.library.dto.ApiResponse;
import com.library.dto.BorrowCreateDTO;
import com.library.entity.Books;
import com.library.entity.BorrowRecord;
import com.library.entity.User;
import com.library.entity.Reservation;
import com.library.repository.BooksRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.UserRepository;
import com.library.repository.ReservationRepository;
import com.library.repository.FineRepository;
import com.library.service.BorrowService;
import com.library.service.AuthService;
import com.library.service.EmailNotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {
    private final BorrowRecordRepository borrowRecordRepository;
    private final BooksRepository booksRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final FineRepository fineRepository;
    private final AuthService authService;
    private final EmailNotificationService emailNotificationService;

    @Override
    @Transactional
    public ApiResponse<?> borrowBook(BorrowCreateDTO borrowDTO) {
        Long userId = authService.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));

        // 检查是否有未支付的罚款
        BigDecimal unpaidFine = fineRepository.getTotalUnpaidFine(userId);
        if (unpaidFine.compareTo(BigDecimal.ZERO) > 0) {
            return ApiResponse.error(400, "存在未支付的罚款，无法借阅图书");
        }

        // 检查图书是否存在且可借
        Books book = booksRepository.findById(borrowDTO.getBookId())
                .orElseThrow(() -> new EntityNotFoundException("图书不存在"));
        if (!book.getBranchId().equals(borrowDTO.getBranchId())) {
            return ApiResponse.error(400, "图书不属于该分馆");
        }
        if (book.getAvailableNum() <= 0) {
            return ApiResponse.error(400, "图书已无库存，可尝试预定");
        }

        // 检查学生用户借阅数量是否超过限制（16本）
        long borrowedCount = borrowRecordRepository.countByUserIdAndStatus(
                userId, BorrowRecord.BorrowStatus.BORROWED);
        if (user.getUserType() == User.UserType.STUDENT && borrowedCount >= 16) {
            return ApiResponse.error(400, "学生用户最多可借阅16本图书");
        }

        // 计算借阅期限（学生30天，教师60天）
        LocalDateTime borrowTime = LocalDateTime.now();
        LocalDateTime dueTime = user.getUserType() == User.UserType.STUDENT
                ? borrowTime.plusDays(30)
                : borrowTime.plusDays(60);

        // 创建借阅记录（修复：将 book.getId() 改为 book.getBookId()）
        BorrowRecord borrowRecord = new BorrowRecord();
        borrowRecord.setUserId(userId);
        borrowRecord.setBookId(book.getBookId().longValue()); // 关键修复：使用正确的 getter 方法，并转为 Long 类型
        borrowRecord.setBorrowTime(borrowTime);
        borrowRecord.setDueTime(dueTime);
        borrowRecord.setStatus(BorrowRecord.BorrowStatus.BORROWED);
        borrowRecord.setBranchId(borrowDTO.getBranchId());
        borrowRecord.setReminderCount(0);
        borrowRecordRepository.save(borrowRecord);

        // 更新图书可借数量
        book.setAvailableNum(book.getAvailableNum() - 1);
        if (book.getAvailableNum() == 0) {
            book.setStatus("out_of_stock");
        }
        booksRepository.save(book);

        return ApiResponse.success("借阅成功", borrowRecord);
    }

    @Override
    @Transactional
    public ApiResponse<?> returnBook(Long borrowId) {
        Long userId = authService.getCurrentUserId();
        BorrowRecord borrowRecord = borrowRecordRepository.findById(borrowId)
                .orElseThrow(() -> new EntityNotFoundException("借阅记录不存在"));

        // 检查是否是本人借阅或管理员
        if (!borrowRecord.getUserId().equals(userId)
                && !authService.isSystemAdmin()
                && !authService.isBranchAdmin()) {
            return ApiResponse.error(403, "无权归还他人借阅的图书");
        }

        // 已归还则无需处理
        if (borrowRecord.getStatus() == BorrowRecord.BorrowStatus.RETURNED) {
            return ApiResponse.error(400, "该图书已归还");
        }

        // 计算逾期天数
        LocalDateTime returnTime = LocalDateTime.now();
        long overdueDays = 0;
        if (returnTime.isAfter(borrowRecord.getDueTime())) {
            overdueDays = borrowRecord.getDueTime().until(returnTime, java.time.temporal.ChronoUnit.DAYS);
        }

        // 更新借阅记录
        borrowRecord.setReturnTime(returnTime);
        borrowRecord.setOverdueDays((int) overdueDays);
        borrowRecord.setStatus(BorrowRecord.BorrowStatus.RETURNED);
        borrowRecordRepository.save(borrowRecord);

        // 更新图书可借数量（修复：确保此处获取图书ID的方法正确）
        Books book = booksRepository.findById(borrowRecord.getBookId())
                .orElseThrow(() -> new EntityNotFoundException("图书不存在"));
        book.setAvailableNum(book.getAvailableNum() + 1);
        book.setStatus("normal");
        booksRepository.save(book);

        // 如有逾期，创建罚款记录
        if (overdueDays > 0) {
            BigDecimal fineAmount = BigDecimal.valueOf(overdueDays * 1.0);
            fineRepository.calculateOverdueFine(borrowRecord.getId(), fineAmount);
        }

        // 检查该图书是否有预定，如有则通知第一个预定者
        List<Reservation> pendingReservations = reservationRepository.findByBookIdAndStatusOrderByReserveTimeAsc(
                borrowRecord.getBookId(), Reservation.ReservationStatus.PENDING);

        if (!pendingReservations.isEmpty()) {
            // 获取最早预定的用户
            Reservation earliestReservation = pendingReservations.get(0);
            earliestReservation.setStatus(Reservation.ReservationStatus.READY);
            reservationRepository.save(earliestReservation);

            // 通知预定者图书可借
            emailNotificationService.notifyReservationAvailable(earliestReservation.getId());
        }
        return ApiResponse.success("归还成功", borrowRecord);
    }

    // 其他方法保持不变...
    @Override
    @Transactional
    public ApiResponse<?> renewBook(Long bookId) {
        Long userId = authService.getCurrentUserId();
        // 查找当前用户未归还的借阅记录
        BorrowRecord borrowRecord = borrowRecordRepository.findByUserIdAndBookIdAndStatus(
                        userId, bookId, BorrowRecord.BorrowStatus.BORROWED)
                .orElseThrow(() -> new EntityNotFoundException("未找到该图书的借阅记录"));

        // 检查图书是否有预定，如有则不能续借
        List<Reservation> pendingReservations = reservationRepository.findByBookIdAndStatus(
                bookId, Reservation.ReservationStatus.PENDING);
        if (!pendingReservations.isEmpty()) {
            return ApiResponse.error(400, "该图书已有预定，无法续借");
        }

        // 检查是否已逾期，逾期则不能续借
        if (borrowRecord.getOverdueDays() != null && borrowRecord.getOverdueDays() > 0) {
            return ApiResponse.error(400, "图书已逾期，无法续借");
        }

        // 续借期限：学生30天，教师60天
        User user = userRepository.findById(userId).orElseThrow();
        LocalDateTime newDueTime = user.getUserType() == User.UserType.STUDENT
                ? borrowRecord.getDueTime().plusDays(30)
                : borrowRecord.getDueTime().plusDays(60);

        borrowRecord.setDueTime(newDueTime);
        borrowRecordRepository.save(borrowRecord);

        return ApiResponse.success("续借成功", newDueTime);
    }

    @Override
    public ApiResponse<?> getMyBorrowList(Pageable pageable) {
        Long userId = authService.getCurrentUserId();
        Page<BorrowRecord> borrowRecords = borrowRecordRepository.findByUserIdAndStatus(
                userId, BorrowRecord.BorrowStatus.BORROWED, pageable);
        return ApiResponse.success(borrowRecords);
    }

    @Override
    public ApiResponse<?> getMyBorrowHistory(Pageable pageable) {
        Long userId = authService.getCurrentUserId();
        Page<BorrowRecord> borrowRecords = borrowRecordRepository.findByUserIdAndStatusIn(
                userId, List.of(BorrowRecord.BorrowStatus.RETURNED, BorrowRecord.BorrowStatus.OVERDUE), pageable);
        return ApiResponse.success(borrowRecords);
    }

    @Override
    public ApiResponse<?> getAllBorrowRecords(Integer branchId, String status, Pageable pageable) {
        Page<BorrowRecord> borrowRecords;
        if (branchId != null && status != null) {
            borrowRecords = borrowRecordRepository.findByBranchIdAndStatus(
                    branchId, BorrowRecord.BorrowStatus.valueOf(status), pageable);
        } else if (branchId != null) {
            borrowRecords = borrowRecordRepository.findByBranchId(branchId, pageable);
        } else if (status != null) {
            borrowRecords = borrowRecordRepository.findByStatus(
                    BorrowRecord.BorrowStatus.valueOf(status), pageable);
        } else {
            borrowRecords = borrowRecordRepository.findAll(pageable);
        }
        return ApiResponse.success(borrowRecords);
    }

    @Override
    @Transactional
    public ApiResponse<?> updateBorrowStatus(Long borrowId, String status) {
        BorrowRecord borrowRecord = borrowRecordRepository.findById(borrowId)
                .orElseThrow(() -> new EntityNotFoundException("借阅记录不存在"));
        borrowRecord.setStatus(BorrowRecord.BorrowStatus.valueOf(status));
        borrowRecordRepository.save(borrowRecord);
        return ApiResponse.success("状态更新成功");
    }


}