package com.example.services.controller;

import com.example.services.dto.request.UpdateWalletRequest;
import com.example.services.dto.response.ApiResponse;
import com.example.services.dto.response.WalletResponse;
import com.example.services.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<WalletResponse>> getWallet(@PathVariable String userId) {
        WalletResponse response = walletService.getWalletForUser(userId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{userId}/add")
    public ResponseEntity<ApiResponse<WalletResponse>> addBalance(@PathVariable String userId,
                                                                  @RequestParam BigDecimal amount) {
        WalletResponse response = walletService.addBalance(userId, amount);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{userId}")
    public ResponseEntity<ApiResponse<WalletResponse>> updateWallet(@PathVariable String userId,
                                                                    @RequestBody @Valid UpdateWalletRequest request) {
        WalletResponse response = walletService.updateWallet(userId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{userId}/deduct")
    public ResponseEntity<ApiResponse<WalletResponse>> deductBalance(@PathVariable String userId, @RequestParam BigDecimal amount) {
        WalletResponse response = walletService.deductBalance(userId, amount);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{userId}/balance")
    public ResponseEntity<ApiResponse<BigDecimal>> getBalance(@PathVariable String userId) {
        BigDecimal balance = walletService.getBalance(userId);
        return ResponseEntity.ok(ApiResponse.success(balance));
    }
}
