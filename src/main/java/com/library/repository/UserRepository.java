//src/main/java/com/library/repository/UserRepository.java
package com.library.repository;

import com.library.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    List<User> findByUserType(User.UserType userType);
    List<User> findByBranchId(Integer branchId); // 新增方法
}