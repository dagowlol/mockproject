package com.example.demo.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.example.demo.dto.response.APIResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<APIResponse<Void>> handleAppException(AppException ex) {
        return ResponseEntity.status(ex.getStatus()).body(APIResponse.<Void>builder()
            .code(ex.getStatus().value())
            .message(ex.getMessage())
            .result(null)
            .build());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<APIResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(APIResponse.<Void>builder()
            .code(400)
            .message(ex.getMessage())
            .result(null)
            .build());
    }
}
