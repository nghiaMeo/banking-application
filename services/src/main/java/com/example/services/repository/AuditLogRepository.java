package com.example.services.repository;

import com.example.services.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    // Find by user email
    Page<AuditLog> findByUserEmail(String userEmail, Pageable pageable);

    // Find by action
    Page<AuditLog> findByAction(String action, Pageable pageable);

    // Find failed login attempts
    @Query("SELECT a FROM AuditLog a WHERE a.action = 'LOGIN_FAILED' " +
            "AND a.userEmail = :email AND a.createdAt >= :since")
    List<AuditLog> findFailedLoginAttempts(
            @Param("email") String email,
            @Param("since") LocalDateTime since
    );

    // Find sensitive operations
    @Query("SELECT a FROM AuditLog a WHERE a.userEmail = :email " +
            "AND a.action IN ('TRANSFER', 'PASSWORD_RESET', 'OTP_VERIFIED') " +
            "ORDER BY a.createdAt DESC")
    Page<AuditLog> findSensitiveOperations(
            @Param("email") String email,
            Pageable pageable
    );

    // Count failed attempts
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.userEmail = :email " +
            "AND a.action = 'LOGIN_FAILED' AND a.createdAt >= :since")
    long countFailedAttempts(
            @Param("email") String email,
            @Param("since") LocalDateTime since
    );
}