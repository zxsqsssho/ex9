package com.library;

import com.library.entity.User;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 修改数据库用户密码
    @Override
    public void run(String... args) {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            // 使用BCrypt加密密码
            String encryptedPassword = passwordEncoder.encode("123456");
            user.setPassword(encryptedPassword);
        }

        // 保存到数据库
        userRepository.saveAll(users);
        System.out.println("已成功更新所有用户密码");
    }
}