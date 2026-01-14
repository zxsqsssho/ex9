//src/main/java/com/library/service/impl/FineServiceImpl.java
package com.library.service.impl;

import com.library.dto.ApiResponse;
import com.library.entity.Fine;
import com.library.entity.BorrowRecord;
import com.library.entity.User;
import com.library.repository.FineRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.UserRepository;
import com.library.service.FineService;
import com.library.service.AuthService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FineServiceImpl implements FineService {
    private final FineRepository fineRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final UserRepository userRepository;
    private final AuthService authService;

    @Override
    public ApiResponse<?> getMyFines(Pageable pageable) {
        Long userId = authService.getCurrentUserId();
        // 核心修复：替换为关联查询方法
        Page<Fine> fines = fineRepository.findByBorrowRecordUserId(userId, pageable);
        return ApiResponse.success(fines);
    }

    @Override
    @Transactional
    public ApiResponse<?> payFine(Long fineId) {
        Long userId = authService.getCurrentUserId();
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new EntityNotFoundException("罚款记录不存在"));

        // 检查是否是本人罚款或管理员（通过借阅记录关联用户）
        BorrowRecord borrowRecord = borrowRecordRepository.findById(fine.getRecordId())
                .orElseThrow(() -> new EntityNotFoundException("借阅记录不存在"));
        if (!borrowRecord.getUserId().equals(userId) && !authService.isSystemAdmin() && !authService.isBranchAdmin()) {
            return ApiResponse.error(403, "无权支付他人的罚款");
        }

        // 已支付则无需处理
        if ("paid".equals(fine.getPayStatus())) {
            return ApiResponse.error(400, "该罚款已支付");
        }

        fine.setPayStatus("paid");
        fine.setPayTime(java.time.LocalDateTime.now());
        fineRepository.save(fine);

        return ApiResponse.success("支付成功");
    }

    @Override
    @Transactional
    public ApiResponse<?> batchPayFines(List<Long> fineIds) {
        Long userId = authService.getCurrentUserId();
        List<Fine> fines = fineRepository.findAllById(fineIds);

        for (Fine fine : fines) {
            BorrowRecord borrowRecord = borrowRecordRepository.findById(fine.getRecordId())
                    .orElseThrow(() -> new EntityNotFoundException("借阅记录不存在"));
            if (!borrowRecord.getUserId().equals(userId) && !authService.isSystemAdmin() && !authService.isBranchAdmin()) {
                return ApiResponse.error(403, "存在无权支付的罚款记录");
            }
            if ("paid".equals(fine.getPayStatus())) {
                return ApiResponse.error(400, "存在已支付的罚款记录");
            }
            fine.setPayStatus("paid");
            fine.setPayTime(java.time.LocalDateTime.now());
        }

        fineRepository.saveAll(fines);
        return ApiResponse.success("批量支付成功");
    }

    @Override
    public ApiResponse<?> getFineDetail(Long fineId) {
        Long userId = authService.getCurrentUserId();
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new EntityNotFoundException("罚款记录不存在"));

        // 检查是否是本人罚款或管理员
        BorrowRecord borrowRecord = borrowRecordRepository.findById(fine.getRecordId())
                .orElseThrow(() -> new EntityNotFoundException("借阅记录不存在"));
        if (!borrowRecord.getUserId().equals(userId) && !authService.isSystemAdmin() && !authService.isBranchAdmin()) {
            return ApiResponse.error(403, "无权查看他人的罚款详情");
        }

        return ApiResponse.success(fine);
    }

    @Override
    public ApiResponse<?> applyFineReduction(Long fineId, String reason) {
        // 实际项目中需结合审批流程，此处简化处理
        return ApiResponse.success("减免申请已提交，等待管理员审核");
    }

    @Override
    public ApiResponse<?> getAllFines(Integer branchId, String payStatus, Pageable pageable) {
        Page<Fine> fines;
        if (branchId != null && payStatus != null) {
            fines = fineRepository.findByBorrowRecordBranchIdAndPayStatus(branchId, payStatus, pageable);
        } else if (branchId != null) {
            fines = fineRepository.findByBorrowRecordBranchId(branchId, pageable);
        } else if (payStatus != null) {
            fines = fineRepository.findByPayStatus(payStatus, pageable);
        } else {
            fines = fineRepository.findAll(pageable);
        }
        return ApiResponse.success(fines);
    }

    @Override
    @Transactional
    public ApiResponse<?> updateFineStatus(Long fineId, String status) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new EntityNotFoundException("罚款记录不存在"));
        fine.setPayStatus(status);
        if ("paid".equals(status) && fine.getPayTime() == null) {
            fine.setPayTime(java.time.LocalDateTime.now());
        }
        fineRepository.save(fine);
        return ApiResponse.success("罚款状态更新成功");
    }
}