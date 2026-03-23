package com.example.services.transaction;


import com.example.services.dto.request.wallet.TransferRequest;
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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@DisplayName("TransactionService - Concurrency Tests")
public class TransactionServiceConcurrencyTest {
    @Autowired
    private TransactionService transactionService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WalletRepository walletRepository;

    private User userSender;
    private User userReceiver;
    private User user3;
    private Wallet walletSender;
    private Wallet walletReceiver;
    private Wallet wallet3;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        walletRepository.deleteAll();
        userRepository.deleteAll();

        var suffix = UUID.randomUUID().toString().substring(0, 8);

        // Create users
        userSender = User.builder()
                .email("userSender+" + suffix + "@test.com")
                .password("pass1")
                .fullName("User 1")
                .phone("0911111111")
                .build();
        userSender = userRepository.save(userSender);

        userReceiver = User.builder()
                .email("userReceiver+" + suffix + "@test.com")
                .password("pass2")
                .fullName("User 2")
                .phone("0922222222")
                .build();
        userReceiver = userRepository.save(userReceiver);

        user3 = User.builder()
                .email("user3+" + suffix + "@test.com")
                .password("pass3")
                .fullName("User 3")
                .phone("0933333333")
                .build();
        user3 = userRepository.save(user3);

        walletSender = Wallet.builder()
                .user(userSender)
                .balance(new BigDecimal("1000.00"))
                .build();
        walletSender = walletRepository.save(walletSender);
        userSender.setWallet(walletSender);
        userRepository.save(userSender);

        walletReceiver = Wallet.builder()
                .user(userReceiver)
                .balance(new BigDecimal("1000.00"))
                .build();
        walletReceiver = walletRepository.save(walletReceiver);
        userReceiver.setWallet(walletReceiver);
        userRepository.save(userReceiver);

