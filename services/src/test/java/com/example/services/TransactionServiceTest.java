package com.example.services;


import com.example.services.dto.request.TransferRequest;
import com.example.services.entity.Transaction;
import com.example.services.entity.TransactionType;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
@DisplayName("TransactionService - Transfer Tests")
public class TransactionServiceTest {

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
                .email("sender@example.com")
                .password("password")
                .fullName("Van Sender")
                .phone("009323232233")
                .build();
        userSender = userRepository.save(userSender);

        userReceiver = User.builder()
                .email("receiver@example.com")
                .password("password")
                .fullName("Van Receiver")
                .phone("032323256595")
                .build();
        userReceiver = userRepository.save(userReceiver);

        walletSender = Wallet.builder()
                .user(userSender)
                .balance(new BigDecimal("50000.00"))
                .build();
        walletSender = walletRepository.save(walletSender);
        userSender.setWallet(walletSender);
        userRepository.save(userSender);

        walletReceiver = Wallet.builder()
                .user(userReceiver)
                .balance(new BigDecimal("120000.00"))
                .build();
        walletReceiver = walletRepository.save(walletReceiver);
        userReceiver.setWallet(walletReceiver);
        userRepository.save(userReceiver);
    }

    @Test
    @DisplayName("Transfer - Success (50000 - 100 = 49900, 120000 + 100 = 120100)")
    void testTransferSuccess() {
        TransferRequest transferRequest = TransferRequest.builder()
                .receiverId(userReceiver.getId())
                .amount(new BigDecimal("100.00"))
                .description("Payment")
                .build();

        transactionService.transfer(userSender.getId(), transferRequest);

        var updatedWalletSender = walletRepository.findById(walletSender.getId()).orElseThrow();
        assertEquals(new BigDecimal("900.00"), updatedWalletSender.getBalance(),
                "Sender balance should be 900 (1000 - 100)");

        var updatedWalletReceiver = walletRepository.findById(walletReceiver.getId()).orElseThrow();
        assertEquals(new BigDecimal("600.00"), updatedWalletReceiver.getBalance(),
                "Receiver balance should be 600 (500 + 100)");

        var transactions = transactionRepository.findAll();
        assertEquals(2, transactions.size(),
                "Should have 2 transaction records (OUT + IN)");

        var groupId = transactions.getFirst().getGroupId();
        assertTrue(transactions.stream().allMatch(t -> t.getGroupId().equals(groupId)),
                "Both transactions should have same groupId");

        var transferCount = transactions.stream().filter(t -> t.getType() == TransactionType.TRANSFER)
                .count();
        assertEquals(2,transferCount, "Both should be TRANSFER type");
    }


}
