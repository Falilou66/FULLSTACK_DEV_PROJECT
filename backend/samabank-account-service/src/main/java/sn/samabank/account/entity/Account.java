package sn.samabank.account.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.http.HttpStatus;
import sn.samabank.account.shared.BusinessException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_number", nullable = false, unique = true, length = 25)
    private String accountNumber;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "opened_at", nullable = false)
    private Instant openedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    @Column(nullable = false)
    private int version;

    protected Account() {}

    public static Account create(UUID customerId, String accountNumber, AccountType type) {
        Account a = new Account();
        a.customerId    = customerId;
        a.accountNumber = accountNumber;
        a.type          = type;
        a.status        = AccountStatus.ACTIVE;
        a.balance       = BigDecimal.ZERO;
        a.currency      = "XOF";
        a.openedAt      = Instant.now();
        return a;
    }

    public void debit(BigDecimal amount) {
        if (this.status != AccountStatus.ACTIVE) {
            throw new BusinessException("ACCOUNT_NOT_ACTIVE", "Le compte n'est pas actif",
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_AMOUNT", "Le montant doit être positif",
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new BusinessException("INSUFFICIENT_BALANCE", "Solde insuffisant pour cette opération",
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Solde disponible : " + this.balance + " XOF, Montant demandé : " + amount + " XOF");
        }
        this.balance = this.balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        if (this.status != AccountStatus.ACTIVE) {
            throw new BusinessException("ACCOUNT_NOT_ACTIVE", "Le compte n'est pas actif",
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("INVALID_AMOUNT", "Le montant doit être positif",
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
        this.balance = this.balance.add(amount);
    }

    public void suspend() {
        if (this.status == AccountStatus.CLOSED) {
            throw new BusinessException("ACCOUNT_ALREADY_CLOSED",
                    "Impossible de suspendre un compte clôturé", HttpStatus.CONFLICT);
        }
        this.status = AccountStatus.SUSPENDED;
    }

    public void reactivate() {
        if (this.status == AccountStatus.CLOSED) {
            throw new BusinessException("ACCOUNT_ALREADY_CLOSED",
                    "Impossible de réactiver un compte clôturé", HttpStatus.CONFLICT);
        }
        this.status = AccountStatus.ACTIVE;
    }

    public boolean isActive() { return this.status == AccountStatus.ACTIVE; }

    public UUID getId()              { return id; }
    public String getAccountNumber() { return accountNumber; }
    public UUID getCustomerId()      { return customerId; }
    public AccountType getType()     { return type; }
    public AccountStatus getStatus() { return status; }
    public BigDecimal getBalance()   { return balance; }
    public String getCurrency()      { return currency; }
    public Instant getOpenedAt()     { return openedAt; }
    public Instant getClosedAt()     { return closedAt; }
    public Instant getCreatedAt()    { return createdAt; }
    public Instant getUpdatedAt()    { return updatedAt; }
    public int getVersion()          { return version; }
}
