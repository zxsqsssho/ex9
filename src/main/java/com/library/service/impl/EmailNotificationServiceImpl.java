// src/main/java/com/library/service/impl/EmailNotificationServiceImpl.java
package com.library.service.impl;

import com.library.entity.*;
import com.library.repository.*;
import com.library.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationServiceImpl implements EmailNotificationService {

    private final ReservationRepository reservationRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final UserRepository userRepository;
    private final BooksRepository booksRepository;
    private final NotificationRepository notificationRepository;

    // 模拟邮件发送（实际项目中可替换为真正的邮件服务）
    private void sendEmail(String to, String subject, String content) {
        log.info("===== 发送邮件通知 =====");
        log.info("收件人: {}", to);
        log.info("主题: {}", subject);
        log.info("内容: {}", content);
        log.info("===== 邮件发送完成 =====");
    }

    private void saveNotification(Long userId, String title, String content,
                                  Notification.NotificationType type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setType(type);
        notification.setRead(false);
        notification.setImportant(type == Notification.NotificationType.OVERDUE_REMINDER);
        notification.setCreatedAt(LocalDateTime.now());

        notificationRepository.save(notification);
    }


    @Override
    @Transactional
    public void notifyReservationAvailable(Long reservationId) {
        try {
            Reservation reservation = reservationRepository.findById(reservationId)
                    .orElseThrow(() -> new RuntimeException("预定记录不存在"));

            if (reservation.getStatus() != Reservation.ReservationStatus.PENDING) {
                log.warn("图书尚未准备就绪，预定ID: {}", reservationId);
                return;
            }

            User user = userRepository.findById(reservation.getUserId())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            Books book = booksRepository.findById(reservation.getBookId())
                    .orElseThrow(() -> new RuntimeException("图书不存在"));

            String subject = "图书可借通知 - " + book.getBookName();
            String content = String.format(
                    "亲爱的 %s 用户，\n\n" +
                            "您预定的图书《%s》已经准备就绪，可以前来借阅。\n" +
                            "预定编号：%d\n" +
                            "图书信息：%s（ISBN：%s）\n" +
                            "分馆ID：%d\n" +
                            "请在 %s 之前前来借阅，逾期预定将自动取消。\n\n" +
                            "感谢使用图书馆管理系统！",
                    user.getRealName(),
                    book.getBookName(),
                    reservation.getId(),
                    book.getAuthor(),
                    book.getIsbn(),
                    reservation.getBranchId(),
                    reservation.getExpiryTime()
            );

            sendEmail(user.getEmail(), subject, content);
            log.info("已发送图书可借通知给用户: {}, 邮箱: {}", user.getUsername(), user.getEmail());
            saveNotification(
                    user.getId(),
                    subject,
                    content,
                    Notification.NotificationType.RESERVATION_AVAILABLE
            );

        } catch (Exception e) {
            log.error("发送预定可借通知失败，预定ID: {}", reservationId, e);
        }
    }

    @Override
    @Transactional
    public void notifyCurrentBorrowerForReservation(Long bookId, Long reservationUserId) {
        try {
            // 查找当前借阅该图书的用户（使用修复后的方法）
            List<BorrowRecord> currentBorrows = borrowRecordRepository
                    .findByBookIdAndStatus(bookId, BorrowRecord.BorrowStatus.BORROWED);

            if (currentBorrows.isEmpty()) {
                log.info("图书当前没有被借阅，图书ID: {}", bookId);
                return;
            }

            Books book = booksRepository.findById(bookId)
                    .orElseThrow(() -> new RuntimeException("图书不存在"));

            User reservationUser = userRepository.findById(reservationUserId)
                    .orElseThrow(() -> new RuntimeException("预定用户不存在"));

            for (BorrowRecord borrowRecord : currentBorrows) {
                User borrower = userRepository.findById(borrowRecord.getUserId())
                        .orElseThrow(() -> new RuntimeException("借阅用户不存在"));

                String subject = "图书预定提醒 - " + book.getBookName();
                String content = String.format(
                        "亲爱的 %s 用户，\n\n" +
                                "您借阅的图书《%s》已被其他用户预定。\n" +
                                "预定用户：%s\n" +
                                "图书信息：%s（ISBN：%s）\n" +
                                "当前借阅到期时间：%s\n\n" +
                                "请尽快归还图书，以便其他用户借阅。\n" +
                                "感谢您的配合！",
                        borrower.getRealName(),
                        book.getBookName(),
                        reservationUser.getRealName(),
                        book.getAuthor(),
                        book.getIsbn(),
                        borrowRecord.getDueTime()
                );

                sendEmail(borrower.getEmail(), subject, content);
                log.info("已发送预定提醒给当前借阅者: {}, 邮箱: {}", borrower.getUsername(), borrower.getEmail());
                saveNotification(
                        borrower.getId(),
                        subject,
                        content,
                        Notification.NotificationType.RESERVATION_REMINDER
                );
            }
        } catch (Exception e) {
            log.error("发送预定提醒失败，图书ID: {}, 预定用户ID: {}", bookId, reservationUserId, e);
        }
    }

    @Override
    @Transactional
    public void sendOverdueReminder(Long borrowRecordId) {
        try {
            BorrowRecord borrowRecord = borrowRecordRepository.findById(borrowRecordId)
                    .orElseThrow(() -> new RuntimeException("借阅记录不存在"));

            if (borrowRecord.getStatus() != BorrowRecord.BorrowStatus.OVERDUE) {
                log.warn("该借阅记录未逾期，借阅记录ID: {}", borrowRecordId);
                return;
            }

            User user = userRepository.findById(borrowRecord.getUserId())
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            Books book = booksRepository.findById(borrowRecord.getBookId())
                    .orElseThrow(() -> new RuntimeException("图书不存在"));

            // 计算逾期周数
            long overdueWeeks = Duration.between(borrowRecord.getDueTime(), LocalDateTime.now())
                    .toDays() / 7;

            String subject = String.format("图书逾期提醒（第%d周） - %s", overdueWeeks, book.getBookName());
            String content = String.format(
                    "亲爱的 %s 用户，\n\n" +
                            "您借阅的图书《%s》已逾期 %d 周。\n" +
                            "借阅记录ID：%d\n" +
                            "图书信息：%s（ISBN：%s）\n" +
                            "应还日期：%s\n" +
                            "逾期天数：%d 天\n" +
                            "逾期周数：%d 周\n\n" +
                            "请尽快归还图书，以免产生更多罚款。\n" +
                            "当前状态：%s\n\n" +
                            "感谢使用图书馆管理系统！",
                    user.getRealName(),
                    book.getBookName(),
                    overdueWeeks,
                    borrowRecord.getId(),
                    book.getAuthor(),
                    book.getIsbn(),
                    borrowRecord.getDueTime(),
                    borrowRecord.getOverdueDays() != null ? borrowRecord.getOverdueDays() : 0,
                    overdueWeeks,
                    borrowRecord.getStatus()
            );

            sendEmail(user.getEmail(), subject, content);
            log.info("已发送逾期提醒给用户: {}, 逾期周数: {}, 邮箱: {}",
                    user.getUsername(), overdueWeeks, user.getEmail());
            saveNotification(
                    user.getId(),
                    subject,
                    content,
                    Notification.NotificationType.OVERDUE_REMINDER
            );
        } catch (Exception e) {
            log.error("发送逾期提醒失败，借阅记录ID: {}", borrowRecordId, e);
        }
    }

    // 定时任务：每天凌晨2点检查并发送通知
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void scheduledNotificationTasks() {
        log.info("开始执行定时通知任务...");

        try {
            // 1. 检查预定到期的图书，通知预定者图书可借
            checkAndNotifyReservationReady();

            // 2. 检查逾期图书，发送分级提醒
            checkAndSendOverdueReminders();

            log.info("定时通知任务执行完成");
        } catch (Exception e) {
            log.error("定时通知任务执行失败", e);
        }
    }

    private void checkAndNotifyReservationReady() {
        try {
            // 查找状态为READY的预定记录
            List<Reservation> readyReservations = reservationRepository
                    .findByStatus(Reservation.ReservationStatus.PENDING);

            log.info("找到 {} 个准备就绪的预定记录", readyReservations.size());

            for (Reservation reservation : readyReservations) {
                try {
                    notifyReservationAvailable(reservation.getId());
                    log.info("已处理预定可借通知: {}", reservation.getId());
                } catch (Exception e) {
                    log.error("发送预定可借通知失败: {}", reservation.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("检查预定就绪状态失败", e);
        }
    }

    private void checkAndSendOverdueReminders() {
        try {
            // 查找所有逾期借阅记录（使用修复后的方法）
            List<BorrowRecord> overdueRecords = borrowRecordRepository
                    .findAllByStatus(BorrowRecord.BorrowStatus.OVERDUE);

            log.info("找到 {} 个逾期借阅记录", overdueRecords.size());

            for (BorrowRecord record : overdueRecords) {
                try {
                    // 计算逾期周数
                    long overdueWeeks = Duration.between(record.getDueTime(), LocalDateTime.now())
                            .toDays() / 7;

                    // 只在2周、6周、10周时发送提醒
                    if (overdueWeeks == 2 || overdueWeeks == 6 || overdueWeeks == 10) {
                        sendOverdueReminder(record.getId());
                        log.info("已发送逾期{}周提醒: {}", overdueWeeks, record.getId());
                    }
                } catch (Exception e) {
                    log.error("发送逾期提醒失败: {}", record.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("检查逾期记录失败", e);
        }
    }

}