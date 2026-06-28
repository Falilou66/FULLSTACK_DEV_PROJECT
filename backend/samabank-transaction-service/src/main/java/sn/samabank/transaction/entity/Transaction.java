package sn.samabank.transaction.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "correlation_id", nullable = false, unique = true)
    private UUID correlationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status;

    @Column(name = "source_account_id")
    private UUID sourceAccountId;

    @Column(name = "target_account_id")
    private UUID targetAccountId;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(length = 500)
    private String description;

    @Column(name = "executed_by", nullable = false)
    private UUID executedBy;

    @Column(nullable = false, length = 20)
    private String channel;

    @Column(name = "executed_at", nullable = false)
    private Instant executedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Transaction() {}

    public static Transaction createDeposit(UUID targetAccountId, BigDecimal amount, String description, UUID executedBy, String channel) {
        Transaction t = new Transaction();
        t.correlationId = UUID.randomUUID();
        t.type = TransactionType.DEPOSIT;
        t.status = TransactionStatus.COMPLETED;
        t.targetAccountId = targetAccountId;
        t.amount = amount;
        t.currency = "XOF";
        t.description = description;
        t.executedBy = executedBy;
        t.channel = channel;
        t.executedAt = Instant.now();
        return t;
    }

    public static Transaction createWithdrawal(UUID sourceAccountId, BigDecimal amount, String description, UUID executedBy, String channel) {
        Transaction t = new Transaction();
        t.correlationId = UUID.randomUUID();
        t.type = TransactionType.WITHDRAWAL;
        t.status = TransactionStatus.COMPLETED;
        t.sourceAccountId = sourceAccountId;
        t.amount = amount;
        t.currency = "XOF";
        t.description = description;
        t.executedBy = executedBy;
        t.channel = channel;
        t.executedAt = Instant.now();
        return t;
    }

    public static Transaction createTransfer(UUID sourceAccountId, UUID targetAccountId, BigDecimal amount, String description, UUID executedBy, String channel) {
        Transaction t = new Transaction();
        t.correlationId = UUID.randomUUID();
        t.type = TransactionType.TRANSFER;
        t.status = TransactionStatus.COMPLETED;
        t.sourceAccountId = sourceAccountId;
        t.targetAccountId = targetAccountId;
        t.amount = amount;
        t.currency = "XOF";
        t.description = description;
        t.executedBy = executedBy;
        t.channel = channel;
        t.executedAt = Instant.now();
        return t;
    }

    public UUID getId() { return id; }
    public UUID getCorrelationId() { return correlationId; }
    public TransactionType getType() { return type; }
    public TransactionStatus getStatus() { return status; }
    public UUID getSourceAccountId() { return sourceAccountId; }
    public UUID getTargetAccountId() { return targetAccountId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getDescription() { return description; }
    public UUID getExecutedBy() { return executedBy; }
    public String getChannel() { return channel; }
    public Instant getExecutedAt() { return executedAt; }
    public Instant getCreatedAt() { return createdAt; }
}
