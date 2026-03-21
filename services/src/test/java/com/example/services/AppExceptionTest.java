package com.example.services;

import com.example.services.exception.AppException;
import com.example.services.exception.enums.ErrorCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppExceptionTest {

    @Test
    void testAppException_WithErrorCodeOnly() {
        AppException exception = new AppException(ErrorCode.WALLET_NOT_FOUND);

        assertEquals(ErrorCode.WALLET_NOT_FOUND, exception.getErrorCode());
        assertEquals("Wallet not found", exception.getMessage());
    }

    @Test
    void testAppException_WithCustomMessage() {
        AppException exception = new AppException(
                ErrorCode.INSUFFICIENT_BALANCE
        );

        assertEquals(ErrorCode.INSUFFICIENT_BALANCE, exception.getErrorCode());
        assertEquals("Insufficient Balance", exception.getMessage());
    }

    @Test
    void testAppException_CanBeThrown() {
        AppException exception = assertThrows(AppException.class, () -> {
            throw new AppException(ErrorCode.BAD_REQUEST);
        });

        assertNotNull(exception);
        assertEquals("User Not Found", exception.getMessage());
    }
}

