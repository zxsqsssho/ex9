package com.library.service;

import org.springframework.data.domain.Pageable;
import com.library.dto.ApiResponse;
import java.util.List;

public interface FineService {
    // 核心修改：添加 String payStatus 参数
    ApiResponse<?> getMyFines(String payStatus, Pageable pageable);
    ApiResponse<?> payFine(Long fineId);
    ApiResponse<?> batchPayFines(List<Long> fineIds);
    ApiResponse<?> getFineDetail(Long fineId);
    ApiResponse<?> applyFineReduction(Long fineId, String reason);
    ApiResponse<?> getAllFines(String userName, String bookName, Integer branchId, String payStatus, Pageable pageable);
    ApiResponse<?> updateFineStatus(Long fineId, String status);
}