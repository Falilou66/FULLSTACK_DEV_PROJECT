package sn.samabank.customer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sn.samabank.customer.client.AuditServiceClient;
import sn.samabank.customer.client.AuthServiceClient;
import sn.samabank.customer.client.NotificationServiceClient;
import sn.samabank.customer.client.dto.CreateAuditEventRequest;
import sn.samabank.customer.client.dto.SendEmailRequest;
import sn.samabank.customer.client.dto.UserCreationRequest;
import sn.samabank.customer.client.dto.UserResponse;
import sn.samabank.customer.client.dto.UserStatusUpdateRequest;
import sn.samabank.customer.dto.CreateCustomerRequest;
import sn.samabank.customer.dto.CustomerResponse;
import sn.samabank.customer.dto.UpdateCustomerRequest;
import sn.samabank.customer.entity.Customer;
import sn.samabank.customer.entity.CustomerStatus;
import sn.samabank.customer.repository.CustomerRepository;
import sn.samabank.customer.shared.BusinessException;

import java.util.Map;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    private final CustomerRepository customerRepository;
    private final AuthServiceClient authServiceClient;
    private final AuditServiceClient auditServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    public CustomerService(CustomerRepository customerRepository,
                           AuthServiceClient authServiceClient,
                           AuditServiceClient auditServiceClient,
                           NotificationServiceClient notificationServiceClient) {
        this.customerRepository      = customerRepository;
        this.authServiceClient       = authServiceClient;
        this.auditServiceClient      = auditServiceClient;
        this.notificationServiceClient = notificationServiceClient;
    }

    @Transactional
    public CustomerResponse create(CreateCustomerRequest request,
                                   UUID createdBy,
                                   String actorRole,
                                   String executorUsername,
                                   String channel,
                                   String ipAddress) {

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(
                    "EMAIL_ALREADY_EXISTS",
                    "Un client avec cet email existe déjà",
                    HttpStatus.CONFLICT
            );
        }

        // Create user in auth-service
        UserResponse userResponse = authServiceClient.createUser(new UserCreationRequest(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPassword(),
                "CUSTOMER"
        ));

        String customerNumber = generateCustomerNumber();
        Customer customer = Customer.create(
                userResponse.id(),
                customerNumber,
                request.getFirstName(),
                request.getLastName(),
                request.getDateOfBirth(),
                request.getEmail(),
                request.getPhone(),
                request.getAddress()
        );
        customerRepository.save(customer);

        log.info("[CUSTOMER] Créé — number: {} name: {} userId: {} by: {} ({})",
                customerNumber, customer.getFullName(), userResponse.id(), executorUsername, createdBy);

        try {
            auditServiceClient.createAuditEvent(new CreateAuditEventRequest(
                    "CUSTOMER_CREATED",
                    createdBy,
                    actorRole,
                    "Customer",
                    customer.getId(),
                    null,
                    ipAddress,
                    channel,
                    Map.of(
                            "customerNumber", customerNumber,
                            "fullName", customer.getFullName(),
                            "email", customer.getEmail(),
                            "userId", userResponse.id().toString(),
                            "executedByUsername", executorUsername
                    )
            ));
        } catch (Exception e) {
            log.warn("[CUSTOMER] Audit call failed: {}", e.getMessage());
        }

        try {
            notificationServiceClient.sendEmail(new SendEmailRequest(
                    customer.getEmail(),
                    customer.getFirstName(),
                    "WELCOME",
                    "Bienvenue chez SamaBank !",
                    buildWelcomeHtml(customer.getFirstName(), userResponse.username())
            ));
        } catch (Exception e) {
            log.warn("[CUSTOMER] Notification call failed: {}", e.getMessage());
        }

        return CustomerResponse.from(customer);
    }

    public CustomerResponse getMyProfile(UUID userId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.notFound("Profil client", userId));
        return CustomerResponse.from(customer);
    }

    public CustomerResponse getById(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> BusinessException.notFound("Client", customerId));
        return CustomerResponse.from(customer);
    }

    public CustomerResponse getByUserId(UUID userId) {
        Customer customer = customerRepository.findByUserId(userId)
                .orElseThrow(() -> BusinessException.notFound("Client par userId", userId));
        return CustomerResponse.from(customer);
    }

    public Page<CustomerResponse> getAll(CustomerStatus status, String search, Pageable pageable) {
        return customerRepository.findAllWithFilters(status, search, pageable)
                .map(CustomerResponse::from);
    }

    public long countAll() {
        return customerRepository.count();
    }

    public long countActive() {
        return customerRepository.findAllWithFilters(CustomerStatus.ACTIVE, null, Pageable.unpaged()).getTotalElements();
    }

    @Transactional
    public CustomerResponse update(UUID customerId,
                                   UpdateCustomerRequest request,
                                   UUID updatedBy,
                                   String actorRole,
                                   String executorUsername,
                                   String channel,
                                   String ipAddress) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> BusinessException.notFound("Client", customerId));

        String oldName    = customer.getFullName();
        String oldPhone   = customer.getPhone();
        String oldAddress = customer.getAddress();

        customer.update(
                request.getFirstName(),
                request.getLastName(),
                request.getPhone(),
                request.getAddress()
        );
        customerRepository.save(customer);

        log.info("[CUSTOMER] Modifié — id: {} by: {} ({})", customerId, executorUsername, updatedBy);

        try {
            auditServiceClient.createAuditEvent(new CreateAuditEventRequest(
                    "CUSTOMER_UPDATED",
                    updatedBy,
                    actorRole,
                    "Customer",
                    customer.getId(),
                    null,
                    ipAddress,
                    channel,
                    Map.of(
                            "oldName", oldName,
                            "newName", customer.getFullName(),
                            "oldPhone", oldPhone != null ? oldPhone : "",
                            "newPhone", customer.getPhone() != null ? customer.getPhone() : "",
                            "executedByUsername", executorUsername
                    )
            ));
        } catch (Exception e) {
            log.warn("[CUSTOMER] Audit call failed: {}", e.getMessage());
        }

        return CustomerResponse.from(customer);
    }

    @Transactional
    public CustomerResponse suspend(UUID customerId,
                                    UUID suspendedBy,
                                    String actorRole,
                                    String executorUsername,
                                    String channel,
                                    String ipAddress) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> BusinessException.notFound("Client", customerId));

        customer.suspend();
        customerRepository.save(customer);

        try {
            authServiceClient.updateUserStatus(customer.getUserId(),
                    new UserStatusUpdateRequest("SUSPENDED"));
        } catch (Exception e) {
            log.warn("[CUSTOMER] Auth status update failed: {}", e.getMessage());
        }

        log.info("[CUSTOMER] Suspendu — id: {} by: {} ({})", customerId, executorUsername, suspendedBy);

        try {
            auditServiceClient.createAuditEvent(new CreateAuditEventRequest(
                    "CUSTOMER_SUSPENDED",
                    suspendedBy,
                    actorRole,
                    "Customer",
                    customer.getId(),
                    null,
                    ipAddress,
                    channel,
                    Map.of(
                            "customerNumber", customer.getCustomerNumber(),
                            "fullName", customer.getFullName(),
                            "previousStatus", "ACTIVE",
                            "userId", customer.getUserId().toString(),
                            "executedByUsername", executorUsername
                    )
            ));
        } catch (Exception e) {
            log.warn("[CUSTOMER] Audit call failed: {}", e.getMessage());
        }

        return CustomerResponse.from(customer);
    }

    @Transactional
    public CustomerResponse reactivate(UUID customerId,
                                       UUID reactivatedBy,
                                       String actorRole,
                                       String executorUsername,
                                       String channel,
                                       String ipAddress) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> BusinessException.notFound("Client", customerId));

        customer.reactivate();
        customerRepository.save(customer);

        try {
            authServiceClient.updateUserStatus(customer.getUserId(),
                    new UserStatusUpdateRequest("ACTIVE"));
        } catch (Exception e) {
            log.warn("[CUSTOMER] Auth status update failed: {}", e.getMessage());
        }

        log.info("[CUSTOMER] Réactivé — id: {} by: {} ({})", customerId, executorUsername, reactivatedBy);

        try {
            auditServiceClient.createAuditEvent(new CreateAuditEventRequest(
                    "CUSTOMER_REACTIVATED",
                    reactivatedBy,
                    actorRole,
                    "Customer",
                    customer.getId(),
                    null,
                    ipAddress,
                    channel,
                    Map.of(
                            "customerNumber", customer.getCustomerNumber(),
                            "fullName", customer.getFullName(),
                            "previousStatus", "SUSPENDED",
                            "userId", customer.getUserId().toString(),
                            "executedByUsername", executorUsername
                    )
            ));
        } catch (Exception e) {
            log.warn("[CUSTOMER] Audit call failed: {}", e.getMessage());
        }

        return CustomerResponse.from(customer);
    }

    private String generateCustomerNumber() {
        long count = customerRepository.count() + 1;
        return String.format("CUST-%010d", count);
    }

    private String buildWelcomeHtml(String firstName, String username) {
        return """
            <html><body style="font-family:Arial,sans-serif;max-width:600px;margin:auto;padding:20px">
              <div style="background:#1D4ED8;padding:20px;border-radius:8px 8px 0 0">
                <h1 style="color:white;margin:0">SamaBank</h1>
              </div>
              <div style="border:1px solid #e5e7eb;border-top:none;padding:24px;border-radius:0 0 8px 8px">
                <h2 style="color:#1D4ED8">Bienvenue chez SamaBank !</h2>
                <p>Bonjour <strong>%s</strong>,</p>
                <p>Votre compte a été créé avec succès.</p>
                <div style="background:#F0FDF4;padding:16px;border-radius:6px;margin:16px 0">
                  <p style="margin:0"><strong>Votre identifiant de connexion :</strong> %s</p>
                </div>
              </div>
            </body></html>
            """.formatted(firstName, username);
    }
}
