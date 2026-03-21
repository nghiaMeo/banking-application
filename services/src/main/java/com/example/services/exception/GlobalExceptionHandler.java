package com.example.services.exception;

import com.example.services.dto.response.APIResponse;
import com.example.services.exception.enums.ErrorCode;
import jakarta.validation.ConstraintViolation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.util.Objects;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private static final String MIN_ATTRIBUTE = "min";

    /**
     * Xử lý AppException
     */
    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<APIResponse> handleAppException(AppException e) {
        ErrorCode errorCode = e.getErrorCode();
        APIResponse apiResponse = new APIResponse();

        if (!Objects.isNull(errorCode)) {
            apiResponse.setCode(errorCode.getCode());
            apiResponse.setMessage(errorCode.getMessage());
        }

        return ResponseEntity.status(errorCode != null ? errorCode.getHttpStatusCode() : HttpStatus.BAD_REQUEST).body(apiResponse);
    }

    /**
     * Xử lý AccessDeniedException
     */
    @ExceptionHandler(value = AccessDeniedException.class)
    public ResponseEntity<APIResponse> handleAccessDeniedException(AccessDeniedException e) {
        ErrorCode errorCode = ErrorCode.UNAUTHORIZED;
        APIResponse apiResponse = new APIResponse();

        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(errorCode.getMessage());

        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(apiResponse);
    }

    /**
     * Xử lý Validation Error
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {

        String enumKey = Objects.requireNonNull(e.getFieldError()).getDefaultMessage();
        ErrorCode errorCode = ErrorCode.INVALID_KEY;
        Map<String, Object> attributes = null;

        try {
            errorCode = ErrorCode.valueOf(enumKey);
            var constraintViolations = e.getBindingResult().getAllErrors().getFirst().unwrap(ConstraintViolation.class);

            attributes = constraintViolations.getConstraintDescriptor().getAttributes();

        } catch (IllegalArgumentException ex) {
            log.warn("Invalid enum key: {}", enumKey);
        }

        APIResponse apiResponse = new APIResponse();
        apiResponse.setCode(errorCode.getCode());
        apiResponse.setMessage(Objects.nonNull(attributes) ?
                mapAttributes(errorCode.getMessage(), attributes) : errorCode.getMessage());

        return ResponseEntity.badRequest().body(apiResponse);
    }

    /**
     * Xử lý Exception chung (CUỐI CÙNG)
     */
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<APIResponse> handleGeneral(Exception e) {
        APIResponse apiResponse = new APIResponse();
        apiResponse.setCode(500);
        apiResponse.setMessage("Internal Server Error");
        if (e.getMessage() != null && e.getMessage().contains("No static resource")) {
            APIResponse apiResponses = new APIResponse();
            apiResponse.setCode(404);
            apiResponse.setMessage("Resource not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(apiResponses);
        }
        log.error("Unexpected error: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
    }

    private String mapAttributes(String message, Map<String, Object> attributes) {
        String minValue = attributes.get(MIN_ATTRIBUTE).toString();
        return message.replace("{" + MIN_ATTRIBUTE + "}", minValue);
    }
}