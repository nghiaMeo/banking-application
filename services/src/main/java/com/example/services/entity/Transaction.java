package com.example.services.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(
        name = "transaction",
        indexes = {
                // Speed up wallet transaction pagination & sorting
                @Index(name = "idx_transaction_wallet_created_at", columnList = "wallet_id, created_at"),
                @Index(name = "idx_transaction_created_at", columnList = "created_at"),
                // Speed up transfer linkage queries (optional, helps analytics)
                @Index(name = "idx_transaction_related_wallet_id", columnList = "related_wallet_id"),
                @Index(name = "idx_transaction_group_id", columnList = "group_id"),
                // Already unique but keeping an explicit index name helps readability/DB tools
                @Index(name = "idx_transaction_idempotency_key", columnList = "idempotency_key")
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_wallet_id")
    private Wallet relatedWallet;

    @Column(name = "group_id", length = 36)
    private String groupId;

    @Column(name = "idempotency_key", length = 36, unique = true)
    private String idempotencyKey;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private String description;

}
