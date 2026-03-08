package com.example.services.dto.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateUserRequest {


    @NotBlank(message = "Email can't be empty")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Password can't be empty")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;


    @NotBlank(message = "Your name can't be empty")
    private String fullName;

    @NotBlank(message = "Invalid phone number")
    private String phone;
}
