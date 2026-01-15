package com.library.service.impl;

import com.library.entity.Books;
import com.library.repository.BooksRepository;
import com.library.service.BooksService;
import com.library.service.AuthService;
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
    private final AuthService authService; // 注入AuthService获取当前用户信息

    // 基础 CRUD 方法
    @Override
    public Page<Books> findAll(Pageable pageable) {
        // 核心修改：分馆管理员查询所有图书，不筛选分馆
        return booksRepository.findAll(pageable);
    }

    @Override
    public Books findById(Long id) {
        Books book = booksRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("图书不存在"));
        // 保留：分馆管理员只能修改本馆图书，但可查看所有分馆图书详情
        return book;
    }

    @Override
    @Transactional
    public Books save(Books books) {
        // 保留：分馆管理员只能添加/修改本馆图书
        if (authService.isBranchAdmin()) {
            Integer currentBranchId = authService.getCurrentUserBranchId();
            books.setBranchId(currentBranchId); // 强制设置为当前管理员的分馆ID
        }
        return booksRepository.save(books);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Books book = findById(id);
        // 保留：分馆管理员只能删除本馆图书
        if (authService.isBranchAdmin() && !book.getBranchId().equals(authService.getCurrentUserBranchId())) {
            throw new EntityNotFoundException("无权删除其他分馆图书");
        }
        booksRepository.deleteById(id);
    }

    // 核心修复1：4 参数多条件查询（接口声明，必须实现）
    @Override
    public Page<Books> findBooks(Integer branchId, String bookName, String author, Pageable pageable) {
        // 复用多条件查询逻辑，未传递的参数设为 null
        return findBooks(branchId, bookName, author, null, null, null, null, pageable);
    }

    // 核心修复2：8 参数多条件查询（重载，支持更多筛选条件）
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
        // 核心修改：移除分馆管理员的强制branchId筛选，允许查询所有分馆
        return booksRepository.findAll((root, query, cb) -> {
            Predicate predicates = cb.conjunction();

            // 分馆ID筛选：仅当传入branchId时生效（系统管理员可筛选，分馆管理员可手动选择）
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
            // 图书类型精确查询
            if (bookType != null && !bookType.isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("bookType"), bookType));
            }
            // 状态精确匹配（AVAILABLE/OUT_OF_STOCK）
            if (status != null && !status.isBlank()) {
                predicates = cb.and(predicates, cb.equal(root.get("status"), status));
            }

            return predicates;
        }, pageable);
    }
}