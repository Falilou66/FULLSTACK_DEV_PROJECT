package sn.samabank.customer.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import sn.samabank.customer.dto.CreateCustomerRequest;
import sn.samabank.customer.dto.CustomerResponse;
import sn.samabank.customer.dto.UpdateCustomerRequest;
import sn.samabank.customer.entity.CustomerStatus;
import sn.samabank.customer.service.CustomerService;
import sn.samabank.customer.shared.ApiResponse;
import sn.samabank.customer.shared.SecurityContextHelper;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@Tag(name = "Customer", description = "Gestion des clients SamaBank")
public class CustomerController {

    private final CustomerService customerService;
    private final SecurityContextHelper securityHelper;

    public CustomerController(CustomerService customerService,
                              SecurityContextHelper securityHelper) {
        this.customerService = customerService;
        this.securityHelper  = securityHelper;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TELLER','ADMIN')")
    @Operation(summary = "Créer un client")
    public ResponseEntity<ApiResponse<CustomerResponse>> create(
            @Valid @RequestBody CreateCustomerRequest request,
            @AuthenticationPrincipal UUID userId,
            HttpServletRequest httpRequest) {

        String role     = securityHelper.getCurrentRole();
        String username = securityHelper.getCurrentUsername();
        String channel  = securityHelper.getCurrentChannel();
        String ip       = securityHelper.getIpAddress(httpRequest);

        CustomerResponse response = customerService.create(request, userId, role, username, channel, ip);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Mon profil client")
    public ResponseEntity<ApiResponse<CustomerResponse>> me(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(customerService.getMyProfile(userId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TELLER','ADMIN')")
    @Operation(summary = "Détail d'un client")
    public ResponseEntity<ApiResponse<CustomerResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(customerService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TELLER','ADMIN')")
    @Operation(summary = "Lister les clients")
    public ResponseEntity<ApiResponse<Page<CustomerResponse>>> getAll(
            @RequestParam(required = false) CustomerStatus status,
            @RequestParam(required = false) String search,
            @Parameter(description = "Numéro de page (0-based)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Nombre d'éléments par page", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Tri : propriété,direction", example = "createdAt,desc")
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        Pageable pageable = parsePageable(page, size, sort);
        return ResponseEntity.ok(ApiResponse.ok(customerService.getAll(status, search, pageable)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TELLER','ADMIN')")
    @Operation(summary = "Modifier un client")
    public ResponseEntity<ApiResponse<CustomerResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCustomerRequest request,
            @AuthenticationPrincipal UUID userId,
            HttpServletRequest httpRequest) {

        String role     = securityHelper.getCurrentRole();
        String username = securityHelper.getCurrentUsername();
        String channel  = securityHelper.getCurrentChannel();
        String ip       = securityHelper.getIpAddress(httpRequest);

        return ResponseEntity.ok(ApiResponse.ok(
                customerService.update(id, request, userId, role, username, channel, ip)));
    }

    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Suspendre un client")
    public ResponseEntity<ApiResponse<CustomerResponse>> suspend(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId,
            HttpServletRequest httpRequest) {

        String role     = securityHelper.getCurrentRole();
        String username = securityHelper.getCurrentUsername();
        String channel  = securityHelper.getCurrentChannel();
        String ip       = securityHelper.getIpAddress(httpRequest);

        return ResponseEntity.ok(ApiResponse.ok(
                customerService.suspend(id, userId, role, username, channel, ip)));
    }

    @PatchMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Réactiver un client")
    public ResponseEntity<ApiResponse<CustomerResponse>> reactivate(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId,
            HttpServletRequest httpRequest) {

        String role     = securityHelper.getCurrentRole();
        String username = securityHelper.getCurrentUsername();
        String channel  = securityHelper.getCurrentChannel();
        String ip       = securityHelper.getIpAddress(httpRequest);

        return ResponseEntity.ok(ApiResponse.ok(
                customerService.reactivate(id, userId, role, username, channel, ip)));
    }

    private Pageable parsePageable(int page, int size, String sort) {
        Set<String> allowedProperties = Set.of(
                "createdAt", "firstName", "lastName", "email", "customerNumber", "status"
        );

        String property;
        Sort.Direction direction;

        if (sort == null || sort.isBlank()) {
            property  = "createdAt";
            direction = Sort.Direction.DESC;
        } else {
            String[] parts = sort.split(",");
            property = parts[0].trim();
            if (!allowedProperties.contains(property)) {
                property = "createdAt";
            }
            direction = (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim()))
                    ? Sort.Direction.ASC : Sort.Direction.DESC;
        }

        return PageRequest.of(page, size, Sort.by(direction, property));
    }
}
