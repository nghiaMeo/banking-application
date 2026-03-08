package com.example.services.service;

import com.example.services.dto.request.UpdateWalletRequest;
import com.example.services.dto.response.WalletResponse;
import com.example.services.entity.User;
import com.example.services.entity.Wallet;
import com.example.services.exception.AppException;
import com.example.services.exception.enums.ErrorStatus;
import com.example.services.mapper.WalletMapper;
import com.example.services.repository.UserRepository;
import com.example.services.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final WalletMapper walletMapper;

    public Wallet createWalletForUser(User user) {
        Wallet wallet = Wallet.builder().user(user).balance(BigDecimal.ZERO).build();
        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Wallet created: {}", user.getId());
        return savedWallet;
    }

    public WalletResponse getWalletForUser(String userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(
                        ErrorStatus.WALLET_NOT_FOUND.getCode(), ErrorStatus.WALLET_NOT_FOUND.getMessage()));
        return walletMapper.toWalletResponse(wallet);
    }

    public WalletResponse updateWallet(String userId, UpdateWalletRequest updateWalletRequest) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(
                        ErrorStatus.WALLET_NOT_FOUND.getCode(), ErrorStatus.WALLET_NOT_FOUND.getMessage()));
        wallet.setBalance(updateWalletRequest.getBalance());
        Wallet updated = walletRepository.save(wallet);
        log.info("Wallet balance updated for user: {} to {}", userId, updateWalletRequest.getBalance());

        return walletMapper.toWalletResponse(updated);

    }

    public WalletResponse addBalance(String userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(
                        ErrorStatus.WALLET_NOT_FOUND.getCode(), ErrorStatus.WALLET_NOT_FOUND.getMessage()));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorStatus.AMOUNT_INVALID.getCode(), ErrorStatus.AMOUNT_INVALID.getMessage());
        }
        wallet.setBalance(wallet.getBalance().add(amount));

        Wallet update = walletRepository.save(wallet);
        log.info("Balance added for user: {} amount: {}", userId, amount);
        return walletMapper.toWalletResponse(update);
    }

    public WalletResponse deductBalance(String userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(
                        ErrorStatus.WALLET_NOT_FOUND.getCode(), ErrorStatus.WALLET_NOT_FOUND.getMessage()));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorStatus.AMOUNT_INVALID.getCode(), ErrorStatus.AMOUNT_INVALID.getMessage());
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new AppException(ErrorStatus.AMOUNT_IS_ZERO.getCode(), ErrorStatus.AMOUNT_IS_ZERO.getMessage());
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        Wallet update = walletRepository.save(wallet);
        log.info("Balance deducted for user: {} amount: {}", userId, amount);
        return walletMapper.toWalletResponse(update);
    }

    public BigDecimal getBalance(String userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(
                        ErrorStatus.WALLET_NOT_FOUND.getCode(), ErrorStatus.WALLET_NOT_FOUND.getMessage()));
        return wallet.getBalance();
    }


}
