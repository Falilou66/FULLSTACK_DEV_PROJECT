package sn.samabank.transaction.dto;

import sn.samabank.transaction.entity.Transaction;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionResponse(
    UUID id,
    UUID correlationId,
    String type,
    String status,
    UUID sourceAccountId,
    UUID targetAccountId,
    BigDecimal amount,
    String currency,
    String description,
    UUID executedBy,
    String channel,
    Instant executedAt
) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
            t.getId(), t.getCorrelationId(), t.getType().name(), t.getStatus().name(),
            t.getSourceAccountId(), t.getTargetAccountId(), t.getAmount(), t.getCurrency(),
            t.getDescription(), t.getExecutedBy(), t.getChannel(), t.getExecutedAt()
        );
    }
}
