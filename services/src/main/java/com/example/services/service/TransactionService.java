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
        log.info("transfer from {} to {}", senderId, transferRequest);
        if (senderId.equals(transferRequest.getReceiverId())) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
        var firstId = senderId.compareTo(transferRequest.getReceiverId()) < 0
                ? senderId
                : transferRequest.getReceiverId();

        var secondId = senderId.compareTo(transferRequest.getReceiverId()) < 0
                ? transferRequest.getReceiverId()
                : senderId;

        var wallet1 = walletRepository.findById(firstId).orElseThrow(
                () -> new AppException(ErrorCode.WALLET_NOT_FOUND)
        );

        var wallet2 = walletRepository.findById(secondId).orElseThrow(
                () -> new AppException(ErrorCode.WALLET_NOT_FOUND)
        );

        var senderWallet = senderId.equals(wallet1.getUser().getId()) ? wallet1 : wallet2;

        var receiverWallet = senderId.equals(wallet2.getUser().getId()) ? wallet2 : wallet1;

        if (senderWallet.getBalance().compareTo(transferRequest.getAmount()) < 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_BALANCE);
        }

        var newSenderBalance = senderWallet.getBalance().subtract(transferRequest.getAmount());

        var newReceiverBalance = receiverWallet.getBalance().add(transferRequest.getAmount());

        walletRepository.save(wallet1);
        walletRepository.save(wallet2);

        log.info("Balances updated - sender: {}, receiver: {}", newSenderBalance, newReceiverBalance);

        var groupId = UUID.randomUUID().toString();

        var senderTransaction = Transaction.builder()
                .wallet(senderWallet)
                .type(TransactionType.TRANSFER)
                .amount(transferRequest.getAmount())
                .description("Transfer to "+ transferRequest.getReceiverId())
                .relatedWallet(receiverWallet)
                .groundId(groupId)
                .build();
        transactionRepository.save(senderTransaction);

        var receiverTransaction = Transaction.builder()
                .wallet(receiverWallet)
                .type(TransactionType.TRANSFER)
                .amount(transferRequest.getAmount())
                .description("Transfer from "+ senderId)
                .relatedWallet(senderWallet)
                .groundId(groupId)
                .build();
        transactionRepository.save(receiverTransaction);
        log.info("Transfer completed - groupId: {}, from: {}, to: {}, amount: {}",
                groupId, senderId, transferRequest.getReceiverId(), transferRequest.getAmount());


    }
}
