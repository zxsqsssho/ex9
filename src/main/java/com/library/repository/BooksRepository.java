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
}