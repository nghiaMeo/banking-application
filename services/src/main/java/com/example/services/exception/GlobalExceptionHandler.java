package com.example.services.exception;

import com.example.services.dto.response.ApiResponse;
import com.example.services.exception.enums.ErrorStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return ResponseEntity.badRequest().body(ApiResponse.error(errors.toString()));

    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneral(Exception ex) {
        return ResponseEntity.badRequest().body(ApiResponse.error("Internal Server Error"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneral(RuntimeException ex) {
        return ResponseEntity.status(ErrorStatus.BAD_REQUEST.getCode())
                .body(ApiResponse
                        .builder()
                        .success(false)
                        .message(ex.getMessage())
                        .statusCode(ErrorStatus.BAD_REQUEST.getCode())
                        .data(ErrorStatus.BAD_REQUEST.getMessage())
                        .build());
    }
}
