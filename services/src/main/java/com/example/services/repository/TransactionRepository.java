package com.example.services.repository;

import com.example.services.entity.Transaction;
import com.example.services.entity.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    Page<Transaction> findByWalletId(UUID userId, Pageable pageable);

    Page<Transaction> findByGroupId(String groupId, Pageable pageable);


    @Query("SELECT t FROM Transaction t WHERE t.wallet.user.id = :userId AND t.type = :type ORDER BY t.createdAt DESC")
    Page<Transaction> findByUserAndType(@Param("userId") UUID userId, @Param("type") TransactionType type, Pageable pageable);


}
