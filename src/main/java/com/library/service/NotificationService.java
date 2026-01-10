package com.library.service;

import com.library.entity.Reservation;

public interface NotificationService {
    // 预定提醒功能
    void checkReservationAvailable();
    void notifyCurrentLender(Long bookId);
    // 新增：通知预定者图书可借（公开方法）
    void notifyReservationAvailable(Reservation reservation);
    // 邮件处理
    void processPendingEmails();
    // 逾期提醒功能
    void checkOverdueBooks();
}