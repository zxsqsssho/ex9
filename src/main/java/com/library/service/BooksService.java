package com.library.service;

import com.library.entity.Books;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BooksService {
    // 基础 CRUD 方法
    Page<Books> findAll(Pageable pageable);
    Books findById(Long id);
    Books save(Books books);
    void deleteById(Long id);

    // 核心方法1：4 参数多条件查询（接口声明，必须实现）
    Page<Books> findBooks(Integer branchId, String bookName, String author, Pageable pageable);

    // 核心方法2：8 参数多条件查询（重载，支持更多筛选条件）
    Page<Books> findBooks(
            Integer branchId,
            String bookName,
            String author,
            String isbn,
            String category,
            String bookType,
            String status,
            Pageable pageable);

}