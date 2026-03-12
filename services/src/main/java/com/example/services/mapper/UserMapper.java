package com.example.services.mapper;

import com.example.services.dto.request.CreateUserRequest;
import com.example.services.dto.request.UpdateUserRequest;
import com.example.services.dto.response.UserResponse;
import com.example.services.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "wallet", ignore = true)
    User toEntity(CreateUserRequest request);


    @Mapping(source = "id", target = "id")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "fullName", target = "fullName")
    @Mapping(source = "phone", target = "phone")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "wallet", ignore = true)
    void updateUserFromRequest(UpdateUserRequest request, @MappingTarget User user);

    // ✅ Map + normalize
    default User toEntityWithNormalization(CreateUserRequest request) {
        User user = toEntity(request);
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPhone(request.getPhone().trim());
        user.setFullName(request.getFullName().trim());
        return user;
    }
}
