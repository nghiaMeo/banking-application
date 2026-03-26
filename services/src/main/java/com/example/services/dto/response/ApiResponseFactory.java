package com.example.services.dto.response;

/**
 * Helper to keep API success responses consistent.
 */
public final class ApiResponseFactory {
    private ApiResponseFactory() {
    }

    public static <T> APIResponse<T> ok(T data) {
        return APIResponse.<T>builder()
                .code(200)
                .data(data)
                .build();
    }

    public static <T> APIResponse<T> ok(String message, T data) {
        return APIResponse.<T>builder()
                .code(200)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> APIResponse<T> ok(String message) {
        return APIResponse.<T>builder()
                .code(200)
                .message(message)
                .build();
    }
}

