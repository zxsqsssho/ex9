//src/main/java/com/library/config/DataInitializer.java
package com.library.config;

import com.library.entity.Role;
import com.library.entity.User;
import com.library.repository.RoleRepository;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner initDatabase(
            UserRepository userRepository,
            RoleRepository roleRepository) {
        return args -> {
            // 创建角色
            Role systemAdminRole = createRoleIfNotExists(
                    roleRepository,
                    "ROLE_SYSTEM_ADMIN",
                    "系统管理员",
                    Set.of(
                            "user:create", "user:read", "user:update", "user:delete",
                            "book:create", "book:read", "book:update", "book:delete",
                            "borrow:create", "borrow:read", "borrow:update", "borrow:delete"
                    ),
                    true
            );

            Role branchAdminRole = createRoleIfNotExists(
                    roleRepository,
                    "ROLE_BRANCH_ADMIN",
                    "分馆管理员",
                    Set.of(
                            "user:create", "user:read", "user:update",
                            "book:create", "book:read", "book:update",
                            "borrow:create", "borrow:read", "borrow:update"
                    ),
                    true
            );

            Role teacherRole = createRoleIfNotExists(
                    roleRepository,
                    "ROLE_TEACHER",
                    "教师",
                    Set.of(
                            "book:read",
                            "borrow:create", "borrow:read"
                    ),
                    true
            );

            Role studentRole = createRoleIfNotExists(
                    roleRepository,
                    "ROLE_STUDENT",
                    "学生",
                    Set.of(
                            "book:read",
                            "borrow:create", "borrow:read"
                    ),
                    true
            );

            // 创建默认系统管理员
            if (!userRepository.existsByUsername("admin")) {
                User adminUser = new User();
                adminUser.setUsername("admin");
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                adminUser.setRealName("系统管理员");
                adminUser.setEmail("admin@library.com");
                adminUser.setUserType(User.UserType.SYSTEM_ADMIN);
                adminUser.setStatus(User.UserStatus.ACTIVE);
                adminUser.setRole(systemAdminRole);
                adminUser.setBranchId(1); // 校本部
                userRepository.save(adminUser);
                System.out.println("创建默认管理员用户: admin/admin123");
            }

            // 创建各分馆管理员
            String[] branchAdmins = {
                    "branchadmin1", "branchadmin2", "branchadmin3", "branchadmin4", "branchadmin5"
            };
            String[] branchNames = {"校本部", "黄塘校区", "程江校区", "江南校区", "丰顺校区"};

            for (int i = 0; i < branchAdmins.length; i++) {
                if (!userRepository.existsByUsername(branchAdmins[i])) {
                    User branchAdmin = new User();
                    branchAdmin.setUsername(branchAdmins[i]);
                    branchAdmin.setPassword(passwordEncoder.encode("branch123"));
                    branchAdmin.setRealName(branchNames[i] + "管理员");
                    branchAdmin.setEmail(branchAdmins[i] + "@library.com");
                    branchAdmin.setUserType(User.UserType.BRANCH_ADMIN);
                    branchAdmin.setStatus(User.UserStatus.ACTIVE);
                    branchAdmin.setRole(branchAdminRole);
                    branchAdmin.setBranchId(i + 1);
                    userRepository.save(branchAdmin);
                    System.out.println("创建分馆管理员用户: " + branchAdmins[i] + "/branch123 (分馆ID: " + (i+1) + ")");
                }
            }

            // 创建教师用户
            String[] teachers = {"teacher1", "teacher2", "teacher3"};
            String[] teacherNames = {"张老师", "王老师", "李老师"};

            for (int i = 0; i < teachers.length; i++) {
                if (!userRepository.existsByUsername(teachers[i])) {
                    User teacher = new User();
                    teacher.setUsername(teachers[i]);
                    teacher.setPassword(passwordEncoder.encode("teacher123"));
                    teacher.setRealName(teacherNames[i]);
                    teacher.setEmail(teachers[i] + "@school.com");
                    teacher.setUserType(User.UserType.TEACHER);
                    teacher.setStatus(User.UserStatus.ACTIVE);
                    teacher.setRole(teacherRole);
                    teacher.setBranchId((i % 5) + 1); // 分配到不同分馆
                    userRepository.save(teacher);
                    System.out.println("创建教师用户: " + teachers[i] + "/teacher123");
                }
            }

            // 创建学生用户
            String[] students = {"student1", "student2", "student3", "student4", "student5"};
            String[] studentNames = {"李同学", "王同学", "张同学", "赵同学", "刘同学"};

            for (int i = 0; i < students.length; i++) {
                if (!userRepository.existsByUsername(students[i])) {
                    User student = new User();
                    student.setUsername(students[i]);
                    student.setPassword(passwordEncoder.encode("student123"));
                    student.setRealName(studentNames[i]);
                    student.setEmail(students[i] + "@school.com");
                    student.setUserType(User.UserType.STUDENT);
                    student.setStatus(User.UserStatus.ACTIVE);
                    student.setRole(studentRole);
                    student.setBranchId((i % 5) + 1); // 分配到不同分馆
                    userRepository.save(student);
                    System.out.println("创建学生用户: " + students[i] + "/student123");
                }
            }

            System.out.println("数据初始化完成！");
        };
    }

    private Role createRoleIfNotExists(RoleRepository roleRepository,
                                       String name,
                                       String description,
                                       Set<String> permissions,
                                       boolean systemRole) {
        return roleRepository.findByName(name)
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName(name);
                    role.setDescription(description);
                    role.setPermissions(permissions);
                    role.setSystemRole(systemRole);
                    return roleRepository.save(role);
                });
    }
}