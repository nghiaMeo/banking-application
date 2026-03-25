package com.example.services.controller;

import com.example.services.dto.request.otp.RequestOtpRequest;
import com.example.services.dto.request.otp.ResetPasswordRequest;
import com.example.services.dto.request.otp.VerifyOtpRequest;
import com.example.services.dto.response.APIResponse;
import com.example.services.exception.AppException;
import com.example.services.service.AuthService;
import com.example.services.service.OtpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth/otp")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication with OTP")
public class AuthOtpController {

    private final OtpService otpService;
    private final AuthService authService;


    /*
     * POST /api/auth/request-otp
     * Body: { "email": "user@example.com" }
     * Response: { "message": "OTP sent to email" }
     */
    @PostMapping("/request-otp")
    @Operation(summary = "Request OTP for email verification")
    public APIResponse<Object> requestOtp(@Valid @RequestBody RequestOtpRequest request) {

        log.info("OTP request from email: {}", request.getEmail());

        try {
            otpService.checkRateLimit(request.getEmail());

            var otp = otpService.generateOtp(request.getEmail());

            return APIResponse.<Object>builder()
                    .message("OTP sent to your email. Valid for 2 minutes.")
                    .build();
        } catch (AppException e) {
            return APIResponse.<Object>builder()
                    .message(e.getMessage())
                    .build();
        }
    }

    /**Verify OTP
     * POST /api/auth/verify-otp
     * Body: { "email": "user@example.com", "otp": "123456" }
     * Response: { "message": "OTP verified successfully" }
     */
    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP")
    public APIResponse<Object> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        try {
            otpService.verifyOtp(request.getEmail(), request.getOtp());

            return APIResponse.<Object>builder()
                    .message("OTP verified successfully")
                    .build();
        } catch (AppException e) {
            return APIResponse.<Object>builder()
                    .message(e.getMessage())
                    .build();
        }
    }

    /*Reset Password with OTP
     * POST /api/auth/reset-password
     * Body: {
     *   "email": "user@example.com",
     *   "otp": "123456",
     *   "newPassword": "newPassword123"
     * }
     * Response: { "message": "Password reset successfully" }
     */
    @PostMapping("/request-password")
    @Operation(summary = "Reset password using OTP")
    public APIResponse<Object> requestPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            otpService.verifyOtp(request.getEmail(), request.getOtp());
            authService.resetPassword(request.getEmail(), request.getNewPassword());
            return APIResponse.<Object>builder()
                    .message("Password reset successfully")
                    .build();
        } catch (AppException e) {
            return APIResponse.<Object>builder()
                    .message(e.getMessage())
                    .build();
        }
    }

    // Check OTP status (for testing)
    @GetMapping("/otp-status/{email}")
    @Operation(summary = "Check OTP status (for testing only)")
    public APIResponse<Object> checkOtpStatus(@PathVariable String email) {
        var exists = otpService.otpExists(email);
        var ttl = otpService.getOtpTtl(email);
        var attempts = otpService.getRemainingTempts(email);

        var status = Map.of(
                "otpExists", exists,
                "ttlSeconds", ttl,
                "remainingAttempts", attempts
        );

        return APIResponse.builder()
                .message(status.toString())
                .build();
    }
}
