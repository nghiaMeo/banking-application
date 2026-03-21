package com.example.services.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Transfer request")
public class TransferRequest {
    @NotNull(message = "Receiver ID can't be null")
    @Schema(description = "Receiver user ID")
    private UUID receiverId;

    @NotNull(message = "Amount can't be null")
    @DecimalMin(value = "0.01",message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Schema(description = "Transfer description/note")
    private String description;

    @Schema(description = "Idempotency key (for preventing duplicates)")
    private String idempotencyKey;


}
