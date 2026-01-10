package com.library.repository;

import com.library.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserIdAndStatus(Long userId, Reservation.ReservationStatus status);
    List<Reservation> findByExpiryTimeBeforeAndStatus(LocalDateTime expiryTime, Reservation.ReservationStatus status);
    List<Reservation> findByStatus(Reservation.ReservationStatus status);

    // 修复第161行：按图书ID+状态查询预定记录
    List<Reservation> findByBookIdAndStatus(Long bookId, Reservation.ReservationStatus status);

    // 新增：检查用户是否已预定该图书（多状态匹配）
    boolean existsByUserIdAndBookIdAndStatusIn(
            Long userId, Long bookId, List<Reservation.ReservationStatus> statuses);

    // 新增：按用户ID分页查询所有预定
    Page<Reservation> findByUserId(Long userId, Pageable pageable);

    // 新增：支持分页的 findByStatus 方法（解决参数不匹配问题）
    Page<Reservation> findByStatus(Reservation.ReservationStatus status, Pageable pageable);

    // 新增：按图书ID+状态查询并按预定时间升序排序
    List<Reservation> findByBookIdAndStatusOrderByReserveTimeAsc(
            Long bookId, Reservation.ReservationStatus status);

    // 新增：按分馆ID+状态分页查询
    Page<Reservation> findByBranchIdAndStatus(
            Integer branchId, Reservation.ReservationStatus status, Pageable pageable);

    // 新增：按分馆ID分页查询
    Page<Reservation> findByBranchId(Integer branchId, Pageable pageable);
}