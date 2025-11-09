package com.rdm.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String message;
    private String error;
    private LocalDateTime timestamp;
    private String path;
    private List<String> details;

    public ErrorResponse(int status, String message, String error, String path) {
        this.status = status;
        this.message = message;
        this.error = error;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String message, String error, String path, List<String> details) {
        this.status = status;
        this.message = message;
        this.error = error;
        this.path = path;
        this.timestamp = LocalDateTime.now();
        this.details = details;
    }
}
