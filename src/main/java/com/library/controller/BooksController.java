//src/main/java/com/library/controller/BooksController.java
package com.library.controller;

import com.library.dto.*;
import com.library.entity.Books;
import com.library.entity.User;
import com.library.service.BooksService;
import com.library.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BooksController {
    private final BooksService booksService;
    private final AuthService authService;

    /**
     * 根据ID获取图书详情
     */
    @GetMapping("/{id}")
    public ApiResponse<?> getBookById(@PathVariable Long id) {
        Books book = booksService.findById(id);
        return ApiResponse.success(book);
    }

    /**
     * 添加图书（系统管理员/分馆管理员）
     */
    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> addBook(@Valid @RequestBody BookCreateDTO bookDTO) {
        // 分馆管理员只能添加本分馆的图书
        if (authService.isBranchAdmin()) {
            bookDTO.setBranchId(authService.getCurrentUserBranchId());
        }
        Books savedBook = booksService.save(bookDTO);
        return ApiResponse.success("图书添加成功", savedBook);
    }

    /**
     * 修改图书信息（系统管理员/分馆管理员）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookUpdateDTO bookDTO) {
        Books book = booksService.findById(id);
        // 分馆管理员只能修改本分馆的图书
        if (authService.isBranchAdmin() && !book.getBranchId().equals(authService.getCurrentUserBranchId())) {
            return ApiResponse.error(403, "无权修改其他分馆的图书");
        }
        Books updatedBook = booksService.update(id, bookDTO);
        return ApiResponse.success("图书更新成功", updatedBook);
    }

    /**
     * 删除图书（系统管理员/分馆管理员，需库存为0）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> deleteBook(@PathVariable Long id) {
        Books book = booksService.findById(id);
        // 分馆管理员只能删除本分馆的图书
        if (authService.isBranchAdmin() && !book.getBranchId().equals(authService.getCurrentUserBranchId())) {
            return ApiResponse.error(403, "无权删除其他分馆的图书");
        }
        // 库存不为0不能删除
        if (book.getAvailableNum() > 0) {
            return ApiResponse.error(400, "图书仍有可借库存，无法删除");
        }
        booksService.deleteById(id);
        return ApiResponse.success("图书删除成功");
    }

    @PostMapping("/query")
    public ApiResponse<?> queryBooks(@RequestBody BookQueryDTO dto) {

        Pageable pageable = PageRequest.of(
                dto.getPageNum() - 1,
                dto.getPageSize()
        );

        Page<Books> booksPage = booksService.findBooks(
                dto.getBranchId(),
                dto.getBookName(),
                dto.getAuthor(),
                dto.getBookType(),
                dto.getStatus(),
                dto.getCategory(),
                pageable
        );

        // ⭐⭐ 关键：Entity → DTO 映射
        Page<BookListDTO> dtoPage = booksPage.map(book -> {
            BookListDTO d = new BookListDTO();
            d.setBookId(book.getBookId());
            d.setBookName(book.getBookName());
            d.setAuthor(book.getAuthor());
            d.setIsbn(book.getIsbn());
            d.setCategory(book.getCategory());
            d.setBookType(book.getBookType());
            d.setAvailableNum(book.getAvailableNum());
            d.setStatus(book.getStatus());

            d.setBranchId(book.getBranchId());
            d.setBranchName(
                    book.getBranch() != null ? book.getBranch().getBranchName() : null
            );

            return d;
        });

        return ApiResponse.success(dtoPage);
    }




}