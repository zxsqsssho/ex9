//src/main/java/com/library/service/impl/NotificationServiceImpl.java
package com.library.service.impl;

import com.library.entity.*;
import com.library.repository.*;
import com.library.service.EmailService;
import com.library.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final ReservationRepository reservationRepository;
    private final BorrowRecordRepository borrowRecordRepository;
    private final EmailNotificationRepository emailNotificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    // ========== 预定提醒功能 ==========
    /**
     * 功能1：图书可借时通知预定者
     * 定时任务：每5分钟检查一次预定图书是否可借
     */
    @Override
    @Scheduled(fixedDelay = 300000) // 每5分钟检查一次
    @Transactional
    public void checkReservationAvailable() {
        log.info(" 执行预定检查任务：检查预定图书是否可借...");

        // 1. 获取所有待处理的预定记录
        List<Reservation> pendingReservations = reservationRepository
                .findByStatus(Reservation.ReservationStatus.PENDING);

        log.info(" 发现 {} 个待处理的预定", pendingReservations.size());

        // 2. 检查每个预定对应的图书是否已归还
        for (Reservation reservation : pendingReservations) {
            // 检查图书当前是否被借出
            BorrowRecord borrowRecord = borrowRecordRepository
                    .findByBookIdAndStatus(reservation.getBookId(), BorrowRecord.BorrowStatus.BORROWED)
                    .orElse(null);

            if (borrowRecord == null) {
                // 图书已归还，通知预定者
                log.info(" 图书 {} 已归还，通知预定者 {}", reservation.getBookId(), reservation.getUserId());
                notifyReservationAvailable(reservation);
            }
        }

        // 3. 检查预定是否过期（可选功能）
        checkExpiredReservations();
    }

    /**
     * 功能2：图书被预定时通知当前借阅者
     * 当其他模块创建预定后，调用此方法通知当前借阅者
     */
    @Override
    public void notifyCurrentLender(Long bookId) {
        log.info(" 执行通知任务：图书 {} 被预定，通知当前借阅者", bookId);

        // 1. 查找当前借阅者
        BorrowRecord borrowRecord = borrowRecordRepository
                .findByBookIdAndStatus(bookId, BorrowRecord.BorrowStatus.BORROWED)
                .orElse(null);

        if (borrowRecord != null) {
            // 2. 获取借阅者信息
            userRepository.findById(borrowRecord.getUserId()).ifPresent(user -> {
                log.info(" 找到当前借阅者：{} ({})", user.getRealName(), user.getEmail());

                // 3. 创建邮件通知记录
                createEmailNotification(user, bookId, "RESERVATION_NOTIFY_LENDER");

                // 4. 模拟发送邮件
                sendReservationNotificationEmail(user, bookId);
            });
        } else {
            log.info(" 图书 {} 当前未被借出，无需通知当前借阅者", bookId);
        }
    }

    /**
     * 辅助方法：通知预定者图书可借
     */
    private void notifyReservationAvailable(Reservation reservation) {
        userRepository.findById(reservation.getUserId()).ifPresent(user -> {
            // 1. 更新预定状态为可借
            reservation.setStatus(Reservation.ReservationStatus.READY);
            reservationRepository.save(reservation);

            log.info(" 更新预定 {} 状态为可借", reservation.getId());

            // 2. 创建邮件通知记录
            createEmailNotification(user, reservation.getBookId(), "RESERVATION_AVAILABLE");

            // 3. 模拟发送邮件
            sendAvailableNotificationEmail(user, reservation.getBookId());

            log.info(" 已完成通知预定者：{}，图书ID: {}", user.getRealName(), reservation.getBookId());
        });
    }

    /**
     * 辅助方法：创建邮件通知记录
     */
    private void createEmailNotification(User user, Long bookId, String notificationType) {
        EmailNotification notification = new EmailNotification();
        notification.setUserId(user.getId());
        notification.setEmail(user.getEmail());
        notification.setSubject(getNotificationSubject(notificationType, bookId));
        notification.setContent(getNotificationContent(notificationType, user, bookId));
        notification.setType(EmailNotification.NotificationType.valueOf(notificationType));

        emailNotificationRepository.save(notification);
        log.info(" 创建邮件通知记录：{} -> {}", notificationType, user.getEmail());
    }

    /**
     * 辅助方法：发送预定通知邮件（给当前借阅者）
     */
    private void sendReservationNotificationEmail(User user, Long bookId) {
        String to = user.getEmail();
        String subject = "【图书馆】图书预定通知";
        String content = String.format("""
            亲爱的 %s 同学/老师：
            
            系统通知：您当前借阅的图书（编号：%d）已被其他用户预定。
            
            预定者将在图书归还后获得优先借阅权。
            
            温馨提示：
            1. 请按时归还图书，避免产生逾期罚款
            2. 如需续借，请在到期前办理
            3. 有任何问题请联系图书馆管理员
            
            感谢您的配合！
            
            -----------------------------
            嘉应学院图书馆
            通知时间：%s
            """,
                user.getRealName(),
                bookId,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );

        emailService.sendEmail(to, subject, content);
        log.info(" 已发送预定通知邮件给当前借阅者：{}", user.getRealName());
    }

    /**
     * 辅助方法：发送可借通知邮件（给预定者）
     */
    private void sendAvailableNotificationEmail(User user, Long bookId) {
        String to = user.getEmail();
        String subject = "【图书馆】预定图书可借通知";
        String content = String.format("""
            亲爱的 %s 同学/老师：
            
            好消息！您预定的图书（编号：%d）现已可借！
            
            重要提醒：
            1. 请在24小时内前来借阅，否则预定将自动取消
            2. 借阅时请出示学生证/工作证
            3. 借阅期限：学生30天，教师60天
            
            祝您阅读愉快！
            
            -----------------------------
            嘉应学院图书馆
            可借时间：%s
            """,
                user.getRealName(),
                bookId,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );

        emailService.sendEmail(to, subject, content);
        log.info(" 已发送可借通知邮件给预定者：{}", user.getRealName());
    }

    /**
     * 辅助方法：获取通知主题
     */
    private String getNotificationSubject(String type, Long bookId) {
        switch (type) {
            case "RESERVATION_AVAILABLE":
                return "预定图书可借通知 - 图书ID: " + bookId;
            case "RESERVATION_NOTIFY_LENDER":
                return "图书被预定通知 - 图书ID: " + bookId;
            default:
                return "图书馆系统通知";
        }
    }

    /**
     * 辅助方法：获取通知内容
     */
    private String getNotificationContent(String type, User user, Long bookId) {
        String baseContent = String.format("""
            用户：%s
            邮箱：%s
            图书ID：%d
            通知时间：%s
            """,
                user.getRealName(),
                user.getEmail(),
                bookId,
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );

        if ("RESERVATION_AVAILABLE".equals(type)) {
            return "【预定图书可借通知】\n" + baseContent + "\n您的预定图书现已可借，请及时借阅。";
        } else {
            return "【图书被预定通知】\n" + baseContent + "\n您借阅的图书已被预定，请按时归还。";
        }
    }

    /**
     * 辅助方法：检查过期预定
     */
    private void checkExpiredReservations() {
        List<Reservation> expiredReservations = reservationRepository
                .findByExpiryTimeBeforeAndStatus(LocalDateTime.now(), Reservation.ReservationStatus.PENDING);

        for (Reservation reservation : expiredReservations) {
            reservation.setStatus(Reservation.ReservationStatus.CANCELLED);
            reservationRepository.save(reservation);
            log.info(" 预定已过期并取消：{}", reservation.getId());
        }
    }

    // ========== 邮件处理功能 ==========

    /**
     * 处理待发送邮件
     * 定时任务：每1分钟处理一次
     */
    @Override
    @Scheduled(fixedDelay = 60000) // 每1分钟处理一次
    @Transactional
    public void processPendingEmails() {
        List<EmailNotification> pendingEmails = emailNotificationRepository
                .findByStatus(EmailNotification.NotificationStatus.PENDING);

        if (!pendingEmails.isEmpty()) {
            log.info(" 处理待发送邮件，数量：{}", pendingEmails.size());

            for (EmailNotification email : pendingEmails) {
                try {
                    // 模拟发送邮件
                    emailService.sendEmail(email.getEmail(), email.getSubject(), email.getContent());

                    // 更新状态为已发送
                    email.setStatus(EmailNotification.NotificationStatus.SENT);
                    email.setSendTime(LocalDateTime.now());
                    emailNotificationRepository.save(email);

                    log.info(" 邮件发送成功：{} -> {}", email.getType(), email.getEmail());
                } catch (Exception e) {
                    // 模拟发送失败
                    email.setStatus(EmailNotification.NotificationStatus.FAILED);
                    email.setErrorMessage("模拟发送失败（作业模式）");
                    emailNotificationRepository.save(email);
                    log.error(" 邮件发送失败：{}", e.getMessage());
                }
            }
        }
    }

     //========== 逾期提醒功能 ==========

    @Override
    @Scheduled(cron = "0 0 9 * * ?") // 每天9点执行
    @Transactional
    public void checkOverdueBooks() {
        log.info("⏰ 检查逾期图书...");

        LocalDateTime now = LocalDateTime.now();
        List<BorrowRecord> overdueRecords = borrowRecordRepository
                .findByReturnTimeIsNullAndDueTimeBefore(now);

        for (BorrowRecord record : overdueRecords) {
            // 计算逾期天数
            long overdueDays = record.getDueTime().until(now, java.time.temporal.ChronoUnit.DAYS);
            record.setOverdueDays((int) overdueDays);
            record.setStatus(BorrowRecord.BorrowStatus.OVERDUE);
            borrowRecordRepository.save(record);

            // 检查是否需要发送提醒（第2、6、10周）
            if (overdueDays == 14 || overdueDays == 42 || overdueDays == 70) {
                sendOverdueReminder(record);
            }
        }
    }

    private void sendOverdueReminder(BorrowRecord record) {
        userRepository.findById(record.getUserId()).ifPresent(user -> {
            // 创建邮件通知记录
            EmailNotification notification = new EmailNotification();
            notification.setUserId(user.getId());
            notification.setEmail(user.getEmail());

            int overdueWeeks = record.getOverdueDays() / 7;
            notification.setSubject(String.format("【图书馆】图书逾期提醒（第%d周）", overdueWeeks));

            String content = String.format("""
                亲爱的 %s 同学/老师：

                提醒通知：
                您借阅的图书（编号：%d）已逾期 %d 天（约 %d 周）。

                重要提示：
                ⚠️  逾期超过1个月后，每本书每天罚款1元
                ⚠️  有未付清的罚款将无法再借阅其他图书
                ⚠️  请尽快归还图书以避免进一步罚款

                当前逾期天数：%d 天
                预计罚款金额：%.2f 元

                请及时归还图书或联系图书馆管理员处理。

                -----------------------------
                嘉应学院图书馆
                联系电话：0753-2186000
                提醒时间：%s
                """,
                    user.getRealName(),
                    record.getBookId(),
                    record.getOverdueDays(),
                    overdueWeeks,
                    record.getOverdueDays(),
                    calculateFine(record.getOverdueDays()),
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );

            notification.setContent(content);
            notification.setType(EmailNotification.NotificationType.OVERDUE_REMINDER);

            emailNotificationRepository.save(notification);

            // 模拟发送邮件
            emailService.sendEmail(user.getEmail(), notification.getSubject(), notification.getContent());

            log.info("📨 已发送逾期提醒给用户：{}，逾期 {} 天", user.getRealName(), record.getOverdueDays());
        });
    }

    // ========== 辅助方法 ==========

    private double calculateFine(int overdueDays) {
        if (overdueDays <= 30) {
            return 0.0; // 第一个月不罚款
        }
        return (overdueDays - 30) * 1.0; // 超过1个月，每天1元
    }
}