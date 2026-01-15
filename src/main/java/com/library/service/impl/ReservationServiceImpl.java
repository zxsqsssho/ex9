package com.library.service.impl;

import com.library.dto.ApiResponse;
import com.library.dto.ReservationDTO;
import com.library.entity.*;
import com.library.repository.*;
import com.library.service.EmailNotificationService;
import com.library.service.ReservationService;
import com.library.service.AuthService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

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
    private final BranchesRepository branchesRepository;


    @Override
    @Transactional
    public ApiResponse<?> reserveBook(Long bookId, Integer branchId) {
        Long userId = authService.getCurrentUserId();
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));

        Books book = booksRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("图书不存在"));
        if (!book.getBranchId().equals(branchId)) {
            return ApiResponse.error(400, "图书不属于该分馆");
        }

        // 状态对齐：只校验PENDING/READY（用户已预定或可借阅时不可重复预定）
        boolean hasReserved = reservationRepository.existsByUserIdAndBookIdAndStatusIn(
                userId, bookId, List.of(Reservation.ReservationStatus.PENDING, Reservation.ReservationStatus.READY));
        if (hasReserved) {
            return ApiResponse.error(400, "您已预定该图书，无需重复预定");
        }

        boolean hasBorrowed = borrowRecordRepository.existsByUserIdAndBookIdAndStatus(
                userId, bookId, BorrowRecord.BorrowStatus.BORROWED);
        if (hasBorrowed) {
            return ApiResponse.error(400, "您已借阅该图书，无需预定");
        }

        if (book.getAvailableNum() > 0) {
            return ApiResponse.error(400, "该图书当前可借，建议直接借阅");
        }

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

        List<BorrowRecord> currentBorrows = borrowRecordRepository
                .findByBookIdAndStatus(bookId, BorrowRecord.BorrowStatus.BORROWED);
        if (!currentBorrows.isEmpty()) {
            try {
                emailNotificationService.notifyCurrentBorrowerForReservation(bookId, userId);
                log.info("已发送预定通知给当前借阅者，图书ID: {}", bookId);
            } catch (Exception e) {
                log.error("发送预定通知失败，图书ID: {}", bookId, e);
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

        if (!reservation.getUserId().equals(userId) && !authService.isSystemAdmin() && !authService.isBranchAdmin()) {
            return ApiResponse.error(403, "无权取消他人的预定");
        }

        // 状态对齐：已取消或已完成不可取消
        if (reservation.getStatus() == Reservation.ReservationStatus.CANCELLED ||
                reservation.getStatus() == Reservation.ReservationStatus.COMPLETED) {
            return ApiResponse.error(400, "该预定已取消或已完成");
        }

        reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
        return ApiResponse.success("取消预定成功");
    }

    // 定时任务：每天凌晨2点自动取消过期的等待中预定
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    @Override
    public void autoCancelExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> expiredReservations = reservationRepository.findByStatusAndExpiryTimeBefore(
                Reservation.ReservationStatus.PENDING, now);
        for (Reservation reservation : expiredReservations) {
            reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
        }
        reservationRepository.saveAll(expiredReservations);
        log.info("自动取消过期预定 {} 条", expiredReservations.size());
    }

    @Override
    public ApiResponse<?> getBookReservationQueue(Long bookId) {
        autoCancelExpiredReservations();

        Page<Reservation> reservations = reservationRepository.findByBookIdOrderByReserveTimeAsc(
                bookId, org.springframework.data.domain.PageRequest.of(0, 100));

        List<Map<String, Object>> queueList = reservations.stream().map(reservation -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", reservation.getId());
            map.put("bookId", reservation.getBookId());
            User user = userRepository.findById(reservation.getUserId()).orElse(null);
            map.put("userRealName", user != null ? user.getRealName() : "未知用户");
            map.put("reserveTime", reservation.getReserveTime());
            map.put("expiryTime", reservation.getExpiryTime());
            map.put("status", reservation.getStatus());
            map.put("branchId", reservation.getBranchId());
            return map;
        }).collect(Collectors.toList());

        Page<Map<String, Object>> responsePage = new PageImpl<>(
                queueList, org.springframework.data.domain.PageRequest.of(0, 100), reservations.getTotalElements()
        );
        return ApiResponse.success(responsePage);
    }

    @Override
    @Transactional
    public ApiResponse<?> completeReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("预定记录不存在"));

        // 状态流转对齐数据库：PENDING→READY，READY→COMPLETED
        switch (reservation.getStatus()) {
            case PENDING:
                reservation.setStatus(Reservation.ReservationStatus.READY);
                break;
            case READY:
                reservation.setStatus(Reservation.ReservationStatus.COMPLETED);
                break;
            default:
                return ApiResponse.error(400, "当前状态不支持该操作");
        }

        reservationRepository.save(reservation);
        return ApiResponse.success("操作成功", reservation);
    }

    @Override
    @Transactional
    public ApiResponse<?> updateReservationStatus(Long reservationId) {
        return completeReservation(reservationId);
    }

    /**
     * 核心修复：用户个人预定记录 - 支持状态筛选
     */
    @Override
    public ApiResponse<?> getMyReservations(String status, Pageable pageable) {
        Long userId = authService.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error(401, "未登录");
        }

        Reservation.ReservationStatus reserveStatus = null;
        // 状态参数转换（兼容前端传递的字符串）
        if (StringUtils.hasText(status)) {
            try {
                reserveStatus = Reservation.ReservationStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("无效的预定状态：{}，忽略筛选", status);
                reserveStatus = null;
            }
        }

        // 核心：根据状态筛选查询
        Page<Reservation> reservations;
        if (reserveStatus != null) {
            // 调用带状态的查询方法
            reservations = reservationRepository.findByUserIdAndStatus(userId, reserveStatus, pageable);
        } else {
            // 无状态筛选时查询所有
            reservations = reservationRepository.findByUserId(userId, pageable);
        }

        // DTO 转换（保持不变）
        List<ReservationDTO> dtoList = reservations.stream().map(record -> {
            ReservationDTO dto = new ReservationDTO();
            dto.setId(record.getId());
            dto.setUserId(record.getUserId());
            dto.setBookId(record.getBookId());
            dto.setReserveTime(record.getReserveTime());
            dto.setExpiryTime(record.getExpiryTime());
            dto.setStatus(record.getStatus());
            dto.setBranchId(record.getBranchId());

            // 关联图书信息
            Books book = booksRepository.findById(record.getBookId()).orElse(null);
            dto.setBookName(book != null ? book.getBookName() : "未知图书");
            dto.setAuthor(book != null ? book.getAuthor() : "未知作者");

            // 关联分馆信息
            Branches branch = branchesRepository.findById(record.getBranchId()).orElse(null);
            dto.setBranchName(branch != null ? branch.getBranchName() : "未知分馆");

            return dto;
        }).collect(Collectors.toList());

        Page<ReservationDTO> dtoPage = new PageImpl<>(dtoList, pageable, reservations.getTotalElements());
        return ApiResponse.success(dtoPage);
    }

    // 修复状态筛选：getAllReservations方法补充status参数处理
    @Override
    public ApiResponse<?> getAllReservations(
            String userName,
            String bookName,
            Integer branchId,
            String status,
            Pageable pageable) {
        autoCancelExpiredReservations();
        String finalUserName = StringUtils.hasText(userName) ? userName : null;
        String finalBookName = StringUtils.hasText(bookName) ? bookName : null;
        Reservation.ReservationStatus reserveStatus = null;

        // 核心修复：严格处理status参数转换，无效值直接设为null（避免筛选失效）
        if (StringUtils.hasText(status)) {
            try {
                // 确保前端传递的状态字符串与枚举完全匹配（区分大小写）
                reserveStatus = Reservation.ReservationStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("无效的预定状态：{}，忽略筛选", status);
                reserveStatus = null;
            }
        }

        // 强制按bookId升序排序
        Pageable sortedPageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "bookId")
        );

        Page<Reservation> reservations;
        if (finalUserName != null && finalBookName != null) {
            // 确保传递reserveStatus参数到查询方法
            reservations = reservationRepository.findByUserNameAndBookName(
                    finalUserName, finalBookName, branchId, reserveStatus, sortedPageable);
        } else if (finalUserName != null) {
            reservations = reservationRepository.findByUserName(
                    finalUserName, branchId, reserveStatus, sortedPageable);
        } else if (finalBookName != null) {
            reservations = reservationRepository.findByBookName(
                    finalBookName, branchId, reserveStatus, sortedPageable);
        } else if (branchId != null && reserveStatus != null) {
            reservations = reservationRepository.findByBranchIdAndStatus(branchId, reserveStatus, sortedPageable);
        } else if (branchId != null) {
            reservations = reservationRepository.findByBranchId(branchId, sortedPageable);
        } else if (reserveStatus != null) {
            // 状态筛选生效：直接按状态查询
            reservations = reservationRepository.findByStatus(reserveStatus, sortedPageable);
        } else {
            reservations = reservationRepository.findAll(sortedPageable);
        }

        // 封装返回结果（保持不变）
        List<Map<String, Object>> result = reservations.stream().map(record -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", record.getId());
            map.put("bookId", record.getBookId());
            Books book = booksRepository.findById(record.getBookId()).orElse(null);
            map.put("bookName", book != null ? book.getBookName() : "未知图书");
            map.put("author", book != null ? book.getAuthor() : "未知作者");
            map.put("availableNum", book != null ? book.getAvailableNum() : 0);
            map.put("branchId", record.getBranchId());
            map.put("status", record.getStatus());
            User user = userRepository.findById(record.getUserId()).orElse(null);
            map.put("userRealName", user != null ? user.getRealName() : "未知用户");
            map.put("reserveTime", record.getReserveTime());
            map.put("expiryTime", record.getExpiryTime());
            return map;
        }).collect(Collectors.toList());

        Page<Map<String, Object>> responsePage = new PageImpl<>(result, sortedPageable, reservations.getTotalElements());
        return ApiResponse.success(responsePage);
    }




}