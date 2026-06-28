package sn.samabank.account.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class AmountRequest {

    @NotNull(message = "Le montant est obligatoire")
    @DecimalMin(value = "0.01", message = "Le montant doit être positif")
    private BigDecimal amount;

    public BigDecimal getAmount()         { return amount; }
    public void setAmount(BigDecimal v)   { this.amount = v; }
}
