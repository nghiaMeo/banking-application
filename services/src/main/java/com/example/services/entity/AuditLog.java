package com.example.services.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/*Audit Log Entity
 * Stores all sensitive operations:
 * - Login
 * - Password reset
 * - Transfer
 * - OTP verification
 * - Failed login attempts
 */
@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_user_email", columnList = "user_email"),
        @Index(name = "idx_action", columnList = "action"),
        @Index(name = "idx_created_at", columnList = "created_at")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    @Schema(description = "Action type", example = "LOGIN")
    private String action;

    @Column(nullable = false)
    @Schema(description = "User email", example = "user@example.com")
    private String userEmail;

    @Column(length = 255)
    @Schema(description = "Additional details")
    private String additionalDetails;

    @Column(length = 50)
    @Schema(description = "Status", example = "SUCCESS")
    private String status;

    @Column(length = 45)
    @Schema(description = "IP address", example = "192.168.1.1")
    private String ipAddress;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    @Schema(description = "Created timestamp")
    private LocalDateTime createdAt;

    public static class Action {
        public static final String LOGIN = "LOGIN";
        public static final String LOGIN_FAILED = "LOGIN_FAILED";
        public static final String LOGOUT = "LOGOUT";
        public static final String PASSWORD_RESET = "PASSWORD_RESET";
        public static final String OTP_REQUESTED = "OTP_REQUESTED";
        public static final String OTP_VERIFIED = "OTP_VERIFIED";
        public static final String TRANSFER = "TRANSFER";
        public static final String DEPOSIT = "DEPOSIT";
        public static final String WITHDRAW = "WITHDRAW";
        public static final String UNAUTHORIZED_ACCESS = "UNAUTHORIZED_ACCESS";
    }

    public static class Status {
        public static final String SUCCESS = "SUCCESS";
        public static final String FAILED = "FAILED";
        public static final String BLOCKED = "BLOCKED";
    }


}
