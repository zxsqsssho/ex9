package com.library.service;

import com.library.dto.BookCreateDTO;
import com.library.dto.BookUpdateDTO;
import com.library.entity.Books;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BooksService {
    Page<Books> findAll(Pageable pageable);
    Books findById(Long id);
    Books save(BookCreateDTO bookDTO);
    Books update(Long id, BookUpdateDTO bookDTO);
    void deleteById(Long id);
    Page<Books> findBooks(Integer branchId, String bookName, String author, Pageable pageable);
}