package com.example.services.exception.enums;

import lombok.*;

@NoArgsConstructor(force = true)
@AllArgsConstructor
@Getter
public enum ErrorStatus {
    // Validation errors
    VALIDATION_ERROR(400, "Validation failed"),

    // User errors
    USER_NOT_FOUND(404, "User not found"),
    USER_ALREADY_EXISTS(409, "User already exists"),
    EMAIL_ALREADY_EXISTS(400, "Email already exists"),
    PHONE_NUMBER_ALREADY_EXISTS(400, "Email already exists"),

    INVALID_EMAIL(400, "Invalid email format"),
    INVALID_PASSWORD(400, "Invalid password"),

    // Authentication errors
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),

    // Server errors
    INTERNAL_SERVER_ERROR(500, "Internal server error"),
    DATABASE_ERROR(500, "Database error"),

    // Other errors
    BAD_REQUEST(400, "Bad request"),
    NOT_FOUND(404, "Not found"),
    CONFLICT(409, "Conflict"),
    WALLET_NOT_FOUND(404, "Wallet not found"),
    AMOUNT_INVALID(8001, "Amount in valid"),
    AMOUNT_IS_ZERO(8002, "Amount is zero"),
    ;

    private final int code;
    private final String message;

}
