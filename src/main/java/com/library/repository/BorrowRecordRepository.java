//src/main/java/com/library/repository/BorrowRecordRepository.java
package com.library.repository;

import com.library.entity.BorrowRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    Optional<BorrowRecord> findByBookIdAndStatus(Long bookId, BorrowRecord.BorrowStatus status);
    List<BorrowRecord> findByReturnTimeIsNullAndDueTimeBefore(LocalDateTime dueTime);

    Optional<BorrowRecord> findByUserIdAndBookIdAndStatus(Long userId, Long bookId, BorrowRecord.BorrowStatus status);
    long countByBookIdAndStatusIn(Long bookId, List<String> statuses);
}


