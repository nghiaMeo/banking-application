package com.example.services.dto.response;


import com.example.services.entity.Transaction;
import com.example.services.entity.TransactionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Transaction response")
public class TransactionResponse {

    @Schema(description = "Transaction id")
    private UUID transactionId;

    @Schema(description = "Amount")
    private BigDecimal amount;

    @Schema(description = "Transaction type")
    private TransactionType transactionType;

    @Schema(description = "Description")
    private String description;

    @Schema(description = "Created at")
    private LocalDateTime createdAt;

}
