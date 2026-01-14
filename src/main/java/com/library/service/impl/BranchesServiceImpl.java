//src/main/java/com/library/service/impl/BranchesServiceImpl.java
package com.library.service.impl;

import com.library.entity.Branches;
import com.library.repository.BranchesRepository;
import com.library.service.BranchesService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BranchesServiceImpl implements BranchesService {

    private final BranchesRepository branchesRepository;

    @Override
    public List<Branches> findAll() {
        return branchesRepository.findAll();
    }
}
