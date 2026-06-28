package sn.samabank.transaction.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.samabank.transaction.client.AccountServiceClient;
import sn.samabank.transaction.client.AuditServiceClient;
import sn.samabank.transaction.client.CustomerServiceClient;
import sn.samabank.transaction.client.NotificationServiceClient;
import sn.samabank.transaction.client.dto.*;
import sn.samabank.transaction.dto.*;
import sn.samabank.transaction.entity.Transaction;
import sn.samabank.transaction.repository.TransactionRepository;
import sn.samabank.transaction.shared.BusinessException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final CustomerServiceClient customerServiceClient;
    private final AuditServiceClient auditServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    public TransactionService(TransactionRepository transactionRepository,
                              AccountServiceClient accountServiceClient,
                              CustomerServiceClient customerServiceClient,
                              AuditServiceClient auditServiceClient,
                              NotificationServiceClient notificationServiceClient) {
        this.transactionRepository = transactionRepository;
        this.accountServiceClient = accountServiceClient;
        this.customerServiceClient = customerServiceClient;
        this.auditServiceClient = auditServiceClient;
        this.notificationServiceClient = notificationServiceClient;
    }

    @Transactional
    public TransactionResponse deposit(DepositRequest request, UUID executedBy, String actorRole,
                                       String executorUsername, String channel, String ipAddress) {
        AccountResponse target;
        try {
            target = accountServiceClient.credit(request.getTargetAccountId(),
                new AmountBody(request.getAmount()));
        } catch (Exception e) {
            throw new BusinessException("ACCOUNT_NOT_FOUND", "Compte cible introuvable ou inactif", HttpStatus.NOT_FOUND);
        }

        Transaction tx = Transaction.createDeposit(request.getTargetAccountId(), request.getAmount(),
            request.getDescription(), executedBy, channel);
        transactionRepository.save(tx);

        log.info("[TX] Depot — compte: {} montant: {} XOF by: {} ({})",
            target.accountNumber(), request.getAmount(), executorUsername, executedBy);

        try {
            auditServiceClient.createAuditEvent(new CreateAuditEventRequest(
                "DEPOSIT_COMPLETED", executedBy, actorRole, "Transaction", tx.getId(),
                null, ipAddress, channel,
                Map.of("amount", request.getAmount().toString(),
                       "targetAccount", target.accountNumber(),
                       "executedByUsername", executorUsername)
            ));
        } catch (Exception e) {
            log.warn("[TX] Audit call failed: {}", e.getMessage());
        }

        try {
            CustomerResponse customer = customerServiceClient.getById(target.customerId());
            notificationServiceClient.sendEmail(new SendEmailRequest(
                customer.email(), customer.firstName(),
                "DEPOSIT_CONFIRMATION",
                "Confirmation de depot — " + request.getAmount().toPlainString() + " XOF",
                buildDepositHtml(customer.firstName(), target.accountNumber(), request.getAmount())
            ));
        } catch (Exception e) {
            log.warn("[TX] Notification call failed: {}", e.getMessage());
        }

        return TransactionResponse.from(tx);
    }

    @Transactional
    public TransactionResponse withdraw(WithdrawalRequest request, UUID executedBy, String actorRole,
                                        String executorUsername, String channel, String ipAddress) {
        AccountResponse source;
        try {
            source = accountServiceClient.debit(request.getSourceAccountId(),
                new AmountBody(request.getAmount()));
        } catch (Exception e) {
            throw new BusinessException("INSUFFICIENT_BALANCE_OR_NOT_FOUND",
                "Solde insuffisant ou compte introuvable", HttpStatus.CONFLICT);
        }

        Transaction tx = Transaction.createWithdrawal(request.getSourceAccountId(), request.getAmount(),
            request.getDescription(), executedBy, channel);
        transactionRepository.save(tx);

        log.info("[TX] Retrait — compte: {} montant: {} XOF by: {} ({})",
            source.accountNumber(), request.getAmount(), executorUsername, executedBy);

        try {
            auditServiceClient.createAuditEvent(new CreateAuditEventRequest(
                "WITHDRAWAL_COMPLETED", executedBy, actorRole, "Transaction", tx.getId(),
                null, ipAddress, channel,
                Map.of("amount", request.getAmount().toString(),
                       "sourceAccount", source.accountNumber(),
                       "executedByUsername", executorUsername)
            ));
        } catch (Exception e) {
            log.warn("[TX] Audit call failed: {}", e.getMessage());
        }

        try {
            CustomerResponse customer = customerServiceClient.getById(source.customerId());
            notificationServiceClient.sendEmail(new SendEmailRequest(
                customer.email(), customer.firstName(),
                "WITHDRAWAL_CONFIRMATION",
                "Confirmation de retrait — " + request.getAmount().toPlainString() + " XOF",
                buildWithdrawalHtml(customer.firstName(), source.accountNumber(), request.getAmount())
            ));
        } catch (Exception e) {
            log.warn("[TX] Notification call failed: {}", e.getMessage());
        }

        return TransactionResponse.from(tx);
    }

    @Transactional
    public TransactionResponse transfer(TransferRequest request, UUID executedBy, String actorRole,
                                        String executorUsername, String channel, String ipAddress) {
        if (request.getSourceAccountId().equals(request.getTargetAccountId())) {
            throw new BusinessException("SAME_ACCOUNT_TRANSFER",
                "Le compte source et le compte cible doivent etre differents", HttpStatus.UNPROCESSABLE_ENTITY);
        }

        AccountResponse source;
        AccountResponse target;
        try {
            source = accountServiceClient.debit(request.getSourceAccountId(), new AmountBody(request.getAmount()));
            target = accountServiceClient.credit(request.getTargetAccountId(), new AmountBody(request.getAmount()));
        } catch (Exception e) {
            throw new BusinessException("TRANSFER_FAILED", "Virement echoue: " + e.getMessage(), HttpStatus.CONFLICT);
        }

        Transaction tx = Transaction.createTransfer(request.getSourceAccountId(), request.getTargetAccountId(),
            request.getAmount(), request.getDescription(), executedBy, channel);
        transactionRepository.save(tx);

        log.info("[TX] Virement — de: {} vers: {} montant: {} XOF by: {} ({})",
            source.accountNumber(), target.accountNumber(), request.getAmount(), executorUsername, executedBy);

        try {
            auditServiceClient.createAuditEvent(new CreateAuditEventRequest(
                "TRANSFER_COMPLETED", executedBy, actorRole, "Transaction", tx.getId(),
                null, ipAddress, channel,
                Map.of("amount", request.getAmount().toString(),
                       "sourceAccount", source.accountNumber(),
                       "targetAccount", target.accountNumber(),
                       "executedByUsername", executorUsername)
            ));
        } catch (Exception e) {
            log.warn("[TX] Audit call failed: {}", e.getMessage());
        }

        try {
            CustomerResponse customer = customerServiceClient.getById(source.customerId());
            notificationServiceClient.sendEmail(new SendEmailRequest(
                customer.email(), customer.firstName(),
                "TRANSFER_CONFIRMATION",
                "Confirmation de virement — " + request.getAmount().toPlainString() + " XOF",
                buildTransferHtml(customer.firstName(), source.accountNumber(), target.accountNumber(), request.getAmount())
            ));
        } catch (Exception e) {
            log.warn("[TX] Notification call failed: {}", e.getMessage());
        }

        return TransactionResponse.from(tx);
    }

    public Page<TransactionResponse> getHistory(UUID accountId, Instant from, Instant to, Pageable pageable) {
        if (from != null && to != null) {
            return transactionRepository.findByAccountIdAndPeriod(accountId, from, to, pageable).map(TransactionResponse::from);
        }
        return transactionRepository.findByAccountId(accountId, pageable).map(TransactionResponse::from);
    }

    public TransactionResponse getById(UUID transactionId) {
        return transactionRepository.findById(transactionId)
            .map(TransactionResponse::from)
            .orElseThrow(() -> BusinessException.notFound("Transaction", transactionId));
    }

    public Page<TransactionResponse> getMyAccountTransactions(UUID userId, Instant from, Instant to, Pageable pageable) {
        CustomerResponse customer;
        try {
            customer = customerServiceClient.getByUserId(userId);
        } catch (Exception e) {
            throw BusinessException.notFound("Profil client", userId);
        }
        List<AccountResponse> accounts = accountServiceClient.getByCustomerId(customer.id());
        if (accounts.isEmpty()) return Page.empty(pageable);
        List<UUID> accountIds = accounts.stream().map(AccountResponse::id).toList();
        if (from != null && to != null) {
            return transactionRepository.findByAccountIdsAndPeriod(accountIds, from, to, pageable).map(TransactionResponse::from);
        }
        return transactionRepository.findByAccountIds(accountIds, pageable).map(TransactionResponse::from);
    }

    public Page<TransactionResponse> getAllTransactions(Instant from, Instant to, Pageable pageable) {
        return transactionRepository.findAllTransactions(pageable).map(TransactionResponse::from);
    }

    public Page<TransactionResponse> getMyTransactions(UUID executedBy, Instant from, Instant to, Pageable pageable) {
        if (from != null && to != null) {
            return transactionRepository.findByExecutedByAndPeriod(executedBy, from, to, pageable).map(TransactionResponse::from);
        }
        return transactionRepository.findByExecutedBy(executedBy, pageable).map(TransactionResponse::from);
    }

    private String buildDepositHtml(String name, String account, BigDecimal amount) {
        return "<html><body><p>Bonjour <b>" + name + "</b>, depot de <b>+" + amount.toPlainString() + " XOF</b> sur le compte " + account + ".</p></body></html>";
    }

    private String buildWithdrawalHtml(String name, String account, BigDecimal amount) {
        return "<html><body><p>Bonjour <b>" + name + "</b>, retrait de <b>-" + amount.toPlainString() + " XOF</b> du compte " + account + ".</p></body></html>";
    }

    private String buildTransferHtml(String name, String source, String target, BigDecimal amount) {
        return "<html><body><p>Bonjour <b>" + name + "</b>, virement de <b>" + amount.toPlainString() + " XOF</b> de " + source + " vers " + target + ".</p></body></html>";
    }
}
