//src/main/java/com/library/controller/BorrowController.java
package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.BorrowCreateDTO;
import com.library.service.AuthService;
import com.library.service.BorrowService; // 新增：导入 BorrowService 接口
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrow")
@RequiredArgsConstructor
public class BorrowController {
    private final BorrowService borrowService;
    private final AuthService authService;

    /**
     * 用户借阅图书
     */
    @PostMapping
    public ApiResponse<?> borrowBook(@Valid @RequestBody BorrowCreateDTO borrowDTO) {
        return borrowService.borrowBook(borrowDTO);
    }

    /**
     * 用户归还图书
     */
    @PostMapping("/return/{borrowId}")
    public ApiResponse<?> returnBook(@PathVariable Long borrowId) {
        return borrowService.returnBook(borrowId);
    }

    /**
     * 用户续借图书
     */
    @PostMapping("/renew/{bookId}")
    public ApiResponse<?> renewBook(@PathVariable Long bookId) {
        return borrowService.renewBook(bookId);
    }

    /**
     * 获取当前用户借阅列表
     */
    @GetMapping("/my-borrow")
    public ApiResponse<?> getMyBorrowList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return borrowService.getMyBorrowList(pageable);
    }

    /**
     * 获取当前用户借阅历史
     */
    @GetMapping("/my-history")
    public ApiResponse<?> getMyBorrowHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return borrowService.getMyBorrowHistory(pageable);
    }

    /**
     * 管理员获取所有借阅记录
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> getAllBorrowRecords(
            @RequestParam(required = false) Integer branchId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return borrowService.getAllBorrowRecords(branchId, status, pageable);
    }

    /**
     * 管理员更新借阅记录状态
     */
    @PutMapping("/{borrowId}/status")
    @PreAuthorize("hasRole('SYSTEM_ADMIN') or hasRole('BRANCH_ADMIN')")
    public ApiResponse<?> updateBorrowStatus(
            @PathVariable Long borrowId,
            @RequestParam String status) {
        return borrowService.updateBorrowStatus(borrowId, status);
    }
}