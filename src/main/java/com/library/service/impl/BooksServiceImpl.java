package com.library.service.impl;

import com.library.entity.Books;
import com.library.repository.BooksRepository;
import com.library.service.BooksService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BooksServiceImpl implements BooksService {
    private final BooksRepository booksRepository;

    // 实现接口：分页查询所有图书
    @Override
    public Page<Books> findAll(Pageable pageable) {
        return booksRepository.findAll(pageable);
    }

    // 实现接口：根据ID查询图书
    @Override
    public Books findById(Long id) {
        return booksRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("图书不存在"));
    }

    // 实现接口：新增/修改图书
    @Override
    @Transactional
    public Books save(Books books) {
        return booksRepository.save(books);
    }

    // 实现接口：根据ID删除图书
    @Override
    @Transactional
    public void deleteById(Long id) {
        booksRepository.deleteById(id);
    }

    // 核心修复1：实现接口中缺失的 4 参数 findBooks 方法
    @Override
    public Page<Books> findBooks(Integer branchId, String bookName, String author, Pageable pageable) {
        // 复用多条件查询逻辑，未传递的参数设为 null
        return findBooks(branchId, bookName, author, null, null, null, null, pageable);
    }

    // 核心修复2：实现接口中 8 参数的重载 findBooks 方法（多条件查询）
    @Override
    public Page<Books> findBooks(
            Integer branchId,
            String bookName,
            String author,
            String isbn,
            String bookType,
            String status,
            String category,
            Pageable pageable
    ) {
        return booksRepository.findAll((root, query, cb) -> {
            var predicates = cb.conjunction();

            if (branchId != null && branchId > 0) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("branchId"), branchId));
            }

            if (bookName != null && !bookName.isBlank()) {
                predicates = cb.and(predicates,
                        cb.like(root.get("bookName"), "%" + bookName + "%"));
            }

            if (author != null && !author.isBlank()) {
                predicates = cb.and(predicates,
                        cb.like(root.get("author"), "%" + author + "%"));
            }

            // ISBN 精确匹配（关键）
            if (isbn != null && !isbn.isBlank()) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("isbn"), isbn));
            }

            if (bookType != null && !bookType.isBlank()) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("bookType"), bookType));
            }

            if (status != null && !status.isBlank()) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("status"), status));
            }

            if (category != null && !category.isBlank()) {
                predicates = cb.and(predicates,
                        cb.equal(root.get("category"), category));
            }

            return predicates;
        }, pageable);
    }

}