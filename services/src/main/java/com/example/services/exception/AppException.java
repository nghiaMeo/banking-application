package com.example.services.exception;


import com.example.services.exception.enums.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AppException extends RuntimeException {

    private ErrorCode errorCode;


}
