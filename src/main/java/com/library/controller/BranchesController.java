//src/main/java/com/library/controller/BranchesController.java
package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.entity.Branches;
import com.library.service.BranchesService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
@RequiredArgsConstructor
public class BranchesController {

    private final BranchesService branchesService;

    /**
     * 获取所有分馆（给前端下拉框用）
     */
    @GetMapping
    public ApiResponse<List<Branches>> getAllBranches() {
        return ApiResponse.success(branchesService.findAll());
    }
}

