package com.example.services.transaction;


import com.example.services.entity.User;
import com.example.services.entity.Wallet;
import com.example.services.repository.TransactionRepository;
import com.example.services.repository.UserRepository;
import com.example.services.repository.WalletRepository;
import com.example.services.service.TransactionService;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

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

    private User user1;
    private User user2;
    private Wallet wallet1;
    private Wallet wallet2;
}
