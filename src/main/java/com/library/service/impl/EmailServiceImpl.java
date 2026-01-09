//src/main/java/com/library/service/impl/EmailServiceImpl.java
package com.library.service.impl;

import com.library.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Override
    public void sendEmail(String to, String subject, String content) {
        // 控制台输出模拟邮件发送
        System.out.println("\n" + "=".repeat(60));
        System.out.println("  【模拟邮件发送】");
        System.out.println("-".repeat(60));
        System.out.println("发件人: library@jiaying.edu.cn");
        System.out.println("收件人: " + to);
        System.out.println("主 题: " + subject);
        System.out.println("时 间: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("-".repeat(60));
        System.out.println("内容:");
        System.out.println(content);
        System.out.println("=".repeat(60) + "\n");

        log.info("模拟发送邮件到: {}, 主题: {}", to, subject);
    }

    @Override
    public void sendTestEmail() {
        String to = "test@jiaying.edu.cn";
        String subject = "图书馆系统测试邮件";
        String content = """
            亲爱的用户，
            
            这是一封来自嘉应学院图书馆系统的测试邮件。
            
            当前为测试模式，邮件仅显示在控制台。
            
            祝您使用愉快！
            
            ******************************
            嘉应学院图书馆管理系统
            联系电话: 0753-2186000
            邮箱: library@jiaying.edu.cn
            ******************************
            """;

        sendEmail(to, subject, content);
    }
}