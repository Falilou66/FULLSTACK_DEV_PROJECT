package sn.samabank.account.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import sn.samabank.account.dto.AccountResponse;
import sn.samabank.account.dto.OpenAccountRequest;
import sn.samabank.account.entity.AccountStatus;
import sn.samabank.account.entity.AccountType;
import sn.samabank.account.service.AccountService;
import sn.samabank.account.shared.ApiResponse;
import sn.samabank.account.shared.SecurityContextHelper;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@Tag(name = "Account", description = "Gestion des comptes bancaires")
public class AccountController {

    private final AccountService accountService;
    private final SecurityContextHelper securityHelper;

    public AccountController(AccountService accountService, SecurityContextHelper securityHelper) {
        this.accountService = accountService;
        this.securityHelper = securityHelper;
    }

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('TELLER','ADMIN')")
    @Operation(summary = "Liste paginée de tous les comptes")
    public ResponseEntity<ApiResponse<Page<AccountResponse>>> getAllAccounts(
            @RequestParam(required = false) AccountStatus status,
            @RequestParam(required = false) AccountType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "openedAt,desc") String sort) {
        Pageable pageable = parsePageable(page, size, sort);
        return ResponseEntity.ok(ApiResponse.ok(accountService.getAll(status, type, pageable)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('TELLER','ADMIN')")
    @Operation(summary = "Ouvrir un compte")
    public ResponseEntity<ApiResponse<AccountResponse>> open(
            @Valid @RequestBody OpenAccountRequest request,
            @AuthenticationPrincipal UUID userId,
            HttpServletRequest httpRequest) {
        String role = securityHelper.getCurrentRole();
        String username = securityHelper.getCurrentUsername();
        String channel = securityHelper.getCurrentChannel();
        String ip = securityHelper.getIpAddress(httpRequest);
        AccountResponse response = accountService.open(request, userId, role, username, channel, ip);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(response));
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Mes comptes")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> myAccounts(
            @AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(ApiResponse.ok(accountService.getMyAccounts(userId)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('TELLER','ADMIN')")
    @Operation(summary = "Détail d'un compte")
    public ResponseEntity<ApiResponse<AccountResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(accountService.getById(id)));
    }

    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Suspendre un compte")
    public ResponseEntity<ApiResponse<AccountResponse>> suspend(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId,
            HttpServletRequest httpRequest) {
        String role = securityHelper.getCurrentRole();
        String username = securityHelper.getCurrentUsername();
        String channel = securityHelper.getCurrentChannel();
        String ip = securityHelper.getIpAddress(httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(accountService.suspend(id, userId, role, username, channel, ip)));
    }

    @PatchMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Réactiver un compte")
    public ResponseEntity<ApiResponse<AccountResponse>> reactivate(
            @PathVariable UUID id,
            @AuthenticationPrincipal UUID userId,
            HttpServletRequest httpRequest) {
        String role = securityHelper.getCurrentRole();
        String username = securityHelper.getCurrentUsername();
        String channel = securityHelper.getCurrentChannel();
        String ip = securityHelper.getIpAddress(httpRequest);
        return ResponseEntity.ok(ApiResponse.ok(accountService.reactivate(id, userId, role, username, channel, ip)));
    }

    private Pageable parsePageable(int page, int size, String sort) {
        Set<String> allowed = Set.of("openedAt", "createdAt", "accountNumber", "balance", "status", "type");
        String property = "openedAt";
        Sort.Direction direction = Sort.Direction.DESC;
        if (sort != null && !sort.isBlank()) {
            String[] parts = sort.split(",");
            if (allowed.contains(parts[0].trim())) property = parts[0].trim();
            if (parts.length > 1 && "asc".equalsIgnoreCase(parts[1].trim())) direction = Sort.Direction.ASC;
        }
        return PageRequest.of(page, size, Sort.by(direction, property));
    }
}
