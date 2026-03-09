package com.example.services.controller;

import com.example.services.dto.request.CreateUserRequest;
import com.example.services.dto.request.UpdateUserRequest;
import com.example.services.dto.response.ApiResponse;
import com.example.services.dto.response.UserResponse;
import com.example.services.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    public ApiResponse<UserResponse> createUser(@RequestBody @Valid CreateUserRequest request) {
        return ApiResponse.<UserResponse>builder()
                .data(userService.create(request))
                .build();
    }

    @GetMapping("/all")
    public ApiResponse<List<UserResponse>> getAllUsers() {
        return ApiResponse.<List<UserResponse>>builder()
                .data(userService.allUsers())
                .build();
    }

    @PutMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable String id,
            @RequestBody @Valid UpdateUserRequest request) {
        UserResponse response = userService.updateUser(UUID.fromString(id), request);
        return userService.updateUser(UUID.fromString(id),request);
    }

}
