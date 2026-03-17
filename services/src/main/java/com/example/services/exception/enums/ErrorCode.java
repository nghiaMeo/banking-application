package com.example.services.exception.enums;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ErrorCode {
    PHONE_NUMBER_ALREADY_EXISTS(409, "Phone number already existts", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_NOT_FOUND(404, "User Not Found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(409, "User Already Exists", HttpStatus.BAD_REQUEST),
    USER_NOT_LOGGED_IN(401, "User Not Logged In", HttpStatus.UNAUTHORIZED),
    USER_LOGIN_FAILED(402, "User Login Failed", HttpStatus.UNAUTHORIZED),
    WALLET_NOT_FOUND(500, "Wallet not found", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCESS_DENIED(403, "Access Denied", HttpStatus.FORBIDDEN),
    ACCESS_DENIED_ERROR(403, "Access Denied", HttpStatus.FORBIDDEN),
    EMAIL_ALREADY_EXISTS(409, "Email Already Exists", HttpStatus.CONFLICT),
    EMAIL_NOT_EXISTS(409, "Email Not Exists", HttpStatus.CONFLICT),
    INVALID_PASSWORD(409, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_BIRTHDAY(409, "Age must be at least {min}", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(401, "Unauthorized", HttpStatus.FORBIDDEN),
    UNCAUGHT_EXCEPTION(500, "Uncaught Exception", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_USERNAME(1003, "Username must at least {min} characters", HttpStatus.BAD_REQUEST),
    UNAUTHENTICATED(1004, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    AUTHENTICATION_REQUIRED(1005, "Authentication Required", HttpStatus.UNAUTHORIZED),
    AUTHENTICATION_EXPIRED(1006, "Authentication Expired", HttpStatus.UNAUTHORIZED),
    AUTHENTICATION_FAILED_EXPIRED(1007, "Authentication Failed Expired", HttpStatus.UNAUTHORIZED),
    AUTHENTICATION_EXPIRED_EXPIRED(1008, "Authentication Expired Expired", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(1009, "Token Expired", HttpStatus.UNAUTHORIZED),
    TOKEN_NOT_EXPIRED(1010, "Token Not Expired", HttpStatus.UNAUTHORIZED),
    TOKEN_INVALID(1011, "Token Invalid", HttpStatus.UNAUTHORIZED),
    INVALID_KEY(1012, "Invalid Key", HttpStatus.BAD_REQUEST),
    INVALID_AMOUNT(1013, "Invalid Amount", HttpStatus.BAD_REQUEST),
    AMOUNT_IS_ZERO(1014, "Amount is zero", HttpStatus.BAD_REQUEST),
    FORBIDDEN(1016, "Forbidden - Access denied", HttpStatus.FORBIDDEN),  // ← Add this
    INSUFFICIENT_BALANCE(1015, "Insufficient Balance", HttpStatus.BAD_REQUEST),
    BAD_REQUEST(9000, "User Not Found", HttpStatus.BAD_REQUEST),

    ;

    private int code;
    private String message;
    private HttpStatusCode httpStatusCode;
}