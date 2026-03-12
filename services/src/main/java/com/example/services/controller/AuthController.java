package com.example.services.controller;


import com.example.services.dto.request.CreateUserRequest;
import com.example.services.dto.request.LoginRequest;
import com.example.services.dto.response.APIResponse;
import com.example.services.dto.response.AuthResponse;
import com.example.services.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "APIs for user authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Register a new user account JWT token")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or email already exists"),
            @ApiResponse(responseCode = "500", description = "Internal server error")

    })
    public APIResponse<AuthResponse> register(@RequestBody @Valid CreateUserRequest request) {
        return APIResponse.<AuthResponse>builder()
                .data(authService.register(request))
                .build();
    }

    @PostMapping("/login")
    @Operation(summary = "login user", description = "Authenticate user with email and password")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input email or password"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")

    })
    public APIResponse<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        return APIResponse.<AuthResponse>builder()
                .data(authService.login(request))
                .build();
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Generate new access token refresh token")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Token refreshed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid or expired refresh token"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public APIResponse<AuthResponse> refreshAccessToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        AuthResponse refreshed = authService.refreshAccessToken(token);
        return APIResponse.<AuthResponse>builder()
                .data(refreshed)
                .build();
    }

}
