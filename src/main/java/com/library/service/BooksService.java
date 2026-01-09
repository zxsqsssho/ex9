//src/main/java/com/library/service/BooksService.java
package com.library.service;

import com.library.entity.Books;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface BooksService {
    // 移除MyBatis-Plus的Page，替换为JPA的Page
    Page<Books> findAll(Pageable pageable); // 分页查询所有
    Books findById(Long id); // 根据主键查询
    Books save(Books books); // 新增/修改
    void deleteById(Long id); // 根据主键删除
}