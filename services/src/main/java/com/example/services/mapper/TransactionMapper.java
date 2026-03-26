package com.example.services.mapper;

import com.example.services.dto.response.TransactionResponse;
import com.example.services.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    @Mapping(source = "id", target = "transactionId")
    @Mapping(source = "type", target = "transactionType")
    TransactionResponse toTransactionResponse(Transaction transaction);
}
