// src/main/java/com/library/controller/NotificationController.java
package com.library.controller;

import com.library.dto.ApiResponse;
import com.library.dto.NotificationDTO;
import com.library.service.EmailNotificationService;
import com.library.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final EmailNotificationService emailNotificationService;
    private final NotificationService notificationService;

    @PostMapping("/reservation-available/{reservationId}")
    public ApiResponse<?> notifyReservationAvailable(@PathVariable Long reservationId) {
        try {
            emailNotificationService.notifyReservationAvailable(reservationId);
            return ApiResponse.success("通知发送成功", null);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "通知发送失败: " + e.getMessage());
        }
    }

    @PostMapping("/reservation-reminder")
    public ApiResponse<?> notifyReservationReminder(
            @RequestParam Long bookId,
            @RequestParam Long reservationUserId) {
        try {
            emailNotificationService.notifyCurrentBorrowerForReservation(bookId, reservationUserId);
            return ApiResponse.success("提醒发送成功", null);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "提醒发送失败: " + e.getMessage());
        }
    }

    @PostMapping("/overdue-reminder/{borrowRecordId}")
    public ApiResponse<?> sendOverdueReminder(@PathVariable Long borrowRecordId) {
        try {
            emailNotificationService.sendOverdueReminder(borrowRecordId);
            return ApiResponse.success("逾期提醒发送成功", null);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "逾期提醒发送失败: " + e.getMessage());
        }
    }

    @PostMapping("/trigger-scheduled-tasks")
    public ApiResponse<?> triggerScheduledTasks() {
        try {
            emailNotificationService.scheduledNotificationTasks();
            return ApiResponse.success("定时任务触发成功", null);
        } catch (Exception e) {
            return ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "定时任务执行失败: " + e.getMessage());
        }
    }

    @GetMapping("/user")
    public ApiResponse<?> getUserNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return notificationService.getUserNotifications(pageable);
    }

    @GetMapping("/unread-count")
    public ApiResponse<?> getUnreadCount() {
        return notificationService.getUnreadCount();
    }

    @PutMapping("/mark-read/{id}")
    public ApiResponse<?> markAsRead(@PathVariable Long id) {
        return notificationService.markAsRead(id);
    }

    @PutMapping("/mark-all-read")
    public ApiResponse<?> markAllAsRead() {
        return notificationService.markAllAsRead();
    }

    @GetMapping("/admin/all")
    public ApiResponse<?> getAllNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return notificationService.getAllNotifications(pageable);
    }

    @PostMapping("/admin/system")
    public ApiResponse<?> sendSystemNotification(@RequestBody NotificationDTO notificationDTO) {
        return notificationService.sendSystemNotification(notificationDTO);
    }

    @PostMapping("/admin/important")
    public ApiResponse<?> sendImportantNotification(@RequestBody NotificationDTO notificationDTO) {
        return notificationService.sendImportantNotification(notificationDTO);
    }
}