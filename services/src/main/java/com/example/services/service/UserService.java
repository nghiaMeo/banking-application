package com.example.services.service;


import com.example.services.dto.request.CreateUserRequest;
import com.example.services.dto.request.UpdateUserRequest;
import com.example.services.dto.response.UserResponse;
import com.example.services.entity.User;
import com.example.services.entity.Wallet;
import com.example.services.exception.AppException;
import com.example.services.exception.enums.ErrorCode;
import com.example.services.mapper.UserMapper;
import com.example.services.repository.UserRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final WalletService walletService;
    private final UserMapper userMapper;

    public UserResponse create(CreateUserRequest request) {
        var emailRequest = request.getEmail();
        if (userRepository.existsByEmail(emailRequest)) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        var phoneRequest = request.getPhone();
        if (userRepository.existsByPhone(phoneRequest)) {
            throw new AppException(ErrorCode.PHONE_NUMBER_ALREADY_EXISTS);
        }
        User user = userMapper.toEntity(request);
        User savedUser = userRepository.save(user);

        Wallet wallet = walletService.createWalletForUser(user);
        savedUser.setWallet(wallet);
        userRepository.save(savedUser);
        log.info("User created with id: {}", savedUser.getId());
        return userMapper.toResponse(savedUser);
    }

    public List<UserResponse> allUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toResponse).collect(Collectors.toList());

    }

    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new
                                AppException(ErrorCode.USER_NOT_FOUND)
                );
        // Email
        var emailRequest = request.getEmail();
        if (hasValue(emailRequest) && !user.getEmail().equals(emailRequest)) {
            if (userRepository.existsByEmail(emailRequest)) {
                throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
            }
            user.setEmail(request.getEmail());
        }

        // FullName
        var fullNameRequest = request.getFullName();
        if (hasValue(fullNameRequest) && !user.getFullName().equals(fullNameRequest)) {
            user.setFullName(fullNameRequest);
        }

        // Phone
        var phoneRequest = request.getPhone();
        if (hasValue(phoneRequest) && !user.getPhone().equals(phoneRequest)) {
            user.setPhone(phoneRequest);
        }

        // Password
        var passwordRequest = request.getPassword();
        if (hasValue(passwordRequest)) {
            user.setPassword(passwordRequest);
        }

        User updated = userRepository.save(user);

        return userMapper.toResponse(updated);
    }

    private boolean hasValue(String value) {
        return value != null && !value.trim().isEmpty();
    }

}
