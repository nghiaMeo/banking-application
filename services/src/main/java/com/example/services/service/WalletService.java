package com.example.services.service;

import com.example.services.dto.request.UpdateWalletRequest;
import com.example.services.dto.response.WalletResponse;
import com.example.services.entity.User;
import com.example.services.entity.Wallet;
import com.example.services.exception.AppException;
import com.example.services.exception.enums.ErrorCode;
import com.example.services.mapper.WalletMapper;
import com.example.services.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;

    public Wallet createWalletForUser(User user) {
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);

        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Wallet created: {}", user.getId());
        return savedWallet;
    }

    public WalletResponse getWalletForUser(UUID userId) {
        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow(() -> new AppException(ErrorCode.USER_ALREADY_EXISTS));
        return walletMapper.toWalletResponse(wallet);
    }

    public WalletResponse updateWallet(UUID userId, UpdateWalletRequest updateWalletRequest) {
        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow(() -> new AppException(ErrorCode.USER_ALREADY_EXISTS));
        wallet.setBalance(updateWalletRequest.getBalance());
        Wallet updated = walletRepository.save(wallet);
        log.info("Wallet balance updated for user: {} to {}", userId, updateWalletRequest.getBalance());

        return walletMapper.toWalletResponse(updated);

    }

    public WalletResponse addBalance(UUID userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow(() -> new AppException(ErrorCode.USER_ALREADY_EXISTS));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.INVALID_AMOUNT);
        }
        wallet.setBalance(wallet.getBalance().add(amount));

        Wallet update = walletRepository.save(wallet);
        log.info("Balance added for user: {} amount: {}", userId, amount);
        return walletMapper.toWalletResponse(update);
    }

    public WalletResponse deductBalance(UUID userId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.INVALID_AMOUNT);
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new AppException(ErrorCode.AMOUNT_IS_ZERO);
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        Wallet update = walletRepository.save(wallet);
        log.info("Balance deducted for user: {} amount: {}", userId, amount);
        return walletMapper.toWalletResponse(update);
    }

    public BigDecimal getBalance(UUID userId) {
        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow(() -> new AppException(ErrorCode.INVALID_AMOUNT));
        return wallet.getBalance();
    }


}
