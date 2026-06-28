package sn.samabank.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class WithdrawalRequest {
    @NotNull private UUID sourceAccountId;
    @NotNull @DecimalMin("0.01") private BigDecimal amount;
    private String description;

    public UUID getSourceAccountId() { return sourceAccountId; }
    public void setSourceAccountId(UUID v) { this.sourceAccountId = v; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal v) { this.amount = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
}
