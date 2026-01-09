//src/main/java/com/library/service/NotificationService.java
package com.library.service;

public interface NotificationService {
    // 预定提醒功能
    void checkReservationAvailable();
    void notifyCurrentLender(Long bookId);

    // 邮件处理
    void processPendingEmails();

    // 逾期提醒功能
    void checkOverdueBooks();

}