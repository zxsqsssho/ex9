package com.library.service.impl;

import com.library.dto.ApiResponse;
import com.library.dto.BorrowCreateDTO;
import com.library.dto.BorrowRecordDTO;
import com.library.entity.*;
import com.library.repository.BooksRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.UserRepository;
import com.library.repository.ReservationRepository;
import com.library.repository.BranchesRepository;
import com.library.repository.FineRepository;
import com.library.service.BorrowService;
import com.library.service.AuthService;
import com.library.service.EmailNotificationService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
// 确保正确实现 BorrowService 接口
public class BorrowServiceImpl implements BorrowService {
    private final BorrowRecordRepository borrowRecordRepository;
    private final BooksRepository booksRepository;
    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final FineRepository fineRepository;
    private final AuthService authService;
    private final EmailNotificationService emailNotificationService;
    private final BranchesRepository branchesRepository;

    // 核心修正：确保方法签名与 BorrowService 接口完全一致
    @Override
    @Transactional
    public ApiResponse<?> borrowBook(BorrowCreateDTO borrowDTO) {
        Long userId = authService.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));

        // 1. 检查用户是否有未支付的罚款
        BigDecimal unpaidFine = fineRepository.getTotalUnpaidFine(userId);
        if (unpaidFine.compareTo(BigDecimal.ZERO) > 0) {
            return ApiResponse.error(400, "存在未支付的罚款（金额：" + unpaidFine + "元），无法借阅图书");
        }

        // 2. 检查图书是否存在且属于该分馆
        Books book = booksRepository.findById(borrowDTO.getBookId())
                .orElseThrow(() -> new EntityNotFoundException("图书不存在"));
        if (!book.getBranchId().equals(borrowDTO.getBranchId())) {
            return ApiResponse.error(400, "图书不属于该分馆，无法借阅");
        }

        // 3. 检查图书库存是否充足
        if (book.getAvailableNum() <= 0) {
            return ApiResponse.error(400, "图书已无库存，可尝试预定");
        }

        // 4. 检查学生用户借阅数量限制（最多16本）
        long borrowedCount = borrowRecordRepository.countByUserIdAndStatus(
                userId, BorrowRecord.BorrowStatus.BORROWED);
        if (user.getUserType() == User.UserType.STUDENT && borrowedCount >= 16) {
            return ApiResponse.error(400, "学生用户最多可借阅16本图书，当前已借阅" + borrowedCount + "本");
        }

        // 5. 计算借阅期限（学生30天，教师60天）
        LocalDateTime borrowTime = LocalDateTime.now();
        LocalDateTime dueTime = user.getUserType() == User.UserType.STUDENT
                ? borrowTime.plusDays(30)
                : borrowTime.plusDays(60);

        // 6. 创建借阅记录
        BorrowRecord borrowRecord = new BorrowRecord();
        borrowRecord.setUserId(userId);
        borrowRecord.setBookId(book.getBookId().longValue()); // 图书ID为Integer，转换为Long类型
        borrowRecord.setBorrowTime(borrowTime);
        borrowRecord.setDueTime(dueTime);
        borrowRecord.setStatus(BorrowRecord.BorrowStatus.BORROWED);
        borrowRecord.setBranchId(borrowDTO.getBranchId());
        borrowRecord.setReminderCount(0);
        borrowRecordRepository.save(borrowRecord);

        // 7. 更新图书可借数量
        book.setAvailableNum(book.getAvailableNum() - 1);
        if (book.getAvailableNum() == 0) {
            book.setStatus("out_of_stock");
        } else {
            book.setStatus("normal");
        }
        booksRepository.save(book);

        return ApiResponse.success("借阅成功", borrowRecord);
    }

    // 其他方法保持不变，确保所有接口方法都有实现
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
            overdueDays = borrowRecord.getDueTime().until(returnTime, ChronoUnit.DAYS);
        }

        // 更新借阅记录
        borrowRecord.setReturnTime(returnTime);
        borrowRecord.setOverdueDays((int) overdueDays);
        borrowRecord.setStatus(BorrowRecord.BorrowStatus.RETURNED);
        borrowRecordRepository.save(borrowRecord);

        // 更新图书可借数量
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
            Reservation earliestReservation = pendingReservations.get(0);
            emailNotificationService.notifyReservationAvailable(earliestReservation.getId());
        }

        return ApiResponse.success("归还成功", borrowRecord);
    }

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
        List<BorrowRecordDTO> dtoList = borrowRecords.stream().map(record -> {
            BorrowRecordDTO dto = new BorrowRecordDTO();
            dto.setId(record.getId());
            dto.setUserId(record.getUserId());
            dto.setBookId(record.getBookId());
            dto.setBorrowTime(record.getBorrowTime());
            dto.setDueTime(record.getDueTime());
            dto.setReturnTime(record.getReturnTime());
            dto.setStatus(record.getStatus());
            dto.setOverdueDays(record.getOverdueDays());
            dto.setBranchId(record.getBranchId());

            // 关联图书
            Books book = booksRepository.findById(record.getBookId()).orElse(null);
            if (book != null) {
                dto.setBookName(book.getBookName());
                dto.setAuthor(book.getAuthor());
            }

            // 关联分馆
            Branches branch = branchesRepository.findById(record.getBranchId()).orElse(null);
            if (branch != null) {
                dto.setBranchName(branch.getBranchName());
            }

            return dto;
        }).collect(Collectors.toList());

        Page<BorrowRecordDTO> dtoPage = new PageImpl<>(dtoList, pageable, borrowRecords.getTotalElements());
        return ApiResponse.success(dtoPage);
    }

    @Override
    public ApiResponse<?> getMyBorrowHistory(Pageable pageable) {
        Long userId = authService.getCurrentUserId();
        Page<BorrowRecord> borrowRecords = borrowRecordRepository.findByUserIdAndStatusIn(
                userId, List.of(BorrowRecord.BorrowStatus.RETURNED, BorrowRecord.BorrowStatus.OVERDUE), pageable);
        List<BorrowRecordDTO> dtoList = borrowRecords.stream().map(record -> {
            BorrowRecordDTO dto = new BorrowRecordDTO();
            dto.setId(record.getId());
            dto.setUserId(record.getUserId());
            dto.setBookId(record.getBookId());
            dto.setBorrowTime(record.getBorrowTime());
            dto.setDueTime(record.getDueTime());
            dto.setReturnTime(record.getReturnTime());
            dto.setStatus(record.getStatus());
            dto.setOverdueDays(record.getOverdueDays());
            dto.setBranchId(record.getBranchId());

            // 关联图书
            Books book = booksRepository.findById(record.getBookId()).orElse(null);
            if (book != null) {
                dto.setBookName(book.getBookName());
                dto.setAuthor(book.getAuthor());
            }

            // 关联分馆
            Branches branch = branchesRepository.findById(record.getBranchId()).orElse(null);
            if (branch != null) {
                dto.setBranchName(branch.getBranchName());
            }

            return dto;
        }).collect(Collectors.toList());

        Page<BorrowRecordDTO> dtoPage = new PageImpl<>(dtoList, pageable, borrowRecords.getTotalElements());
        return ApiResponse.success(dtoPage);
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

    @Override
    public ApiResponse<?> getAllBorrowRecords(
            String userName,
            String bookName,
            Integer branchId,
            String status,
            Pageable pageable) {
        Page<BorrowRecord> borrowRecords;
        String finalUserName = StringUtils.hasText(userName) ? userName : null;
        String finalBookName = StringUtils.hasText(bookName) ? bookName : null;
        BorrowRecord.BorrowStatus borrowStatus = null;

        if (StringUtils.hasText(status)) {
            try {
                borrowStatus = BorrowRecord.BorrowStatus.valueOf(status);
            } catch (IllegalArgumentException e) {
                borrowStatus = null;
            }
        }

        // 多条件组合查询
        if (finalUserName != null && finalBookName != null) {
            borrowRecords = borrowRecordRepository.findByUserNameAndBookName(
                    finalUserName, finalBookName, branchId, borrowStatus, pageable);
        } else if (finalUserName != null) {
            borrowRecords = borrowRecordRepository.findByUserName(
                    finalUserName, branchId, borrowStatus, pageable);
        } else if (finalBookName != null) {
            borrowRecords = borrowRecordRepository.findByBookName(
                    finalBookName, branchId, borrowStatus, pageable);
        } else if (branchId != null && borrowStatus != null) {
            borrowRecords = borrowRecordRepository.findByBranchIdAndStatus(branchId, borrowStatus, pageable);
        } else if (branchId != null) {
            borrowRecords = borrowRecordRepository.findByBranchId(branchId, pageable);
        } else if (borrowStatus != null) {
            borrowRecords = borrowRecordRepository.findByStatus(borrowStatus, pageable);
        } else {
            borrowRecords = borrowRecordRepository.findAll(pageable);
        }

        // 新增：补充用户名称和图书名称（关联查询）
        List<Map<String, Object>> result = borrowRecords.stream().map(record -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", record.getId());
            map.put("userId", record.getUserId());
            // 关联查询用户真实姓名
            User user = userRepository.findById(record.getUserId()).orElse(null);
            map.put("userRealName", user != null ? user.getRealName() : "未知用户");
            // 关联查询图书名称
            Books book = booksRepository.findById(record.getBookId()).orElse(null);
            map.put("bookName", book != null ? book.getBookName() : "未知图书");
            map.put("borrowTime", record.getBorrowTime());
            map.put("dueTime", record.getDueTime());
            map.put("returnTime", record.getReturnTime());
            map.put("status", record.getStatus());
            map.put("overdueDays", record.getOverdueDays() != null ? record.getOverdueDays() : 0);
            map.put("branchId", record.getBranchId());
            return map;
        }).collect(Collectors.toList());

        Page<Map<String, Object>> responsePage = new PageImpl<>(
                result, pageable, borrowRecords.getTotalElements()
        );
        return ApiResponse.success(responsePage);
    }

    // 计算逾期天数（辅助方法）
    private Integer calculateOverdueDays(LocalDateTime returnTime, LocalDateTime dueTime) {
        if (returnTime == null) {
            LocalDateTime now = LocalDateTime.now();
            return now.isAfter(dueTime) ? Math.toIntExact(ChronoUnit.DAYS.between(dueTime, now)) : 0;
        } else {
            return returnTime.isAfter(dueTime) ? Math.toIntExact(ChronoUnit.DAYS.between(dueTime, returnTime)) : 0;
        }
    }
}