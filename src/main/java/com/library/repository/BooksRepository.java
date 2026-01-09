package com.library.repository;

import com.library.entity.Books;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// JPA的Repository接口，继承JpaRepository<实体类, 主键类型>，自带CRUD方法
@Repository
public interface BooksRepository extends JpaRepository<Books, Long> {
    // 自定义查询方法（JPA支持方法名自动生成SQL，无需写XML/注解）
    // 示例：根据书名模糊查询
    List<Books> findByBookNameLike(String bookName);
}