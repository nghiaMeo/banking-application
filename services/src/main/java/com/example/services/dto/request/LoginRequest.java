package com.example.services.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "User login request")
public class LoginRequest {

    @NotBlank(message = "Email can't be empty")
    @Email(message = "Email should be valid")
    @Schema(description = "User email", example = "user@gmail.com")
    private String email;

    @NotBlank(message = "Password can't be empty")
    @Schema(description = "User password", example = "Password123")
    private String password;
}
