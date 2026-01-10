package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.BookCreateDTO;
import com.library.dto.BookUpdateDTO;
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
     * 分页查询图书（所有用户可访问）
     */
    @GetMapping
    public ApiResponse<?> getBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) String bookName,
            @RequestParam(required = false) String author) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Books> booksPage = booksService.findBooks(branchId, bookName, author, pageable);
        return ApiResponse.success(booksPage);
    }

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
}