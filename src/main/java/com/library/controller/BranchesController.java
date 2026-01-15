package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.entity.Branches;
import com.library.repository.BranchesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchesController {
    private final BranchesRepository branchesRepository;

    /**
     * 获取所有分馆列表（公开接口，无需管理员权限）
     */
    @GetMapping
    public ApiResponse<?> getAllBranches() {
        List<Branches> branches = branchesRepository.findAll();
        return ApiResponse.success(branches);
    }
}