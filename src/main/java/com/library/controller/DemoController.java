//src/main/java/com/library/controller/DemoController.java
package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.service.EmailService;
import com.library.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 *    测试，可以删除
 */
@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class DemoController {

    private final EmailService emailService;
    private final NotificationService notificationService;

    /**
     * 发送测试邮件
     */
    @GetMapping("/email/test")
    public ApiResponse<?> sendTestEmail() {
        emailService.sendTestEmail();
        return ApiResponse.success("测试邮件已发送到控制台");
    }

    /**
     * 手动触发预定检查
     */
    @GetMapping("/check/reservations")
    public ApiResponse<?> checkReservations() {
        notificationService.checkReservationAvailable();
        return ApiResponse.success("预定检查完成，结果在控制台查看");
    }

    /**
     * 手动触发逾期检查
     */
    @GetMapping("/check/overdue")
    public ApiResponse<?> checkOverdue() {
        notificationService.checkOverdueBooks();
        return ApiResponse.success("逾期检查完成，结果在控制台查看");
    }

    /**
     * 手动发送待处理邮件
     */
    @GetMapping("/send/emails")
    public ApiResponse<?> sendEmails() {
        notificationService.processPendingEmails();
        return ApiResponse.success("邮件处理完成，结果在控制台查看");
    }

    /**
     * 发送预定通知演示
     */
    @GetMapping("/email/reservation")
    public ApiResponse<?> sendReservationEmail() {
        String to = "student@jiaying.edu.cn";
        String subject = "【演示】图书预定通知";
        String content = """
            亲爱的用户：
            
            这是图书预定通知的演示邮件。
            
            当您预定的图书被其他用户预定，您将收到此类通知。
            
            当前为演示模式，邮件内容仅显示在控制台。
            
            -----------------------------
            演示系统
            时间：""" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        emailService.sendEmail(to, subject, content);
        return ApiResponse.success("预定通知邮件已发送到控制台");
    }

    /**
     * 发送逾期提醒演示
     */
    @GetMapping("/email/overdue")
    public ApiResponse<?> sendOverdueEmail() {
        String to = "teacher@jiaying.edu.cn";
        String subject = "【演示】图书逾期提醒（第2周）";
        String content = """
            亲爱的张老师：
            
            您的图书已逾期14天（第2周），请及时归还。
            
            逾期罚款规则：
            - 超过30天后，每天罚款1元
            - 有未付罚款无法借阅新书
            
            当前为演示模式，邮件内容仅显示在控制台。
            
            -----------------------------
            演示系统
            时间：""" + java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        emailService.sendEmail(to, subject, content);
        return ApiResponse.success("逾期提醒邮件已发送到控制台");
    }
}