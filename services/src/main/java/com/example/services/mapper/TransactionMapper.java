package com.example.services.mapper;

import com.example.services.dto.response.TransactionResponse;
import com.example.services.entity.Transaction;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TransactionMapper {
    TransactionResponse toTransactionResponse(Transaction transaction);
}
