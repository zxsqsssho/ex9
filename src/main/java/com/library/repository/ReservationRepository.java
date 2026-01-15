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
    // 原有方法保持不变，新增以下2个方法用于图书状态筛选
    /**
     * 筛选图书状态为AVAILABLE（availableNum > 0）的预定记录
     */
    @Query(value = "SELECT r FROM Reservation r " +
            "JOIN Books b ON r.bookId = b.bookId " +
            "JOIN User u ON r.userId = u.id " +
            "WHERE (:userName IS NULL OR u.realName LIKE %:userName%) " +
            "AND (:bookName IS NULL OR b.bookName LIKE %:bookName%) " +
            "AND (:branchId IS NULL OR r.branchId = :branchId) " +
            "AND b.availableNum > 0",
            countQuery = "SELECT COUNT(r) FROM Reservation r " +
                    "JOIN Books b ON r.bookId = b.bookId " +
                    "JOIN User u ON r.userId = u.id " +
                    "WHERE (:userName IS NULL OR u.realName LIKE %:userName%) " +
                    "AND (:bookName IS NULL OR b.bookName LIKE %:bookName%) " +
                    "AND (:branchId IS NULL OR r.branchId = :branchId) " +
                    "AND b.availableNum > 0")
    Page<Reservation> findByBookStatusAvailable(
            @Param("userName") String userName,
            @Param("bookName") String bookName,
            @Param("branchId") Integer branchId,
            Pageable pageable);

    /**
     * 筛选图书状态为QUEUED（availableNum <= 0）的预定记录
     */
    @Query(value = "SELECT r FROM Reservation r " +
            "JOIN Books b ON r.bookId = b.bookId " +
            "JOIN User u ON r.userId = u.id " +
            "WHERE (:userName IS NULL OR u.realName LIKE %:userName%) " +
            "AND (:bookName IS NULL OR b.bookName LIKE %:bookName%) " +
            "AND (:branchId IS NULL OR r.branchId = :branchId) " +
            "AND b.availableNum <= 0",
            countQuery = "SELECT COUNT(r) FROM Reservation r " +
                    "JOIN Books b ON r.bookId = b.bookId " +
                    "JOIN User u ON r.userId = u.id " +
                    "WHERE (:userName IS NULL OR u.realName LIKE %:userName%) " +
                    "AND (:bookName IS NULL OR b.bookName LIKE %:bookName%) " +
                    "AND (:branchId IS NULL OR r.branchId = :branchId) " +
                    "AND b.availableNum <= 0")
    Page<Reservation> findByBookStatusQueued(
            @Param("userName") String userName,
            @Param("bookName") String bookName,
            @Param("branchId") Integer branchId,
            Pageable pageable);


    // 按用户ID + 状态 分页查询
    Page<Reservation> findByUserIdAndStatus(Long userId, Reservation.ReservationStatus status, Pageable pageable);

    // 按状态和过期时间查询
    List<Reservation> findByStatusAndExpiryTimeBefore(
            Reservation.ReservationStatus status,
            LocalDateTime expiryTime);

    // 按状态查询
    List<Reservation> findByStatus(Reservation.ReservationStatus status);

    // 按图书ID + 状态查询
    List<Reservation> findByBookIdAndStatus(Long bookId, Reservation.ReservationStatus status);

    // 核心确保：按图书ID + 状态 + 预定时间升序查询
    List<Reservation> findByBookIdAndStatusOrderByReserveTimeAsc(
            Long bookId, Reservation.ReservationStatus status);

    // 检查用户是否已预定（PENDING/READY）
    boolean existsByUserIdAndBookIdAndStatusIn(
            Long userId, Long bookId, List<Reservation.ReservationStatus> statuses);

    // 按用户ID查询
    Page<Reservation> findByUserId(Long userId, Pageable pageable);

    // 按状态分页查询
    Page<Reservation> findByStatus(Reservation.ReservationStatus status, Pageable pageable);

    // 统计用户预定数量
    long countByUserIdAndStatus(Long userId, Reservation.ReservationStatus status);

    // 按分馆ID查询
    Page<Reservation> findByBranchId(Integer branchId, Pageable pageable);

    // 按分馆ID + 状态查询
    Page<Reservation> findByBranchIdAndStatus(
            Integer branchId, Reservation.ReservationStatus status, Pageable pageable);

    @Query(value = "SELECT r FROM Reservation r " +
            "JOIN User u ON r.userId = u.id " +
            "JOIN Books b ON r.bookId = b.bookId " +
            "WHERE r.bookId = :bookId " +
            "ORDER BY r.reserveTime ASC",
            countQuery = "SELECT COUNT(r) FROM Reservation r WHERE r.bookId = :bookId")
    Page<Reservation> findByBookIdOrderByReserveTimeAsc(@Param("bookId") Long bookId, Pageable pageable);
    @Query(value = "SELECT r FROM Reservation r " +
            "JOIN User u ON r.userId = u.id " +
            "WHERE (:userName IS NULL OR u.realName LIKE %:userName%) " +
            "AND (:branchId IS NULL OR r.branchId = :branchId) " +
            "AND (:status IS NULL OR r.status = :status)",
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
    @Query(value = "SELECT r FROM Reservation r " +
            "JOIN Books b ON r.bookId = b.bookId " +
            "WHERE (:bookName IS NULL OR b.bookName LIKE %:bookName%) " +
            "AND (:branchId IS NULL OR r.branchId = :branchId) " +
            "AND (:status IS NULL OR r.status = :status)",
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
    @Query(value = "SELECT r FROM Reservation r " +
            "JOIN User u ON r.userId = u.id " +
            "JOIN Books b ON r.bookId = b.bookId " +
            "WHERE (:userName IS NULL OR u.realName LIKE %:userName%) " +
            "AND (:bookName IS NULL OR b.bookName LIKE %:bookName%) " +
            "AND (:branchId IS NULL OR r.branchId = :branchId) " +
            "AND (:status IS NULL OR r.status = :status)",
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
    List<Reservation> findByExpiryTimeBeforeAndStatus(LocalDateTime expiryTime, Reservation.ReservationStatus status);
}