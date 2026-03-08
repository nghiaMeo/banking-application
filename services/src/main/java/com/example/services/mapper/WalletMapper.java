package com.example.services.mapper;

import com.example.services.dto.request.UpdateWalletRequest;
import com.example.services.dto.response.WalletResponse;
import com.example.services.entity.Wallet;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface WalletMapper {
    @Mapping(source = "user.id", target = "userId")
    WalletResponse toWalletResponse(Wallet wallet);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateWallet(UpdateWalletRequest updateWalletRequest, @MappingTarget Wallet wallet);

}
