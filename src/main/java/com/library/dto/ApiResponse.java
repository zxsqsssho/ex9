//src/main/java/com/library/dto/ApiResponse.java
package com.library.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private String path;

    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage("Success");
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(200);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    public static ApiResponse<?> error(int code, String message) {
        ApiResponse<?> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
}