// src/main/java/com/library/dto/NotificationDTO.java
package com.library.dto;

import lombok.Data;

@Data
public class NotificationDTO {
    private Long userId;  // 可选，如果为空则发送给所有用户
    private String title;
    private String content;
    private boolean important; // 是否为重要通知
}