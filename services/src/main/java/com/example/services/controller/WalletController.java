package com.example.services.controller;

import com.example.services.dto.request.wallet.DepositRequest;
import com.example.services.dto.request.wallet.TransferRequest;
import com.example.services.dto.request.wallet.WithdrawRequest;
import com.example.services.dto.response.APIResponse;
import com.example.services.dto.response.ApiResponseFactory;
import com.example.services.dto.response.PaginationResponse;
import com.example.services.dto.response.TransactionResponse;
import com.example.services.service.TransactionService;
import com.example.services.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.math.BigDecimal;
import java.util.UUID;


@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@Tag(name = "Wallet Management", description = "APIs for managing user wallets")
@Slf4j
public class WalletController {

    private final WalletService walletService;
    private final TransactionService transactionService;


    @GetMapping("/balance")
    @Operation(summary = "Get wallet balance")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "404", description = "Wallet not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public APIResponse<BigDecimal> getBalance(Authentication authentication) {
        var userId = UUID.fromString(authentication.getName());
        var balance = walletService.getBalance(userId);
        return ApiResponseFactory.ok(balance);
    }

    @PostMapping("/deposit")
    @Operation(summary = "Deposit money", description = "Add money to wallet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Deposit successful"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Wallet not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public APIResponse<BigDecimal> deposit(
            @Valid @RequestBody DepositRequest request,
            Authentication authentication) {

        var userId = authentication.getName();
        var newBalance = walletService.deposit(request, UUID.fromString(userId));

        return ApiResponseFactory.ok("Deposit successful", newBalance);
    }


    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw money", description = "Withdraw money from wallet")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Withdraw successful"),
            @ApiResponse(responseCode = "400", description = "Invalid input or insufficient balance"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Wallet not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public APIResponse<BigDecimal> withdraw(
            @Valid @RequestBody WithdrawRequest request,
            Authentication authentication) {

        var userId = UUID.fromString(authentication.getName());
        var newBalance = walletService.withdraw( request, userId);
        return ApiResponseFactory.ok("Withdraw successful", newBalance);
    }


    @PostMapping("/transfer")
    @Operation(summary = "Transfer money", description = "Transfer money to another user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transfer successful"),
            @ApiResponse(responseCode = "400", description = "Invalid input or insufficient balance"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Wallet not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public APIResponse<String> transfer(
            @Valid @RequestBody TransferRequest request,
            Authentication authentication) {

        var senderId = UUID.fromString(authentication.getName());
        transactionService.transfer(senderId, request);

        return ApiResponseFactory.<String>ok("transfer Successfully");
    }


    @GetMapping("/transactions")
    @Operation(summary = "Get transaction history")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success"),
            @ApiResponse(responseCode = "403", description = "Forbidden"),
            @ApiResponse(responseCode = "404", description = "Wallet not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public APIResponse<PaginationResponse<TransactionResponse>>
            getTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        var userId = UUID.fromString(authentication.getName());
        PaginationResponse<TransactionResponse> transactions = walletService.getTransactionsByUserId(
                userId,
                page,
                size
        );

        return ApiResponseFactory.ok(transactions);
    }

}