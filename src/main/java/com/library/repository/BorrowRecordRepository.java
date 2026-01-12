package com.library.repository;

import com.library.entity.BorrowRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    // 查找指定图书和状态的借阅记录（返回Optional，用于查询单个记录）
    Optional<BorrowRecord> findOneByBookIdAndStatus(Long bookId, BorrowRecord.BorrowStatus status);

    // 查找指定图书和状态的借阅记录（返回List，用于查询多个记录）
    List<BorrowRecord> findByBookIdAndStatus(Long bookId, BorrowRecord.BorrowStatus status);

    // 查找未归还且已到期的借阅记录
    List<BorrowRecord> findByReturnTimeIsNullAndDueTimeBefore(LocalDateTime dueTime);

    // 查找用户、图书和状态的借阅记录
    Optional<BorrowRecord> findByUserIdAndBookIdAndStatus(Long userId, Long bookId, BorrowRecord.BorrowStatus status);

    // 按状态查询（返回List，用于EmailNotificationServiceImpl）
    List<BorrowRecord> findAllByStatus(BorrowRecord.BorrowStatus status);

    // 统计指定图书和状态的借阅记录数量
    long countByBookIdAndStatusIn(Long bookId, List<String> statuses);

    // 检查用户是否借阅了某本书
    boolean existsByUserIdAndBookIdAndStatus(Long userId, Long bookId, BorrowRecord.BorrowStatus status);

    // 新增：统计用户当前借阅数量
    long countByUserIdAndStatus(Long userId, BorrowRecord.BorrowStatus status);

    // 新增：用户当前借阅记录（分页）
    Page<BorrowRecord> findByUserIdAndStatus(Long userId, BorrowRecord.BorrowStatus status, Pageable pageable);

    // 新增：用户借阅历史（分页）
    Page<BorrowRecord> findByUserIdAndStatusIn(Long userId, List<BorrowRecord.BorrowStatus> statuses, Pageable pageable);

    // 新增：分馆+状态查询（分页）
    Page<BorrowRecord> findByBranchIdAndStatus(Integer branchId, BorrowRecord.BorrowStatus status, Pageable pageable);

    // 新增：分馆查询（分页）
    Page<BorrowRecord> findByBranchId(Integer branchId, Pageable pageable);

    // 新增：状态查询（分页）
    Page<BorrowRecord> findByStatus(BorrowRecord.BorrowStatus status, Pageable pageable);
}