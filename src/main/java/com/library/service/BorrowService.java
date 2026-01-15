package com.library.service;

import com.library.dto.BorrowCreateDTO;
import com.library.dto.ApiResponse;
import org.springframework.data.domain.Pageable;

public interface BorrowService {
    ApiResponse<?> borrowBook(BorrowCreateDTO borrowDTO);
    ApiResponse<?> returnBook(Long borrowId);
    ApiResponse<?> renewBook(Long bookId);
    ApiResponse<?> getMyBorrowList(Pageable pageable);
    ApiResponse<?> getMyBorrowHistory(Pageable pageable);

    // 【新增方法】支持userName和bookName筛选
    ApiResponse<?> getAllBorrowRecords(String userName, String bookName, Integer branchId, String status, Pageable pageable);

    ApiResponse<?> updateBorrowStatus(Long borrowId, String status);
}