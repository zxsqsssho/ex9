package com.library.repository;

import com.library.entity.Branches;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // 关键：添加@Repository注解

@Repository // 让Spring识别为数据访问Bean，必须加！
public interface BranchesRepository extends JpaRepository<Branches, Integer> {
    // 继承JpaRepository<实体类, 主键类型>，Branches主键branchId是Integer类型
}