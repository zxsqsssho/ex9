package com.library.service.impl;

import com.library.dto.ApiResponse;
import com.library.dto.FineDTO;
import com.library.entity.Fine;
import com.library.entity.BorrowRecord;
import com.library.entity.Books;
import com.library.entity.Branches;
import com.library.repository.FineRepository;
import com.library.repository.BorrowRecordRepository;
import com.library.repository.UserRepository;
import com.library.repository.BooksRepository;
import com.library.repository.BranchesRepository;
import com.library.service.FineService;
import com.library.service.AuthService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FineServiceImpl implements FineService {
    private final FineRepository fineRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final UserRepository userRepository;
    private final BooksRepository booksRepository;
    private final BranchesRepository branchesRepository;
    private final AuthService authService;

    /**
     * 核心修复：接收 payStatus 参数，按状态筛选 + 类型转换兼容
     */
    @Override
    public ApiResponse<?> getMyFines(String payStatus, Pageable pageable) {
        Long userId = authService.getCurrentUserId();
        if (userId == null) {
            return ApiResponse.error(401, "未登录");
        }

        Page<Fine> fines;
        // 按支付状态筛选（空值查询所有）
        if (StringUtils.hasText(payStatus)) {
            // 调用带状态的查询方法，确保类型转换
            fines = fineRepository.findByBorrowRecordUserIdAndPayStatus(userId, payStatus, pageable);
        } else {
            // 无状态筛选，查询所有
            fines = fineRepository.findByBorrowRecordUserId(userId, pageable);
        }

        // DTO 转换（处理空值 + 类型兼容）
        List<FineDTO> dtoList = fines.stream().map(fine -> {
            FineDTO dto = new FineDTO();
            dto.setFineId(fine.getFineId());
            dto.setRecordId(fine.getRecordId());
            dto.setFineAmount(fine.getFineAmount());
            dto.setPayStatus(fine.getPayStatus());
            dto.setPayTime(fine.getPayTime());

            // 关联借阅记录（处理 int → long 类型转换）
            BorrowRecord borrowRecord = null;
            if (fine.getRecordId() != null) {
                borrowRecord = borrowRecordRepository.findById(fine.getRecordId().longValue())
                        .orElse(null);
            }

            // 关联图书和分馆（空值处理）
            Books book = borrowRecord != null ? booksRepository.findById(borrowRecord.getBookId()).orElse(null) : null;
            Branches branch = borrowRecord != null ? branchesRepository.findById(borrowRecord.getBranchId()).orElse(null) : null;

            // 字段赋值（默认值兜底）
            dto.setBookName(book != null ? book.getBookName() : "未知图书");
            dto.setIsbn(book != null ? book.getIsbn() : "未知ISBN");
            dto.setOverdueDays(borrowRecord != null && borrowRecord.getOverdueDays() != null ? borrowRecord.getOverdueDays() : 0);
            dto.setBranchName(branch != null ? branch.getBranchName() : "未知分馆");

            return dto;
        }).collect(Collectors.toList());

        Page<FineDTO> dtoPage = new PageImpl<>(dtoList, pageable, fines.getTotalElements());
        return ApiResponse.success(dtoPage);
    }

    // 其他方法保持不变...
    @Override
    @Transactional
    public ApiResponse<?> payFine(Long fineId) {
        Long userId = authService.getCurrentUserId();
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new EntityNotFoundException("罚款记录不存在"));
        BorrowRecord borrowRecord = borrowRecordRepository.findById(fine.getRecordId().longValue())
                .orElseThrow(() -> new EntityNotFoundException("借阅记录不存在"));
        if (!borrowRecord.getUserId().equals(userId) && !authService.isSystemAdmin() && !authService.isBranchAdmin()) {
            return ApiResponse.error(403, "无权支付他人的罚款");
        }
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
            BorrowRecord borrowRecord = borrowRecordRepository.findById(fine.getRecordId().longValue())
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
        BorrowRecord borrowRecord = borrowRecordRepository.findById(fine.getRecordId().longValue())
                .orElseThrow(() -> new EntityNotFoundException("借阅记录不存在"));
        if (!borrowRecord.getUserId().equals(userId) && !authService.isSystemAdmin() && !authService.isBranchAdmin()) {
            return ApiResponse.error(403, "无权查看他人的罚款详情");
        }

        FineDTO dto = new FineDTO();
        dto.setFineId(fine.getFineId());
        dto.setRecordId(fine.getRecordId());
        dto.setFineAmount(fine.getFineAmount());
        dto.setPayStatus(fine.getPayStatus());
        dto.setPayTime(fine.getPayTime());

        Books book = booksRepository.findById(borrowRecord.getBookId()).orElse(null);
        dto.setBookName(book != null ? book.getBookName() : "未知图书");
        dto.setIsbn(book != null ? book.getIsbn() : "未知ISBN");
        dto.setOverdueDays(borrowRecord.getOverdueDays() != null ? borrowRecord.getOverdueDays() : 0);

        Branches branch = branchesRepository.findById(borrowRecord.getBranchId()).orElse(null);
        dto.setBranchName(branch != null ? branch.getBranchName() : "未知分馆");

        return ApiResponse.success(dto);
    }

    @Override
    public ApiResponse<?> applyFineReduction(Long fineId, String reason) {
        return ApiResponse.success("减免申请已提交，等待管理员审核");
    }

    @Override
    public ApiResponse<?> getAllFines(
            String userName,
            String bookName,
            Integer branchId,
            String payStatus,
            Pageable pageable) {
        String finalUserName = StringUtils.hasText(userName) ? userName : null;
        String finalBookName = StringUtils.hasText(bookName) ? bookName : null;
        String finalPayStatus = StringUtils.hasText(payStatus) ? payStatus : null;
        Integer queryBranchId = branchId;
        if (authService.isBranchAdmin() && queryBranchId == null) {
            queryBranchId = authService.getCurrentUserBranchId();
        }

        Page<Object[]> resultPage = fineRepository.findAllWithUserAndBook(
                finalUserName,
                finalBookName,
                queryBranchId,
                finalPayStatus,
                pageable
        );

        List<Map<String, Object>> content = resultPage.stream().map(row -> {
            Map<String, Object> map = new HashMap<>();
            map.put("fineId", row[0]);
            map.put("recordId", row[1]);
            map.put("fineAmount", row[2]);
            map.put("payStatus", row[3]);
            map.put("payTime", row[4]);
            map.put("userId", row[5]);
            map.put("userRealName", row[6]);
            map.put("bookName", row[7]);
            map.put("overdueDays", row[8] != null ? row[8] : 0);
            map.put("branchId", row[9]);
            return map;
        }).collect(Collectors.toList());

        Page<Map<String, Object>> responsePage = new PageImpl<>(content, pageable, resultPage.getTotalElements());
        return ApiResponse.success(responsePage);
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