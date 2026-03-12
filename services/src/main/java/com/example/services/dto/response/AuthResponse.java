package com.example.services.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Authentication response with tokens")
public class AuthResponse {

    @Schema(description = "JWT Access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9")
    private String accessToken;

    @Schema(description = "Refresh Token", example = "eyJhbGci2iJIUzI1NiIsInR5cCI6IkpXVCJ9")
    private String refreshToken;

    @Schema(description = "Token Type", example = "Bear")
    private String tokenType;

    @Schema(description = "Access token expiration time (ms)", example = "360000")
    private Long expiresIn;

    @Schema(description = "User information")
    private UserResponse user;

}
