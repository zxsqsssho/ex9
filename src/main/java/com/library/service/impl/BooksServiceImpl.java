// src/main/java/com/library/service/impl/BooksServiceImpl.java
package com.library.service.impl;

import com.library.dto.BookCreateDTO;
import com.library.dto.BookUpdateDTO;
import com.library.entity.Books;
import com.library.repository.BooksRepository;
import com.library.service.BooksService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
    public Books save(BookCreateDTO bookDTO) {
        Books book = new Books();
        book.setBookName(bookDTO.getBookName());
        book.setAuthor(bookDTO.getAuthor());
        book.setIsbn(bookDTO.getIsbn());
        book.setCategory(bookDTO.getCategory());
        book.setBranchId(bookDTO.getBranchId());
        book.setBookType(bookDTO.getBookType());
        book.setTotalNum(bookDTO.getTotalNum());
        book.setAvailableNum(bookDTO.getAvailableNum());
        book.setStatus(bookDTO.getAvailableNum() > 0 ? "normal" : "out_of_stock");
        book.setCreateTime(LocalDateTime.now());
        return booksRepository.save(book);
    }

    @Override
    @Transactional
    public Books update(Long id, BookUpdateDTO bookDTO) {
        Books book = findById(id);
        if (bookDTO.getBookName() != null) {
            book.setBookName(bookDTO.getBookName());
        }
        if (bookDTO.getAuthor() != null) {
            book.setAuthor(bookDTO.getAuthor());
        }
        if (bookDTO.getCategory() != null) {
            book.setCategory(bookDTO.getCategory());
        }
        if (bookDTO.getBookType() != null) {
            book.setBookType(bookDTO.getBookType());
        }
        if (bookDTO.getTotalNum() != null) {
            book.setTotalNum(bookDTO.getTotalNum());
        }
        if (bookDTO.getAvailableNum() != null) {
            book.setAvailableNum(bookDTO.getAvailableNum());
        }
        if (bookDTO.getStatus() != null) {
            book.setStatus(bookDTO.getStatus());
        }
        return booksRepository.save(book);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        booksRepository.deleteById(id);
    }

    @Override
    public Page<Books> findBooks(
            Integer branchId,
            String bookName,
            String author,
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
