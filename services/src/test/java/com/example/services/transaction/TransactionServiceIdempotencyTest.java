package com.example.services.transaction;


import com.example.services.dto.request.TransferRequest;
import com.example.services.entity.User;
import com.example.services.entity.Wallet;
import com.example.services.repository.TransactionRepository;
import com.example.services.repository.UserRepository;
import com.example.services.repository.WalletRepository;
import com.example.services.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
@Transactional
@DisplayName("TransactionService - Idempotency Tests")
public class TransactionServiceIdempotencyTest {
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private User userSender;
    private User userReceiver;
    private Wallet walletSender;
    private Wallet walletReceiver;

    @BeforeEach
    void setUp() {
        userSender = User.builder()
                .email("userSender@test.com")
                .password("pass1")
                .fullName("User 1")
                .phone("0911111111")
                .build();
        userSender = userRepository.save(userSender);

        userReceiver = User.builder()
                .email("userReceiver@test.com")
                .password("pass2")
                .fullName("User 2")
                .phone("0922222222")
                .build();
        userReceiver = userRepository.save(userReceiver);

        walletSender = Wallet.builder()
                .user(userSender)
                .balance(new BigDecimal("1000.00"))
                .build();
        walletSender = walletRepository.save(walletSender);
        userSender.setWallet(walletSender);
        userRepository.save(userSender);

        walletReceiver = Wallet.builder()
                .user(userReceiver)
                .balance(new BigDecimal("500.00"))
                .build();
        walletReceiver = walletRepository.save(walletReceiver);
        userReceiver.setWallet(walletReceiver);
        userRepository.save(userReceiver);
    }

    @Test
    @DisplayName("Idempotency - Duplicate request (same key)")
    void testIdempotencyDuplicateRequest() {
        var idempotencyKey = UUID.randomUUID().toString();
        var request = TransferRequest.builder()
                .receiverId(userReceiver.getId())
                .amount(new BigDecimal("100.00"))
                .idempotencyKey(idempotencyKey)
                .build();

        var txBase = transactionRepository.count();
        transactionService.transfer(userSender.getId(),request);

        var w1 = walletRepository.findById(walletSender.getId()).orElseThrow();
        var w2 = walletRepository.findById(walletReceiver.getId()).orElseThrow();
        assertEquals(new BigDecimal("900.00"), w1.getBalance());
        assertEquals(new BigDecimal("600.00"), w2.getBalance());

        var initialCount = transactionRepository.count();
        assertEquals(txBase + 2, initialCount,
                "One transfer persists sender + receiver rows");

        transactionService.transfer(userSender.getId(),request);

        w1 = walletRepository.findById(walletSender.getId()).orElseThrow();
        w2 = walletRepository.findById(walletReceiver.getId()).orElseThrow();
        assertEquals(new BigDecimal("900.00"), w1.getBalance(),
                "Balance should stay 900 (not become 800)");
        assertEquals(new BigDecimal("600.00"), w2.getBalance(),
                "Balance should stay 600 (not become 700)");

        var finalCount = transactionRepository.count();
        assertEquals(initialCount, finalCount,
                "No new transactions should be created for duplicate request");
    }

    @Test
    @DisplayName("Idempotency - Different keys (different requests)")
    void testIdempotencyDifferentRequests() {
        var idempotencyKey1 = UUID.randomUUID().toString();
        var idempotencyKey2 = UUID.randomUUID().toString();

        var request1 = TransferRequest.builder()
                .receiverId(userReceiver.getId())
                .amount(new BigDecimal("100.00"))
                .idempotencyKey(idempotencyKey1)
                .build();

        var request2 = TransferRequest.builder()
                .receiverId(userReceiver.getId())
                .amount(new BigDecimal("50.00"))
                .idempotencyKey(idempotencyKey2)
                .build();

        var txBase = transactionRepository.count();
        transactionService.transfer(userSender.getId(),request1);

        var w1 = walletRepository.findById(walletSender.getId()).orElseThrow();
        assertEquals(new BigDecimal("900.00"), w1.getBalance());
        assertEquals(txBase + 2, transactionRepository.count());

        transactionService.transfer(userSender.getId(),request2);

        var senderAfter = walletRepository.findById(walletSender.getId()).orElseThrow();
        assertEquals(new BigDecimal("850.00"), senderAfter.getBalance(),
                "Sender after both transfers: 1000 - 100 - 50 = 850");
        var receiverAfter = walletRepository.findById(walletReceiver.getId()).orElseThrow();
        assertEquals(new BigDecimal("650.00"), receiverAfter.getBalance(),
                "Receiver: 500 + 100 + 50 = 650");

        assertEquals(txBase + 4, transactionRepository.count(),
                "Two transfers: +2 rows each (sender + receiver)");
    }

    @Test
    @DisplayName("Idempotency - No key provided (new request)")
    void testIdempotencyNoKeyProvided() {
        var request = TransferRequest.builder()
                .receiverId(userReceiver.getId())
                .amount(new BigDecimal("100.00"))
                .build();

        var txBase = transactionRepository.count();
        transactionService.transfer(userSender.getId(),request);

        var w1 = walletRepository.findById(walletSender.getId()).orElseThrow();
        assertEquals(new BigDecimal("900.00"), w1.getBalance());
        assertEquals(txBase + 2, transactionRepository.count());

        transactionService.transfer(userSender.getId(),request);
        w1 = walletRepository.findById(walletSender.getId()).orElseThrow();
        assertEquals(new BigDecimal("800.00"), w1.getBalance(),
                "Sender balance after two transfers: 1000 - 100 - 100 = 800");
        assertEquals(txBase + 4, transactionRepository.count(),
                "Two transfers: +2 rows each (sender + receiver)");
    }
}

/*
*
*   BEFORE (Can deadlock):
Thread A: 1 → 2 (lock 1, lock 2)
Thread B: 2 → 1 (lock 2, lock 1) ← DEADLOCK!

 AFTER (No deadlock):
Thread A: 1 → 2
  - firstId = 1, secondId = 2
  - lock 1, lock 2 ✓

Thread B: 2 → 1
  - firstId = 1, secondId = 2  (same order!)
  - lock 1 (wait for A), lock 2
  - Safe ✓
* */
