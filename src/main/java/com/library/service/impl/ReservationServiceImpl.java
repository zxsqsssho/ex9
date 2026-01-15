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

    // 预定图书（保持不变，强制关联当前分馆）
    @Override
    @Transactional
    public ApiResponse<?> reserveBook(Long bookId, Integer branchId) {
        Long userId = authService.getCurrentUserId();
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("用户不存在"));
        Books book = booksRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("图书不存在"));
        // 分馆管理员只能预定本馆图书
        if (authService.isBranchAdmin() && !book.getBranchId().equals(authService.getCurrentUserBranchId())) {
            return ApiResponse.error(403, "无权预定其他分馆的图书");
        }
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

    // 取消预定（保持不变）
    @Override
    @Transactional
    public ApiResponse<?> cancelReservation(Long reservationId) {
        Long userId = authService.getCurrentUserId();
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("预定记录不存在"));
        // 分馆管理员只能取消本馆预定
        if (authService.isBranchAdmin() && !reservation.getBranchId().equals(authService.getCurrentUserBranchId())) {
            return ApiResponse.error(403, "无权取消其他分馆的预定");
        }
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

    // 定时任务：每天凌晨2点自动取消过期的等待中预定（保持不变）
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

    // 查看图书预定队列（核心修改：分馆管理员仅查本馆）
    @Override
    public ApiResponse<?> getBookReservationQueue(Long bookId) {
        autoCancelExpiredReservations();
        // 分馆管理员只能查看本馆图书队列
        if (authService.isBranchAdmin()) {
            Books book = booksRepository.findById(bookId).orElseThrow(() -> new EntityNotFoundException("图书不存在"));
            if (!book.getBranchId().equals(authService.getCurrentUserBranchId())) {
                return ApiResponse.error(403, "无权查看其他分馆的预定队列");
            }
        }
        Page<Reservation> reservations = reservationRepository.findByBookIdOrderByReserveTimeAsc(
                bookId, PageRequest.of(0, 100));
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
                queueList, PageRequest.of(0, 100), reservations.getTotalElements()
        );
        return ApiResponse.success(responsePage);
    }

    // 完成预定（保持不变）
    @Override
    @Transactional
    public ApiResponse<?> completeReservation(Long reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new EntityNotFoundException("预定记录不存在"));
        // 分馆管理员只能操作本馆预定
        if (authService.isBranchAdmin() && !reservation.getBranchId().equals(authService.getCurrentUserBranchId())) {
            return ApiResponse.error(403, "无权操作其他分馆的预定");
        }
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

    // 我的预定记录（保持不变）
    @Override
    public ApiResponse<?> getMyReservations(String status, Pageable pageable) {
        Long userId = authService.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error(401, "未登录");
        }
        Reservation.ReservationStatus reserveStatus = null;
        if (StringUtils.hasText(status)) {
            try {
                reserveStatus = Reservation.ReservationStatus.valueOf(status.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("无效的预定状态：{}，忽略筛选", status);
                reserveStatus = null;
            }
        }
        Page<Reservation> reservations;
        if (reserveStatus != null) {
            reservations = reservationRepository.findByUserIdAndStatus(userId, reserveStatus, pageable);
        } else {
            reservations = reservationRepository.findByUserId(userId, pageable);
        }
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

    // 管理员获取所有预定记录（核心修改：分馆管理员仅查本馆）
    @Override
    public ApiResponse<?> getAllReservations(
            String userName,
            String bookName,
            Integer branchId,
            String bookStatus,
            Pageable pageable) {
        // 分馆管理员强制筛选本馆
        if (authService.isBranchAdmin()) {
            branchId = authService.getCurrentUserBranchId();
        }
        autoCancelExpiredReservations();
        String finalUserName = StringUtils.hasText(userName) ? userName : null;
        String finalBookName = StringUtils.hasText(bookName) ? bookName : null;
        Reservation.ReservationStatus reserveStatus = null;
        if (StringUtils.hasText(bookStatus) && !"AVAILABLE".equals(bookStatus) && !"QUEUED".equals(bookStatus)) {
            try {
                reserveStatus = Reservation.ReservationStatus.valueOf(bookStatus.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("无效的预定状态：{}，忽略筛选", bookStatus);
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
        // 图书状态筛选逻辑（AVAILABLE/QUEUED）
        if (StringUtils.hasText(bookStatus)) {
            switch (bookStatus) {
                case "AVAILABLE":
                    reservations = reservationRepository.findByBookStatusAvailable(
                            finalUserName, finalBookName, branchId, sortedPageable);
                    break;
                case "QUEUED":
                    reservations = reservationRepository.findByBookStatusQueued(
                            finalUserName, finalBookName, branchId, sortedPageable);
                    break;
                default:
                    if (reserveStatus != null) {
                        reservations = getReservationsByCombination(finalUserName, finalBookName, branchId, reserveStatus, sortedPageable);
                    } else {
                        reservations = getReservationsByCombination(finalUserName, finalBookName, branchId, null, sortedPageable);
                    }
                    break;
            }
        } else {
            reservations = getReservationsByCombination(finalUserName, finalBookName, branchId, reserveStatus, sortedPageable);
        }
        // 结果封装
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

    // 辅助方法：多条件组合查询（保持不变）
    private Page<Reservation> getReservationsByCombination(
            String userName, String bookName, Integer branchId,
            Reservation.ReservationStatus status, Pageable pageable) {
        if (userName != null && bookName != null) {
            return reservationRepository.findByUserNameAndBookName(userName, bookName, branchId, status, pageable);
        } else if (userName != null) {
            return reservationRepository.findByUserName(userName, branchId, status, pageable);
        } else if (bookName != null) {
            return reservationRepository.findByBookName(bookName, branchId, status, pageable);
        } else if (branchId != null && status != null) {
            return reservationRepository.findByBranchIdAndStatus(branchId, status, pageable);
        } else if (branchId != null) {
            return reservationRepository.findByBranchId(branchId, pageable);
        } else if (status != null) {
            return reservationRepository.findByStatus(status, pageable);
        } else {
            return reservationRepository.findAll(pageable);
        }
    }
}