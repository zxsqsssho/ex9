package com.library.repository;

import com.library.entity.Fine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FineRepository extends JpaRepository<Fine, Long> {

    // 调用存储过程计算逾期罚款
    @Modifying
    @Transactional
    @Query(value = "CALL CalculateOverdueFine(:recordId, :fineAmount)", nativeQuery = true)
    void calculateOverdueFine(@Param("recordId") Long recordId, @Param("fineAmount") BigDecimal fineAmount);

    // 核心修复1：移除 CAST 转换，依赖 MySQL 隐式转换（int → bigint 兼容）
    @Query(value = "SELECT IFNULL(SUM(f.fine_amount), 0.00) FROM fines f " +
            "JOIN borrow_records br ON f.record_id = br.id " + // 移除 CAST
            "WHERE br.user_id = :userId AND f.pay_status = 'unpaid'", nativeQuery = true)
    BigDecimal getTotalUnpaidFine(@Param("userId") Long userId);

    // 按支付状态查询罚款
    Page<Fine> findByPayStatus(String payStatus, Pageable pageable);

    // 核心修复2：按用户ID查询（移除 CAST）
    @Query(value = "SELECT f.* FROM fines f " +
            "JOIN borrow_records br ON f.record_id = br.id " + // 移除 CAST(f.record_id AS BIGINT)
            "WHERE br.user_id = :userId", nativeQuery = true)
    Page<Fine> findByBorrowRecordUserId(@Param("userId") Long userId, Pageable pageable);

    // 核心修复3：按用户ID+支付状态查询（移除 CAST）
    @Query(value = "SELECT f.* FROM fines f " +
            "JOIN borrow_records br ON f.record_id = br.id " + // 移除 CAST
            "WHERE br.user_id = :userId AND f.pay_status = :payStatus", nativeQuery = true)
    Page<Fine> findByBorrowRecordUserIdAndPayStatus(
            @Param("userId") Long userId,
            @Param("payStatus") String payStatus,
            Pageable pageable);

    // 核心修复4：分馆关联查询（移除 CAST）
    @Query(value = "SELECT f.* FROM fines f " +
            "JOIN borrow_records br ON f.record_id = br.id " + // 移除 CAST
            "WHERE br.branch_id = :branchId", nativeQuery = true)
    Page<Fine> findByBorrowRecordBranchId(@Param("branchId") Integer branchId, Pageable pageable);

    // 按分馆+支付状态查询
    @Query(value = "SELECT f.* FROM fines f " +
            "JOIN borrow_records br ON f.record_id = br.id " + // 移除 CAST
            "WHERE br.branch_id = :branchId AND f.pay_status = :payStatus", nativeQuery = true)
    Page<Fine> findByBorrowRecordBranchIdAndPayStatus(
            @Param("branchId") Integer branchId,
            @Param("payStatus") String payStatus,
            Pageable pageable);

    /**
     * 核心新增：关联查询（罚款+借阅+用户+图书+分馆），支持多条件筛选
     * 用于管理员查询，分馆管理员自动筛选本馆数据
     */
    @Query(value = "SELECT " +
            "f.fine_id, " +
            "f.record_id, " +
            "f.fine_amount, " +
            "f.pay_status, " +
            "f.pay_time, " +
            "br.user_id, " +
            "u.real_name AS user_real_name, " +
            "b.book_name AS book_name, " +
            "br.overdue_days, " +
            "br.branch_id, " +
            "brn.branch_name AS branch_name " +
            "FROM fines f " +
            "JOIN borrow_records br ON f.record_id = br.id " + // 关联借阅记录（通过record_id）
            "JOIN users u ON br.user_id = u.id " + // 关联用户表
            "JOIN books b ON br.book_id = b.book_id " + // 关联图书表
            "JOIN branches brn ON br.branch_id = brn.branch_id " + // 关联分馆表（获取分馆名称）
            "WHERE " +
            "(:userName IS NULL OR u.real_name LIKE %:userName%) " + // 用户名模糊查询
            "AND (:bookName IS NULL OR b.book_name LIKE %:bookName%) " + // 图书名模糊查询
            "AND (:branchId IS NULL OR br.branch_id = :branchId) " + // 分馆ID精确筛选（分馆管理员强制传入）
            "AND (:payStatus IS NULL OR f.pay_status = :payStatus)", // 支付状态筛选
            countQuery = "SELECT COUNT(f.fine_id) " +
                    "FROM fines f " +
                    "JOIN borrow_records br ON f.record_id = br.id " +
                    "JOIN users u ON br.user_id = u.id " +
                    "JOIN books b ON br.book_id = b.book_id " +
                    "JOIN branches brn ON br.branch_id = brn.branch_id " +
                    "WHERE " +
                    "(:userName IS NULL OR u.real_name LIKE %:userName%) " +
                    "AND (:bookName IS NULL OR b.book_name LIKE %:bookName%) " +
                    "AND (:branchId IS NULL OR br.branch_id = :branchId) " +
                    "AND (:payStatus IS NULL OR f.pay_status = :payStatus)",
            nativeQuery = true)
    Page<Object[]> findAllWithUserAndBookAndBranch(
            @Param("userName") String userName,
            @Param("bookName") String bookName,
            @Param("branchId") Integer branchId,
            @Param("payStatus") String payStatus,
            Pageable pageable);

    /**
     * 核心修复5：原有管理员多条件查询（保留，兼容历史调用）
     */
    @Query(value = "SELECT f.fine_id, f.record_id, f.fine_amount, f.pay_status, f.pay_time, " +
            "br.user_id, u.real_name AS user_real_name, b.book_name AS book_name, br.overdue_days, br.branch_id " +
            "FROM fines f " +
            "JOIN borrow_records br ON f.record_id = br.id " + // 移除 CAST
            "JOIN users u ON br.user_id = u.id " +
            "JOIN books b ON br.book_id = b.book_id " +
            "WHERE (:userName IS NULL OR u.real_name LIKE %:userName%) " +
            "AND (:bookName IS NULL OR b.book_name LIKE %:bookName%) " +
            "AND (:branchId IS NULL OR br.branch_id = :branchId) " +
            "AND (:payStatus IS NULL OR f.pay_status = :payStatus)",
            countQuery = "SELECT COUNT(f.fine_id) " +
                    "FROM fines f " +
                    "JOIN borrow_records br ON f.record_id = br.id " + // 移除 CAST
                    "JOIN users u ON br.user_id = u.id " +
                    "JOIN books b ON br.book_id = b.book_id " +
                    "WHERE (:userName IS NULL OR u.real_name LIKE %:userName%) " +
                    "AND (:bookName IS NULL OR b.book_name LIKE %:bookName%) " +
                    "AND (:branchId IS NULL OR br.branch_id = :branchId) " +
                    "AND (:payStatus IS NULL OR f.pay_status = :payStatus)",
            nativeQuery = true)
    Page<Object[]> findAllWithUserAndBook(
            @Param("userName") String userName,
            @Param("bookName") String bookName,
            @Param("branchId") Integer branchId,
            @Param("payStatus") String payStatus,
            Pageable pageable);
}