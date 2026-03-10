package com.example.services.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request to create a new user")
public class CreateUserRequest {


    @NotBlank(message = "Email can't be empty")
    @Email(message = "Email should be valid")
    @Schema(description = "User email address", example = "user@gmail.com")
    private String email;

    @NotBlank(message = "Password can't be empty")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Schema(description = "User password (min 8 characters)", example = "Password123")
    private String password;


    @NotBlank(message = "Your name can't be empty")
    @Schema(description = "User full name", example = "Nghia Meow")
    private String fullName;

    @NotBlank(message = "Invalid phone number")
    @Schema(description = "User phone number", example = "0320302302")
    private String phone;
}
