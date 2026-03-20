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
            log.info("Transfer initiated - from: {}, to: {}, amount: {}",
                    senderId, transferRequest.getReceiverId(), transferRequest.getAmount());

            // S1: Validate sender != receiver
            if (senderId.equals(transferRequest.getReceiverId())) {
                throw new AppException(
                        ErrorCode.BAD_REQUEST
                );
            }

            // S2: Get sender wallet directly by senderId
            var senderWallet = walletRepository.findByUserIdForUpdate(senderId)
                    .orElseThrow(() -> new AppException(
                            ErrorCode.WALLET_NOT_FOUND
                    ));

            log.debug("Sender wallet locked - userId: {}, balance: {}",
                    senderId, senderWallet.getBalance());

            // S3: Get receiver wallet directly by receiverId
            var receiverWallet = walletRepository.findByUserIdForUpdate(transferRequest.getReceiverId())
                    .orElseThrow(() -> new AppException(
                            ErrorCode.WALLET_NOT_FOUND
                    ));

            log.debug("Receiver wallet locked - userId: {}, balance: {}",
                    transferRequest.getReceiverId(), receiverWallet.getBalance());

            // S4: Check sender balance
            if (senderWallet.getBalance().compareTo(transferRequest.getAmount()) < 0) {
                log.warn("Insufficient balance - sender: {}, required: {}, current: {}",
                        senderId, transferRequest.getAmount(), senderWallet.getBalance());
                throw new AppException(
                        ErrorCode.INSUFFICIENT_BALANCE
                );
            }

            // S5: Calculate new balances
            var senderNewBalance = senderWallet.getBalance().subtract(transferRequest.getAmount());
            var receiverNewBalance = receiverWallet.getBalance().add(transferRequest.getAmount());

            log.debug("Calculated balances - sender: {} -> {}, receiver: {} -> {}",
                    senderWallet.getBalance(), senderNewBalance,
                    receiverWallet.getBalance(), receiverNewBalance);

            // S6: Update sender balance
            senderWallet.setBalance(senderNewBalance);
            walletRepository.save(senderWallet);

            log.info("Sender debited - userId: {}, amount: {}, newBalance: {}",
                    senderId, transferRequest.getAmount(), senderNewBalance);

            // S7: Update receiver balance
            receiverWallet.setBalance(receiverNewBalance);
            walletRepository.save(receiverWallet);

            log.info("Receiver credited - userId: {}, amount: {}, newBalance: {}",
                    transferRequest.getReceiverId(), transferRequest.getAmount(), receiverNewBalance);

            // S8: Generate group ID
            var groupId = UUID.randomUUID().toString();

            // S9: Save sender transaction (TRANSFER OUT)
            var senderTransaction = Transaction.builder()
                    .wallet(senderWallet)
                    .type(TransactionType.TRANSFER)
                    .amount(transferRequest.getAmount())
                    .description("Transfer to " + transferRequest.getReceiverId())
                    .relatedWallet(receiverWallet)
                    .groupId(groupId)
                    .build();
            transactionRepository.save(senderTransaction);

            // ✅ S10: Save receiver transaction (TRANSFER IN)
            var receiverTransaction = Transaction.builder()
                    .wallet(receiverWallet)
                    .type(TransactionType.TRANSFER)
                    .amount(transferRequest.getAmount())
                    .description("Transfer from " + senderId)
                    .relatedWallet(senderWallet)
                    .groupId(groupId)
                    .build();
            transactionRepository.save(receiverTransaction);

            log.info("Transfer completed successfully - groupId: {}, from: {}, to: {}, amount: {}",
                    groupId, senderId, transferRequest.getReceiverId(), transferRequest.getAmount());

        } catch (AppException e) {
            log.error("Transfer failed with AppException - from: {}, to: {}, error: {}",
                    senderId, transferRequest.getReceiverId(), e.getMessage());
        } catch (Exception e) {
            log.error("Transfer failed with unexpected error - from: {}, to: {}, error: {}",
                    senderId, transferRequest.getReceiverId(), e.getMessage(), e);
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

    }
}