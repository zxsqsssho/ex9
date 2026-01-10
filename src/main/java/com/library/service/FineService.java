// FineService.java
package com.library.service;

import org.springframework.data.domain.Pageable;
import com.library.dto.ApiResponse;
import java.util.List;

public interface FineService {
    ApiResponse<?> getMyFines(Pageable pageable);
    ApiResponse<?> payFine(Long fineId);
    ApiResponse<?> batchPayFines(List<Long> fineIds);
    ApiResponse<?> getFineDetail(Long fineId);
    ApiResponse<?> applyFineReduction(Long fineId, String reason);
    ApiResponse<?> getAllFines(Integer branchId, String payStatus, Pageable pageable);
    ApiResponse<?> updateFineStatus(Long fineId, String status);
}