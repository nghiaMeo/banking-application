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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

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
    private long initialTransactionCount;

    @BeforeEach
    void setUp() {
        // Create sender user
        userSender = User.builder()
                .email("sender@example.com")
                .password("password")
                .fullName("Van Sender")
                .phone("009323232233")
                .build();
        userSender = userRepository.save(userSender);

        // Create receiver user
        userReceiver = User.builder()
                .email("receiver@example.com")
                .password("password")
                .fullName("Van Receiver")
                .phone("032323256595")
                .build();
        userReceiver = userRepository.save(userReceiver);

        // Create sender wallet with 1000 balance
        walletSender = Wallet.builder()
                .user(userSender)
                .balance(new BigDecimal("1000.00"))
                .build();
        walletSender = walletRepository.save(walletSender);
        userSender.setWallet(walletSender);
        userRepository.save(userSender);

        // Create receiver wallet with 500 balance
        walletReceiver = Wallet.builder()
                .user(userReceiver)
                .balance(new BigDecimal("500.00"))
                .build();
        walletReceiver = walletRepository.save(walletReceiver);
        userReceiver.setWallet(walletReceiver);
        userRepository.save(userReceiver);

        initialTransactionCount = transactionRepository.count();
    }

    /**
     * TEST 1: Successful transfer
     * Sender: 1000 - 100 = 900
     * Receiver: 500 + 100 = 600
     */
    @Test
    @DisplayName("Transfer - Success (1000 - 100 = 900, 500 + 100 = 600)")
    void testTransferSuccess() {
        // Arrange
        TransferRequest transferRequest = TransferRequest.builder()
                .receiverId(userReceiver.getId())
                .amount(new BigDecimal("100.00"))
                .description("Payment")
                .build();

        // Act
        transactionService.transfer(userSender.getId(), transferRequest);

        // Assert - Sender balance
        Wallet updatedWalletSender = walletRepository.findById(walletSender.getId()).orElseThrow();
        assertEquals(new BigDecimal("900.00"), updatedWalletSender.getBalance(),
                "Sender balance should be 900 (1000 - 100)");

        // Assert - Receiver balance
        Wallet updatedWalletReceiver = walletRepository.findById(walletReceiver.getId()).orElseThrow();
        assertEquals(new BigDecimal("600.00"), updatedWalletReceiver.getBalance(),
                "Receiver balance should be 600 (500 + 100)");

        // Assert - Transaction count
        List<Transaction> transactions = transactionRepository.findAll().stream()
                .filter(t -> t.getWallet().getId().equals(walletSender.getId())
                        || t.getWallet().getId().equals(walletReceiver.getId()))
                .toList();
        assertEquals(2, transactions.size(),
                "Should have 2 transaction records (OUT + IN)");

        // Assert - Same groupId
        String groupId = transactions.getFirst().getGroupId();
        assertTrue(transactions.stream().allMatch(t -> t.getGroupId().equals(groupId)),
                "Both transactions should have same groupId");

        // Assert - Both are TRANSFER type
        long transferCount = transactions.stream()
                .filter(t -> t.getType() == TransactionType.TRANSFER)
                .count();
        assertEquals(2, transferCount, "Both should be TRANSFER type");
    }

    /**
     * TEST 2: Insufficient balance - should Roll back
     */
    @Test
    @DisplayName("Transfer - Insufficient Balance (Rollback)")
    void testTransferInsufficientBalanceRollback() {
        // Arrange - Get original balances
        BigDecimal originalSenderBalance = walletSender.getBalance();
        BigDecimal originalReceiverBalance = walletReceiver.getBalance();

        TransferRequest request = TransferRequest.builder()
                .receiverId(userReceiver.getId())
                .amount(new BigDecimal("2000.00"))  // More than balance (1000)
                .build();

        // Act - service logs AppException internally and does not rethrow
        assertDoesNotThrow(() -> transactionService.transfer(userSender.getId(), request));

        // Assert - Verify rollback (balances unchanged)
        Wallet rolledSender = walletRepository.findById(walletSender.getId()).orElseThrow();
        Wallet rolledReceiver = walletRepository.findById(walletReceiver.getId()).orElseThrow();

        assertEquals(originalSenderBalance, rolledSender.getBalance(),
                "Sender balance should be rolled back to original");
        assertEquals(originalReceiverBalance, rolledReceiver.getBalance(),
                "Receiver balance should be rolled back to original");

        // Assert - No transactions created
        assertEquals(initialTransactionCount, transactionRepository.count(),
                "No transactions should be created (rollback)");
    }

    /**
     * TEST 3: Self transfer - should fail
     */
    @Test
    @DisplayName("Transfer - Self Transfer (Error)")
    void testTransferSelfTransferError() {
        // Arrange
        TransferRequest request = TransferRequest.builder()
                .receiverId(userSender.getId())  // ← Same as sender!
                .amount(new BigDecimal("100.00"))
                .build();

        // Act - service logs AppException internally and does not rethrow
        assertDoesNotThrow(() -> transactionService.transfer(userSender.getId(), request));

        // Assert - Balance unchanged
        Wallet unchangedWallet = walletRepository.findById(walletSender.getId()).orElseThrow();
        assertEquals(new BigDecimal("1000.00"), unchangedWallet.getBalance(),
                "Balance should be unchanged");

        // Assert - No transactions
        assertEquals(initialTransactionCount, transactionRepository.count(),
                "No transactions should be created");
    }

    /**
     * TEST 4: Receiver not found
     */
    @Test
    @DisplayName("Transfer - Receiver Not Found")
    void testTransferReceiverNotFound() {
        // Arrange
        UUID nonExistentId = UUID.randomUUID();
        TransferRequest request = TransferRequest.builder()
                .receiverId(nonExistentId)
                .amount(new BigDecimal("100.00"))
                .build();

        // Act - service logs AppException internally and does not rethrow
        assertDoesNotThrow(() -> transactionService.transfer(userSender.getId(), request));

        // Assert - Sender balance unchanged (rollback)
        Wallet unchangedWallet = walletRepository.findById(walletSender.getId()).orElseThrow();
        assertEquals(new BigDecimal("1000.00"), unchangedWallet.getBalance());

        // Assert - No transactions
        assertEquals(initialTransactionCount, transactionRepository.count());
    }

    /**
     * TEST 5: Sender not found
     */
    @Test
    @DisplayName("Transfer - Sender Not Found")
    void testTransferSenderNotFound() {
        // Arrange
        UUID nonExistentSenderId = UUID.randomUUID();
        TransferRequest request = TransferRequest.builder()
                .receiverId(userReceiver.getId())
                .amount(new BigDecimal("100.00"))
                .build();

        // Act - service logs AppException internally and does not rethrow
        assertDoesNotThrow(() -> transactionService.transfer(nonExistentSenderId, request));

        // Assert - Receiver balance unchanged
        Wallet unchangedWallet = walletRepository.findById(walletReceiver.getId()).orElseThrow();
        assertEquals(new BigDecimal("500.00"), unchangedWallet.getBalance());

        // Assert - No transactions
        assertEquals(initialTransactionCount, transactionRepository.count());
    }

    /**
     * TEST 6: Multiple sequential transfers
     */
    @Test
    @DisplayName("Transfer - Multiple Transfers (Sequential)")
    void testTransferMultipleTransfers() {
        // Transfer 1: User sends 100
        TransferRequest request1 = TransferRequest.builder()
                .receiverId(userReceiver.getId())
                .amount(new BigDecimal("100.00"))
                .build();

        transactionService.transfer(userSender.getId(), request1);

        // Verify after first transfer
        Wallet w1 = walletRepository.findById(walletSender.getId()).orElseThrow();
        Wallet w2 = walletRepository.findById(walletReceiver.getId()).orElseThrow();
        assertEquals(new BigDecimal("900.00"), w1.getBalance());
        assertEquals(new BigDecimal("600.00"), w2.getBalance());

        // Transfer 2: User sends 50
        TransferRequest request2 = TransferRequest.builder()
                .receiverId(userReceiver.getId())
                .amount(new BigDecimal("50.00"))
                .build();

        transactionService.transfer(userSender.getId(), request2);

        // Verify after second transfer
        w1 = walletRepository.findById(walletSender.getId()).orElseThrow();
        w2 = walletRepository.findById(walletReceiver.getId()).orElseThrow();
        assertEquals(new BigDecimal("850.00"), w1.getBalance(),
                "Sender: 900 - 50 = 850");
        assertEquals(new BigDecimal("650.00"), w2.getBalance(),
                "Receiver: 600 + 50 = 650");

        // Assert - 4 transactions (2 per transfer)
        assertEquals(initialTransactionCount + 4, transactionRepository.count(),
                "Should create 4 new transactions (2 per transfer)");
    }

    /**
     * TEST 7: Transaction records integrity
     */
    @Test
    @DisplayName("Transfer - Transaction Records (Integrity Check)")
    void testTransferIntegrityCheck() {
        // Arrange
        TransferRequest request = TransferRequest.builder()
                .receiverId(userReceiver.getId())
                .amount(new BigDecimal("100.00"))
                .description("Test Payment")
                .build();

        // Act
        transactionService.transfer(userSender.getId(), request);

        // Assert - Get transactions
        List<Transaction> transactions = transactionRepository.findAll().stream()
                .filter(t -> t.getWallet().getId().equals(walletSender.getId())
                        || t.getWallet().getId().equals(walletReceiver.getId()))
                .toList();
        assertEquals(2, transactions.size(), "There should be two transactions");

        // Find sender and receiver transactions
        Transaction senderTx = transactions.stream()
                .filter(t -> t.getWallet().getId().equals(walletSender.getId()))
                .findFirst()
                .orElseThrow();

        Transaction receiverTx = transactions.stream()
                .filter(t -> t.getWallet().getId().equals(walletReceiver.getId()))
                .findFirst()
                .orElseThrow();

        // Assert - Sender transaction
        assertEquals(TransactionType.TRANSFER, senderTx.getType());
        assertEquals(new BigDecimal("100.00"), senderTx.getAmount());
        assertTrue(senderTx.getDescription().contains("Transfer to"));
        assertEquals(walletReceiver.getId(), senderTx.getRelatedWallet().getId());
        assertNotNull(senderTx.getGroupId());

        // Assert - Receiver transaction
        assertEquals(TransactionType.TRANSFER, receiverTx.getType());
        assertEquals(new BigDecimal("100.00"), receiverTx.getAmount());
        assertTrue(receiverTx.getDescription().contains("Transfer from"));
        assertEquals(walletSender.getId(), receiverTx.getRelatedWallet().getId());
        assertNotNull(receiverTx.getGroupId());

        // Assert - Same groupId
        assertEquals(senderTx.getGroupId(), receiverTx.getGroupId(),
                "Both transactions should have same groupId");

        // Assert - Timestamps
        assertNotNull(senderTx.getCreatedAt());
        assertNotNull(receiverTx.getCreatedAt());
    }

    /**
     * TEST 8: Zero amount transfer
     */
    @Test
    @DisplayName("Transfer - Zero Amount (No change)")
    void testTransferZeroAmount() {
        // Arrange
        TransferRequest request = TransferRequest.builder()
                .receiverId(userReceiver.getId())
                .amount(new BigDecimal("0.00"))
                .build();

        // Act
        transactionService.transfer(userSender.getId(), request);

        // Assert - Balances unchanged
        Wallet w1 = walletRepository.findById(walletSender.getId()).orElseThrow();
        Wallet w2 = walletRepository.findById(walletReceiver.getId()).orElseThrow();

        assertEquals(new BigDecimal("1000.00"), w1.getBalance(),
                "Sender balance should be unchanged");
        assertEquals(new BigDecimal("500.00"), w2.getBalance(),
                "Receiver balance should be unchanged");

        // 2 transactions still created (with 0 amount)
        assertEquals(initialTransactionCount + 2, transactionRepository.count(),
                "Should create 2 new transactions even with 0 amount");
    }
}