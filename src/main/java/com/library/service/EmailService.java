//src/main/java/com/library/service/EmailService.java
package com.library.service;

/**
 * 邮件发送模块
 */
public interface EmailService {
    void sendEmail(String to, String subject, String content);
    void sendTestEmail();
}
