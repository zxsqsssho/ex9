//src/main/java/com/library/repository/BorrowRecordRepository.java
package com.library.repository;

import com.library.entity.BorrowRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
    long countByUserIdAndStatusIn(Long userId, List<BorrowRecord.BorrowStatus> statuses);

    // 修正：用户名+图书名联合筛选（添加%模糊匹配）
    @Query(value = "SELECT br FROM BorrowRecord br " +
            "JOIN User u ON br.userId = u.id " +
            "JOIN Books b ON br.bookId = b.bookId " +
            "WHERE (:userName IS NULL OR u.realName LIKE %:userName%) " +
            "AND (:bookName IS NULL OR b.bookName LIKE %:bookName%) " +
            "AND (:branchId IS NULL OR br.branchId = :branchId) " +
            "AND (:status IS NULL OR br.status = :status)",
            countQuery = "SELECT COUNT(br) FROM BorrowRecord br " +
                    "JOIN User u ON br.userId = u.id " +
                    "JOIN Books b ON br.bookId = b.bookId " +
                    "WHERE (:userName IS NULL OR u.realName LIKE %:userName%) " +
                    "AND (:bookName IS NULL OR b.bookName LIKE %:bookName%) " +
                    "AND (:branchId IS NULL OR br.branchId = :branchId) " +
                    "AND (:status IS NULL OR br.status = :status)")
    Page<BorrowRecord> findByUserNameAndBookName(
            @Param("userName") String userName,
            @Param("bookName") String bookName,
            @Param("branchId") Integer branchId,
            @Param("status") BorrowRecord.BorrowStatus status,
            Pageable pageable);

    // 修正：单独用户名筛选（添加%）
    @Query(value = "SELECT br FROM BorrowRecord br " +
            "JOIN User u ON br.userId = u.id " +
            "WHERE (:userName IS NULL OR u.realName LIKE %:userName%) " +
            "AND (:branchId IS NULL OR br.branchId = :branchId) " +
            "AND (:status IS NULL OR br.status = :status)",
            countQuery = "SELECT COUNT(br) FROM BorrowRecord br " +
                    "JOIN User u ON br.userId = u.id " +
                    "WHERE (:userName IS NULL OR u.realName LIKE %:userName%) " +
                    "AND (:branchId IS NULL OR br.branchId = :branchId) " +
                    "AND (:status IS NULL OR br.status = :status)")
    Page<BorrowRecord> findByUserName(
            @Param("userName") String userName,
            @Param("branchId") Integer branchId,
            @Param("status") BorrowRecord.BorrowStatus status,
            Pageable pageable);

    // 修正：单独图书名筛选（添加%）
    @Query(value = "SELECT br FROM BorrowRecord br " +
            "JOIN Books b ON br.bookId = b.bookId " +
            "WHERE (:bookName IS NULL OR b.bookName LIKE %:bookName%) " +
            "AND (:branchId IS NULL OR br.branchId = :branchId) " +
            "AND (:status IS NULL OR br.status = :status)",
            countQuery = "SELECT COUNT(br) FROM BorrowRecord br " +
                    "JOIN Books b ON br.bookId = b.bookId " +
                    "WHERE (:bookName IS NULL OR b.bookName LIKE %:bookName%) " +
                    "AND (:branchId IS NULL OR br.branchId = :branchId) " +
                    "AND (:status IS NULL OR br.status = :status)")
    Page<BorrowRecord> findByBookName(
            @Param("bookName") String bookName,
            @Param("branchId") Integer branchId,
            @Param("status") BorrowRecord.BorrowStatus status,
            Pageable pageable);

    // 新增：关联用户+图书查询所有借阅记录（支持多条件筛选）
    @Query(value = "SELECT br.id, br.book_id, br.borrow_time, br.branch_id, br.due_time, " +
            "br.overdue_days, br.reminder_count, br.return_time, br.status, br.user_id, " +
            "u.real_name AS user_real_name, b.book_name AS book_name " +
            "FROM borrow_records br " +
            "JOIN users u ON br.user_id = u.id " +
            "JOIN books b ON br.book_id = b.book_id " +
            "WHERE (:userName IS NULL OR u.real_name LIKE %:userName%) " +
            "AND (:bookName IS NULL OR b.book_name LIKE %:bookName%) " +
            "AND (:branchId IS NULL OR br.branch_id = :branchId) " +
            "AND (:status IS NULL OR br.status = :status)",
            countQuery = "SELECT COUNT(br.id) " +
                    "FROM borrow_records br " +
                    "JOIN users u ON br.user_id = u.id " +
                    "JOIN books b ON br.book_id = b.book_id " +
                    "WHERE (:userName IS NULL OR u.real_name LIKE %:userName%) " +
                    "AND (:bookName IS NULL OR b.book_name LIKE %:bookName%) " +
                    "AND (:branchId IS NULL OR br.branch_id = :branchId) " +
                    "AND (:status IS NULL OR br.status = :status)",
            nativeQuery = true)
    Page<Object[]> findAllWithUserAndBook(
            @Param("userName") String userName,
            @Param("bookName") String bookName,
            @Param("branchId") Integer branchId,
            @Param("status") String status,
            Pageable pageable);
}