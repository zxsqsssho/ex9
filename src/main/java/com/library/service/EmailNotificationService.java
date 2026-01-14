//src/main/java/com/library/service/EmailNotificationService.java
package com.library.service;

public interface EmailNotificationService {

    /**
     * 图书可借时通知预定者
     * @param reservationId 预定记录ID
     */
    void notifyReservationAvailable(Long reservationId);

    /**
     * 图书被预定时通知当前借阅者尽快还书
     * @param bookId 图书ID
     * @param reservationUserId 预定用户ID
     */
    void notifyCurrentBorrowerForReservation(Long bookId, Long reservationUserId);

    /**
     * 发送逾期提醒邮件
     * @param borrowRecordId 借阅记录ID
     */
    void sendOverdueReminder(Long borrowRecordId);

    /**
     * 执行定时通知任务
     */
    void scheduledNotificationTasks();
}