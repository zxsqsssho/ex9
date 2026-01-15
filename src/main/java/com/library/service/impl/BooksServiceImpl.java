package com.library.service.impl;

import com.library.entity.Books;
import com.library.repository.BooksRepository;
import com.library.service.BooksService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BooksServiceImpl implements BooksService {
    private final BooksRepository booksRepository;

    @Override
    public Page<Books> findAll(Pageable pageable) {
        return booksRepository.findAll(pageable);
    }

    @Override
    public Books findById(Long id) {
        return booksRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("图书不存在"));
    }

    @Override
    @Transactional
    public Books save(Books books) {
        return booksRepository.save(books);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        booksRepository.deleteById(id);
    }

    @Override
    public Page<Books> findBooks(Integer branchId, String bookName, String author, Pageable pageable) {
        return findBooks(branchId, bookName, author, null, null, null, null, pageable);
    }

    @Override
    public Page<Books> findBooks(
            Integer branchId,
            String bookName,
            String author,
            String isbn,
            String category,
            String bookType,
            String status,
            Pageable pageable
    ) {
        return booksRepository.findAll((root, query, cb) -> {
            Predicate predicates = cb.conjunction();

            // 分馆ID条件
            if (branchId != null && branchId > 0) {
                predicates = cb.and(predicates, cb.equal(root.get("branchId"), branchId));
            }

            // 图书名称模糊查询
            if (bookName != null && !bookName.isBlank()) {
                predicates = cb.and(predicates, cb.like(root.get("bookName"), "%" + bookName + "%"));
            }

            // 作者模糊查询
            if (author != null && !author.isBlank()) {
                predicates = cb.and(predicates, cb.like(root.get("author"), "%" + author + "%"));
            }

            // ISBN精确查询
            if (isbn != null && !isbn.isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("isbn"), isbn));
            }

            // 分类精确查询
            if (category != null && !category.isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("category"), category));
            }

            // 图书类型精确查询（核心修复：确保与前端传递的book/magazine一致）
            if (bookType != null && !bookType.isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("bookType"), bookType));
            }

            // 状态精确查询（核心修复：确保与前端传递的AVAILABLE/OUT_OF_STOCK一致）
            if (status != null && !status.isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }

            return predicates;
        }, pageable);
    }
}