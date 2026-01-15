// src/main/java/com/library/repository/BooksRepository.java
package com.library.repository;

import com.library.entity.Books;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BooksRepository
        extends JpaRepository<Books, Long>,
        JpaSpecificationExecutor<Books> {
    List<Books> findByBookNameLike(String bookName);
    Page<Books> findByBranchId(Integer branchId, Pageable pageable);
    Page<Books> findByBookNameLike(String bookName, Pageable pageable);
    Page<Books> findByAuthorLike(String author, Pageable pageable);
    Page<Books> findByBranchIdAndBookNameLike(Integer branchId, String bookName, Pageable pageable);
    Page<Books> findByBranchIdAndAuthorLike(Integer branchId, String author, Pageable pageable);
    Page<Books> findByBookNameLikeAndAuthorLike(String bookName, String author, Pageable pageable);
    Page<Books> findByBranchIdAndBookNameLikeAndAuthorLike(Integer branchId, String bookName, String author, Pageable pageable);
    boolean existsByIsbn(String isbn);

    // 核心新增：支持 branchId + bookName(模糊) + author(模糊) + isbn(精确) 多条件查询
    Page<Books> findByBranchIdAndBookNameLikeAndAuthorLikeAndIsbn(
            Integer branchId,
            String bookName,
            String author,
            String isbn,
            Pageable pageable);

    // 核心补充：统计特定状态的图书数量（解决错误4、5）
    long countByStatus(String status);

    // 在 BooksRepository 中继续添加以下方法，适配不同条件组合
    Page<Books> findByBookNameLikeAndAuthorLikeAndIsbn(String bookName, String author, String isbn, Pageable pageable);
    Page<Books> findByBranchIdAndBookNameLikeAndIsbn(Integer branchId, String bookName, String isbn, Pageable pageable);
    Page<Books> findByBranchIdAndAuthorLikeAndIsbn(Integer branchId, String author, String isbn, Pageable pageable);
    Page<Books> findByBranchIdAndIsbn(Integer branchId, String isbn, Pageable pageable);
    Page<Books> findByBookNameLikeAndIsbn(String bookName, String isbn, Pageable pageable);
    Page<Books> findByAuthorLikeAndIsbn(String author, String isbn, Pageable pageable);
    Page<Books> findByIsbn(String isbn, Pageable pageable);
}