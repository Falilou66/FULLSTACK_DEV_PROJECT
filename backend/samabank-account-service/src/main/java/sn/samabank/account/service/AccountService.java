package sn.samabank.account.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.samabank.account.client.AuditServiceClient;
import sn.samabank.account.client.CustomerServiceClient;
import sn.samabank.account.client.NotificationServiceClient;
import sn.samabank.account.client.dto.CreateAuditEventRequest;
import sn.samabank.account.client.dto.CustomerResponse;
import sn.samabank.account.client.dto.SendEmailRequest;
import sn.samabank.account.dto.AccountResponse;
import sn.samabank.account.dto.OpenAccountRequest;
import sn.samabank.account.entity.Account;
import sn.samabank.account.entity.AccountStatus;
import sn.samabank.account.entity.AccountType;
import sn.samabank.account.repository.AccountRepository;
import sn.samabank.account.shared.BusinessException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final CustomerServiceClient customerServiceClient;
    private final AuditServiceClient auditServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    public AccountService(AccountRepository accountRepository,
                          CustomerServiceClient customerServiceClient,
                          AuditServiceClient auditServiceClient,
                          NotificationServiceClient notificationServiceClient) {
        this.accountRepository       = accountRepository;
        this.customerServiceClient   = customerServiceClient;
        this.auditServiceClient      = auditServiceClient;
        this.notificationServiceClient = notificationServiceClient;
    }

    @Transactional
    public AccountResponse open(OpenAccountRequest request,
                                UUID openedBy,
                                String actorRole,
                                String executorUsername,
                                String channel,
                                String ipAddress) {

        CustomerResponse customer;
        try {
            customer = customerServiceClient.getCustomerById(request.getCustomerId());
        } catch (Exception e) {
            throw BusinessException.notFound("Client", request.getCustomerId());
        }

        if (!"ACTIVE".equals(customer.status())) {
            throw new BusinessException("CUSTOMER_NOT_ACTIVE",
                    "Impossible d'ouvrir un compte pour un client inactif", HttpStatus.CONFLICT);
        }

        accountRepository.findByCustomerIdAndTypeAndStatus(
                        request.getCustomerId(), request.getType(), AccountStatus.ACTIVE)
                .ifPresent(existing -> {
                    throw new BusinessException("ACCOUNT_ALREADY_EXISTS",
                            "Ce client possède déjà un compte " + request.getType().name() + " actif",
                            HttpStatus.CONFLICT,
                            "Compte existant : " + existing.getAccountNumber());
                });

        String accountNumber = generateAccountNumber();
        Account account = Account.create(request.getCustomerId(), accountNumber, request.getType());
        accountRepository.save(account);

        log.info("[ACCOUNT] Ouvert — number: {} customer: {} type: {} by: {} ({})",
                accountNumber, request.getCustomerId(), request.getType(), executorUsername, openedBy);

        try {
            auditServiceClient.createAuditEvent(new CreateAuditEventRequest(
                    "ACCOUNT_OPENED", openedBy, actorRole, "Account", account.getId(),
                    null, ipAddress, channel,
                    Map.of(
                            "accountNumber", accountNumber,
                            "type", account.getType().name(),
                            "customerId", request.getCustomerId().toString(),
                            "customerName", customer.firstName() + " " + customer.lastName(),
                            "executedByUsername", executorUsername
                    )
            ));
        } catch (Exception e) {
            log.warn("[ACCOUNT] Audit call failed: {}", e.getMessage());
        }

        try {
            notificationServiceClient.sendEmail(new SendEmailRequest(
                    customer.email(),
                    customer.firstName(),
                    "ACCOUNT_OPENED",
                    "Votre compte SamaBank est ouvert",
                    buildAccountOpenedHtml(customer.firstName(), accountNumber, account.getType().name())
            ));
        } catch (Exception e) {
            log.warn("[ACCOUNT] Notification call failed: {}", e.getMessage());
        }

        return AccountResponse.from(account);
    }

    public Page<AccountResponse> getAll(AccountStatus status, AccountType type, Pageable pageable) {
        return accountRepository.findAllWithFilters(status, type, pageable).map(AccountResponse::from);
    }

    public List<AccountResponse> getByCustomerId(UUID customerId) {
        return accountRepository.findAllByCustomerId(customerId).stream()
                .map(AccountResponse::from).toList();
    }

    public List<AccountResponse> getMyAccounts(UUID userId) {
        try {
            var customer = customerServiceClient.getCustomerByUserId(userId);
            return accountRepository.findAllByCustomerId(customer.id())
                    .stream().map(AccountResponse::from).toList();
        } catch (Exception e) {
            log.warn("[ACCOUNT] getMyAccounts — customer lookup failed for userId {}: {}", userId, e.getMessage());
            return List.of();
        }
    }

    public AccountResponse getById(UUID accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> BusinessException.notFound("Compte", accountId));
        return AccountResponse.from(account);
    }

    public long countAll() {
        return accountRepository.count();
    }

    public long countActive() {
        return accountRepository.countByStatus(AccountStatus.ACTIVE);
    }

    @Transactional
    public AccountResponse debit(UUID accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> BusinessException.notFound("Compte", accountId));
        account.debit(amount);
        accountRepository.save(account);
        return AccountResponse.from(account);
    }

    @Transactional
    public AccountResponse credit(UUID accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> BusinessException.notFound("Compte", accountId));
        account.credit(amount);
        accountRepository.save(account);
        return AccountResponse.from(account);
    }

    @Transactional
    public AccountResponse suspend(UUID accountId, UUID suspendedBy, String actorRole,
                                   String executorUsername, String channel, String ipAddress) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> BusinessException.notFound("Compte", accountId));

        account.suspend();
        accountRepository.save(account);

        log.info("[ACCOUNT] Suspendu — id: {} by: {} ({})", accountId, executorUsername, suspendedBy);

        try {
            auditServiceClient.createAuditEvent(new CreateAuditEventRequest(
                    "ACCOUNT_SUSPENDED", suspendedBy, actorRole, "Account", account.getId(),
                    null, ipAddress, channel,
                    Map.of("accountNumber", account.getAccountNumber(),
                            "previousStatus", "ACTIVE",
                            "customerId", account.getCustomerId().toString(),
                            "executedByUsername", executorUsername)
            ));
        } catch (Exception e) {
            log.warn("[ACCOUNT] Audit call failed: {}", e.getMessage());
        }

        return AccountResponse.from(account);
    }

    @Transactional
    public AccountResponse reactivate(UUID accountId, UUID reactivatedBy, String actorRole,
                                      String executorUsername, String channel, String ipAddress) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> BusinessException.notFound("Compte", accountId));

        account.reactivate();
        accountRepository.save(account);

        log.info("[ACCOUNT] Réactivé — id: {} by: {} ({})", accountId, executorUsername, reactivatedBy);

        try {
            auditServiceClient.createAuditEvent(new CreateAuditEventRequest(
                    "ACCOUNT_REACTIVATED", reactivatedBy, actorRole, "Account", account.getId(),
                    null, ipAddress, channel,
                    Map.of("accountNumber", account.getAccountNumber(),
                            "previousStatus", "SUSPENDED",
                            "customerId", account.getCustomerId().toString(),
                            "executedByUsername", executorUsername)
            ));
        } catch (Exception e) {
            log.warn("[ACCOUNT] Audit call failed: {}", e.getMessage());
        }

        return AccountResponse.from(account);
    }

    private String generateAccountNumber() {
        long count = accountRepository.count() + 1;
        int year = java.time.LocalDate.now().getYear();
        return String.format("SB-%d-%010d", year, count);
    }

    private String buildAccountOpenedHtml(String firstName, String accountNumber, String type) {
        String typeLabel = "SAVINGS".equals(type) ? "Épargne" : "Courant";
        return """
            <html><body style="font-family:Arial,sans-serif">
            <p>Bonjour <strong>%s</strong>,</p>
            <p>Votre nouveau compte bancaire est ouvert.</p>
            <p><strong>Numéro :</strong> %s | <strong>Type :</strong> %s</p>
            </body></html>
            """.formatted(firstName, accountNumber, typeLabel);
    }
}
