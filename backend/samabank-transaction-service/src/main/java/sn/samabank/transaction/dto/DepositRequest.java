package sn.samabank.transaction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public class DepositRequest {
    @NotNull private UUID targetAccountId;
    @NotNull @DecimalMin("0.01") private BigDecimal amount;
    private String description;

    public UUID getTargetAccountId() { return targetAccountId; }
    public void setTargetAccountId(UUID v) { this.targetAccountId = v; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal v) { this.amount = v; }
    public String getDescription() { return description; }
    public void setDescription(String v) { this.description = v; }
}
