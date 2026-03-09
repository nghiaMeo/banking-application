package com.example.services.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateWalletRequest {

    @NotNull(message = "Balance can't be null")
    @DecimalMin(value = "0.01", message = "Balance must be greater than or equal to 0")
    private BigDecimal balance;
}
