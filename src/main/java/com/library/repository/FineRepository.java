//com/library/repository/FineRepository.java
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
    // 保留有效方法
    @Modifying
    @Transactional
    @Query(value = "CALL CalculateOverdueFine(:recordId, :fineAmount)", nativeQuery = true)
    void calculateOverdueFine(@Param("recordId") Long recordId, @Param("fineAmount") BigDecimal fineAmount);

    @Query(value = "SELECT IFNULL(SUM(f.fine_amount), 0.00) FROM fines f " +
            "JOIN borrow_records br ON f.record_id = br.id " +
            "WHERE br.user_id = :userId AND f.pay_status = 'unpaid'", nativeQuery = true)
    BigDecimal getTotalUnpaidFine(@Param("userId") Long userId);

    Page<Fine> findByPayStatus(String payStatus, Pageable pageable);

    // ========== 核心修复：移除无效方法 ==========
    // 删除：Page<Fine> findByUserId(Long userId, Pageable pageable);

    // ========== 补全：按用户ID查询罚款（通过关联借阅记录） ==========
    @Query(value = "SELECT f.* FROM fines f " +
            "JOIN borrow_records br ON f.record_id = br.id " +
            "WHERE br.user_id = :userId", nativeQuery = true)
    Page<Fine> findByBorrowRecordUserId(@Param("userId") Long userId, Pageable pageable);

    // ========== 补全：按用户ID+支付状态查询罚款 ==========
    @Query(value = "SELECT f.* FROM fines f " +
            "JOIN borrow_records br ON f.record_id = br.id " +
            "WHERE br.user_id = :userId AND f.pay_status = :payStatus", nativeQuery = true)
    Page<Fine> findByBorrowRecordUserIdAndPayStatus(
            @Param("userId") Long userId,
            @Param("payStatus") String payStatus,
            Pageable pageable);

    // 保留之前补充的分馆关联查询方法
    @Query(value = "SELECT f.* FROM fines f " +
            "JOIN borrow_records br ON f.record_id = br.id " +
            "WHERE br.branch_id = :branchId", nativeQuery = true)
    Page<Fine> findByBorrowRecordBranchId(@Param("branchId") Integer branchId, Pageable pageable);

    @Query(value = "SELECT f.* FROM fines f " +
            "JOIN borrow_records br ON f.record_id = br.id " +
            "WHERE br.branch_id = :branchId AND f.pay_status = :payStatus", nativeQuery = true)
    Page<Fine> findByBorrowRecordBranchIdAndPayStatus(
            @Param("branchId") Integer branchId,
            @Param("payStatus") String payStatus,
            Pageable pageable);
}