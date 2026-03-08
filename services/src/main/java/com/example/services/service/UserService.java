package com.example.services.service;


import com.example.services.dto.request.CreateUserRequest;
import com.example.services.dto.request.UpdateUserRequest;
import com.example.services.dto.response.UserResponse;
import com.example.services.entity.User;
import com.example.services.entity.Wallet;
import com.example.services.exception.AppException;
import com.example.services.exception.enums.ErrorStatus;
import com.example.services.mapper.UserMapper;
import com.example.services.repository.UserRepository;
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
    private WalletService walletService;
    private final UserMapper userMapper;

    public UserResponse create(CreateUserRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(
                () -> new AppException(ErrorStatus.EMAIL_ALREADY_EXISTS.getCode(), ErrorStatus.EMAIL_ALREADY_EXISTS.getMessage())
        );

        var phoneRequest = request.getPhone();
        if (userRepository.existsByPhone(phoneRequest)){
            throw new AppException(ErrorStatus.PHONE_NUMBER_ALREADY_EXISTS.getCode(), ErrorStatus.PHONE_NUMBER_ALREADY_EXISTS.getMessage());
        }

        Wallet wallet = walletService.createWalletForUser(user);
        User save = userMapper.toEntity(request);
        User saved = userRepository.save(save);
        log.info("User created with id: {}", saved.getId());
        return userMapper.toResponse(saved);
    }

    public List<UserResponse> allUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(userMapper::toResponse).collect(Collectors.toList());

    }

    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new
                                AppException(ErrorStatus.USER_NOT_FOUND.getCode(), ErrorStatus.USER_NOT_FOUND.getMessage()
                        )
                );
        // Email
        var emailRequest = request.getEmail();
        if (hasValue(emailRequest) && !user.getEmail().equals(emailRequest)) {
            if (userRepository.existsByEmail(emailRequest)) {
                throw new AppException(ErrorStatus.EMAIL_ALREADY_EXISTS.getCode(), ErrorStatus.EMAIL_ALREADY_EXISTS.getMessage());
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