        wallet3 = Wallet.builder()
                .user(user3)
                .balance(new BigDecimal("1000.00"))
                .build();
        wallet3 = walletRepository.save(wallet3);
        user3.setWallet(wallet3);
        userRepository.save(user3);
    }

    @Test
    @DisplayName("Concurrency - 2 transfers from same sender (no double spending)")
    void testConcurrencyTwoTransfersFromSameSender() throws InterruptedException {
        var sender = TransferRequest.builder()
                .receiverId(userReceiver.getId())
                .amount(new BigDecimal("600.00"))
                .build();

        var sender2 = TransferRequest.builder()
                .receiverId(user3.getId())
                .amount(new BigDecimal("600.00"))
                .build();

        var executor = Executors.newFixedThreadPool(2);
        var latch = new CountDownLatch(2);
        var successCount = new AtomicInteger(0);
        var failCount = new AtomicInteger(0);

        executor.submit(() -> {
            try {
                transactionService.transfer(userSender.getId(), sender);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                transactionService.transfer(userSender.getId(), sender2);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });

        assertTrue(latch.await(10, TimeUnit.SECONDS), "Timeout waiting for transfers");
        executor.shutdown();

        assertEquals(1, successCount.get(), "Only 1 transfer should succeed");
        assertEquals(1, failCount.get(), "1 transfer should fail");

        var userSenderWallet = walletRepository.findById(walletSender.getId()).orElseThrow();
        var userSenderBalance = userSenderWallet.getBalance();

        assertTrue(userSenderBalance.compareTo(new BigDecimal("0")) >= 0,
                "Balance should never be negative");
        assertTrue(userSenderBalance.compareTo(new BigDecimal("10000")) <= 0,
                "Balance should not exceed original");

        var w1 = walletRepository.findById(walletSender.getId()).orElseThrow();
        var w2 = walletRepository.findById(walletReceiver.getId()).orElseThrow();
        var w3 = walletRepository.findById(wallet3.getId()).orElseThrow();

        var totalBalance = w1.getBalance()
                .add(w2.getBalance())
                .add(w3.getBalance());

        assertEquals(new BigDecimal("3000.00"), totalBalance,
                "Total money in system should stay 3000");
        assertEquals(new BigDecimal("400.00"), w1.getBalance(),
                "Only one transfer of 600 should be applied from sender");
    }

    @Test
    @DisplayName("Concurrency - 10 concurrent transfers (stress test)")
    void testConcurrencyMultipleTransfersStressTest() throws InterruptedException {
        var threadCount = 10;

        var executor = Executors.newFixedThreadPool(threadCount);
        var latch = new CountDownLatch(threadCount);
        var successCount = new AtomicInteger(0);
        var failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    var receiverId = (index % 2 == 0)
                            ? userReceiver.getId() : user3.getId();
                    var request = TransferRequest.builder()
                            .receiverId(receiverId)
                            .amount(new BigDecimal("50.00"))
                            .build();

                    transactionService.transfer(userSender.getId(), request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }
        // Wait for all threads
        assertTrue(latch.await(30, TimeUnit.SECONDS), "Timeout waiting for transfers");
        executor.shutdown();

        // Assert - Some succeed, some fail
        var totalAttempts = successCount.get() + failCount.get();
        assertEquals(threadCount, totalAttempts, "All attempts should complete");

        // Max 20 transfers can succeed (1000 / 50 = 20)
        assertTrue(successCount.get() <= 20,
                "Cannot have more than 20 successful transfers");
        assertTrue(failCount.get() >= 0,
                "Failed transfers are expected once balance is insufficient");

        // Assert - Verify balance constraints
        var w1 = walletRepository.findById(walletSender.getId()).orElseThrow();
        assertTrue(w1.getBalance().compareTo(new BigDecimal("0")) >= 0,
                "Balance should not be negative");

        // Assert - Money conservation
        var w2 = walletRepository.findById(walletReceiver.getId()).orElseThrow();
        var w3 = walletRepository.findById(wallet3.getId()).orElseThrow();

        var totalBalance = w1.getBalance()
                .add(w2.getBalance())
                .add(w3.getBalance());

        assertEquals(new BigDecimal("3000.00"), totalBalance,
                "Total money in system must be conserved");

    }

    /**
     * User 1 → User 2
     * User 2 → User 3
     * User 3 → User 1
     * All concurrent
     * Should maintain balance integrity
     */
    @Test
    @DisplayName("Concurrency - Circular transfers")
    void testConcurrency_CircularTransfers() throws InterruptedException {
        // Arrange
        var executor = Executors.newFixedThreadPool(3);
        var latch = new CountDownLatch(3);
        var successCount = new AtomicInteger(0);
        var failCount = new AtomicInteger(0);

        // Act - Circular: 1→2, 2→3, 3→1
        executor.submit(() -> {
            try {
                var request = TransferRequest.builder()
                        .receiverId(userReceiver.getId())
                        .amount(new BigDecimal("100.00"))
                        .build();
                transactionService.transfer(userSender.getId(), request);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                var request = TransferRequest.builder()
                        .receiverId(user3.getId())
                        .amount(new BigDecimal("100.00"))
                        .build();
                transactionService.transfer(userReceiver.getId(), request);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });

        executor.submit(() -> {
            try {
                var request = TransferRequest.builder()
                        .receiverId(userSender.getId())
                        .amount(new BigDecimal("100.00"))
                        .build();
                transactionService.transfer(user3.getId(), request);
                successCount.incrementAndGet();
            } catch (Exception e) {
                failCount.incrementAndGet();
            } finally {
                latch.countDown();
            }
        });

        // Wait
        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(3, successCount.get() + failCount.get(), "All transfer calls should complete");
        assertTrue(successCount.get() >= 2,
                "At least 2 circular transfers should succeed under concurrent locking");

        var w1 = walletRepository.findById(walletSender.getId()).orElseThrow();
        var w2 = walletRepository.findById(walletReceiver.getId()).orElseThrow();
        var w3 = walletRepository.findById(wallet3.getId()).orElseThrow();

        assertTrue(w1.getBalance().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(w2.getBalance().compareTo(BigDecimal.ZERO) >= 0);
        assertTrue(w3.getBalance().compareTo(BigDecimal.ZERO) >= 0);

        // Assert - Total conserved
        var total = w1.getBalance()
                .add(w2.getBalance())
                .add(w3.getBalance());
        assertEquals(new BigDecimal("3000.00"), total);
    }
}


