package com.example.services.exception;


import com.example.services.exception.enums.ErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AppException extends RuntimeException {

    private final int errorCode;
    private final String message;

}
