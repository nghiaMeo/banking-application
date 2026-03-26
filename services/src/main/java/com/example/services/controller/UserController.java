package com.example.services.controller;

import com.example.services.dto.request.CreateUserRequest;
import com.example.services.dto.request.UpdateUserRequest;
import com.example.services.exception.AppException;
import com.example.services.exception.enums.ErrorCode;
import com.example.services.dto.response.APIResponse;
import com.example.services.dto.response.UserResponse;
import com.example.services.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {
    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile")
    public APIResponse<UserResponse> getProfile(Authentication authentication) {
        var userId = authentication.getName();
        UserResponse userResponse = userService.getUserById(UUID.fromString(userId));
        return  APIResponse.<UserResponse>builder()
                .data(userResponse)
                .build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    public APIResponse<UserResponse> getUserById(@PathVariable UUID id, Authentication authentication) {
        var currentUserId = authentication.getName();
        if(!currentUserId.equals(id.toString())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        var userId = userService.getUserById(id);
        return APIResponse.<UserResponse>builder()
                .data(userId)
                .build();
    }


    @PostMapping
    @Operation(
            summary = "Create new user",
            description = "Create a new user with email, password, full name, and phone"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = APIResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400", description = "Invalid input or email already exists"),
            @ApiResponse(
                    responseCode = "500", description = "Internal server error")
    })
    public APIResponse<UserResponse> createUser(@RequestBody @Valid CreateUserRequest request) {
        return APIResponse.<UserResponse>builder()
                .data(userService.create(request))
                .build();
    }

    @GetMapping("/all")
    @Operation(summary = "Get all users", description = "Retrieve list of all users")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200",description = "Users retrieve successfully"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public APIResponse<List<UserResponse>> getAllUsers() {
        return APIResponse.<List<UserResponse>>builder()
                .data(userService.allUsers())
                .build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "update user", description = "update user information (email, full name, phone, password)")
    @ApiResponses(
            value = {
                    @ApiResponse(responseCode = "200", description = "User update successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input"),
                    @ApiResponse(responseCode = "404", description = "User not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            }
    )
    public APIResponse<UserResponse> updateUser(
            @PathVariable UUID id,
            @RequestBody @Valid UpdateUserRequest request, Authentication authentication) {
        var currentUserId = authentication.getName();
        if (!currentUserId.equals(id.toString())) {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        UserResponse response = userService.updateUser(id, request);
        return APIResponse.<UserResponse>builder()
                .data(response)
                .build();
    }


}
