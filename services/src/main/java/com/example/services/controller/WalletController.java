package com.example.services.controller;

import com.example.services.dto.request.UpdateWalletRequest;
import com.example.services.dto.response.APIResponse;
import com.example.services.dto.response.WalletResponse;
import com.example.services.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.math.BigDecimal;
import java.util.UUID;


@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Tag(name = "Wallet Management", description = "APIs for managing user wallets")
public class WalletController {

    private final WalletService walletService;

    /**
     * Lấy wallet info
     * GET /api/wallet/{userId}
     */
    @GetMapping("/{userId}")
    @Operation(summary = "Get wallet", description = "Retrieve wallet information for a specific user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wallet found"),
            @ApiResponse(responseCode = "404", description = "Wallet not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public APIResponse<WalletResponse> getWallet(@PathVariable UUID userId) {
        WalletResponse response = walletService.getWalletForUser(userId);
        return APIResponse.<WalletResponse>builder()
                .data(response)
                .build();
    }

    /**
     * Update/Set balance (SET giá trị)
     * PUT /api/wallet/{userId}
     * Body: {"balance": 10000.00}
     */
    @PutMapping("/{userId}")
    @Operation(summary = "Update wallet balance", description = "Set wallet balance to a specific amount")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Wallet update successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Wallet not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public APIResponse<WalletResponse> updateWallet(
            @PathVariable UUID userId,
            @RequestBody @Valid UpdateWalletRequest request) {
        WalletResponse response = walletService.updateWallet(userId, request);
        return APIResponse.<WalletResponse>builder()
                .data(response)
                .build();
    }

    /**
     * Add Balance (Nạp tiền)
     * POST /api/wallet/{userId}/add?amount=100.00
     */
    @PostMapping("/{userId}/add")
    @Operation(summary = "Add balance", description = "Add money to wallet(deposit)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Balance added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Wallet not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public APIResponse<WalletResponse> addBalance(
            @PathVariable UUID userId,
            @RequestParam BigDecimal amount) {
        WalletResponse response = walletService.addBalance(userId, amount);
        return APIResponse.<WalletResponse>builder()
                .data(response)
                .build();
    }

    /**
     * Deduct Balance (Rút tiền)
     * POST /api/wallet/{userId}/deduct?amount=50.00
     */
    @PostMapping("/{userId}/deduct")
    @Operation(summary = "Deduct balance", description = "Deduct money from wallet (withdrawal)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Balance deduct successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid amount or insufficient balance"),
            @ApiResponse(responseCode = "404", description = "Wallet not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public APIResponse<WalletResponse> deductBalance(
            @PathVariable UUID userId,
            @RequestParam BigDecimal amount) {
        WalletResponse response = walletService.deductBalance(userId, amount);
        return APIResponse.<WalletResponse>builder()
                .data(response)
                .build();
    }

    /**
     * Lấy số dư
     * GET /api/wallet/{userId}/balance
     */
    @GetMapping("/{userId}/balance")
    @Operation(summary = "Get balance", description = "Get current wallet balance")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Balance retrieve successfully"),
            @ApiResponse(responseCode = "404", description = "Wallet not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public APIResponse<BigDecimal> getBalance(@PathVariable UUID userId) {
        BigDecimal balance = walletService.getBalance(userId);
        return APIResponse.<BigDecimal>builder()
                .data(balance)
                .build();
    }
}