package com.example.services.controller;

import com.example.services.dto.request.UpdateWalletRequest;
import com.example.services.dto.response.APIResponse;
import com.example.services.dto.response.WalletResponse;
import com.example.services.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;


@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    /**
     * Lấy wallet info
     * GET /api/wallet/{userId}
     */
    @GetMapping("/{userId}")
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
    public APIResponse<WalletResponse> addBalance(
            @PathVariable UUID userId,
            @RequestParam BigDecimal amount) {
        WalletResponse response = walletService.addBalance(userId, amount);
        return APIResponse.<WalletResponse>builder()
                .data(response)
                .build();
    }

    /**
     *  Deduct Balance (Rút tiền)
     * POST /api/wallet/{userId}/deduct?amount=50.00
     */
    @PostMapping("/{userId}/deduct")
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
    public APIResponse<BigDecimal> getBalance(@PathVariable UUID userId) {
        BigDecimal balance = walletService.getBalance(userId);
        return APIResponse.<BigDecimal>builder()
                .data(balance)
                .build();
    }
}