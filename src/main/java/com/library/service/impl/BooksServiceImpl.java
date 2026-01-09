//src/main/java/com/library/service/impl/BooksServiceImpl.java
package com.library.service.impl;

import com.library.entity.Books;
import com.library.repository.BooksRepository;
import com.library.service.BooksService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class BooksServiceImpl implements BooksService {

    // 注入JPA的Repository，替换原来的MyBatis Mapper
    @Autowired
    private BooksRepository booksRepository;

    @Override
    public Page<Books> findAll(Pageable pageable) {
        // 调用JPA Repository自带的分页方法
        return booksRepository.findAll(pageable);
    }

    @Override
    public Books findById(Long id) {
        // 调用JPA Repository的查询方法，返回Optional，避免空指针
        return booksRepository.findById(id).orElse(null);
    }

    @Override
    public Books save(Books books) {
        // 调用JPA Repository的保存方法（新增/修改通用）
        return booksRepository.save(books);
    }

    @Override
    public void deleteById(Long id) {
        // 调用JPA Repository的删除方法
        booksRepository.deleteById(id);
    }
}