package com.example.services.service;

import com.example.services.exception.AppException;
import com.example.services.exception.enums.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final int OTP_LENGTH = 6;
    private static final int OTP_TTL_MINUTES = 2;
    private static final String OTP_KEY_PREFIX = "OTP:";

    /*
     * Generate 6-digit OTP and save to Redis
     * Key: "OTP:email@example.com"
     * Value: "123456"
     * TTL: 2 minutes (120 seconds)
     */
    public String generateOtp(String email) {
        log.info("Generating OTP for email: " + email);

        var otp = String.format(
                "%06d",
                new Random().nextInt(900000) + 100000
        );

        var key = OTP_KEY_PREFIX + email;

        redisTemplate.opsForValue().set(key, otp,
                Duration.ofMinutes(OTP_TTL_MINUTES)
        );
        log.info("✅ OTP generated and saved to Redis - key: {}, TTL: {}min",
                key, OTP_TTL_MINUTES);

        return otp;
    }

    /* Verify OTP from Redis
     * Steps:
     * 1. Get OTP from Redis
     * 2. Check if expired (null = expired)
     * 3. Compare with provided OTP
     * 4. Delete OTP (use once)
     */
    public boolean verifyOtp(String email, String providedOtp) {
        log.info("Verifying OTP for email: " + email);
        var key = OTP_KEY_PREFIX + email;

        var storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp == null) {
            log.warn("⏰ OTP expired for email: {}", email);
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }

        if (!storedOtp.toString().equals(providedOtp)) {
            log.warn("❌ Invalid OTP for email: {}", email);
            throw new AppException(ErrorCode.OTP_INVALID);
        }

        redisTemplate.delete(key);
        log.info("✅ OTP verified and deleted for email: {}", email);
        return true;
    }

    /*Check rate limit before generating OTP
     * Key: "RATE_LIMIT:email@example.com"
     * Max: 5 OTP requests per minute
     */
    public void checkRateLimit(String email) {
        log.info("Checking Rate Limit for email: " + email);

        var key = "RATE_LIMIT:" + email;
        var count = (Integer) redisTemplate.opsForValue().get(key);

        if (count != null && count >= 5) {
            log.warn("Rate limit exceeded for email: {} (count: {})", email, count);
            throw new AppException(ErrorCode.OTP_MANY_REQUEST);
        }

        var newCount = redisTemplate.opsForValue().increment(key);
        log.debug(" Rate limit counter - email: {}, count: {}", email, newCount);
        if (newCount == 1) {
            redisTemplate.expire(key, Duration.ofMinutes(1));
            log.debug("Rate limit TTL set to 1 minute");
        }
    }

    /**
     * Helper: Get remaining OTP TTL
     */
    public long getOtpTtl(String email) {
        var key = OTP_KEY_PREFIX + email;
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);

    }

    /**
     * Helper: Check if OTP exists
     */
    public boolean otpExists(String email) {
        var key = OTP_KEY_PREFIX + email;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Helper: Get remaining rate limit attempts
     */
    public int getRemainingTempts(String email) {
        var key = "RATE LIMIT:" + email;
        var count = (Integer) redisTemplate.opsForValue().get(key);
        return Math.max(0, 5 - (count != null ? count : 0));
    }


}
