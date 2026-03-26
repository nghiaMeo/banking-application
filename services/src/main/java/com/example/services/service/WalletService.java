package com.example.services.service;

import com.example.services.dto.request.wallet.DepositRequest;
import com.example.services.dto.request.wallet.UpdateWalletRequest;
import com.example.services.dto.request.wallet.WithdrawRequest;
import com.example.services.dto.response.TransactionResponse;
import com.example.services.dto.response.WalletResponse;
import com.example.services.entity.Transaction;
import com.example.services.entity.TransactionType;
import com.example.services.entity.User;
import com.example.services.entity.Wallet;
import com.example.services.exception.AppException;
import com.example.services.exception.enums.ErrorCode;
import com.example.services.mapper.TransactionMapper;
import com.example.services.mapper.WalletMapper;
import com.example.services.repository.TransactionRepository;
import com.example.services.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 50;

    private final WalletRepository walletRepository;
    private final WalletMapper walletMapper;
    private final TransactionMapper transactionMapper;
    private final TransactionRepository transactionRepository;

    public Page<TransactionResponse> getTransactions(UUID walletId, int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = size <= 0 ? DEFAULT_PAGE_SIZE : Math.min(MAX_PAGE_SIZE, size);

        Pageable pageable = PageRequest.of(
                safePage,
                safeSize,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
        Page<Transaction> transactions = transactionRepository.findByWalletId(walletId, pageable);

        log.info("Retrieved {} transactions for wallet: {}",
                transactions.getContent().size(), walletId);

        return transactions.map(transactionMapper::toTransactionResponse);
    }


    @Transactional(rollbackFor = Exception.class)
    public BigDecimal deposit(DepositRequest request, UUID userId) {
        var wallet = getWalletByUserId(userId);
        verifyWalletOwnership(userId,wallet.getId());
        var newBalance = wallet.getBalance().add(request.getAmount());
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);


        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(request.getAmount())
                .type(TransactionType.DEPOSIT)
                .description("Deposit transaction: " + request.getAmount())
                .build();

        transactionRepository.save(transaction);
        log.info("Deposit successful - userId: {}, amount: {}, newBalance: {}",
                userId, request.getAmount(), newBalance);

        return newBalance;
    }

    @Transactional(rollbackFor = Exception.class)
    public BigDecimal withdraw(WithdrawRequest request, UUID userId) {
        var wallet = getWalletByUserId(userId);

        verifyWalletOwnership(userId,wallet.getId());

        if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        BigDecimal newBalance = wallet.getBalance().subtract(request.getAmount());
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        Transaction transaction = Transaction.builder()
                .wallet(wallet)
                .amount(request.getAmount())
                .type(TransactionType.WITHDRAW)
                .description("Withdraw transaction: " + request.getAmount())
                .build();

        transactionRepository.save(transaction);
        log.info("Withdraw successful - userId: {}, amount: {}, new balance: {}",
                userId, request.getAmount(), newBalance);
        return newBalance;
    }


    public Wallet createWalletForUser(User user) {
        var wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);

        Wallet savedWallet = walletRepository.save(wallet);
        log.info("Wallet created: {}", user.getId());
        return savedWallet;
    }


    public WalletResponse updateWallet(UUID userId, UpdateWalletRequest updateWalletRequest) {
        var wallet = walletRepository.findByUserId(userId).orElseThrow(() -> new AppException(ErrorCode.USER_ALREADY_EXISTS));
        wallet.setBalance(updateWalletRequest.getBalance());
        Wallet updated = walletRepository.save(wallet);
        log.info("Wallet balance updated for user: {} to {}", userId, updateWalletRequest.getBalance());

        return walletMapper.toWalletResponse(updated);

    }

    public WalletResponse addBalance(UUID userId, BigDecimal amount) {
        var wallet = getWalletByUserId(userId);
        verifyWalletOwnership(userId,wallet.getId());


        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.INVALID_AMOUNT);
        }
        wallet.setBalance(wallet.getBalance().add(amount));

        var update = walletRepository.save(wallet);
        log.info("Balance added for user: {} amount: {}", userId, amount);
        return walletMapper.toWalletResponse(update);
    }

    public WalletResponse deductBalance(UUID userId, BigDecimal amount) {
        var wallet = getWalletByUserId(userId);
        verifyWalletOwnership(userId, wallet.getId());

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException(ErrorCode.INVALID_AMOUNT);
        }

        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new AppException(ErrorCode.AMOUNT_IS_ZERO);
        }
        wallet.setBalance(wallet.getBalance().subtract(amount));
        var update = walletRepository.save(wallet);
        log.info("Balance deducted for user: {} amount: {}", userId, amount);
        return walletMapper.toWalletResponse(update);
    }

    public BigDecimal getBalance(UUID userId) {
        Wallet wallet = getWalletByUserId(userId);
        verifyWalletOwnership(userId, wallet.getId());
        return wallet.getBalance();
    }

    public WalletResponse getWalletByUserIdResponse(UUID userId) {
        var wallet = walletRepository.findByUserId(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return walletMapper.toWalletResponse(wallet);
    }

    public Wallet getWalletByUserId(UUID userId) {
        return walletRepository.findByUserId(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private void verifyWalletOwnership(UUID userId, UUID walletId) {
        var wallet = walletRepository.findById(walletId).orElseThrow(() -> new AppException(ErrorCode.WALLET_NOT_FOUND));

        if (!wallet.getUser().getId().equals(userId)) {
            log.warn("User {} attempted to access wallet {} of user {}",
                    userId, walletId, wallet.getUser().getId());
            throw new AppException(ErrorCode.FORBIDDEN);
        }
    }

}
