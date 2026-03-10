package com.example.services.controller;

import com.example.services.dto.request.CreateUserRequest;
import com.example.services.dto.request.UpdateUserRequest;
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
import org.hibernate.annotations.Parameter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {
    private final UserService userService;

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
    public UserResponse updateUser(
            @PathVariable String id,
            @RequestBody @Valid UpdateUserRequest request) {
        UserResponse response = userService.updateUser(UUID.fromString(id), request);
        return userService.updateUser(UUID.fromString(id), request);
    }


}
