//src/main/java/com/library/repository/BranchesRepository.java
package com.library.repository;

import com.library.entity.Branches;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BranchesRepository extends JpaRepository<Branches, Integer> {
}
