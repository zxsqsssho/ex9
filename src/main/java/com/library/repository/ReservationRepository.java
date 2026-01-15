package com.library.repository;

import com.library.entity.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // 核心新增：按用户ID + 状态 分页查询（支持用户个人筛选）
    Page<Reservation> findByUserIdAndStatus(Long userId, Reservation.ReservationStatus status, Pageable pageable);

    // 关键补全：按状态和过期时间查询（自动取消过期预定用）
    List<Reservation> findByStatusAndExpiryTimeBefore(
            Reservation.ReservationStatus status,
            LocalDateTime expiryTime);

    List<Reservation> findByStatus(Reservation.ReservationStatus status);
    List<Reservation> findByBookIdAndStatus(Long bookId, Reservation.ReservationStatus status);
    boolean existsByUserIdAndBookIdAndStatusIn(
            Long userId, Long bookId, List<Reservation.ReservationStatus> statuses);
    Page<Reservation> findByUserId(Long userId, Pageable pageable);
    Page<Reservation> findByStatus(Reservation.ReservationStatus status, Pageable pageable);
    List<Reservation> findByBookIdAndStatusOrderByReserveTimeAsc(
            Long bookId, Reservation.ReservationStatus status);

    long countByUserIdAndStatus(Long userId, Reservation.ReservationStatus status);
    // 核心修复1：修正 findByBranchId 方法，仅接收2个参数（删除多余的 String 参数）
    Page<Reservation> findByBranchId(Integer branchId, Pageable pageable);

    // 核心修复2：修正 findByBranchIdAndStatus 方法，参数为（分支ID + 状态枚举 + 分页）
    Page<Reservation> findByBranchIdAndStatus(
            Integer branchId, Reservation.ReservationStatus status, Pageable pageable);
    // 按图书ID查询所有状态的预定队列（按预定时间升序）
    @Query(value = "SELECT r FROM Reservation r " +
            "JOIN User u ON r.userId = u.id " +
            "JOIN Books b ON r.bookId = b.bookId " +
            "WHERE r.bookId = :bookId " +
            "ORDER BY r.reserveTime ASC",
            countQuery = "SELECT COUNT(r) FROM Reservation r WHERE r.bookId = :bookId")
    Page<Reservation> findByBookIdOrderByReserveTimeAsc(
            @Param("bookId") Long bookId,
            Pageable pageable);

    // 1. 按用户名筛选（支持图书状态）
    @Query(value = "SELECT r FROM Reservation r " +
            "JOIN User u ON r.userId = u.id " +
            "WHERE (:userName IS NULL OR u.realName LIKE %:userName%) " +
            "AND (:branchId IS NULL OR r.branchId = :branchId) " +
            "AND (:status IS NULL OR r.status = :status)", // 正确应用status筛选
            countQuery = "SELECT COUNT(r) FROM Reservation r " +
                    "JOIN User u ON r.userId = u.id " +
                    "WHERE (:userName IS NULL OR u.realName LIKE %:userName%) " +
                    "AND (:branchId IS NULL OR r.branchId = :branchId) " +
                    "AND (:status IS NULL OR r.status = :status)")
    Page<Reservation> findByUserName(
            @Param("userName") String userName,
            @Param("branchId") Integer branchId,
            @Param("status") Reservation.ReservationStatus status,
            Pageable pageable);

    // 2. 按图书名筛选（支持图书状态）
    @Query(value = "SELECT r FROM Reservation r " +
            "JOIN Books b ON r.bookId = b.bookId " +
            "WHERE (:bookName IS NULL OR b.bookName LIKE %:bookName%) " +
            "AND (:branchId IS NULL OR r.branchId = :branchId) " +
            "AND (:status IS NULL OR r.status = :status)", // 正确应用status筛选
            countQuery = "SELECT COUNT(r) FROM Reservation r " +
                    "JOIN Books b ON r.bookId = b.bookId " +
                    "WHERE (:bookName IS NULL OR b.bookName LIKE %:bookName%) " +
                    "AND (:branchId IS NULL OR r.branchId = :branchId) " +
                    "AND (:status IS NULL OR r.status = :status)")
    Page<Reservation> findByBookName(
            @Param("bookName") String bookName,
            @Param("branchId") Integer branchId,
            @Param("status") Reservation.ReservationStatus status,
            Pageable pageable);

    // 3. 按用户名+图书名联合筛选（支持图书状态）
    @Query(value = "SELECT r FROM Reservation r " +
            "JOIN User u ON r.userId = u.id " +
            "JOIN Books b ON r.bookId = b.bookId " +
            "WHERE (:userName IS NULL OR u.realName LIKE %:userName%) " +
            "AND (:bookName IS NULL OR b.bookName LIKE %:bookName%) " +
            "AND (:branchId IS NULL OR r.branchId = :branchId) " +
            "AND (:status IS NULL OR r.status = :status)", // 正确应用status筛选
            countQuery = "SELECT COUNT(r) FROM Reservation r " +
                    "JOIN User u ON r.userId = u.id " +
                    "JOIN Books b ON r.bookId = b.bookId " +
                    "WHERE (:userName IS NULL OR u.realName LIKE %:userName%) " +
                    "AND (:bookName IS NULL OR b.bookName LIKE %:bookName%) " +
                    "AND (:branchId IS NULL OR r.branchId = :branchId) " +
                    "AND (:status IS NULL OR r.status = :status)")
    Page<Reservation> findByUserNameAndBookName(
            @Param("userName") String userName,
            @Param("bookName") String bookName,
            @Param("branchId") Integer branchId,
            @Param("status") Reservation.ReservationStatus status,
            Pageable pageable);

    // 4. 按分馆筛选（支持图书状态）
    @Query(value = "SELECT r FROM Reservation r " +
            "JOIN Books b ON r.bookId = b.bookId " +
            "WHERE r.branchId = :branchId " +
            "AND (:bookStatus IS NULL OR " +
            "(b.availableNum > 0 AND :bookStatus = 'AVAILABLE') OR " +
            "(b.availableNum <= 0 AND :bookStatus = 'QUEUED'))",
            countQuery = "SELECT COUNT(r) FROM Reservation r " +
                    "JOIN Books b ON r.bookId = b.bookId " +
                    "WHERE r.branchId = :branchId " +
                    "AND (:bookStatus IS NULL OR " +
                    "(b.availableNum > 0 AND :bookStatus = 'AVAILABLE') OR " +
                    "(b.availableNum <= 0 AND :bookStatus = 'QUEUED'))")
    Page<Reservation> findByBranchId(
            @Param("branchId") Integer branchId,
            @Param("bookStatus") String bookStatus, // 新增图书状态参数
            Pageable pageable);

    // 5. 无其他条件（支持图书状态）
    @Query(value = "SELECT r FROM Reservation r " +
            "JOIN Books b ON r.bookId = b.bookId " +
            "WHERE (:bookStatus IS NULL OR " +
            "(b.availableNum > 0 AND :bookStatus = 'AVAILABLE') OR " +
            "(b.availableNum <= 0 AND :bookStatus = 'QUEUED'))",
            countQuery = "SELECT COUNT(r) FROM Reservation r " +
                    "JOIN Books b ON r.bookId = b.bookId " +
                    "WHERE (:bookStatus IS NULL OR " +
                    "(b.availableNum > 0 AND :bookStatus = 'AVAILABLE') OR " +
                    "(b.availableNum <= 0 AND :bookStatus = 'QUEUED'))")
    Page<Reservation> findAllWithBookStatus(
            @Param("bookStatus") String bookStatus, // 新增图书状态参数
            Pageable pageable);
    // 过期预定查询
    List<Reservation> findByExpiryTimeBeforeAndStatus(LocalDateTime expiryTime, Reservation.ReservationStatus status);
    // 新增：借阅联动 - 根据图书ID和状态查询预定记录
    List<Reservation> findByBookIdAndStatus(Integer bookId, Reservation.ReservationStatus status);


}