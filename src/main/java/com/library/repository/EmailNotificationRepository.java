//src/main/java/com/library/repository/EmailNotificationRepository.java
package com.library.repository;

import com.library.entity.EmailNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmailNotificationRepository extends JpaRepository<EmailNotification, Long> {
    List<EmailNotification> findByStatus(EmailNotification.NotificationStatus status);
}
