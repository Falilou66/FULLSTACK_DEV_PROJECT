package sn.samabank.account.dto;

import sn.samabank.account.entity.Account;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class AccountResponse {

    private final UUID       id;
    private final String     accountNumber;
    private final UUID       customerId;
    private final String     type;
    private final String     status;
    private final BigDecimal balance;
    private final String     currency;
    private final Instant    openedAt;

    private AccountResponse(Builder b) {
        this.id            = b.id;
        this.accountNumber = b.accountNumber;
        this.customerId    = b.customerId;
        this.type          = b.type;
        this.status        = b.status;
        this.balance       = b.balance;
        this.currency      = b.currency;
        this.openedAt      = b.openedAt;
    }

    public static AccountResponse from(Account a) {
        return new Builder()
                .id(a.getId())
                .accountNumber(a.getAccountNumber())
                .customerId(a.getCustomerId())
                .type(a.getType().name())
                .status(a.getStatus().name())
                .balance(a.getBalance())
                .currency(a.getCurrency())
                .openedAt(a.getOpenedAt())
                .build();
    }

    public UUID getId()              { return id; }
    public String getAccountNumber() { return accountNumber; }
    public UUID getCustomerId()      { return customerId; }
    public String getType()          { return type; }
    public String getStatus()        { return status; }
    public BigDecimal getBalance()   { return balance; }
    public String getCurrency()      { return currency; }
    public Instant getOpenedAt()     { return openedAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private UUID id;
        private String accountNumber;
        private UUID customerId;
        private String type;
        private String status;
        private BigDecimal balance;
        private String currency;
        private Instant openedAt;

        public Builder id(UUID v)              { this.id = v; return this; }
        public Builder accountNumber(String v) { this.accountNumber = v; return this; }
        public Builder customerId(UUID v)      { this.customerId = v; return this; }
        public Builder type(String v)          { this.type = v; return this; }
        public Builder status(String v)        { this.status = v; return this; }
        public Builder balance(BigDecimal v)   { this.balance = v; return this; }
        public Builder currency(String v)      { this.currency = v; return this; }
        public Builder openedAt(Instant v)     { this.openedAt = v; return this; }
        public AccountResponse build()         { return new AccountResponse(this); }
    }
}
