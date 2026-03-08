package com.example.services.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateUserRequest {

    @Email(message = "Email should be valid")
    private String email;

    private String fullName;

    private String phone;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
}
