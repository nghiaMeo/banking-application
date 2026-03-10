package com.example.services.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "User information response")
public class UserResponse {

    @Schema(description = "User ID (UUID)", example = "abc123-def456-ghi789")
    private UUID id;

    @Schema(description = "User email", example = "user@gmail.com")
    private String email;

    @Schema(description = "User full name", example = "Nguyen Huu Nghia")
    private String fullName;

    @Schema(description = "User phone number", example = "0123456789")
    private String phone;

    @Schema(description = "User creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "User update timestamp")
    private LocalDateTime updatedAt;
}
