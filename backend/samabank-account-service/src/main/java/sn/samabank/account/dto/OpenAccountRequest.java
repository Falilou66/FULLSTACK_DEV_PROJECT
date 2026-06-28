package sn.samabank.account.dto;

import jakarta.validation.constraints.NotNull;
import sn.samabank.account.entity.AccountType;

import java.util.UUID;

public class OpenAccountRequest {

    @NotNull(message = "L'identifiant du client est obligatoire")
    private UUID customerId;

    @NotNull(message = "Le type de compte est obligatoire")
    private AccountType type;

    public UUID getCustomerId()        { return customerId; }
    public void setCustomerId(UUID v)  { this.customerId = v; }
    public AccountType getType()       { return type; }
    public void setType(AccountType v) { this.type = v; }
}
