package com.edu.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        
        // Determine status code based on exception message
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        
        String message = ex.getMessage();
        if (message != null) {
            if (message.contains("Invalid OTP") || 
                message.contains("Invalid or expired") ||
                message.contains("not found") ||
                message.contains("not setup")) {
                status = HttpStatus.UNAUTHORIZED;
            } else if (message.contains("suspended") ||
                       message.contains("pending approval")) {
                status = HttpStatus.FORBIDDEN;
            } else if (message.contains("Not an admin") ||
                      message.contains("permission") ||
                      message.contains("Access denied")) {
                status = HttpStatus.FORBIDDEN;
            } else if (message.contains("already") ||
                      message.contains("Invalid")) {
                status = HttpStatus.BAD_REQUEST;
            }
        }
        
        return new ResponseEntity<>(body, status);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {
        
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }
}
