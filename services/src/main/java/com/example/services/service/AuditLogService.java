package com.example.services.service;

import com.example.services.entity.AuditLog;
import com.example.services.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    //log audit event
    @Transactional(rollbackFor = Exception.class)
    public void log(String action, String userEmail, String details, String status) {
        log(action, userEmail, details, status, null);
    }

    // log audit event with ip address
    @Transactional(rollbackFor = Exception.class)
    public void log(String action, String userEmail, String details, String status, HttpServletRequest request) {

        var ipAddress = getClientIp(request);

        var auditLog = AuditLog.builder()
                .action(action)
                .userEmail(userEmail)
                .additionalDetails(details)
                .status(status)
                .ipAddress(ipAddress)
                .build();
        auditLogRepository.save(auditLog);
        log.info("Audit log saved - action: {}, user: {}, status: {}",
                action, userEmail, status);

    }

    public long countRecentFailedAttempts(String email,int minutesBack){
        var since = LocalDateTime.now().minusMinutes(minutesBack);
        return auditLogRepository.countFailedAttempts(email,since);
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }

        var ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

}
