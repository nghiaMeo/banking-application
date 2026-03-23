package com.example.services.exception.enums;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(404, "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(409, "User already exists", HttpStatus.CONFLICT),
    USER_NOT_LOGGED_IN(401, "User not logged in", HttpStatus.UNAUTHORIZED),
    USER_LOGIN_FAILED(401, "User login failed", HttpStatus.UNAUTHORIZED),

    // OTP Errors
    OTP_EXPIRED(401, "OTP expired", HttpStatus.UNAUTHORIZED),
    OTP_INVALID(402, "OTP invalid", HttpStatus.UNAUTHORIZED),
    OTP_MANY_REQUEST(403, "Too many OTP requests. Try again after 1 minute", HttpStatus.UNAUTHORIZED),

    // Email errors
    EMAIL_ALREADY_EXISTS(409, "Email already exists", HttpStatus.CONFLICT),
    EMAIL_NOT_EXISTS(404, "Email not exists", HttpStatus.NOT_FOUND),
    INVALID_PASSWORD(400, "Password must be at least 8 characters", HttpStatus.BAD_REQUEST),

    // Phone errors
    PHONE_NUMBER_ALREADY_EXISTS(409, "Phone number already exists", HttpStatus.CONFLICT),

    // Wallet errors
    WALLET_NOT_FOUND(404, "Wallet not found", HttpStatus.NOT_FOUND),
    INSUFFICIENT_BALANCE(400, "Insufficient balance", HttpStatus.BAD_REQUEST),

    // Amount errors
    INVALID_AMOUNT(400, "Invalid amount", HttpStatus.BAD_REQUEST),
    AMOUNT_IS_ZERO(400, "Amount is zero", HttpStatus.BAD_REQUEST),

    // Authentication/Authorization errors
    UNAUTHORIZED(401, "Unauthorized", HttpStatus.UNAUTHORIZED),
    UNAUTHENTICATED(401, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    AUTHENTICATION_REQUIRED(401, "Authentication required", HttpStatus.UNAUTHORIZED),
    AUTHENTICATION_EXPIRED(401, "Authentication expired", HttpStatus.UNAUTHORIZED),
    AUTHENTICATION_FAILED_EXPIRED(401, "Authentication failed - expired", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(401, "Token expired", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID(401, "Token invalid", HttpStatus.UNAUTHORIZED),

    // Access control errors
    FORBIDDEN(403, "Forbidden - Access denied", HttpStatus.FORBIDDEN),
    ACCESS_DENIED(403, "Access denied", HttpStatus.FORBIDDEN),

    // Request errors
    BAD_REQUEST(400, "Bad request", HttpStatus.BAD_REQUEST),
    INVALID_KEY(400, "Invalid key", HttpStatus.BAD_REQUEST),
    INVALID_USERNAME(400, "Username must be at least 3 characters", HttpStatus.BAD_REQUEST),
    INVALID_BIRTHDAY(400, "Age must be at least 18", HttpStatus.BAD_REQUEST),

    // Server errors
    UNCAUGHT_EXCEPTION(500, "Uncaught exception", HttpStatus.INTERNAL_SERVER_ERROR),

    ;

    private int code;
    private String message;
    private HttpStatusCode httpStatusCode;
}