package com.example.services.service;

import com.example.services.dto.request.CreateUserRequest;
import com.example.services.dto.request.LoginRequest;
import com.example.services.dto.response.AuthResponse;
import com.example.services.exception.AppException;
import com.example.services.exception.enums.ErrorCode;
import com.example.services.mapper.UserMapper;
import com.example.services.repository.UserRepository;
import com.example.services.util.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final WalletService walletService;
    private final UserService userService;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    @Transactional(rollbackOn = Exception.class)
    public AuthResponse register(CreateUserRequest userRequest) {
        var userResponse = userService.create(userRequest);

        var userResponseId = userResponse.getId();
        var walletUser = walletService.getWalletByUserIdResponse(userResponseId);



        return AuthResponse.builder()
                .userResponse(userResponse)
                .walletResponse(walletUser)
                .build();
    }

    public AuthResponse login(LoginRequest loginRequest) {
        var email = loginRequest.getEmail();
        var password = loginRequest.getPassword();

        var user = userRepository.findByEmail(email).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.info("Invalid password for user: {} ", email);
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        var accessToken = jwtUtil.generateToken(user.getId(), user.getEmail());
        var refreshToken = jwtUtil.generateRefreshToken(user.getId(), user.getEmail());
        var userLogin = userMapper.toResponse(user);
        var walletUser = walletService.getWalletByUserIdResponse(user.getId());

        log.info("User logged in: {}", user.getEmail());


        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600000L)
                .walletResponse(walletUser)
                .userResponse(userLogin)
                .build();

    }

    public AuthResponse refreshAccessToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        var tokenType = jwtUtil.getTokenType(refreshToken);
        if (!"REFRESH".equals(tokenType)) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        var userId = jwtUtil.extractUserId(refreshToken);
        var email = jwtUtil.extractEmail(refreshToken);

        var user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        var newAccessToken = jwtUtil.generateToken(userId, email);


        log.info("Access token refreshed for user: {}", email);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600000L)
                .userResponse(userMapper.toResponse(user))
                .build();
    }
}
