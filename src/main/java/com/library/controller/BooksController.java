package com.library.controller;

import com.library.dto.*;
import com.library.entity.Books;
import com.library.entity.Branches;
import com.library.repository.BranchesRepository;
import com.library.service.BooksService;
import com.library.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BooksController {
    private final BooksService booksService;
    private final AuthService authService;
    private final BranchesRepository branchesRepository;

    /**
     * 分页查询图书（基础查询，所有用户可访问）
     */
    @GetMapping
    public ApiResponse<?> getBooks(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(pageNum - 1, size);
        Page<Books> booksPage = booksService.findAll(pageable);
        booksPage.getContent().forEach(this::fillBranchName);
        return ApiResponse.success(booksPage);
    }

    /**
     * 多条件搜索图书（适配前端高级查询）
     */
    @GetMapping("/search")
    public ApiResponse<?> searchBooks(BookQueryDTO queryDTO) {
        Pageable pageable = PageRequest.of(queryDTO.getPageNum() - 1, queryDTO.getPageSize());
        Page<Books> booksPage = booksService.findBooks(
                queryDTO.getBranchId(),
                queryDTO.getBookName(),
                queryDTO.getAuthor(),
                queryDTO.getIsbn(),
                queryDTO.getCategory(),
                queryDTO.getBookType(),
                queryDTO.getStatus(),
                pageable
        );
        booksPage.getContent().forEach(this::fillBranchName);
        return ApiResponse.success(booksPage);
    }

    /**
     * 根据ID获取图书详情
     */
    @GetMapping("/{id}")
    public ApiResponse<?> getBookById(@PathVariable Long id) {
        Books book = booksService.findById(id);
        fillBranchName(book);
        return ApiResponse.success(book);
    }

    /**
     * 添加图书（系统管理员/分馆管理员）
     */
    @PostMapping
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> addBook(@Valid @RequestBody BookCreateDTO bookDTO) {
        if (authService.isBranchAdmin()) {
            bookDTO.setBranchId(authService.getCurrentUserBranchId());
        }
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

        Books savedBook = booksService.save(book);
        fillBranchName(savedBook);
        return ApiResponse.success("图书添加成功", savedBook);
    }

    /**
     * 修改图书信息（核心修复：替换 update 为 save 方法）
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> updateBook(
            @PathVariable Long id,
            @Valid @RequestBody BookUpdateDTO bookDTO) {
        Books book = booksService.findById(id);
        // 分馆管理员权限控制
        if (authService.isBranchAdmin() && !book.getBranchId().equals(authService.getCurrentUserBranchId())) {
            return ApiResponse.error(403, "无权修改其他分馆的图书");
        }

        // 部分更新：只修改非空字段
        if (bookDTO.getBookName() != null) book.setBookName(bookDTO.getBookName());
        if (bookDTO.getAuthor() != null) book.setAuthor(bookDTO.getAuthor());
        if (bookDTO.getCategory() != null) book.setCategory(bookDTO.getCategory());
        if (bookDTO.getBookType() != null) book.setBookType(bookDTO.getBookType());
        if (bookDTO.getTotalNum() != null) book.setTotalNum(bookDTO.getTotalNum());
        if (bookDTO.getAvailableNum() != null) {
            book.setAvailableNum(bookDTO.getAvailableNum());
            book.setStatus(bookDTO.getAvailableNum() > 0 ? "normal" : "out_of_stock");
        }
        if (bookDTO.getStatus() != null) book.setStatus(bookDTO.getStatus());

        // 核心修复：BooksService 无 update 方法，使用 save 方法（JPA 新增/更新通用）
        Books updatedBook = booksService.save(book);
        fillBranchName(updatedBook);
        return ApiResponse.success("图书更新成功", updatedBook);
    }

    /**
     * 删除图书（系统管理员/分馆管理员）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> deleteBook(@PathVariable Long id) {
        Books book = booksService.findById(id);
        if (authService.isBranchAdmin() && !book.getBranchId().equals(authService.getCurrentUserBranchId())) {
            return ApiResponse.error(403, "无权删除其他分馆的图书");
        }
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
                dto.getIsbn(),
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

    /**
     * 辅助方法：补充分馆名称
     */
    private void fillBranchName(Books book) {
        if (book.getBranchId() != null) {
            branchesRepository.findById(book.getBranchId())
                    .ifPresent(branch -> book.setBranchName(branch.getBranchName()));
        }
    }
}