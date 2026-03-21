package com.example.services.service;

import com.example.services.dto.request.TransferRequest;
import com.example.services.entity.Transaction;
import com.example.services.entity.TransactionType;
import com.example.services.exception.AppException;
import com.example.services.exception.enums.ErrorCode;
import com.example.services.repository.TransactionRepository;
import com.example.services.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final WalletRepository walletRepository;

    @Transactional(rollbackFor = Exception.class)
    public void transfer(UUID senderId, TransferRequest transferRequest) {
        try {
            log.info("Transfer initiated - from: {}, to: {}, amount: {}, idempotencyKey: {}",
                    senderId, transferRequest.getReceiverId(), transferRequest.getAmount(),
                    transferRequest.getIdempotencyKey());

            // S1: Check idempotency (prevent duplicate processing)
            if (transferRequest.getIdempotencyKey() != null) {
                var existing = transactionRepository.findByIdempotencyKey(transferRequest.getIdempotencyKey());
                if (existing.isPresent()) {
                    log.warn("Duplicate transfer request detected - idempotencyKey: {}",
                            transferRequest.getIdempotencyKey());
                    // Return silently (idempotent)
                    return;
                }
            }

            // S1: Validate sender != receiver
            if (senderId.equals(transferRequest.getReceiverId())) {
                log.warn("Self-transfer attempted - userId: {}", senderId);
                throw new AppException(
                        ErrorCode.BAD_REQUEST
                );
            }

            //  S2: DEADLOCK PREVENTION - Lock in consistent order
            // Always lock smaller ID first, then larger ID
            UUID firstId = senderId.compareTo(transferRequest.getReceiverId()) < 0
                    ? senderId
                    : transferRequest.getReceiverId();
            UUID secondId = senderId.compareTo(transferRequest.getReceiverId()) < 0
                    ? transferRequest.getReceiverId()
                    : senderId;

            log.debug("Lock ordering (deadlock prevention) - first: {}, second: {}", firstId, secondId);

            // S3: Lock wallets in consistent order
            var wallet1 = walletRepository.findByUserIdForUpdate(firstId)
                    .orElseThrow(() -> {
                        log.error("Wallet not found - userId: {}", firstId);
                        return new AppException(
                                ErrorCode.WALLET_NOT_FOUND
                        );
                    });

            var wallet2 = walletRepository.findByUserIdForUpdate(secondId)
                    .orElseThrow(() -> {
                        log.error("Wallet not found - userId: {}", secondId);
                        return new AppException(
                                ErrorCode.WALLET_NOT_FOUND
                        );
                    });

            log.debug("Both wallets locked in consistent order");

            // S4: Determine sender and receiver
            var senderWallet = senderId.equals(wallet1.getUser().getId()) ? wallet1 : wallet2;
            var receiverWallet = senderId.equals(wallet1.getUser().getId()) ? wallet2 : wallet1;

            log.debug("Sender: {}, Receiver: {}",
                    senderWallet.getUser().getId(), receiverWallet.getUser().getId());

            // S5: Check balance
            if (senderWallet.getBalance().compareTo(transferRequest.getAmount()) < 0) {
                log.warn("Insufficient balance - sender: {}, required: {}, current: {}",
                        senderId, transferRequest.getAmount(), senderWallet.getBalance());
                throw new AppException(
                        ErrorCode.INSUFFICIENT_BALANCE
                );
            }

            // S6: Calculate new balances
            var senderNewBalance = senderWallet.getBalance().subtract(transferRequest.getAmount());
            var receiverNewBalance = receiverWallet.getBalance().add(transferRequest.getAmount());

            // S7: Update sender balance
            senderWallet.setBalance(senderNewBalance);
            walletRepository.save(senderWallet);

            // S8: Update receiver balance
            receiverWallet.setBalance(receiverNewBalance);
            walletRepository.save(receiverWallet);

            log.info("Balances updated - sender: {} → {}, receiver: {} → {}",
                    senderWallet.getBalance().add(transferRequest.getAmount()),
                    senderNewBalance,
                    receiverWallet.getBalance().subtract(transferRequest.getAmount()),
                    receiverNewBalance);

            // S9: Generate group ID
            var groupId = UUID.randomUUID().toString();

            //  Generate idempotency key if not provided
            var idempotencyKey = transferRequest.getIdempotencyKey() != null
                    ? transferRequest.getIdempotencyKey()
                    : UUID.randomUUID().toString();

            // S10: Save sender transaction
            var senderTransaction = Transaction.builder()
                    .wallet(senderWallet)
                    .type(TransactionType.TRANSFER)
                    .amount(transferRequest.getAmount())
                    .description("Transfer to " + transferRequest.getReceiverId())
                    .relatedWallet(receiverWallet)
                    .groupId(groupId)
                    .idempotencyKey(idempotencyKey)
                    .build();
            transactionRepository.save(senderTransaction);

            // S11: Save receiver transaction
            var receiverTransaction = Transaction.builder()
                    .wallet(receiverWallet)
                    .type(TransactionType.TRANSFER)
                    .amount(transferRequest.getAmount())
                    .description("Transfer from " + senderId)
                    .relatedWallet(senderWallet)
                    .groupId(groupId)
                    .idempotencyKey(idempotencyKey)
                    .build();
            transactionRepository.save(receiverTransaction);

            log.info("Transfer completed successfully - groupId: {}, from: {}, to: {}, amount: {}",
                    groupId, senderId, transferRequest.getReceiverId(), transferRequest.getAmount());

        } catch (AppException e) {
            log.error("Transfer failed - error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Transfer failed - unexpected error: {}", e.getMessage(), e);
            throw new AppException(
                    ErrorCode.BAD_REQUEST
            );
        }
    }
}